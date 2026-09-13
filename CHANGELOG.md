# Changelog

All notable changes to Jar Stacker will be documented in this file.

## [0.6.0] - 2026-09-13

### Added
- **Per-Logical-Mob Status Effect State (`LogicalStatusEffectState` & `LogicalStatusRecord`)**:
  - Full per-logical-member tracking of Vanilla `MobEffectInstance` objects (type, amplifier, duration, ambient, visibility, icon, and chained hidden effects) alongside absorption hearts.
  - Strict count invariant maintained: $\text{logicalCount} == \text{healthRecordCount} == \text{statusRecordCount} == \text{burnRecordCount}$.
  - NBT serialization (`saveToNbt()` / `loadFromNbt()`) with roundtrip fidelity and migration fallback for legacy pre-0.6.0 stacks.
- **Per-Logical-Mob Persistent Burn State (`LogicalBurnState` & `LogicalBurnRecord`)**:
  - Independent fire timer countdowns (`remainingFireTicks`) and shared ignition flags per logical member.
  - Fire damage applied authoritatively every 20 ticks unless the member possesses `FIRE_RESISTANCE` or is fire immune.
- **Vanilla Effect Classification & Parity Hardening (`LogicalEffectClassifier` & `LogicalVanillaEffectExecutor`)**:
  - Authoritative classification of all 39 Vanilla 1.21.1 `MobEffect` registry entries into 7 behavior classes: `ATTRIBUTE_ONLY` (9), `PERIODIC_VANILLA` (2), `SPECIAL_LOGICAL` (6), `INSTANT` (2), `VISUAL_STATE` (6), `PASSIVE_SAFE` (10), and `EVENT_DRIVEN_LOGICAL` (4). Complete audit invariant verified: `UNKNOWN == 0` and `UNSUPPORTED == 0` across the entire Vanilla registry.
  - Generic periodic effect execution via `LogicalVanillaEffectExecutor` executing native `applyEffectTick()` within a strictly scoped temporary projection transaction (`LogicalEffectExecutionContext`) with zero state leakage.
  - Event-driven logical parity (`EVENT_DRIVEN_LOGICAL`): `INFESTED` executes native `MobEffect.onMobHurt()` on logical hurt events; `OOZING`, `WEAVING`, and `WIND_CHARGED` execute native `MobEffect.onMobRemoved(KILLED)` on logical death events.
  - Multi-death exact parity: $K$ dying affected members trigger exactly $K$ Vanilla effect callbacks without representative double-triggers.
  - Full-stack area application: Splash and Lingering potions apply normally to all $N$ logical members without entity extraction storms ($O(1)$ physical mob footprint preserved).
  - Conservative bounded fallback (`extractUnsupportedMob`): reserved strictly for unknown or modded non-`minecraft` effects, bounded by `MAX_UNSUPPORTED_AREA_FALLBACK = 5` with transactional rollback.
- **Single-Tick Deduplication & Projection (`LogicalStatusEffectManager`)**:
  - Cancelled native entity effect ticking in `LivingEntityMixin.tickEffects()`; single authoritative execution loop ticks down durations via `MobEffectInstanceAccessor.jarstacker$invokeTickDownDuration()`.
  - Representative projection synchronizes active member 0's visual state (Invisibility, Glowing, fire display) and attribute modifiers (Strength, Weakness, Speed, Slowness).
  - Dynamic active member switching (`onActiveMemberSwitched`) cleans stale attribute modifiers when member 0 dies or is extracted.
- **Real Integration Verification (`RI1`–`RI6`)**:
  - Real Tipped Arrow projectile collision tested via real `Arrow` entity impact.
  - Real Splash Potion explosion tested via real `ThrownPotion` entity.
  - Real `AreaEffectCloud` lifecycle tested with initial application, reapplication delay, and exit boundaries.
- **PASSIVE_SAFE Runtime Behavior & Physical Parity (`PS1`–`PS6`)**:
  - `LivingEntityMixin` intercepts `hasEffect(Holder)`, `getEffect(Holder)`, `getActiveEffects()`, and `getActiveEffectsMap()` guarded by `jarstacker$stackCount > 1` so Vanilla systems querying active status effects operate with 100% physical-Vanilla parity.
  - Real physical parity verified for Levitation (`PS1`), Water Breathing (`PS2`), Slow Falling (`PS3`), Jump Boost (`PS4`), Active Member Switch (`PS5`), and Health Boost max HP scaling/clamping (`PS6`).
- **Bounded Unsupported Modded Area Effects (`UA1`–`UA6`)**:
  - Bounded singleton fallback extracting $\min(5, N-1)$ singletons for modded unsupported Area effects (Splash potions and Lingering clouds) instead of unconstrained $N-1$ extraction storms.
  - 20-tick reapplication cooldown (`UA4`) preventing lingering cloud exposure from continually extracting mobs every tick.
  - State and count integrity rollback (`UA6`) ensuring 0 state loss on allocation or placement failure.
- **Vanilla Area & Event-Driven Parity (`VA1`–`VA10`)**:
  - Splash Oozing $\times 100$ and Wind Charged $\times 100$ status distribution without entity storms.
  - Lingering Weaving $\times 100$ with cloud cooldown gating.
  - Infested hurt rolls, Oozing slime spawning, Weaving cobwebs, and Wind Charged burst explosions on logical events.
  - Exact $K$-callback multi-death accounting and unaffected member isolation.
  - Complete NBT save/reload fidelity for all four event-driven effects.
- **Inspect Command Integration**:
  - Extended `/jarstacker inspect` to report Status Virtualization details (known effects, unsupported effects, effect integrity, fallback reasons).
- **Automated Test Suite (314/314 Tests Passing, 100%)**:
  - 10 Vanilla Area & Event-Driven tests: `VA1`–`VA10`.
  - 6 PASSIVE_SAFE parity tests: `PS1`–`PS6` (Levitation, Water Breathing, Slow Falling, Jump Boost, Member Switch, Health Boost).
  - 6 Bounded Unsupported Modded Area tests: `UA1`–`UA6` (Single baseline, Splash $\times 100$, Lingering $\times 100$, Cloud cooldown, Materialization limit, Rollback).
  - 16 generic effect parity tests: `GE1`–`GE6` (generic parity & audit), `RI1`–`RI6` (real integration), `FB1`–`FB4` (fallback verification).
  - 39 baseline V0.6.0 tests: `S1`–`S23` (status effects), `F1`–`F7` (burning), `P1`–`P6` (projection), `ST1`–`ST3` (stress benchmarks).
  - 237 historical regression tests covering V0.1.0 through V0.5.2.
- **Documentation**: Added comprehensive architecture guide in `docs/LOGICAL_STATUS_EFFECTS_V0.6.0.md`.

## [0.5.2] - 2026-09-13

### Added
- **Runtime Equipment Extraction (`RuntimeCombatStateTransitionHandler`)**:
  - Implemented dynamic singleton extraction when an unequipped stack ($N \ge 2$) equips damageable gear (weapons, armor, tools) via pickup, dispenser, command, or drops: $\text{Stack} \times N \to \text{Stack} \times(N-1) \text{ unequipped} + \text{Physical equipped} \times 1$.
  - Atomic transfer of the active logical member's health (`activeHp`), item assignment, safe spatial placement, 60-second merge cooldown (`mergeCooldownTicks = 1200`), and automatic transactional rollback on placement failure.
  - Intercepted equipment changes cleanly via `MobMixin` on `Mob.setItemSlot`.
- **Kill-Credit vs Looting Attribution Decoupling (`CombatDeathContext` & `EnchantmentHelperMixin`)**:
  - Decoupled player kill attribution (`lastHurtByPlayerTime = 100`) from held-weapon Looting levels across all combat vectors.
  - `EnchantmentHelperMixin` intercepts `EnchantmentHelper.getEnchantmentLevel` during death processing to apply the immutable context Looting level.
  - Explicit attribution for direct melee (weapon looting), sweep attacks (primary direct, secondary sweep), projectiles (zero looting unless projectile weapon enchanted), player TNT (player credit, zero looting), player-ignited creepers (player credit, zero looting), and environmental deaths (zero player credit, zero looting).
- **Vanilla 1.21.1 Combat Attribution & MC-3304 Parity (`CombatDeathContext` & `EnchantmentHelperMixin`)**:
  - Decoupled `DamageSource` identity, direct entity, causing entity, kill credit, XP eligibility, and dynamic looting evaluation.
  - Fully reproduced Minecraft Java 1.21.1 Vanilla behavior including **MC-3304** (live evaluation of causing player's mainhand weapon enchantments at death time for arrows and player TNT).
  - Arrow and player-ignited TNT kills dynamically evaluate the causing player's current equipment at death time matching Vanilla (e.g. switching to Looting III sword before arrow/TNT lands awards Looting III; switching away awards 0).
  - Environmental deaths decouple natural environmental death (0 Looting, 0 XP, no player drops) from recent player-damaged environmental death (0 Looting, XP eligible, player-only drops eligible).
  - `EnchantmentHelperMixin` listens on `getEnchantmentLevel` to record evaluated levels in `CombatDeathContext` while allowing native Vanilla lookup without premature freezing.
- **Exact Accounting Invariants**: Guaranteed `requestedDeaths == committedDeaths` and `logicalCount == healthRecordCount` across all single and multi-death events.
- **Automated Combat & Attribution Suite (237/237 Tests Passing)**:
  - 21 automated tests (CE4–CE9, VC1–VC15) covering runtime extraction, rollback, safe placement, and complete Vanilla 1.21.1 attribution/MC-3304 parity, alongside all 216 historical regression tests.
- **Documentation**: Detailed architecture and specifications in `docs/RUNTIME_COMBAT_STATE_V0.5.2.md` and `docs/REQUIREMENTS_V0.5.2_ATTRIBUTION_CORRECTION.md`.

## [0.5.1] - 2026-09-13

### Fixed
- **Legacy Health Migration**: Legacy stacks without `LogicalHealthState` now initialize logical health from their current representative HP (`clamp(currentHP, 0, maxHP)`) instead of resetting to max health.
- **Whole-Stack Death Accounting**: Hardened whole-stack death accounting to prevent duplicate loot or XP.
- **Exact Death Transaction Ownership**: Added explicit `DeathBatch` model guaranteeing $N-1$ virtual deaths $+ 1$ representative Vanilla death $= N$ committed deaths when a stack is wiped out.
- **Conservative Damageable Equipment Semantics**: Excluded mobs bearing damageable equipment across all 6 slots (`HEAD`, `CHEST`, `LEGS`, `FEET`, `MAINHAND`, `OFFHAND`) from stacking; isolated stacks acquiring damageable equipment at runtime to stop shared durability divergence.

### Improved
- **Loot & Experience Attribution**: Preserved Vanilla killer attribution for Looting weapons; prevented unintended player XP on environmental deaths (lava, fire, fall, drowning).
- **XP Aggregation Testing**: Audited total experience value sums rather than volatile orb counts.
- **Performance Documentation**: Clarified logical combat complexity as $O(N)$ metadata processing without $O(N)$ physical entity materialization.
- **Automated Hardening Suite (216/216 Tests Passing)**: Added 18 new automated tests (CM1–CM3, CE1–CE3, D1–D12) covering migration, equipment safety, and death accounting.
- **Technical Documentation**: Added `docs/COMBAT_HARDENING_V0.5.1.md`.

## [0.5.0] - 2026-09-13

### Added
- **Per-Entity Logical Health (`LogicalHealthState`)**: Stacked mobs represent and persist health for every logical member independently (`hp_0, ..., hp_{N-1}`), maintaining the permanent invariant `logicalCount == healthRecordCount`.
- **Vanilla Combat Damage Classification (`LogicalDamageScopeResolver`)**: Centralized damage classifier mapping Minecraft 1.21.1 DamageSources to discrete logical scopes:
  - `SINGLE`: Direct melee (axe, fist, pickaxe, non-sweeping sword) and projectiles (`ARROW`, `TRIDENT`) damage only the active representative (`hp_0`).
  - `SWEEP`: Authorized Vanilla sword sweeps deal primary hit damage to the active member, and sweep damage ($1.0F + \text{SWEEPING\_DAMAGE\_RATIO} \times \text{baseDamage}$) to all remaining $(N-1)$ members of the primary stack and all members of secondary stacks in sweep range.
  - `AREA`: Explosions and lightning strikes damage all $N$ logical entities represented in the stack.
  - `SHARED_ENVIRONMENT`: Shared physical hazards (`LAVA`, `IN_FIRE`, `CAMPFIRE`, `HOT_FLOOR`, `FALL`, `DROWN`, `IN_WALL`, `FREEZE`, `CACTUS`) affect all logical members sharing the representative's position.
- **Fire Source Semantics**: Flaming projectile impacts ignite and damage only the active logical member, while environmental fire or lava ignites and damages the entire logical stack.
- **Multi-Death Pipeline Without Mass Materialization**: When $K$ logical mobs die from an event, stack count decreases by $K$, exact Vanilla loot and XP are awarded $K$ times via `dropAllDeathLoot` and `dropExperience`, and surviving active health is projected to the physical entity without spawning physical mobs.
- **Lifecycle & Persistence Integration**: Health states are preserved and transferred across merges, splits, extractions, and transformations (`LogicalEntityTransformer`).
- **Idempotent V0.4.2 Migration**: Stacks loaded without health records are automatically initialized with full health across all $N$ members.
- **Automated Combat Test Suite (198/198 Tests Passing)**: 25 new tests (H1–H25) covering direct damage, axe single-target, sword non-sweep, sweep primary and secondary propagation, explosion multi-deaths, lightning, lava/fire, flaming arrows, fall damage, loot/XP accounting, stress tests ($\times 100$ and $\times 1000$), invulnerability frames, knockback, and migration, passing alongside all 173 historical tests.
- **Comprehensive Documentation**: Added `docs/LOGICAL_COMBAT_V0.5.0.md`.


### Fixed
- **Single-Mooshroom Stale Label Bug**: Fixed bug where shearing a count-1 Mooshroom resulted in the spawned Cow retaining the `"Mooshroom ×1"` label and refusing to merge into Cow stacks. Mooshroom shearing is now classified under `TRANSFORM_ONE` regardless of stack count, undergoing destination normalization and merging.
- **Auto-Generated Label vs Player Name Tag Discrimination**: Introduced `jarstacker$hasManagedLabel` persistent entity metadata. Replaced brittle string matching with explicit metadata checks, ensuring player-named mobs (even if named `"Mooshroom ×1"` or `"Bob ×2"`) are never overwritten or mistakenly stacked, while Jar Stacker auto-labels are cleanly refreshed upon transformation.

### Added
- **Direct Transformation Association**: Created `MushroomCowMixin` hooking `MushroomCow.shear` directly to `LogicalEntityTransformer.recordDirectTransformation`, linking source Mooshrooms with their destination Cows.
- **Pre-Existing Entity Snapshotting & False Candidate Rejection**: Transformation context snapshots pre-existing nearby destination entities prior to Vanilla execution. Unrelated mobs spawned concurrently during the transformation window are rejected (`TRANSFORM_CAPTURE_CANDIDATE_REJECTED`).
- **Controlled Missed-Capture Recovery**: Added `recoverDestination` to recover transformed entities via candidate deltas if direct capture fails after a confirmed Vanilla commit. Ambiguous situations trigger conservative forward recovery (`TRANSFORM_DESTINATION_AMBIGUOUS`).
- **Destination Normalization (`normalizeDestination`)**: Enforces `count = 1`, resets interaction locks, and resets custom names to the destination type before attempting stack merges.
- **Automated Hardening Suite (173/173 Tests Passing)**: Added C1–C7 and TR11–TR16 covering direct association, candidate rejection, missed-capture recovery, ambiguity handling, idempotency, and Name Tag preservation.

## [0.4.1] - 2026-09-13

### Added
- **Transformation Transaction Lifecycle**: Formally defined explicit transaction phases (`PREPARE`, `VANILLA_EXECUTION`, `VANILLA_COMMITTED`, `JAR_STACKER_COMMIT`) in `LogicalEntityTransformer`.
- **Side-Effect Boundary Principle & Forward Recovery**: Pre-Vanilla validation failures rollback source stack safely; post-Vanilla commit failures never restore source stack count and preserve the transformed entity as a physical count-1 mob (`TRANSFORM_POST_COMMIT_FALLBACK`), conserving total logical count ($(N-1) + 1 = N$).
- **Mooshroom Stew State Compatibility**: Clean (`stewEffects == null`) vs Prepared (`stewEffects != null`) Mooshrooms are strictly separated (`MOOSHROOM_STEW_STATE_MISMATCH`). Prepared Brown Mooshrooms with differing effects are isolated (`MOOSHROOM_STEW_EFFECT_MISMATCH`).
- **Scoped Transformation Capture Context**: Captures only the matching destination `EntityType` within 4 blocks; strictly ignores item drops, XP orbs, and unrelated entity spawns.
- **Inspect Command Upgrades**: `/jarstacker inspect` displays stew state presence and formatted effect signatures.
- **Automated Hardening Suite (160/160 Tests Passing)**: Added M8–M12 and TR2–TR10 covering stew compatibility, pre/post-commit failure handling, destination fallback, and stable anchor shearing.

## [0.4.0] - 2026-09-13

### Added
- **Logical Entity Transformation (`TRANSFORM_ONE`)**: Added support for interactions where one logical entity transforms into a different `EntityType`, keeping remainder and transformed entities strictly single-owned ($N = (N-1) + 1$).
- **Mooshroom Shearing Transformation**: Shearing a Mooshroom stack transforms exactly one logical Mooshroom into an adult `Cow`, drops 5 variant mushrooms, and merges the resulting Cow into any nearby compatible `Cow` stack.
- **Mooshroom Bowl Interactions**: Milk normal Mooshroom stacks with bowls directly (`DIRECT`) without splitting; isolated stateful suspicious stew milking via `EXTRACT_ONE`.
- **Snow Golem Pumpkin Shearing**: Shearing Snow Golems removes the carved pumpkin from exactly one logical golem (`EXTRACT_ONE`), leaving the remainder pumpkin stack at the anchor. Pumpkin-less shearing returns `PASS_THROUGH` without consuming shears.
- **Pig & Strider Saddling**: Saddling adult pigs and striders safely extracts one saddled mob (`EXTRACT_ONE`), which is permanently excluded from stacking to preserve steerable riding state.
- **Strider Cold-State Compatibility**: Preserved Strider shivering/cold state (`isSuffocating()`) so warm and cold striders do not merge together.

### Improved
- **Extended Interaction Resolver**: Updated `MobInteractionResolver` with `TRANSFORM_ONE` mode, Mooshroom flower stew tracking, Snow Golem pumpkin checks, and saddle compatibility.
- **Transactional Rollback**: Built `LogicalEntityTransformer` with thread-local spawn capture, reentrancy guards, and automatic rollback on failure without item or entity loss.
- **Inspect Command Enhancements**: `/jarstacker inspect` now displays Mooshroom variant/stew effects, Snow Golem pumpkin presence, and Saddled status.

## [0.3.2] - 2026-09-12

### Improved
- **Hardened Logical State Integrity**: Enforced strict `logicalCount == logicalStateRecordCount` invariant across all stack lifecycle operations.
- **Single-Ownership State Transfer**: Hardened merge, split, materialization, re-virtualization, and promotion workflows so logical state records have exactly one owner at any time and are transferred without duplication or loss.
- **Centralized Validation & Conservative Repair**: Added `LogicalStateValidator` to audit format, sorting, and count-record parity on save and load, with conservative repair policies (padding missing records without count reduction, trimming extra records without count inflation).
- **Idempotent Chunk Migration**: Hardened schema versioning (`JarStackerLogicalVersion = 1`) to guarantee idempotent migration across repeated chunk loads.
- **Diagnostics & Debug Tooling**: Added `/jarstacker debug state` command and detailed inspection lines in `/jarstacker inspect` for state records and integrity validation status.

### Fixed
- **State Duplication & Loss**: Prevented logical baby growth metadata from being duplicated or lost during stack operations.
- **Repeated Migration Anomaly**: Prevented repeated chunk reload cycles from duplicating or re-padding logical growth entries.
- **Materialization Failure Protection**: Added full rollback protection and interaction cancellation (`InteractionResult.FAIL`) when transient physical materialization fails.
- **Re-Virtualization Safety**: Prevented physical entity deletion if logical re-virtualization insertion fails, preserving autonomous entities in the world.


### Changed
- **Logical Per-Entity Baby Growth State**: Stacked baby mobs now carry an authoritative `BabyGrowthState` storing individual monotonic maturity timestamps (`adultAt`), eliminating representative-only age synchronization.
- **Continuous Baby Feeding Priority**: Feeding a baby stack automatically selects the logical baby closest to adulthood first, preserving continuous feeding UX at the stable interaction anchor.
- **Individual Baby Promotion**: Logical babies transition into adult stacks individually or in batches as they mature, merging cleanly into nearby adult stacks or spawning a single adult representative without entity explosion.
- **Removal of Fixed Growth Lock**: Removed the arbitrary 400-tick growth lock from V0.3.0 in favor of immediate re-virtualization into authoritative logical state.
- **Sheared Sheep Stacking**: Sheared sheep of identical colors are now permitted to stack together (`Sheared Sheep ×N`), maintaining entity optimization.

### Fixed
- **Whole-Stack Baby Age Bleed**: Feeding a stacked baby no longer broadcasts accelerated growth progress across the entire stack or overwrites individual ages.
- **Infinite Sheep Wool Regrowth Exploit**: Intercepted Vanilla `Sheep.ate()` so that one grazing event regrows wool for exactly ONE logical sheep (`Sheared ×N` $\to$ `Sheared ×(N-1)` + `Unsheared ×1`), preserving the remaining sheared stack.
- **Day/Night Time Command Safety**: Baby growth timestamps are anchored to monotonic engine `GameTime` rather than celestial `DayTime`, ensuring `/time set` and `/time add` commands cannot trigger premature promotions or infinite youth.
- **Backward Migration & Persistence**: Seamless chunk load migration for existing V0.3.0 baby stacks and safe primitive `long[]` NBT persistence (`JarStackerBabyGrowth`).

### Documentation
- Created `docs/LOGICAL_ENTITY_STATE_V0.3.1.md` documenting architecture, time models, feeding algorithms, sheep accounting, and benchmarks.

## [0.3.0] - 2026-09-12

### Added
- **General Mob Interaction Framework**: Replaced one-off mob interaction hacks with a reusable, 4-mode interaction architecture (`PASS_THROUGH`, `DIRECT`, `EXTRACT_ONE`, `UNSUPPORTED`).
- **Cow Milking (DIRECT)**: Right-clicking adult cow stacks with a bucket executes directly against the representative entity at the anchor without splitting or creating physical entity churn.
- **Sheep Shearing & Dyeing (EXTRACT_ONE)**: Shearing or dyeing extracts exactly one singleton nearby while keeping remainder at the stable anchor; preserves Vanilla wool drops, shears durability damage, and dye consumption.
- **Baby Growth Feeding**: Feeding baby animal stacks extracts the fed baby and applies an `interactionLockTicks = 400` (20s) growth lock to protect accelerated age progression from being erased by stack synchronization.
- **Safe Taming Attempts**: Attempting to tame untamed wolves or cats extracts a singleton; successful taming assigns an owner and permanently excludes it from stacking (`MobCompatibility.isExcluded`); failed attempts receive a temporary lock (`100 ticks`) before safely remerging.
- **Interaction Lock System**: Added tick-down and persistent NBT serialization (`JarStackerInteractionLock`) for generalized temporary interaction locks.
- **Inspection & Diagnostics**: Extended `/jarstacker inspect` to display sheep color/sheared status, tamed ownership, and remaining interaction lock ticks.
- **Automated Test Suite (I1 - I15)**: Added 15 new interaction tests covering milking, shearing, dyeing, baby growth, taming, breeding regression, placement regression, item accounting, and persistence. Test suite expanded to 90 tests (90/90 passing).

### Documentation
- Created `docs/MOB_INTERACTION_ARCHITECTURE_V0.3.0.md` detailing classification, flows, state compatibility, and documented trade-offs.

## [0.2.5] - 2026-09-12

### Added
- **Stable Interaction Anchor / Continuous Feeding**: Feeding a stacked animal now keeps the clicked remainder stack firmly anchored at its exact position, UUID, and adult state, while extracting a singleton nearby for the love interaction.
- **Interaction Handoff**: Forwarded vanilla feeding interaction to the extracted singleton using `player.interactOn(extracted, hand)` with a reentrancy-safe thread-local guard (`HANDOFF_ACTIVE`).
- **Player View-Ray Clearance**: Enhanced `SplitPlacementResolver` to evaluate the line-of-sight from player eyes to the anchor center, applying a severe candidate penalty (-500.0) to candidates that clip the interaction ray and favoring perpendicular angular clearance.
- **Automated Test Suite (A1 - A10)**: Added 10 tests covering anchor stability, continuous 10-click feeding without camera adjustment, view-ray obstruction avoidance, near-fence containment, corner containment, survival/creative item consumption invariants, stack edge cases ($2 \to 1$), full breeding restack lifecycle, and raycast target stability. Total test suite expanded to 75 tests (75/75 passing).

### Fixed
- Fixed UX camera disorientation when feeding stacked breedable animals by eliminating displacement of the clicked stack.
- Corrected item consumption in Survival mode (exactly 1 food item consumed per feed) and Creative mode (0 consumed) during interaction handoff.
- Prevented client prediction desync on remainder stacks during rapid right-clicking.

### Documentation
- Created `docs/STABLE_INTERACTION_ANCHOR_V0.2.5.md` documenting Minecraft 1.21.1 / Fabric interaction call path, reentrancy protection, view-ray candidate scoring, and empirical test matrices.

## [0.2.4] - 2026-09-12

### Fixed
- Improved safe placement of physical animals created by stack splitting with a bounded safe position resolver (`SplitPlacementResolver`).
- Prevented extracted breeding parents from being spawned through or outside enclosure boundaries via raycast sweep and block collision checks.
- Eliminated severe collision pushing caused by overlapping split entities by ensuring non-overlapping radial placement.
- Reset unsafe inherited movement velocity (`Vec3.ZERO`) during physical extraction.
- Preserved Vanilla BreedGoal navigation and mating physics without artificial movement restrictions.

### Debugging
- Added movement diagnostics (`MovementDiagnostics`) tracking entity life stages, collision pushes, and displacement vectors.
- Documented full root cause analysis, evidence, and before/after comparisons in `docs/SPLIT_PLACEMENT_BUG_V0.2.4.md`.

## [0.2.3] - 2026-09-12

### Fixed
- Fixed unstable behavior when stacked in-love animals interact with Vanilla breeding AI.
- Added safe physical-parent handling for logical breeding stacks where required.
- Prevented breeding merge/split loops.
- Improved breeding lifecycle diagnostics.

### Debugging
- Root cause was reproduced and verified before implementing the fix.

## [0.2.2] - 2026-09-06

### Fixed
- Baby animals can now stack with compatible baby animals regardless of exact negative age.
- Breeding cooldown animals can stack together.
- Animals automatically return to adult stacks after breeding cooldown reaches 0.
- Baby stacks retain exact logical count when growing into adults and naturally merge into adult stacks.

### Changed
- Breedable animal stacking now uses lifecycle-aware groups: `BABY`, `BREEDING_IN_LOVE`, `BREEDING_COOLDOWN`, and `ADULT`.
- Baby age and breeding cooldown are synchronized to the representative entity while stacked.
- In-love stacks can participate in vanilla breeding via `BreedGoalMixin` partner extraction.
- Automated test suite expanded to 46 comprehensive tests including full lifecycle transitions and invariants.

## [0.2.1] - 2026-09-06

### Added
- Mod Menu configuration integration (`Mods → Jar Stacker → Config`).
- YACL configuration interface powered by YetAnotherConfigLib v3 with 5 distinct categories.
- Searchable filter and per-entity rule editing with clean list navigation.
- Interaction-aware mob stack splitting: feeding a stacked animal cleanly extracts a singleton ($1$) and keeps the remainder stack ($N - 1$).
- Automated test suite expanded to 34 tests covering interaction splitting, multiple breeding partners, vanilla baby creation, love state and cooldown re-merge prevention, and persistence.

### Fixed
- Stacked breedable animals can now be separated through feeding and breed using vanilla mechanics.
- Love-mode animals, animals in breeding cooldown, and baby animals are prevented from prematurely re-stacking into adult stacks.

## [0.2.0] - 2026-09-06

### Added
- **Native In-Game Configuration GUI**: Accessible via `/jarstacker config`. Allows full in-game configuration of item stacking, mob stacking, display options, and performance settings. Features dedicated sub-menus for managing Whitelist/Blacklist filters and per-entity custom rules (individual `maxStackSize` and `radius`).
- **Server-Authoritative Networking**: Implemented dedicated networking architecture with 4 discrete payloads (`ConfigRequestPayload` C2S, `ConfigDataPayload` S2C, `ConfigUpdatePayload` C2S, `ConfigResultPayload` S2C). In multiplayer, all validation and file persistence remain strictly server-authoritative with operator permission checks (permission level 2).
- **Revision Protection**: Built-in revision tracking (`configRevision`). Stale config updates submitted by clients against an outdated server revision are safely rejected without overwriting settings.
- **Item & Mob Filtering**: Added configurable filter modes (`BLACKLIST` and `WHITELIST`) for both dropped items and living mobs, allowing server administrators to explicitly allow or exclude specific registry IDs.
- **Per-Entity Custom Rules**: Configurable custom overrides on a per-ID basis (`itemStacking.rules` and `mobStacking.rules`). Administrators can set custom stack limits (e.g. 128 for cobblestone, 20 for zombies) or custom search radii.
- **Stack Inspection Command**: Added `/jarstacker inspect` allowing players to raycast their crosshair onto any entity within 6 blocks to view its stacking status, logical vs. physical count, health, UUID, and any compatibility/exclusion reasons.
- **Rolling Performance Metrics**: `/jarstacker status` now tracks rolling 10-sample average scan times and maximum observed scan durations for both item and mob scan passes.
- **Automated Config Migration & Backup**: Added automatic schema migration from V0.1 (`configVersion: 1`) to V0.2 (`configVersion: 2`). Existing configuration files are safely backed up to `config/jarstacker.json.bak` prior to migration.
- **Enhanced Visual Formatting**: Stack labels now feature vanilla item rarity colors for item names (White, Yellow, Aqua, Light Purple) and gold/yellow quantity styling with thousands-separated numbers (`%,d`).

### Changed
- **Strict Client/Server Separation**: Client UI components reside strictly in `com.jar.jarstacker.client*` and are loaded via Fabric's `client` entrypoint, preventing client-side classloading issues on dedicated servers.
- **Cleaned Up V0.2 Scope**: Dropped unused `labelDistance` configuration to ensure clean, lean configuration architecture.

### Verified Invariants
- Preserved all V0.1.0 correctness guarantees:
  - Exact invariant-preserving player pickup (no item loss or duplication).
  - Conservative mob compatibility checks (variants, baby state, equipment, wool, tags, passengers, and hard exclusions for bosses and villagers).
  - SINGLE mob death mode (representative entity survives with count - 1, full health, and drops 1 mob's worth of loot/XP).
  - NBT persistence (`JarStackerCount`) across chunk unloads and server restarts.
