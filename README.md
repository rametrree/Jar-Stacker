# Jar Stacker (V0.6.0)

**Jar Stacker** is a high-performance Minecraft Fabric mod for Minecraft Java Edition **1.21.1**.

The purpose of the mod is to reduce entity count and significantly improve server/world tick performance (TPS) by combining nearby compatible dropped items and mobs into single representative entities while maintaining exact logical stack counts.

> **Note**: Jar Stacker is an original Fabric/Java implementation inspired by the general concept of entity stacking systems.

---

## Features

### 1. Logical Status Effects & Persistent Burn State (V0.6.0)
- **Per-Logical-Mob Effect State (`LogicalStatusEffectState` & `LogicalStatusRecord`)**:
  - Every logical member in a mob stack tracks its own full Vanilla `MobEffectInstance` state (amplifier, duration, ambient, particles, icon, and chained hidden effects).
  - Preserves the strict invariant $\text{logicalCount} == \text{healthRecordCount} == \text{statusRecordCount} == \text{burnRecordCount}$.
- **Persistent Burning State (`LogicalBurnState` & `LogicalBurnRecord`)**:
  - Each logical member maintains independent fire timer countdowns (`remainingFireTicks`) and shared ignition flags.
  - Ticks fire damage authoritatively every 20 ticks unless the member has `FIRE_RESISTANCE` or is fire immune.
- **Vanilla Effect Classification & Parity Hardening (`LogicalEffectClassifier` & `LogicalVanillaEffectExecutor`)**:
  - Complete classification of all 39 Vanilla 1.21.1 `MobEffect` registry entries into 7 behavior classes (`UNKNOWN == 0`, `UNSUPPORTED == 0` across all Vanilla effects).
  - Scoped temporary projection transaction executes native Vanilla callbacks (`shouldApplyEffectTickThisTick` / `applyEffectTick`) for generic periodic effects with zero state leakage.
  - Event-driven logical parity (`EVENT_DRIVEN_LOGICAL`): `INFESTED` triggers native `MobEffect.onMobHurt()` on logical hurt; `OOZING`, `WEAVING`, and `WIND_CHARGED` trigger native `MobEffect.onMobRemoved(KILLED)` on logical death.
  - Multi-death parity: $K$ dying affected members execute exactly $K$ Vanilla death callbacks without duplicate representative triggers.
  - Splash & Lingering potions apply normally to all $N$ logical members without entity extraction storms ($O(1)$ physical mob footprint).
  - Safe physical fallback (`extractUnsupportedMob`): reserved strictly for unknown or modded non-`minecraft` effects, bounded by `MAX_UNSUPPORTED_AREA_FALLBACK = 5` with transactional rollback.
- **Single-Tick Deduplication & Representative Projection (`LogicalStatusEffectManager`)**:
  - Cancels representative entity native effect loop in `LivingEntityMixin.tickEffects()`; executes authoritative tick once per server tick via `MobEffectInstanceAccessor.jarstacker$invokeTickDownDuration()`.
  - Member 0 projects visual state (Invisibility, Glowing, fire display) and attribute modifiers (Strength, Weakness, Speed, Slowness) onto the representative.
  - Active member death/extraction triggers `onActiveMemberSwitched` to strip stale attribute modifiers and cleanly project the new active member.
- **PASSIVE_SAFE Runtime Behavior & Physical Parity (`PS1`–`PS6`)**:
  - `LivingEntityMixin` intercepts `hasEffect(Holder)`, `getEffect(Holder)`, `getActiveEffects()`, and `getActiveEffectsMap()` guarded by `jarstacker$stackCount > 1` so Vanilla systems querying active status effects operate with 100% physical-Vanilla parity.
  - Physical movement and survival parity verified for Levitation (`PS1`), Water Breathing (`PS2`), Slow Falling (`PS3`), Jump Boost (`PS4`), Active Member Switch (`PS5`), and Health Boost max HP scaling/clamping (`PS6`).
- **Bounded Unsupported Modded Area Effects (`UA1`–`UA6`)**:
  - Bounded singleton fallback extracting $\min(5, N-1)$ singletons for modded unsupported Area effects (Splash potions and Lingering clouds) instead of unconstrained $N-1$ extraction storms.
  - 20-tick reapplication cooldown (`UA4`) preventing lingering cloud exposure from continually extracting mobs every tick.
  - State and count integrity rollback (`UA6`) ensuring 0 state loss on allocation or placement failure.
- **Automated Test Suite (314/314 Tests Passing, 100%)**:
  - 10 Vanilla Area & Event-Driven tests: `VA1`–`VA10`.
  - 6 PASSIVE_SAFE parity tests: `PS1`–`PS6` (Levitation, Water Breathing, Slow Falling, Jump Boost, Member Switch, Health Boost).
  - 6 Bounded Unsupported Modded Area tests: `UA1`–`UA6` (Single baseline, Splash $\times 100$, Lingering $\times 100$, Cloud cooldown, Materialization limit, Rollback).
  - 16 generic effect parity tests: `GE1`–`GE6` (generic parity & audit), `RI1`–`RI6` (real integration), `FB1`–`FB4` (fallback verification).
  - 39 V0.6.0 baseline tests: `S1`–`S23` (status effects), `F1`–`F7` (burning), `P1`–`P6` (projection), `ST1`–`ST3` (stress benchmarks).
  - 237 historical regression tests covering V0.1.0 through V0.5.2.
- **Documentation**: Comprehensive architecture guide in `docs/LOGICAL_STATUS_EFFECTS_V0.6.0.md`.

### 2. Runtime Equipment Extraction & Attribution Integrity (V0.5.2)
- **Runtime Equipment Extraction (`RuntimeCombatStateTransitionHandler`)**:
  - Intercepts mob equipment changes via `MobMixin` on `setItemSlot` (covering mob pickup, dispenser equipping, commands, loot drops).
  - When a stacked mob ($N \ge 2$) equips damageable gear (weapons, armor, tools), it dynamically extracts: $\text{Stack} \times N \to \text{Stack} \times(N-1) \text{ unequipped} + \text{Physical equipped} \times 1$.
  - The extracted singleton inherits the active member's exact health (`activeHp`), receives the item directly, acquires a 60-second merge cooldown (`mergeCooldownTicks = 1200`), is placed safely nearby, and is permanently excluded from unequipped stacking while wearing damageable gear.
  - Failure to allocate safe placement or spawn safely rolls back stack count and equipment without entity or item loss.
- **Kill-Credit vs Looting Attribution Integrity (`CombatDeathContext` & `EnchantmentHelperMixin`)**:
  - Completely decouples player kill credit from held-weapon Looting level across all combat death types.
  - **Direct Melee**: Awards player kill credit + active weapon Looting.
  - **Sweep Attacks**: Primary target receives direct weapon Looting; secondary targets receive sweep kill attribution with sweep-specific looting rules.
  - **Projectiles (Bow / Crossbow / Trident)**: Awards player kill credit (`lastHurtByPlayerTime = 100`), but applies exact projectile weapon enchantments (zero Looting unless fired from a weapon with Looting, strictly ignoring mainhand/offhand swords held at time of projectile impact).
  - **Player TNT & Charged Creeper Indirect Kills**: Attributes player kill credit for player-ignited TNT and player-ignited creepers, with strictly zero weapon Looting.
  - **Environmental & Non-Player Deaths**: Strictly zero player kill credit and zero Looting for lava, fire, drowning, fall, suffocation, and autonomous mob vs mob kills.
  - Enforced via `EnchantmentHelperMixin` intercepting `EnchantmentHelper.getEnchantmentLevel` dynamically bound to immutable `CombatDeathContext`.
- **Vanilla 1.21.1 Combat Attribution & MC-3304 Parity (`CombatDeathContext` & `EnchantmentHelperMixin`)**:
  - Decouples `DamageSource` identity, direct entity, causing entity, kill credit, XP eligibility, and dynamic looting evaluation.
  - **Exact Vanilla 1.21.1 Parity (MC-3304)**: Faithfully reproduces Vanilla 1.21.1's live evaluation of `ATTACKING_ENTITY` mainhand enchantments at death/loot time.
  - **Projectiles & Indirect Kills**: Arrow and player-ignited TNT kills dynamically evaluate the causing player's mainhand equipment at the moment of death, matching Vanilla MC-3304 behavior (e.g. switching to Looting III sword before arrow/TNT lands grants Looting III, switching away grants 0).
  - **Direct Melee & Sweeping**: Correctly awards player kill credit and active weapon Looting.
  - **Environmental & Decoupled Attribution**: Distinguishes between natural environment (0 Looting, 0 XP, no player drops) and recent player hit + environment (0 Looting, XP eligible, player-only drops eligible).
  - **Non-Interference**: Allows native Vanilla lookup for standard sources while recording evaluated looting in `CombatDeathContext`.
- **Strict Accounting**:
  - Guarantees `requestedDeaths == committedDeaths` and `logicalCount == healthRecordCount` across all single and multi-death events.
- **Automated Combat & Attribution Suite (237/237 Tests Passing)**:
  - 21 new automated tests (CE4–CE9, VC1–VC15) covering runtime equipment extraction, rollback, safe placement, and complete Vanilla 1.21.1 attribution/MC-3304 parity, alongside all 216 historical regression tests.
- **Comprehensive Documentation**: Detailed design and implementation specifications in `docs/RUNTIME_COMBAT_STATE_V0.5.2.md` and `docs/REQUIREMENTS_V0.5.2_ATTRIBUTION_CORRECTION.md`.

### 2. Combat State & Death Hardening (V0.5.1)
- **Legacy Health Migration Fix**: Unversioned legacy stacks initialize logical health to their current representative HP (`clamp(currentHP, 0, maxHP)`). Damaged legacy stacks (e.g. Zombie $\times 10$ at 4 HP) initialize to $[4.0, \dots, 4.0]$ and never heal upon upgrade. Chunk reload is strictly idempotent.
- **Conservative Damageable Equipment Semantics**: Mobs bearing damageable equipment across all 6 slots (`HEAD`, `CHEST`, `LEGS`, `FEET`, `MAINHAND`, `OFFHAND`) are excluded from stacking. Existing stacks that acquire damageable equipment at runtime immediately halt logical combat virtualization to prevent shared durability corruption.
- **Exact Death Transaction Ownership (`DeathBatch`)**: Every logical death is strictly owned by either `VIRTUAL_DEATH` or `REPRESENTATIVE_VANILLA_DEATH`, never both.
  - Partial kills ($K < N$): $K$ virtual deaths processed; Vanilla `die()` cancelled; representative stays alive with surviving members.
  - Whole stack kills ($K == N$): $N-1$ virtual deaths processed; $1$ representative Vanilla death executed. Committed deaths $= (N-1) + 1 = N$ exactly.
  - Singleton kills ($N == 1$): $0$ virtual deaths processed; $1$ representative Vanilla death executed. Committed deaths $= 1$.
- **Loot & Experience Attribution**: Player attribution (`lastHurtByPlayerTime = 100`) set before loot/XP generation only on player kills, ensuring independent Looting rolls without granting illicit player XP for environmental deaths (lava, fire, drowning, fall).
- **Accurate Performance Characterization**: AoE damage and multi-death across $N$ logical members are characterized as $O(N)$ metadata processing with $0$ physical mob materialization overhead.
- **Automated Hardening Suite (216/216 Tests Passing)**: 18 new tests (CM1–CM3, CE1–CE3, D1–D12) passing alongside all 198 historical tests.
- **Documentation**: Detailed specifications in `docs/COMBAT_HARDENING_V0.5.1.md`.

### 2. Logical Health & Vanilla Combat Semantics (V0.5.0)
- **Per-Entity Logical Health (`LogicalHealthState`)**: Each stacked mob tracks, projects, and persists independent health records for all $N$ logical members (`hp_0, ..., hp_{N-1}`), maintaining the strict count invariant `logicalCount == healthRecordCount`.
- **Vanilla Damage Scope Resolution (`LogicalDamageScopeResolver`)**: Centralized damage classifier mapping Minecraft 1.21.1 DamageSources to discrete scopes:
  - `SINGLE`: Direct melee (axe, fist, pickaxe, non-sweeping sword) and projectiles (`ARROW`, `TRIDENT`) damage only the active representative (`hp_0`).
  - `SWEEP`: Authorized Vanilla sweeping edge attacks deal primary hit damage to the active member, and sweep damage ($1.0F + \text{SWEEPING\_DAMAGE\_RATIO} \times \text{baseDamage}$) to all remaining $(N-1)$ members of the primary stack and all members of secondary stacks in sweep range.
  - `AREA`: Explosions and lightning strikes damage all $N$ logical entities represented in the stack.
  - `SHARED_ENVIRONMENT`: Shared physical hazards (`LAVA`, `IN_FIRE`, `CAMPFIRE`, `HOT_FLOOR`, `FALL`, `DROWN`, `IN_WALL`, `FREEZE`, `CACTUS`) affect all logical members sharing the representative's position.
- **Fire Source Semantics**: Flaming projectile impacts ignite and damage only the active logical member, while environmental fire or lava ignites and damages the entire logical stack.
- **Multi-Death Pipeline Without Mass Materialization**: When $K$ logical mobs die from an event, stack count decreases by $K$, exact Vanilla loot and XP are awarded $K$ times via `dropAllDeathLoot` and `dropExperience`, and surviving active health is projected to the physical entity without spawning physical mobs.
- **Lifecycle & Persistence Integration**: Health states are preserved and transferred across merges, splits, extractions, and transformations (`LogicalEntityTransformer`).
- **Idempotent V0.4.2 Migration**: Stacks loaded without health records are automatically initialized with full health across all $N$ members.
- **Automated Combat Test Suite (198/198 Tests Passing)**: 25 new tests (H1–H25) covering all combat scopes, multi-death, sweep propagation, loot/XP conservation, stress tests ($\times 100$ and $\times 1000$), invulnerability frames, knockback, and migration.
- **Documentation**: Detailed design and implementation specifications in `docs/LOGICAL_COMBAT_V0.5.0.md`.

### 2. Transformation Capture Integrity & Identity Normalization (V0.4.2)
- **Fixed Single-Mooshroom Stale Label Bug**: Fixed bug where shearing a count-1 Mooshroom resulted in a Cow entity retaining `"Mooshroom ×1"` and being excluded from Cow stacks. Single-entity transformations now route through `LogicalEntityTransformer`, execute identity normalization, and merge cleanly into existing Cow stacks.
- **Direct Transformation Association**: Integrated `MushroomCowMixin` hooking `MushroomCow.shear` at `Level.addFreshEntity` to directly link the sheared source Mooshroom to its destination Cow, eliminating false captures.
- **Pre-Existing Entity Snapshotting & Candidate Rejection**: Snapshots all existing destination-type entities in the vicinity before Vanilla execution. Unrelated mobs spawned concurrently during the transformation window are rejected (`TRANSFORM_CAPTURE_CANDIDATE_REJECTED`).
- **Controlled Missed-Capture Recovery**: If direct capture fails after a confirmed Vanilla commit, `recoverDestination` locates the uniquely spawned entity from the delta between pre-existing snapshots and current entities. If ambiguous, conservative forward recovery is used (`TRANSFORM_DESTINATION_AMBIGUOUS`).
- **Auto-Generated Stack Labels vs Player Name Tags**: Introduced explicit persistent metadata (`jarstacker$hasManagedLabel`) distinguishing auto-generated stack labels from legitimate player custom names (e.g. `"Mushie"` or `"Mooshroom ×1"` given via Name Tag). Player Name Tags are preserved and correctly excluded from stacking without relying on brittle string parsing.
- **Strict Post-Vanilla Forward Recovery**: Permanent invariant $(N-1) + 1 = N$ ensures source stacks are NEVER restored once Vanilla has committed drops or tool durability.
- **Full Test Suite (173/173 Passing)**: 13 new automated tests (C1–C7, TR11–TR16) passing alongside all 160 existing regression tests.

### 2. Transformation Hardening & Mooshroom Stew Compatibility (V0.4.1)
- **Explicit Transformation Transaction Phases**: Formal transaction lifecycle (`PREPARE`, `VANILLA_EXECUTION`, `VANILLA_COMMITTED`, `JAR_STACKER_COMMIT`) guarantees strict ownership transfer during entity transformation.
- **Strict Side-Effect Boundary Principle & Forward Recovery**:
  - **Before Vanilla Commit**: Pre-commit validation failures safely rollback source stack state ($N$).
  - **After Vanilla Commit**: The transformation is irreversible. Once Vanilla produces drops, consumes tool durability, or spawns the destination entity, Jar Stacker **NEVER** restores the source stack count. If destination stacking fails, the entity is preserved as a physical count-1 mob (`TRANSFORM_POST_COMMIT_FALLBACK`), recovering forward with logical count conservation ($(N-1) + 1 = N$).
- **Mooshroom Stew State Compatibility**:
  - Clean (`stewEffects == null`) and Prepared (`stewEffects != null`) Mooshrooms never merge (`MOOSHROOM_STEW_STATE_MISMATCH`).
  - Prepared Brown Mooshrooms with differing stew effects never merge (`MOOSHROOM_STEW_EFFECT_MISMATCH`).
  - Clean Mooshrooms retain `DIRECT` bowl milking (infinite stew, zero splits).
  - Prepared Brown Mooshrooms use `EXTRACT_ONE` to dispense suspicious stew from exactly one logical entity.
- **Scoped Transformation Capture Context**: Captures only the expected `EntityType` within spatial proximity, strictly ignoring item drops, XP orbs, and unrelated mob spawns.
- **Full Test Suite (160/160 Passing)**: 14 new automated tests (M8–M12, TR2–TR10) passing alongside 146 existing regression tests.

### 2. Extended Mob Interactions & Logical Entity Transformation (V0.4.0)
- **Logical Entity Transformation (`TRANSFORM_ONE`)**: Added support for interactions where one logical entity transforms into a different `EntityType`, maintaining transactional single-ownership ($N = (N-1) + 1$).
- **Mooshroom Shearing $\to$ Cow**: Shearing a Mooshroom stack transforms exactly one logical Mooshroom into an adult `Cow`, drops 5 variant mushrooms, and merges the resulting Cow into any nearby compatible `Cow` stack.
- **Mooshroom Bowl Milking**: Unmodified Mooshroom stacks can be milked directly with bowls (`DIRECT`) without physical splits. Mooshrooms fed with flowers to produce suspicious stew milk via isolated `EXTRACT_ONE`.
- **Snow Golem Pumpkin Shearing**: Shearing Snow Golems removes the carved pumpkin from exactly one logical golem (`EXTRACT_ONE`), leaving the remainder pumpkin stack at the anchor. Pumpkin-less shearing returns `PASS_THROUGH` without consuming shears.
- **Pig & Strider Saddling**: Saddling adult pigs and striders safely extracts one saddled mob (`EXTRACT_ONE`), which is permanently excluded from stacking to preserve steerable riding state.
- **Strider Cold-State Compatibility**: Preserved Strider shivering/cold state (`isSuffocating()`) so warm and cold striders do not merge together.
- **Transactional Rollback**: Built `LogicalEntityTransformer` with thread-local spawn capture, reentrancy guards, and automatic rollback on failure without item or entity loss.
- **Full Test Suite (146/146 Passing)**: 18 new automated tests (M1–M7, G1–G3, P1–P3, T1–T3, TR1, IA1) passing alongside 128 existing regression tests.

### 2. Logical State Integrity & Hardening (V0.3.2)
- **Strict Invariant Enforcement**: Centralized validation guarantees that `logicalCount == logicalStateRecordCount` for every stack owning per-entity metadata across all lifecycle stages.
- **Single-Ownership State Transfer**: State records are strictly transferred rather than copied during stack merges, splits, materializations, re-virtualizations, and promotions.
- **Rollback Protection on Failure**: If transient physical materialization fails, logical state records and stack counts roll back immediately, safely cancelling the interaction (`InteractionResult.FAIL`).
- **Autonomous Mob Preservation**: If re-virtualization encounters a storage error, physical mobs are never deleted, ensuring zero mob loss.
- **Centralized Validation & Conservative Repair**: `LogicalStateValidator` inspects, audits, and conservatively repairs inconsistent records on chunk save/load:
  - Missing records are padded from the representative entity's state without decreasing stack count.
  - Extra records are trimmed deterministically without increasing stack count.
  - Non-monotonic timestamps are sorted in-place.
- **Idempotent Chunk Migration**: Versioned metadata schema (`JarStackerLogicalVersion = 1`) guarantees safe, idempotent migration across repeated chunk unloads and reloads.
- **State Debugging Tools**: Added `/jarstacker debug state` and extended `/jarstacker inspect` with record counts and integrity verification status.
- **Full Test Suite (128/128 Passing)**: Automated test suite includes 18 new logical integrity tests (L1 - L18) and a $\times 2,000$ baby cow stress test (ST4), running alongside all 109 existing regression tests.

### 2. Logical Per-Entity State & Baby Growth (V0.3.1)
- **Authoritative Logical Growth State**: Stacked baby mobs store individual monotonic maturity timestamps (`adultAt`) in `BabyGrowthState`, allowing babies of varying ages to coexist in the same stack without losing individual progress.
- **Continuous Feeding Priority**: Feeding a baby stack automatically accelerates the logical baby closest to adulthood first.
- **Individual Baby Promotion**: Babies mature into adulthood individually or in batches without requiring a global per-tick simulation loop, cleanly merging into existing adult stacks or forming new adult representatives.
- **Single-Increment Sheep Regrowth**: Grazing on grass restores wool for exactly ONE sheep in a sheared stack (`Sheared ×N` $\to$ `Sheared ×(N-1)` + `Unsheared ×1`), preventing infinite wool multiplication exploits.
- **Immunity to Time Commands**: Growth timestamps are anchored to monotonic engine `GameTime` ticks, rendering baby growth completely unaffected by celestial `/time set` or `/time add` commands.
- **Full Test Suite (109/109 Passing)**: Automated test suite includes 19 new tests covering baby age coexistence, feeding priority, individual promotion, sheep single-regrowth accounting, time manipulation safety, and stress tests for 1000 babies and 100 sheared sheep.

### 2. General Mob Interaction Framework (V0.3.0)
- **Universal Interaction Resolver**: Evaluates player actions against stacked mobs into 4 distinct modes:
  - `PASS_THROUGH`: Standard Vanilla handling for no-ops or singletons ($N=1$).
  - `DIRECT`: Right-clicking adult cow stacks with a bucket executes directly against the representative entity at the anchor, yielding milk buckets without physical entity splits or overhead.
  - `EXTRACT_ONE`: Virtual extraction transaction ($N \to (N-1) + 1$) for state-changing interactions, keeping the remainder at the stable anchor while forwarding the Vanilla interaction to a safely placed singleton.
  - `UNSUPPORTED`: Complex mounts and inventory entities (horses, llamas, camels) are deferred safely.
- **Sheep Shearing & Dyeing**: Shearing extracts a sheared singleton and produces Vanilla wool drops; dyeing extracts a colored singleton. Sheared sheep stack with sheared sheep (`SHEARED White ×N`), while colors remain strictly separated.
- **Baby Growth Feeding**: Feeding baby animals extracts a singleton with an `interactionLockTicks = 400` (20s) growth lock, ensuring accelerated age progression is preserved without being erased by stack synchronization.
- **Safe Taming Attempts**: Attempting to tame untamed wolves or cats extracts a singleton; successful taming assigns an owner and permanently excludes the mob from stacking (`MobCompatibility.isExcluded`), while failed attempts receive a temporary 5s lock before safely remerging.
- **Stable Interaction Anchor & Continuous Feeding**: Right-clicking a stacked mob keeps the remainder firmly anchored at its exact position, orientation, and UUID, allowing continuous rapid interactions without crosshair readjustment.
- **View-Ray Clearance**: Prevents spawned singletons from obstructing the line-of-sight between player and remainder anchor.
- **Full Test Suite (90/90 Passing)**: Automated test suite includes 15 new interaction tests covering milking, shearing, dyeing, baby growth, taming, placement, and item accounting invariants alongside 75 existing regression tests.

### 2. In-Game Configuration GUI (YACL & Mod Menu)
- **Mod Menu Integration**: Open the config screen directly via `Mods → Jar Stacker → Config`.
- **Modern YACL Interface**: Built-in rich, searchable configuration UI powered by [YetAnotherConfigLib (YACL v3)](https://github.com/isXander/YetAnotherConfigLib), categorized into Items, Mobs, Filters & Rules, Display, and Performance.
- **In-Game Command**: `/jarstacker config` opens the exact same configuration screen.
- **Graceful Fallback**: If YACL is not present, Jar Stacker cleanly falls back to its built-in native screen.
- **Server-Authoritative**: On multiplayer servers, non-operators cannot modify server configurations. Saves are sent via discrete network packets (`ConfigUpdatePayload`) and validated server-side.
- **Revision Protection**: Prevents concurrent editing conflicts via configuration revision checking.

### 3. Lifecycle-Aware Animal Stacking & Breeding
- **Lifecycle Grouping**: Breedable animals are classified into 4 distinct lifecycle groups: `BABY`, `BREEDING_IN_LOVE`, `BREEDING_COOLDOWN`, and `ADULT`.
- **Intra-Lifecycle Merging**:
  - **Baby Stacking**: Baby animals stack with other compatible baby animals (`BABY + BABY → YES`), regardless of differences in exact negative age ticks.
  - **In-Love Stacking**: In-love animals stack with other in-love animals (`IN_LOVE + IN_LOVE → YES`).
  - **Cooldown Stacking**: Post-breeding animals in cooldown stack together (`COOLDOWN + COOLDOWN → YES`), regardless of cooldown tick differences.
  - **Adult Stacking**: Normal adult animals stack together (`ADULT + ADULT → YES`).
- **Cross-Lifecycle Isolation**: Cross-group stacking is strictly prohibited (e.g. Baby never stacks with Adult; Cooldown never stacks with Adult or In-Love).
- **Age & Cooldown Synchronization**: The representative entity's age/cooldown becomes the effective state of the entire logical stack while merged.
- **Seamless Natural Transitions**:
  - **Baby Growth**: When a stacked baby animal reaches age 0, it becomes `ADULT` while retaining its exact logical count (e.g. `Baby ×12` becomes `Adult ×12`), and naturally merges with nearby adult stacks.
  - **Cooldown Expiry**: When breeding cooldown reaches 0, the stack automatically transitions to `ADULT` and rejoins nearby adult stacks.
- **Fluid Vanilla Breeding & Pair Extraction (V0.2.3)**:
  - When two in-love animals mate, vanilla breeding spawns exactly one baby and sets parents to cooldown ($20 \rightarrow 18\text{ adult} + 2\text{ cooldown} + 1\text{ baby} = 21$).
  - Larger `IN_LOVE` stacks naturally support partner extraction (`BreedGoalMixin`) when seeking mating partners without merge/split loops ($10 \rightarrow 8\text{ in-love} + 1\text{ parent A} + 1\text{ parent B}$).
  - Successful breeding clears locks instantly; unsuccessful breeding times out cleanly after 15 seconds without entity leaks.
- **Strict Invariants**: Zero mob duplication, zero mob loss, and pure vanilla food consumption and XP mechanics.

### 4. Safe Split Placement & Enclosure Protection (V0.2.4)
- **Bounded Safe Split Placement**: Extracted breeding entities use `SplitPlacementResolver` to find safe, localized coordinates around the parent stack, validating against block collisions (`level.noCollision`).
- **Enclosure & Fence Containment**: Line-of-sight sweep validation verifies no fence, wall, or impassable collision boundary is crossed between the parent stack and the split entity, ensuring mobs never spawn outside fenced pens.
- **Mutual Push Elimination**: Candidate spacing coordinates avoid overlapping bounding boxes with other participating parents/remainders, eliminating artificial collision repulsion forces.
- **Velocity Reset**: Explicitly zeroes inherited velocity (`setDeltaMovement(Vec3.ZERO)`) on extraction.
- **Preserved Vanilla Physics & BreedGoal**: Full Vanilla mating navigation and physics are preserved without artificial restrictions.

### 4. Item Stacking & Filters
- **Logical Stacks Beyond 64**: Combines compatible dropped `ItemEntity` objects up to a configurable maximum (default: `4096`), far exceeding the vanilla item stack cap of 64.
- **Filter Modes**: Supports `BLACKLIST` and `WHITELIST` filtering.
- **Per-Item Custom Rules**: Override maximum stack size or search radius for specific item IDs (e.g. `minecraft:cobblestone` capped at `128`).
- **Visual Rarity Labels**: Shows floating custom name with item rarity coloring (Common, Uncommon, Rare, Epic) and gold formatted quantities (e.g., `Diamond ×1,264`). Normal single items show no label.
- **Strict Compatibility**: Merges items only when identical in item type, durability, custom name, enchantments, and 1.21 data components. Does not merge damaged items or unstackable items by default.
- **Safe Player Pickup**: Invariant-preserving player pickup logic. As a player walks over a stacked item, their inventory receives items up to capacity, and the exact remainder stays in the world. No item duplication or item loss.
- **Overflow Protection**: Items exceeding `maxStackSize` properly overflow into a secondary item entity without silent deletion.

### 5. Mob Stacking & Custom Rules
- **Conservative Merging**: Merges nearby compatible living mobs into a single representative mob (default max stack: `256`, radius: `6.0` blocks).
- **Filter Modes**: Supports `BLACKLIST` and `WHITELIST` filtering for mob entity types.
- **Per-Mob Custom Rules**: Override maximum stack size or search radius for specific mob IDs (e.g. `minecraft:zombie` capped at `20`).
- **Representative AI & Behavior**: The stacked mob behaves like a normal mob - moving, pathfinding, attacking, and burning under sunlight.
- **Dynamic Stack Labels**: Displays entity name and comma-separated count (e.g. `Zombie ×37`). Normal single mobs show no label.
- **Detailed Compatibility**: Checks entity type, baby/adult status, sheep wool color/sheared state, slime/magma cube size, creeper powered state, variants (wolf, cat, frog, horse, etc.), and equipped armor/weapons.
- **Excluded by Default**: Players, boss mobs (Ender Dragon, Wither), Armor Stands, Villagers, Wandering Traders, tamed animals, named mobs (nametagged), and mobs with passengers.
- **SINGLE Death Mode**: When a mob with stack count > 1 dies, exactly 1 logical mob dies. The mob drops normal loot and experience for 1 mob, plays its death sound and hurt animation, and the representative mob remains alive with `count - 1` and reset health. When the final mob dies, it disappears normally.

### 6. Diagnostics & Inspection
- **Stack Inspection**: `/jarstacker inspect` raycasts the targeted entity (up to 6 blocks) and provides full diagnostic info: logical count, physical count, lifecycle state (`BABY`, `BREEDING_IN_LOVE`, `BREEDING_COOLDOWN`, `ADULT`), age in ticks, health, stackability, and compatibility/exclusion reasons.
- **Rolling Performance Metrics**: `/jarstacker status` reports rolling 10-sample average scan times and maximum observed scan durations for item and mob processing loops.

### 7. Automated Config Migration & Backup
- Automatically migrates older V0.1 configs (`configVersion: 1`) to the V0.2 schema (`configVersion: 2`).
- Automatically creates a backup at `config/jarstacker.json.bak` before writing migrated settings.

### 8. Persistence & Safety
- Logical item and mob stack counts are serialized to NBT (`JarStackerCount`).
- Counts survive chunk unloads, dimension transfers, world saves, and server/game restarts.
- Strict client/server separation: dedicated servers run cleanly with zero client class dependencies.

---

## Requirements & Installation

- **Minecraft**: `1.21.1`
- **Java**: `21`
- **Fabric Loader**: `>=0.19.3`
- **Fabric API**: `>=0.116.15+1.21.1`
- **Optional Dependencies**:
  - [YetAnotherConfigLib (YACL v3)](https://github.com/isXander/YetAnotherConfigLib): For the modern categorized config screen.
  - [Mod Menu](https://modrinth.com/mod/modmenu): To access configuration from the in-game mods list.
- **Environment**: Server-side and Client-side (dedicated servers do not require YACL or Mod Menu).

### Installation
Place `jarstacker-0.2.1.jar` into your Minecraft instance's `mods` folder along with Fabric API.

---

## Configuration (`config/jarstacker.json`)

```json
{
  "configVersion": 2,
  "itemStacking": {
    "enabled": true,
    "radius": 4.0,
    "scanIntervalTicks": 10,
    "maxStackSize": 4096,
    "showLabel": true,
    "stackUnstackableItems": false,
    "filterMode": "BLACKLIST",
    "blacklist": [],
    "whitelist": [],
    "rules": {
      "minecraft:cobblestone": {
        "enabled": true,
        "maxStackSize": 128,
        "radius": 4.0
      }
    }
  },
  "mobStacking": {
    "enabled": true,
    "radius": 6.0,
    "scanIntervalTicks": 20,
    "maxStackSize": 256,
    "showLabel": true,
    "deathMode": "SINGLE",
    "filterMode": "BLACKLIST",
    "blacklist": [],
    "whitelist": [],
    "rules": {
      "minecraft:zombie": {
        "enabled": true,
        "maxStackSize": 20,
        "radius": 6.0
      }
    }
  },
  "display": {
    "showItemLabels": true,
    "showMobLabels": true,
    "itemLabelFormat": "{name} ?{count}",
    "mobLabelFormat": "{name} ?{count}"
  },
  "performance": {
    "debugLogging": false
  }
}
```

---

## Commands

All commands are rooted under `/jarstacker`:

| Command | Permission | Description |
|---|---|---|
| `/jarstacker config` | Client | Opens the native in-game configuration GUI |
| `/jarstacker inspect` | All | Raycasts targeted entity and displays full diagnostic information |
| `/jarstacker status` | All | Displays live stacking status, counts, and rolling performance averages |
| `/jarstacker reload` | OP (level 2) | Reloads `config/jarstacker.json` from disk |
| `/jarstacker items <on\|off>` | OP (level 2) | Toggles item stacking on or off |
| `/jarstacker mobs <on\|off>` | OP (level 2) | Toggles mob stacking on or off |
| `/jarstacker debug` | OP (level 2) | Toggles detailed debug merge logging |
| `/jarstacker test runAll` | OP (level 2) | Runs the comprehensive test suite (25 automated tests) |
| `/jarstacker test runAll` | OP (level 2) | Runs the comprehensive test suite (46 automated tests) |
| `/jarstacker test spawnItems <count>` | OP (level 2) | Spawns test items |
| `/jarstacker test spawnZombies <count>` | OP (level 2) | Spawns test zombies |
| `/jarstacker test runStack` | OP (level 2) | Forces an immediate manual scan and stack |

---

## Build Instructions

To build the mod JAR from source:

```powershell
.\gradlew.bat build
```

The resulting mod JAR will be located in:
```text
build\libs\jarstacker-0.2.1.jar
build\libs\jarstacker-0.2.2.jar
```

---

## Known Limitations (V0.2.1)
## Known Limitations (V0.2.2)

1. **Death Mode**: Currently only `SINGLE` death mode is supported; `ALL` death mode is planned for future versions.
2. **Complex Breedable Mobs**: Entities with complex inventory, saddle, or passenger state (e.g. Horses, Donkeys, Mules, Llamas, Camels) or complex egg/hatching lifecycles (Sniffers, Sea Turtles) remain excluded from automatic interaction splitting to prevent NBT desync or duplication.
3. **Vanilla Mob Cap**: The mod reduces entity counts directly, which may cause vanilla mob spawning rules to spawn more mobs up to the vanilla hostile mob cap unless spawn rates are externally tuned.
2. **Age & Cooldown Synchronization**: As designed for high performance and zero per-logical-mob memory overhead, when baby or cooldown animals merge, the representative entity's age/cooldown becomes the effective state for the logical stack.
3. **Complex Breedable Mobs**: Entities with complex inventory, saddle, or passenger state (e.g. Horses, Donkeys, Mules, Llamas, Camels) or complex egg/hatching lifecycles (Sniffers, Sea Turtles) remain excluded from automatic interaction splitting to prevent NBT desync or duplication.
4. **Vanilla Mob Cap**: The mod reduces entity counts directly, which may cause vanilla mob spawning rules to spawn more mobs up to the vanilla hostile mob cap unless spawn rates are externally tuned.


