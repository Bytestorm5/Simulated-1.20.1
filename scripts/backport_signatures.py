#!/usr/bin/env python3
"""
Signature-level rewrites for the 1.21.1 -> 1.20.1 backport that are regular enough to script. Run after
backport_rewrite.py; idempotent:

    python3 scripts/backport_signatures.py simulated aeronautics offroad

- Drops `HolderLookup.Provider` parameters (1.20.1 block entity, Create behaviour and item save/load methods take
  none) and the matching arguments inside the same file.
- Renames the 1.21 block entity save/load methods to their 1.20.1 names.
- Bridges `useItemOn` / `useWithoutItem` (1.21) to `use` (1.20.1) with the ItemInteractionResult backport.
- Rewrites `stack.get(Components.X)` style item data component calls to the backport's `Components.X.get(stack)`.
- Removes block `codec()` overrides and their `simpleCodec` fields (1.20.1 blocks have no codec).
"""
import re
import sys
from pathlib import Path

SIM_BACKPORT = "dev.simulated_team.simulated.backport"
IMPORT_RE = re.compile(r"^import\s+(static\s+)?([\w.$]+)(\.\*)?\s*;\s*$", re.M)


def add_import(src, fqn):
    if re.search(rf"^import\s+{re.escape(fqn)}\s*;", src, re.M):
        return src
    m = list(IMPORT_RE.finditer(src))
    line = f"import {fqn};\n"
    if m:
        pos = m[-1].end() + 1
        return src[:pos] + line + src[pos:]
    pkg = re.search(r"^package .*;\s*$", src, re.M)
    pos = pkg.end() + 1 if pkg else 0
    return src[:pos] + "\n" + line + src[pos:]


PROVIDER_PARAM = re.compile(r"(?:@\w+(?:\.\w+)*\s+)*(?:final\s+)?HolderLookup\.Provider\s+(\w+)")


def drop_provider(src):
    names = set(m.group(1) for m in PROVIDER_PARAM.finditer(src))
    if not names:
        return src
    # parameter declarations
    src = re.sub(r",\s*" + PROVIDER_PARAM.pattern + r"(?=\s*[,)])", "", src)
    src = re.sub(PROVIDER_PARAM.pattern + r"\s*,\s*", "", src)
    src = re.sub(r"\(\s*" + PROVIDER_PARAM.pattern + r"\s*\)", "()", src)
    # arguments
    for name in names:
        src = re.sub(rf",\s*{name}(?=\s*[,)])", "", src)
        src = re.sub(rf"\(\s*{name}\s*,\s*", "(", src)
        src = re.sub(rf"\(\s*{name}\s*\)", "()", src)
    return src


BE_RENAMES = [
    # 1.21: protected void loadAdditional(CompoundTag, Provider) -> 1.20.1: public void load(CompoundTag)
    (re.compile(r"protected void loadAdditional\(\s*((?:final\s+)?CompoundTag\s+\w+)\s*\)"), r"public void load(\1)"),
    (re.compile(r"\bsuper\.loadAdditional\("), "super.load("),
    (re.compile(r"\.saveCustomOnly\(\)"), ".saveWithoutMetadata()"),
]

USE_ITEM_ON = re.compile(r"(@Override\s*\n\s*)?(?:protected|public) ItemInteractionResult useItemOn\(")
USE_WITHOUT_ITEM = re.compile(r"(@Override\s*\n\s*)?(?:protected|public) InteractionResult useWithoutItem\(")


def bridge_use(src):
    has_item = USE_ITEM_ON.search(src) is not None
    has_without = USE_WITHOUT_ITEM.search(src) is not None
    if not (has_item or has_without):
        return src
    if "ItemInteractionResult.use(" in src or "public InteractionResult use(final BlockState state, final Level level" in src:
        return src
    src = USE_ITEM_ON.sub("public ItemInteractionResult useItemOn(", src)
    src = USE_WITHOUT_ITEM.sub("public InteractionResult useWithoutItem(", src)
    if has_item:
        fallback = ("this.useWithoutItem(state, level, pos, player, hitResult)" if has_without
                    else "super.use(state, level, pos, player, hand, hitResult)")
        body = ("return ItemInteractionResult.use(this.useItemOn(player.getItemInHand(hand), state, level, pos, player, hand, hitResult), hand,\n"
                f"                () -> {fallback});")
    else:
        body = "return this.useWithoutItem(state, level, pos, player, hitResult);"
    bridge = ("    @Override\n"
              "    public InteractionResult use(final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {\n"
              f"        {body}\n"
              "    }\n\n")
    first = min(m.start() for m in (USE_ITEM_ON.search(src), USE_WITHOUT_ITEM.search(src)) if m)
    line_start = src.rfind("\n", 0, first) + 1
    src = src[:line_start] + bridge + src[line_start:]
    for fqn in ["net.minecraft.world.InteractionResult", "net.minecraft.world.InteractionHand",
                "net.minecraft.world.level.block.state.BlockState", "net.minecraft.world.level.Level",
                "net.minecraft.core.BlockPos", "net.minecraft.world.entity.player.Player",
                "net.minecraft.world.phys.BlockHitResult"]:
        src = add_import(src, fqn)
    return src


COMPONENT_CALL = re.compile(
    r"(?<![\w.])((?:this\.)?[a-zA-Z_]\w*(?:\(\))?(?:\.[a-zA-Z_]\w*(?:\([^()]*\))?)*?)"
    r"\.(get|set|has|remove|getOrDefault|update)\(((?:Sim|Aero|Offroad)DataComponents\.[A-Z_]+)\s*(,\s*|\))")


def rewrite_components(src):
    def repl(m):
        recv, method, comp, tail = m.groups()
        if tail.startswith(","):
            return f"{comp}.{method}({recv}, "
        return f"{comp}.{method}({recv})"
    return COMPONENT_CALL.sub(repl, src)


CODEC_METHOD = re.compile(r"\n[ \t]*@Override\s*\n[ \t]*(?:protected|public) MapCodec<\? extends [\w.]*(?:Block|Face\w*)> codec\(\)\s*\{\s*return [\w.]+;\s*\}\n")
CODEC_FIELD = re.compile(r"\n[ \t]*public static (?:final )?MapCodec<\w+> CODEC = simpleCodec\([\w:]+\);\n")


def drop_block_codecs(src):
    src = CODEC_METHOD.sub("\n", src)
    src = CODEC_FIELD.sub("\n", src)
    return src


def rewrite(src):
    original = src
    src = drop_provider(src)
    for pattern, new in BE_RENAMES:
        src = pattern.sub(new, src)
    src = src.replace("import net.minecraft.world.ItemInteractionResult;", f"import {SIM_BACKPORT}.ItemInteractionResult;")
    src = bridge_use(src)
    src = rewrite_components(src)
    src = drop_block_codecs(src)
    if src != original:
        if "HolderLookup" not in src.replace("import net.minecraft.core.HolderLookup;", ""):
            src = src.replace("import net.minecraft.core.HolderLookup;\n", "")
        if not re.search(r"\bMapCodec\b", src.replace("import com.mojang.serialization.MapCodec;", "")):
            src = src.replace("import com.mojang.serialization.MapCodec;\n", "")
    return src


def main(roots):
    changed = 0
    for root in roots:
        for path in Path(root).rglob("*.java"):
            if "/build/" in str(path):
                continue
            text = path.read_text(encoding="utf-8")
            new = rewrite(text)
            if new != text:
                path.write_text(new, encoding="utf-8")
                changed += 1
    print(f"rewrote {changed} files")


if __name__ == "__main__":
    main(sys.argv[1:] or ["simulated", "aeronautics", "offroad"])
