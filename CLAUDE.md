# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Minecraft 1.7.10 Forge mod for GTNH, built with the GTNH convention plugin (gradle wrapper 9.x, Jabel allows modern Java syntax up to ~Java 17 on a JVM 8 target — pattern matching like `if (x instanceof Y y)` is fine).

- Build (with formatting fix + check + reobf): `./gradlew spotlessapply build`
- Quick compile check: `./gradlew compileJava`
- Run client / server: `./gradlew runClient` / `./gradlew runServer`

Note: tests are in `src/main/java/.../tests/` and are manual in-game tests (GTNH multiblock test worlds), not JUnit — `:test` is NO-SOURCE.

## Architecture

### Block / Tile split (1.7.10: block + TileEntity per torch)

- `block/BlockTorcherinoBase` extends `BlockTorch` + `ITileEntityProvider`: handles redstone (powered → `setActive(false)` on the tile), icon registration, block-break TE cleanup. Subclasses only provide `createNewTileEntity` + `onBlockActivated` (opens the ModularUI GUI via `TileEntityGuiFactory.INSTANCE.open(player, tile)`).
- `tile/TileTorcherinoBase` is the GUI-torch base (`TileTorcherinoAccelerated`, `TileCompressedTorcherino`, `TileDoubleCompressedTorcherino`, and the decelerate family). It holds speed slider level + XYZ radii, NBT with backwards-compat migration (old `TimeRate`/`Mode` fields), the ModularUI2 panel (`buildUI` with `DoubleSyncValue` + `SliderWidget`), and per-world torch tracking. Its `updateEntity()` does recursion prevention + bounds caching then calls the `applyAreaEffect(int effectiveSpeed)` hook — the default accelerates every position in range via `AccelerationHelper`; the decelerate family overrides it to register zones in `DecelerationHelper`.
- Tier multipliers are 1 / 9 / 81 (normal / compressed / double compressed) — provided via `getSpeedMultiplier()`; effective speed = slider level × multiplier.
- `tile/TileTorcherinoClassic` is a separate legacy implementation (chat-based mode cycling, no GUI); `tile/TileWirelessTorcherinoBase` + `tile/TileWirelessTorcherinoBase` subclasses are the DataStick-bound wireless torches.

### Acceleration vs deceleration (the two shared helpers in `util/`)

- `AccelerationHelper.accelerateAtPosition(...)` — shared by all torch types. Calls `updateEntity()` N times (or `block.updateTick` for random-tick blocks), with a per-tick time budget (`Config.enableTickBudget`, 1ms default) and "fastest wins" overlap detection via a per-world `TickTracker`. GT machines are handled through the mixin interface below, NOT by re-running `updateEntity`.
- `DecelerationHelper` — decelerate torches register their zone each tick (positions that contain a tile entity; highest rate wins on overlap). 400% (rate 4) ⇒ machine advances 1/4 per real second. Rate ≤ 1 means no deceleration. Two mechanisms:
  - **Whole tick skip** (non-GT tiles): `WorldTileEntityDeceleration_Mixin` (early) `@Redirect`s the single `tileEntity.updateEntity()` call site inside `World.updateEntities()` and skips it on held-back ticks (`worldTick % rate != 0`). **1.7.10 gotcha: `World.updateEntities()` never calls `TileEntity.canUpdate()` — gating at the World call site is the only way** (a `canUpdate` mixin silently does nothing).
  - **Progress-only** (GT machines, `ITileEntityTickDeceleration` marker via `BaseMetaTileEntityDeceleration_Mixin`): they are never skipped — energy flows (EU/t consumption, generator EU/t output) stay normal. The mixin's HEAD/TAIL injects around `BaseMetaTileEntity.updateEntityProfiled()` hold back the natural `mProgresstime` gain on held-back ticks (HEAD also parks max-1 progress so recipes can't complete on a held-back tick). Write path covers `MTEBasicMachine`, `MTEMultiBlockBase`, `MTEBrickedBlastFurnace` (public `mProgresstime`); GT generators have no progress at all (fuel burns straight into EU) so they are fully unaffected. `shouldSkipUpdate`/`shouldHoldBackProgress` both skip `ITorcherinoTile` — torches never affect each other (and `AccelerationHelper` equally ignores torch tiles).

### Mixins — early vs late

- **Early** (`src/main/resources/mixins.Torcherino.json`, hardcoded list, package `mixins.early`): must load before GT. `BaseMetaTileEntityAcceleration_Mixin` implements `ITileEntityTickAcceleration.tickAcceleration()` on `BaseMetaTileEntity` — adds progress directly (`mProgresstime`) instead of re-ticking, which is why EU consumption is not multiplied; it also blacklists `MTEWorldAccelerator` and discounts GT machines via `Config.accelerateGregTechMachineDiscount`. `BaseMetaTileEntityDeceleration_Mixin` implements the `ITileEntityTickDeceleration` marker + progress hold-back for GT machines. `WorldTileEntityDeceleration_Mixin` is the generic decel hook (redirects `World.updateEntities`' tile tick call site). `BlockLeftClickDataStick_Mixin` intercepts vanilla left-clicks for the wireless DataStick.
- **Late** (`mixins.Torcherino.late.json` + `mixins/Mixins.java` enum + `LateMixinPlugin`): per-mod compatibility (EnderIO, CropsNH, Forestry, TecTech, OmniOcular, GT DataStick). New late mixins are added as enum entries with `addTargetMod` / `addCondition` (config-gated) — do NOT hand-edit the late json.
- **1.7.10 remap gotcha**: the mixin AP resolves `@Inject`/`@Redirect` method and `@At` target names against the forge `mcp-srg.srg` (e.g. `updateEntities` → `func_72939_s`, `updateEntity` → `func_145845_h`) — always use the default remap when the name is in the table and let the refmap do the mapping (verify `build/tmp/mixins/mixins.Torcherino.refmap.json` after compiling). Only use `remap = false` for names absent from the srg table (e.g. `TileEntity.canUpdate`), otherwise the annotation processor errors out. `TileEntity.worldObj` is protected — use `getWorldObj()`.

### APIs

- `api/interfaces/ITorcherinoTile` — implemented by ALL torch tiles; `AccelerationHelper`/`DecelerationHelper` skip anything implementing it so torches never affect each other.
- `api/interfaces/ITileEntityTickAcceleration` — precise acceleration contract for TEs that must not be re-ticked (GT machines, Forestry multiblock parts). Implementers return true to consume the accelerated tick.

### Misc

- Blocks/items register in `block/ModBlocks` / `item/ModItems` (`GameRegistry.registerBlock/registerTileEntity`); recipes in `init/ModRecipes` + `init/GTRecipes` (GTRecipes load in `completeInit`, gated by `RecipeConfig`).
- Config lives in `config/Torcherino/Torcherino.cfg` (`Config.java`), recipe config alongside as `recipe.cfg`.
- Lang files: `src/main/resources/assets/torcherino/lang/{en_US,zh_CN,fr_FR,ko_KR}.lang` — block names (`tile.<name>.name`), GUI keys (`torcherino.gui.*`) and chat messages. Always add new keys to all four files.
- Textures: 16×16 PNGs in `assets/torcherino/textures/blocks/`, registered via `registerBlockIcons` as `Torcherino.MODID + ":" + iconName`.
- Logging via `Torcherino.LOG` (Log4j).
- GT5U reference source is available locally at `tmp/GT5-Unofficial-5.09.54.18` (e.g. to check GT class hierarchies before writing GT mixins).
