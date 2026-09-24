#!/usr/bin/env python3
"""
Downgrades structure NBT files (Ponder scenes, GameTest templates) saved by Minecraft 1.21.1 to the 1.20.1 format.

- DataVersion 3955 (1.21.1) -> 3465 (1.20.1)
- Blocks renamed in 1.20.3: minecraft:short_grass -> minecraft:grass
- Item stacks in block entity / entity data: 1.20.5 {id, count:int, components} -> 1.20.1 {id, Count:byte, tag}

Requires nbtlib (pip install nbtlib). Idempotent:

    python3 scripts/convert_structures.py simulated aeronautics offroad
"""
import sys
from pathlib import Path

import nbtlib
from nbtlib.tag import Byte, Compound, Int, List, String

DATA_VERSION_1_20_1 = 3465
BLOCK_RENAMES = {"minecraft:short_grass": "minecraft:grass"}


def convert_item(tag):
    """Converts a 1.20.5+ item stack compound in place. Returns True if it changed."""
    if not isinstance(tag, Compound) or "id" not in tag or "count" not in tag or "Count" in tag:
        return False
    count = int(tag.pop("count"))
    tag["Count"] = Byte(count)
    components = tag.pop("components", None)
    if components:
        converted = Compound()
        name = components.get("minecraft:custom_name")
        if name is not None:
            converted["display"] = Compound({"Name": String(str(name))})
        block_entity = components.get("minecraft:block_entity_data")
        if block_entity is not None:
            converted["BlockEntityTag"] = block_entity
        custom = components.get("minecraft:custom_data")
        if custom is not None:
            converted.update(custom)
        if converted:
            tag["tag"] = converted
    return True


def walk(tag):
    changed = False
    if isinstance(tag, Compound):
        changed |= convert_item(tag)
        for value in tag.values():
            changed |= walk(value)
    elif isinstance(tag, List):
        for value in tag:
            changed |= walk(value)
    return changed


def convert(path):
    nbt = nbtlib.load(path)
    changed = False
    if int(nbt.get("DataVersion", 0)) > DATA_VERSION_1_20_1:
        nbt["DataVersion"] = Int(DATA_VERSION_1_20_1)
        changed = True
    for state in nbt.get("palette", []):
        name = str(state["Name"])
        if name in BLOCK_RENAMES:
            state["Name"] = String(BLOCK_RENAMES[name])
            changed = True
    for block in nbt.get("blocks", []):
        if "nbt" in block:
            changed |= walk(block["nbt"])
    for entity in nbt.get("entities", []):
        changed |= walk(entity.get("nbt"))
    if changed:
        nbt.save()
    return changed


def main(roots):
    count = 0
    for root in roots:
        for path in Path(root).rglob("*.nbt"):
            if "/build/" not in str(path) and convert(path):
                count += 1
    print(f"converted {count} files")


if __name__ == "__main__":
    main(sys.argv[1:] or ["simulated", "aeronautics", "offroad"])
