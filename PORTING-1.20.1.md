# Simulated, Aeronautics and Offroad on Forge 1.20.1

This branch ports Simulated 1.3.2 and its Aeronautics and Offroad modules from Minecraft 1.21.1 / NeoForge 21.1 to
**Minecraft 1.20.1 / Forge 47.4.x**. It builds against the 1.20.1 ports of Veil 4.3.2 (`Bytestorm5/Veil-1.20.1`) and
Sable 2.0.5 (`Bytestorm5/sable-1.20.1`), which keep their 4.x / 2.x APIs.

## Building

1. **Veil and Sable** for 1.20.1 aren't on a public maven yet. Install their jars into Maven Local:
   ```
   scripts/install_local_deps.sh <veil-forge-1.20.1-4.3.2.jar> <sable-forge-1.20.1-2.0.5.jar>
   ```
   This also installs the sable-companion jar that Sable jar-in-jars. Alternatively, build them from source with
   `./gradlew publishToMavenLocal`, Veil first:
   - Veil: `veil-1-20-1-migration-aan416`, at or after `3b0b6fd`
   - Sable: `claude/sable-1-20-1-migration-1vdzus`, at or after `af027cc`

   Both routes install the same coordinates.
2. Build:
   ```
   ./gradlew build
   ```
   - `aeronautics-bundled/build/libs/create-aeronautics-bundled-1.20.1-<version>.jar` is the release jar. It
     jar-in-jars the three mods.
   - Each mod's own jar is in `<mod>/neoforge/build/libs`. The jars are reobfuscated to SRG and each carries its refmap,
     its SRG access transformer and a `MixinConfigs` manifest entry.
3. Check the mixins statically:
   ```
   ./gradlew checkMixins
   ./gradlew checkMixinsProduction -PforgeClientInstall=<dir>
   ```
   Both use Sable's checker (`scripts/mixin-check`), which checks every mixin's targets, injector signatures, `@At`
   targets and shadows.
   - `checkMixins` checks them against the dev (Mojang-named) classes.
   - `checkMixinsProduction` checks the reobfuscated jars and refmaps against SRG-named Minecraft and the original SRG mod
     jars. It catches mixins that only resolve with dev names.

   `<dir>` is a stock Forge client install: `java -jar forge-1.20.1-47.4.10-installer.jar --installClient <dir>`, with an
   empty `launcher_profiles.json` in `<dir>`.

The build uses a Java 21 toolchain that compiles with `--release 17`, because some compile-only dependencies ship Java
21 class files. The output runs on Java 17.

Dependency versions match what Create 6.0.8 (the newest 1.20.1 build) bundles: Forge 47.4.10, Create 6.0.8-291,
Ponder 1.0.91, Flywheel 1.0.5 and Registrate MC1.20-1.3.3. The compat targets are JEI 15, Curios 5.14, CC: Tweaked
1.120.2, Nature's Compass 1.12.0, Explorer's Compass 1.4.0, Embeddium 0.3.31 and Oculus 1.8.0.

Project layout is unchanged:
- The `neoforge` modules are now the Forge loader modules. The directory and package names are kept, so upstream
  changes still merge.
- `all-neoforge` holds the combined dev runs:
  - `./gradlew :all-neoforge:runClient` (`-PquickPlay=<world>` loads straight into a world)
  - `:all-neoforge:runServer`
  - `:all-neoforge:runDataAll`
- `:simulated:neoforge:runGameTest` runs the GameTests.

## How it was ported

The mechanical part is scripted. The scripts are idempotent, so upstream changes can be ported the same way:

- **`scripts/backport_rewrite.py`**:
  - rewrites 1.21 imports to Veil's backports (`foundry.veil.backport.*`: `StreamCodec`, `ByteBufCodecs`,
    `CustomPacketPayload`, `RegistryFriendlyByteBuf`, `DeltaTracker`)
  - rewrites NeoForge imports to their Forge equivalents
  - replaces `ResourceLocation` factories, `X.STREAM_CODEC` constants and a few 1.21 call forms
- **`scripts/backport_signatures.py`**:
  - drops `HolderLookup.Provider` parameters and renames the block entity save/load methods
  - bridges `useItemOn`/`useWithoutItem` to 1.20.1's `Block#use`
  - rewrites item data component calls
  - removes block `codec()`s
- **`scripts/at_to_srg.py`** converts access transformers to SRG names.
- **`scripts/fix_mixin_remap.py`** adds `remap = false` where a mixin targets mod code. Both come from Sable's port.
- **`scripts/convert_structures.py`** downgrades Ponder and GameTest NBT saved by 1.21.1. It sets the data version,
  renames `short_grass` to `grass` and converts item stacks to the 1.20.1 format.

Simulated's own stand-ins for 1.21 types are in `dev.simulated_team.simulated.backport`:

| 1.21 | 1.20.1 stand-in |
|---|---|
| `ItemInteractionResult`, `Block#useItemOn` / `useWithoutItem` | `backport.ItemInteractionResult`. Blocks keep both methods and a generated `use(...)` bridges them the way 1.21's `ServerPlayerGameMode` does |
| Item data components (`DataComponentType`) | `backport.DataComponentType`, stored in the item's NBT under the component id. `stack.get(X)` becomes `X.get(stack)` |

Everything else was ported by hand against the 1.20.1 APIs. The main areas:

| Area | 1.20.1 |
|---|---|
| Loader | `mods.toml`, no-arg `@Mod` constructors, `ForgeConfigSpec`, `LazyOptional` capabilities via `AttachCapabilitiesEvent`, `TickEvent` phases, `RegisterGuiOverlaysEvent`, `initializeClient` client extensions, `ItemAttributeModifierEvent` |
| Save / load | Create `write/read(tag, clientPacket)`, `saveAdditional(tag)` / `load(tag)`, `ItemStack.of` / `save` |
| Rendering | 1.20.1 vertex API (`vertex().color().uv()...endVertex()`), `BufferBuilder.begin/end`, `DefaultVertexFormat` |
| Entities | `defineSynchedData()` + `define`, `IEntityAdditionalSpawnData` + Forge spawn packets |
| Advancements | 1.20.1 `Advancement.Builder`, `FrameType`, `SimpleCriterionTrigger` with `CriteriaTriggers.register`, modelled on Create 6.0.8 |
| Data | Plural folder names (`recipes/`, `tags/blocks/`, ...), `forge:` tags instead of `c:`, pack format 15; generated data regenerated with the 1.20.1 datagen |
| Mixins | Refmaps and SRG names; targets moved to their 1.20.1 equivalents; mixins into Create, Ponder, Flywheel and Sable use `remap = false` |
| Load order | Create, Sable and Simulated are declared with `ordering = "AFTER"`. Forge needs this so registration happens after them |

## Behaviour that differs from 1.21.1

Where 1.20.1 can't do exactly what 1.21 does, the code uses the closest equivalent and has a `// 1.20.1:` comment.

- **Default item components.** Spring bounciness, navigation targets, levitating, tires and Create blocks' default
  tires now come from per-item defaults (`SimDataComponents.getBounciness/getTarget`,
  `AeroDataComponents.getLevitating`, `OffroadDataComponents.getTire`). A value in the stack's NBT overrides the
  default, but it can't remove one.
- **Punch attribute modifiers** (Extendo Grip, Cardboard Sword) are added to the item's attributes instead of
  replacing them.
- **Nav table map markers:** the findable decorations are mansion, monument and red X. The village, temple and trial
  chamber markers don't exist in 1.20.1.
- **Plunger deflection:** 1.20.1 has no projectile deflection hook, so the plunger breaks and redirects the projectile
  from `hurt()`. Arrows bounce off instead of being steered.
- **End Sea preset:** the player is moved into the End with `ServerPlayer#teleportTo`, which skips the End platform and
  credits as 1.21's plain dimension change did.
- **1.21-only content** is replaced:
  - wind charge sound → phantom flap
  - dust plume particle → poof
  - jukebox song registry → `RecordItem`
  - armor material registry → an `ArmorMaterial` enum
  - `Tool` component → a `ShearsItem#getDestroySpeed` hook
  - Aeronautics' situational music uses the 1.20.1 C418 tracks. The 1.21-only track is dropped.
- **JEI 15** has no mod-alias API, so the aliases between the three mods are gone.
- **GUI sprites:** 1.20.1 has no GUI sprite atlas. `SimGuiSprites` builds one from `textures/gui/sprites`, for the
  creative-tab banners and widgets. It uses its own row packer, because 1.20.1's `Stitcher` silently drops a sprite
  when growing the atlas. That bug lost the Aeronautics banner.
- **Linked typewriter:** its custom name is stored in the block entity's NBT. Its ghost slots can't be marked fake,
  because `Slot#isFake` doesn't exist in 1.20.1.
- **Capabilities** are exposed on block entities only, not plain blocks.
- **Superheated burn time** follows Create 6.0.8's blaze burner fuel tag (a fixed 3200 ticks) instead of a data map.
  Envelope furnace fuel comes from `FurnaceFuelBurnTimeEvent`.
- **Screens** use 1.20.1's plain background, not 1.21's blur.

## Workarounds for Veil / Sable 1.20.1 behaviour

- **Veil `3b0b6fd` or later is required.** Earlier Veil 1.20.1 builds don't set `NormalMat`, `VeilBlockFaceBrightness` or
  `VeilRenderTime` for Veil's own shader programs. That makes levitite render black on sub-levels.
- **Veil draws layered block layers (levitite) without `LevelRenderer#renderChunkLayer`.** The levitite world uniforms
  are therefore set when its render state binds the shader.
- **Forge fires `FMLClientSetupEvent` on worker threads**, so Aeronautics' render-type setup is queued onto the main
  thread.
- **Forge strips client-only classes on dedicated servers.** Particle providers and Create's radial wrench blacklist are
  only touched on the client.

## Verification

- `./gradlew build checkMixins checkMixinsProduction`: all modules compile. Both checks report no problems for 155
  injectors.
- **Production:** a stock Forge 1.20.1-47.4.10 dedicated server runs the release jar with Create 6.0.8 and Sable
  `af027cc`. It boots and assembles regions of Simulated, Aeronautics and Offroad blocks into sub-levels. It then saves,
  and reloads the sub-levels with no errors.
- GameTest server: all 4 required tests pass.
- Dedicated dev server with all three mods: boots and generates a world with no errors.
- Dev client with all three mods (Mesa software GL):
  - Simulated, Aeronautics and Offroad blocks render with their block entity renderers.
  - `/sable assemble` turns regions into sub-levels.
  - Levitite lifts its sub-level, and levitite renders correctly both in the world and on sub-levels.
  - The creative tab and its section banners, and the Altitude Sensor screen, render correctly.
- Datagen output (`<mod>/common/src/generated`) was regenerated with the 1.20.1 code.

## Known issues

- **Runtime coverage:** most gameplay features haven't been exercised in game, only compiled and statically checked.
  That includes the optional compat mods (JEI, Curios, CC: Tweaked, the compasses, Embeddium/Oculus) and Offroad
  vehicles.
- **Sable config reload:** Sable builds before `af027cc` reload shaders from Forge's config watcher thread. When
  `sable-client.toml` is created or corrected, this logs a harmless "No GLCapabilities instance set" error.
- **`simulated/neoforge/src/generated` is stale:** it's leftover upstream output that no build uses.
