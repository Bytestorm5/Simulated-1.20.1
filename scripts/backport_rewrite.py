#!/usr/bin/env python3
"""
Mechanical source rewrites for the 1.21.1 -> 1.20.1 backport of Simulated, Aeronautics and Offroad.

Rewrites imports of 1.21-only utility types to the backports Veil 1.20.1 ships (foundry.veil.backport.*), NeoForge
imports to their Forge 1.20.1 equivalents, and a handful of 1.21 call forms to their 1.20.1 equivalents. Adapted from
Sable's scripts/backport_rewrite.py. Idempotent; run from the repository root:

    python3 scripts/backport_rewrite.py simulated aeronautics offroad
"""
import re
import sys
from pathlib import Path

BACKPORT = "foundry.veil.backport"
SIM_BACKPORT = "dev.simulated_team.simulated.backport"

# Fully-qualified 1.21 name -> replacement fully-qualified name
IMPORTS = {
    "net.minecraft.network.codec.StreamCodec": f"{BACKPORT}.network.codec.StreamCodec",
    "net.minecraft.network.codec.StreamDecoder": f"{BACKPORT}.network.codec.StreamDecoder",
    "net.minecraft.network.codec.StreamEncoder": f"{BACKPORT}.network.codec.StreamEncoder",
    "net.minecraft.network.codec.StreamMemberEncoder": f"{BACKPORT}.network.codec.StreamMemberEncoder",
    "net.minecraft.network.codec.ByteBufCodecs": f"{BACKPORT}.network.codec.ByteBufCodecs",
    "net.minecraft.client.DeltaTracker": f"{BACKPORT}.client.DeltaTracker",
    "net.minecraft.network.protocol.common.custom.CustomPacketPayload": f"{BACKPORT}.network.protocol.common.custom.CustomPacketPayload",
    "net.minecraft.network.RegistryFriendlyByteBuf": f"{BACKPORT}.network.RegistryFriendlyByteBuf",
    "com.mojang.blaze3d.vertex.ByteBufferBuilder": f"{BACKPORT}.blaze3d.vertex.ByteBufferBuilder",
    # Moved packages
    "net.minecraft.world.level.chunk.status.ChunkStatus": "net.minecraft.world.level.chunk.ChunkStatus",
    "net.minecraft.client.gui.screens.options.OptionsScreen": "net.minecraft.client.gui.screens.OptionsScreen",
    "net.minecraft.client.gui.screens.options.OptionsSubScreen": "net.minecraft.client.gui.screens.OptionsSubScreen",
    "net.minecraft.world.level.block.entity.EnchantingTableBlockEntity": "net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity",
    "net.minecraft.world.entity.projectile.AbstractArrow": "net.minecraft.world.entity.projectile.AbstractArrow",
    # 1.21 renamed ItemInteractionResult usages are rewritten by hand; the item/block APIs return InteractionResult
    # NeoForge -> Forge
    "net.neoforged.neoforge.common.ModConfigSpec": "net.minecraftforge.common.ForgeConfigSpec",
    "net.neoforged.neoforge.common.NeoForge": "net.minecraftforge.common.MinecraftForge",
    "net.neoforged.neoforge.common.NeoForgeMod": "net.minecraftforge.common.ForgeMod",
    "net.neoforged.neoforge.common.CommonHooks": "net.minecraftforge.common.ForgeHooks",
    "net.neoforged.neoforge.common.Tags": "net.minecraftforge.common.Tags",
    "net.neoforged.neoforge.common.SoundActions": "net.minecraftforge.common.SoundActions",
    "net.neoforged.neoforge.common.data.BlockTagsProvider": "net.minecraftforge.common.data.BlockTagsProvider",
    "net.neoforged.neoforge.common.extensions.IEntityExtension": "net.minecraftforge.common.extensions.IForgeEntity",
    "net.neoforged.neoforge.common.extensions.IBlockExtension": "net.minecraftforge.common.extensions.IForgeBlock",
    "net.neoforged.neoforge.common.extensions.ILevelReaderExtension": "net.minecraftforge.common.extensions.IForgeLevelReader",
    "net.neoforged.bus.api.IEventBus": "net.minecraftforge.eventbus.api.IEventBus",
    "net.neoforged.bus.api.Event": "net.minecraftforge.eventbus.api.Event",
    "net.neoforged.bus.api.SubscribeEvent": "net.minecraftforge.eventbus.api.SubscribeEvent",
    "net.neoforged.bus.api.EventPriority": "net.minecraftforge.eventbus.api.EventPriority",
    "net.neoforged.api.distmarker.Dist": "net.minecraftforge.api.distmarker.Dist",
    "net.neoforged.api.distmarker.OnlyIn": "net.minecraftforge.api.distmarker.OnlyIn",
    "net.neoforged.fml.common.EventBusSubscriber": "net.minecraftforge.fml.common.Mod",
    "net.neoforged.neoforge.registries.DeferredRegister": "net.minecraftforge.registries.DeferredRegister",
    "net.neoforged.neoforge.registries.DeferredHolder": "net.minecraftforge.registries.RegistryObject",
    "net.neoforged.neoforge.registries.RegisterEvent": "net.minecraftforge.registries.RegisterEvent",
    "net.neoforged.neoforge.registries.NeoForgeRegistries": "net.minecraftforge.registries.ForgeRegistries",
    "net.neoforged.neoforge.items.IItemHandler": "net.minecraftforge.items.IItemHandler",
    "net.neoforged.neoforge.items.IItemHandlerModifiable": "net.minecraftforge.items.IItemHandlerModifiable",
    "net.neoforged.neoforge.items.ItemStackHandler": "net.minecraftforge.items.ItemStackHandler",
    "net.neoforged.neoforge.items.SlotItemHandler": "net.minecraftforge.items.SlotItemHandler",
    "net.neoforged.neoforge.items.ItemHandlerHelper": "net.minecraftforge.items.ItemHandlerHelper",
    "net.neoforged.neoforge.energy.IEnergyStorage": "net.minecraftforge.energy.IEnergyStorage",
    "net.neoforged.neoforge.fluids.capability.IFluidHandler": "net.minecraftforge.fluids.capability.IFluidHandler",
    "net.neoforged.neoforge.fluids.capability.IFluidHandlerItem": "net.minecraftforge.fluids.capability.IFluidHandlerItem",
    "net.neoforged.neoforge.fluids.capability.templates.FluidTank": "net.minecraftforge.fluids.capability.templates.FluidTank",
    "net.neoforged.neoforge.fluids.FluidType": "net.minecraftforge.fluids.FluidType",
    "net.neoforged.neoforge.fluids.FluidStack": "net.minecraftforge.fluids.FluidStack",
    "net.neoforged.neoforge.fluids.BaseFlowingFluid": "net.minecraftforge.fluids.ForgeFlowingFluid",
    "net.neoforged.neoforge.fluids.FluidInteractionRegistry": "net.minecraftforge.fluids.FluidInteractionRegistry",
    "net.neoforged.neoforge.capabilities.Capabilities": "net.minecraftforge.common.capabilities.ForgeCapabilities",
    "net.neoforged.neoforge.gametest.GameTestHolder": "net.minecraftforge.gametest.GameTestHolder",
    "net.neoforged.neoforge.gametest.PrefixGameTestTemplate": "net.minecraftforge.gametest.PrefixGameTestTemplate",
    "net.neoforged.neoforge.client.model.data.ModelData": "net.minecraftforge.client.model.data.ModelData",
    "net.neoforged.neoforge.client.ChunkRenderTypeSet": "net.minecraftforge.client.ChunkRenderTypeSet",
    "net.neoforged.neoforge.client.ClientTooltipFlag": "net.minecraftforge.client.ClientTooltipFlag",
    "net.neoforged.neoforge.client.gui.IConfigScreenFactory": "net.minecraftforge.client.ConfigScreenHandler",
    "net.neoforged.neoforge.client.gui.VanillaGuiLayers": "net.minecraftforge.client.gui.overlay.VanillaGuiOverlay",
    "net.neoforged.neoforge.client.extensions.common.IClientItemExtensions": "net.minecraftforge.client.extensions.common.IClientItemExtensions",
    "net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions": "net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions",
    "net.neoforged.neoforge.client.model.generators.ModelFile": "net.minecraftforge.client.model.generators.ModelFile",
    "net.neoforged.neoforge.client.model.generators.ConfiguredModel": "net.minecraftforge.client.model.generators.ConfiguredModel",
    "net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder": "net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder",
    "net.neoforged.neoforge.data.event.GatherDataEvent": "net.minecraftforge.data.event.GatherDataEvent",
    "net.neoforged.neoforge.event.tick.ServerTickEvent": "net.minecraftforge.event.TickEvent",
    "net.neoforged.neoforge.event.tick.LevelTickEvent": "net.minecraftforge.event.TickEvent",
    "net.neoforged.neoforge.client.event.ClientTickEvent": "net.minecraftforge.event.TickEvent",
    "net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent": "net.minecraftforge.event.entity.player.PlayerInteractEvent",
    "net.neoforged.neoforge.entity.IEntityWithComplexSpawn": "net.minecraftforge.entity.IEntityAdditionalSpawnData",
    "net.neoforged.neoforge.common.util.TriState": "net.minecraftforge.eventbus.api.Event",
}

# NeoForge packages that map 1:1 onto Forge packages (same class names)
PACKAGE_PREFIXES = {
    "net.neoforged.fml.": "net.minecraftforge.fml.",
    "net.neoforged.neoforge.event.": "net.minecraftforge.event.",
    "net.neoforged.neoforge.client.event.": "net.minecraftforge.client.event.",
}

# Simple-name renames that accompany the import rewrites above
SIMPLE_RENAMES = {
    r"\bEnchantingTableBlockEntity\b": "EnchantmentTableBlockEntity",
    r"\bModConfigSpec\b": "ForgeConfigSpec",
    r"\bNeoForge\.EVENT_BUS\b": "MinecraftForge.EVENT_BUS",
    r"\bNeoForgeMod\.": "ForgeMod.",
    r"\bNeoForgeRegistries\.": "ForgeRegistries.",
    r"\bIEntityExtension\b": "IForgeEntity",
    r"\bIBlockExtension\b": "IForgeBlock",
    r"\bILevelReaderExtension\b": "IForgeLevelReader",
    r"\bIEntityWithComplexSpawn\b": "IEntityAdditionalSpawnData",
    r"\bCommonHooks\.": "ForgeHooks.",
    r"\bDeferredHolder<([^<>,]*(?:<[^<>]*>)?), *": "RegistryObject<",
    r"\bBaseFlowingFluid\b": "ForgeFlowingFluid",
    r"(?<![\w.])Capabilities\.": "ForgeCapabilities.",
    r"\bVanillaGuiLayers\b": "VanillaGuiOverlay",
    r"(?<![\w.])@EventBusSubscriber\b": "@Mod.EventBusSubscriber",
    r"(?<![\w.])EventBusSubscriber\.Bus\.": "Mod.EventBusSubscriber.Bus.",
}

VANILLA_STREAM_CODECS = {
    "BlockPos": "BLOCK_POS",
    "UUIDUtil": "UUID",
    "ResourceLocation": "RESOURCE_LOCATION",
    "Direction": "DIRECTION",
    "ChunkPos": "CHUNK_POS",
    "SectionPos": "SECTION_POS",
    "GlobalPos": "GLOBAL_POS",
    "ComponentSerialization": "COMPONENT",
}

EXPRESSIONS = [
    # ResourceLocation factories added in 1.21
    (re.compile(r"\bResourceLocation\.fromNamespaceAndPath\("), "new ResourceLocation("),
    (re.compile(r"\bResourceLocation\.parse\("), "new ResourceLocation("),
    (re.compile(r"\bResourceLocation\.withDefaultNamespace\("), "new ResourceLocation("),
    (re.compile(r"\bResourceLocation\.tryParse\("), "ResourceLocation.tryParse("),
    # ItemStack codecs
    (re.compile(r"\bItemStack\.OPTIONAL_STREAM_CODEC\b"), "VanillaStreamCodecs.ITEM_STACK"),
    (re.compile(r"\bItemStack\.STREAM_CODEC\b"), "VanillaStreamCodecs.ITEM_STACK"),
    (re.compile(r"\bComponentSerialization\.STREAM_CODEC\b"), "VanillaStreamCodecs.COMPONENT"),
    # ModConfigSpec values implement BooleanSupplier/IntSupplier/...; ForgeConfigSpec values are plain Suppliers
    (re.compile(r"\.getAs(Boolean|Int|Double|Long)\(\)"), r".get()"),
    # DataFixerUpper 6 (1.20.1) has no argument-less getOrThrow
    (re.compile(r"\.getOrThrow\(\)"), ".getOrThrow(false, error -> { })"),
    # Java 21 APIs
    (re.compile(r"\bThread\.currentThread\(\)\.threadId\(\)"), "Thread.currentThread().getId()"),
    (re.compile(r"\bMath\.clamp\("), "Mth.clamp("),
    # 1.21's DeltaTracker; on 1.20.1 Minecraft keeps the (pause-aware) partial tick itself
    (re.compile(r"\.getTimer\(\)\.getGameTimeDeltaPartialTick\((?:true|false)\)"), ".getFrameTime()"),
    (re.compile(r"\.getTimer\(\)\.getGameTimeDeltaTicks\(\)"), ".getDeltaFrameTime()"),
    (re.compile(r"\bMinecraft\.getInstance\(\)\.getTimer\(\)"), "DeltaTracker.current()"),
    # NBT accounting
    (re.compile(r"\bNbtAccounter\.unlimitedHeap\(\)"), "NbtAccounter.UNLIMITED"),
    (re.compile(r"\bNbtAccounter\.create\("), "new NbtAccounter("),
    # Creative checks
    (re.compile(r"\.hasInfiniteMaterials\(\)"), ".getAbilities().instabuild"),
    # Component helpers added in 1.20.3+
    (re.compile(r"\bComponent\.translationArg\(([^()]*(?:\([^()]*\))*[^()]*)\)"), r"Component.literal(String.valueOf(\1))"),
    (re.compile(r"(?<!componentStyle)\.withColor\((0x[0-9a-fA-F]+|\d+)\)"), r".withStyle(componentStyle -> componentStyle.withColor(\1))"),
]

IMPORT_RE = re.compile(r"^import\s+(static\s+)?([\w.$]+)(\.\*)?\s*;\s*$", re.M)


def add_import(src: str, fqn: str) -> str:
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


def rewrite(src: str) -> str:
    original = src
    for old, new in IMPORTS.items():
        src = re.sub(rf"^import\s+{re.escape(old)}\s*;", f"import {new};", src, flags=re.M)
    for old, new in PACKAGE_PREFIXES.items():
        src = re.sub(rf"^import(\s+static)?\s+{re.escape(old)}", lambda m: f"import{m.group(1) or ''} {new}", src, flags=re.M)
    for old, new in SIMPLE_RENAMES.items():
        src = re.sub(old, new, src)
    for cls, field in VANILLA_STREAM_CODECS.items():
        src = re.sub(rf"(?<![\w.]){cls}\.STREAM_CODEC\b", f"VanillaStreamCodecs.{field}", src)
    for pattern, new in EXPRESSIONS:
        src = pattern.sub(new, src)
    if "VanillaStreamCodecs." in src:
        src = add_import(src, f"{BACKPORT}.network.codec.VanillaStreamCodecs")
    if "DeltaTracker." in src:
        src = add_import(src, f"{BACKPORT}.client.DeltaTracker")
    if "Mth.clamp(" in src:
        src = add_import(src, "net.minecraft.util.Mth")
    if "@Mod.EventBusSubscriber" in src:
        src = add_import(src, "net.minecraftforge.fml.common.Mod")
    if src == original:
        return src
    # Drop duplicate imports produced by the rewrites
    seen = set()
    out = []
    for line in src.split("\n"):
        m = IMPORT_RE.match(line)
        if m:
            if line.strip() in seen:
                continue
            seen.add(line.strip())
        out.append(line)
    src = "\n".join(out)
    # Drop now-unused UUIDUtil imports
    if "import net.minecraft.core.UUIDUtil;" in src and not re.search(r"\bUUIDUtil\.", src):
        src = src.replace("import net.minecraft.core.UUIDUtil;\n", "")
    if "import net.minecraft.network.chat.ComponentSerialization;" in src and not re.search(r"\bComponentSerialization\.", src):
        src = src.replace("import net.minecraft.network.chat.ComponentSerialization;\n", "")
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
