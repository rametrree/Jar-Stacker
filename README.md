# Jar Stacker (v0.6.0)

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Loader_%3E%3D0.19.3-blue.svg)](https://fabricmc.net/)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Tests Passing](https://img.shields.io/badge/Tests-343%20%2F%20343%20Passing-success.svg)](#automated-test-suite)
[![Version](https://img.shields.io/badge/Version-0.6.0-blueviolet.svg)](https://github.com/rametrree/Jar-Stacker)

**Jar Stacker** is an advanced, high-performance entity stacking and logical virtualization mod for **Minecraft Java Edition 1.21.1 (Fabric)**.

Designed to maximize server and single-player tick rates (TPS) under heavy loads, Jar Stacker combines nearby compatible dropped items and living mobs into single representative entities. Unlike simplistic stacking mods that delete entities or flatten state, Jar Stacker operates on a **strict logical virtualization architecture**—preserving individual mob health, status effects, fire timers, equipment invariants, drop attribution, and smooth, seamless item physics with zero item loss.

---

## Key Features at a Glance

- **Latest Entity Wins Item Architecture (v0.6.0)**: Dropped items consolidate into the newest item with natural Vanilla physics, preserving exact position and velocity with zero survivor teleportation or visual pop.
- **Adaptive 2/10 Scan Cadence**: High-frequency scans (every 2 ticks) for active/newly dropped items ensure rapid consolidation during mining, with an ultra-lightweight 10-tick baseline for settled items.
- **Per-Logical-Entity Status Effects & Persistent Burn (v0.6.0)**: All 39 Vanilla 1.21.1 status effects, fire tick timers, and event-driven death triggers are tracked independently per logical mob member.
- **Complete Vanilla Combat & MC-3304 Parity (v0.5.2)**: Decoupled damage attribution, Looting level evaluation, sweeping edge mechanics, and projectile kill credit matching native Minecraft 1.21.1.
- **Dynamic Equipment Extraction**: Mobs picking up damageable weapons or armor dynamically extract as singletons to protect equipment durability.
- **Lifecycle-Aware Breeding & Growth**: Independent baby growth progression, single-increment sheep wool regrowth, and transactional interaction splitting.
- **In-Game Configuration GUI**: Rich, searchable configuration screen powered by YetAnotherConfigLib (YACL v3) and Mod Menu.
- **100% Verified**: 343 automated regression tests passing on every build.

---

## 1. Item Stacking Architecture (v0.6.0)

### Latest Entity Wins Consolidation
When compatible items are detected within the configured radius, the newest `ItemEntity` (determined deterministically by age, spawn sequence, and runtime entity ID) becomes the physical survivor:
- **Zero Positional Teleportation**: The survivor maintains its exact runtime coordinates, velocity, and on-ground state (`distMoved = 0.0`).
- **Seamless Merging**: Older physical entities are cleanly discarded, while their logical counts transfer instantaneously into the survivor.
- **No Artificial Interpolation**: Pure native Minecraft rendering without fragile client render hooks, fake glide animations, or client-side lerp offsets.

### Adaptive 2/10 Scan Cadence
- **Active / Fresh Items**: Evaluated every **2 ticks (~100 ms)** when moving, airborne, or newly spawned (`age <= 40`), delivering near-instant consolidation during rapid block mining.
- **Settled Items**: Bypasses expensive queries and scans every **10 ticks (~500 ms)** once items settle on the ground, keeping CPU overhead below $0.3\text{ ms}$ even with 1,000+ items.

### Broad-Radius Consolidation & Compatibility
- **Configured Radius**: Consolidates items across the configured spherical radius (default: `4.0` blocks) without gating behind physical collision boxes.
- **Strict Data Matching**: Stacks items only when item types, durability, enchantments, custom names, and Minecraft 1.21 data components match identically.
- **Overflow Protection**: Piles exceeding `maxStackSize` (default: `4096`) cleanly overflow into a secondary item entity without silent item deletion.

### Visual Clarity & Configurable Labels
- Custom floating labels display item rarity color (Common, Uncommon, Rare, Epic) and formatted counts (e.g. `Cobblestone ×128`).
- **Clean Single Items**: When `showCountOnlyWhenStacked` is enabled (default), single items (`count == 1`) display no nametag, keeping the world looking 100% Vanilla.

---

## 2. Mob Stacking & Logical Virtualization

### Logical Status Effects & Persistent Fire (v0.6.0)
- **Individual Effect Tracking**: Every logical member in a mob stack stores its own `MobEffectInstance` states (amplifier, duration, ambient flags, particle effects).
- **Persistent Burn Counters**: Fire ticks are counted down independently per member (`remainingFireTicks`). Fire damage ticks authoritatively every 20 ticks.
- **Vanilla Parity**: Event-driven effects trigger exact Vanilla death/hurt callbacks (`OOZING` spawns slimes, `WEAVING` spawns cobwebs, `WIND_CHARGED` triggers wind bursts, and `INFESTED` spawns silverfish on hurt).
- **Zero Mass Entity Overhead**: Splash and lingering potions affect all $N$ members with $O(1)$ physical entity footprint.

### Combat Attribution & MC-3304 Parity (v0.5.2)
- **Decoupled Combat Context (`CombatDeathContext`)**: Independently evaluates killer identity, damage source, player kill attribution, and enchantment levels.
- **MC-3304 Parity**: Bows, crossbows, tridents, and player-ignited TNT evaluate the player's held weapon at the moment of impact/death matching native 1.21.1 behavior.
- **Sweeping & Melee**: Direct hits award full weapon Looting to the primary target; sweeping attacks propagate sweep-specific damage to secondary members.
- **Environmental Deaths**: Lava, fire, drowning, suffocation, and fall deaths never award illicit player kill credit or Looting bonuses.

### Dynamic Equipment Extraction (v0.5.2)
- Stacked mobs picking up or being equipped with damageable weapons, tools, or armor automatically extract the active member as an autonomous singleton ($N \to (N-1) + 1$).
- The extracted singleton retains its exact current health, receives a 60-second merge cooldown, and is excluded from unequipped stacks to prevent durability exploits.

### Logical Health & Multi-Death Pipeline (v0.5.0–v0.5.1)
- **Per-Member Health Records**: Invariant $\text{logicalCount} == \text{healthRecordCount}$ is preserved at all times.
- **Scope Resolution**: Attacks damage the active member (`SINGLE`), all members in sweep range (`SWEEP`), or the entire stack (`AREA` / `SHARED_ENVIRONMENT`).
- **Multi-Death Batching**: When $K$ members die from an explosion or sweeping attack, exactly $K$ deaths are processed, generating exact loot and XP $K$ times without mass physical entity spawning.

### Lifecycle-Aware Animal Breeding & Growth (v0.2.3–v0.4.0)
- **4 Lifecycle Groups**: `BABY`, `BREEDING_IN_LOVE`, `BREEDING_COOLDOWN`, and `ADULT` remain strictly separated.
- **Continuous Feeding**: Feeding a baby stack accelerates maturity for the logical baby closest to adulthood first.
- **Single-Increment Wool Regrowth**: Sheep grazing restores wool for exactly ONE sheep in a sheared stack (`Sheared ×N` $\to$ `Sheared ×(N-1)` + `Unsheared ×1`).
- **Safe Split Placement**: Breeding extracts mating pairs with bounded, line-of-sight collision checks, preventing animals from clipping through fences or walls.

### Interactive Mob Transformations (v0.4.0–v0.4.2)
- **Mooshroom Shearing**: Shearing a Mooshroom stack transforms exactly one logical entity into an adult Cow, drops 5 mushrooms, and merges the Cow into nearby Cow stacks.
- **Mooshroom Stew Compatibility**: Clean Mooshrooms support direct bucket milking; flower-fed brown Mooshrooms extract singletons for suspicious stew.
- **Snow Golems & Saddling**: Pumpkin shearing and saddling extract singletons safely with transactional rollback on failure.

---

## In-Game Configuration GUI

Jar Stacker includes a comprehensive in-game configuration interface powered by [YetAnotherConfigLib (YACL v3)](https://github.com/isXander/YetAnotherConfigLib) and [Mod Menu](https://modrinth.com/mod/modmenu).

- Open via **Mod Menu**: `Mods → Jar Stacker → Config`
- Open via **In-Game Command**: `/jarstacker config`
- **Native Fallback**: If YACL is not installed, Jar Stacker provides a clean built-in configuration UI.
- **Server Authoritative**: Operators configure server settings with client-server network packet synchronization and concurrent editing protection.

### Configuration Schema (`config/jarstacker.json`)

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
    "showCountOnlyWhenStacked": true,
    "itemLabelFormat": "{name} ×{count}",
    "mobLabelFormat": "{name} ×{count}"
  },
  "performance": {
    "debugLogging": false
  }
}
```

---

## Commands

All commands are registered under `/jarstacker`:

| Command | Permission | Description |
|---|---|---|
| `/jarstacker config` | All | Opens the in-game configuration GUI |
| `/jarstacker inspect` | All | Raycasts targeted entity (up to 6m) and displays full diagnostic state |
| `/jarstacker status` | All | Displays live stacking status, counts, and rolling performance averages |
| `/jarstacker reload` | OP (level 2) | Reloads `config/jarstacker.json` from disk |
| `/jarstacker items <on\|off>` | OP (level 2) | Toggles item stacking on or off |
| `/jarstacker mobs <on\|off>` | OP (level 2) | Toggles mob stacking on or off |
| `/jarstacker debug` | OP (level 2) | Toggles detailed debug merge logging |
| `/jarstacker test runAll` | OP (level 2) | Runs the comprehensive automated test suite (343 tests) |
| `/jarstacker test spawnItems <count>` | OP (level 2) | Spawns a batch of test items |
| `/jarstacker test spawnZombies <count>` | OP (level 2) | Spawns a batch of test zombies |
| `/jarstacker test runStack` | OP (level 2) | Triggers an immediate manual scan and stack |

---

## Automated Test Suite

Jar Stacker features an extensive internal integration test suite executed directly inside a dedicated server environment:

```powershell
.\gradlew.bat runServer -PrunTests
```

**Result: 343 / 343 Tests Passing (100%)**

- **IM1–IM29 (Item Stacking Invariants)**: Latest entity wins, zero survivor displacement, motion preservation, adaptive 2/10 scan latency, rapid mining sequences, real `level.destroyBlock` block drops, and count conservation.
- **VA1–VA10 (Vanilla Effect Parity)**: Event-driven callbacks (`INFESTED`, `OOZING`, `WEAVING`, `WIND_CHARGED`), multi-death exact counts, and splash/lingering potion integration.
- **PS1–PS6 (Passive Effect Parity)**: Movement and survival parity for Levitation, Water Breathing, Slow Falling, Jump Boost, and Health Boost.
- **UA1–UA6 (Unsupported Area Fallback)**: Bounded singleton fallback, cooldown reapplication, and state rollback safety.
- **GE1–GE6, RI1–RI6, FB1–FB4**: Generic effect execution and fallback validation.
- **S1–S23, F1–F7, P1–P6**: Status effect tracking, fire countdown persistence, and representative attribute projection.
- **VC1–VC15, CE1–CE9**: Combat attribution integrity, dynamic equipment extraction, and MC-3304 parity.
- **CM1–CM3, D1–D12, H1–H25**: Logical health, damage scoping, and death batching.
- **C1–C7, TR1–TR16, M1–M12, L1–L18**: Transformations, baby growth, and chunk save/load idempotency.

---

## Requirements & Installation

- **Minecraft**: `1.21.1`
- **Java**: `21`
- **Fabric Loader**: `>=0.19.3`
- **Fabric API**: `>=0.116.15+1.21.1`
- **Optional (Recommended)**:
  - [YetAnotherConfigLib (YACL v3)](https://github.com/isXander/YetAnotherConfigLib): For the in-game configuration GUI.
  - [Mod Menu](https://modrinth.com/mod/modmenu): To access configuration from the mods menu.

### Installation
1. Ensure **Fabric Loader** and **Fabric API** are installed.
2. Download `jarstacker-0.6.0.jar` and place it in your `.minecraft/mods` folder.
3. (Optional) Add YACL and Mod Menu for in-game configuration.

---

## Building from Source

To compile the mod JAR from source:

```powershell
# Clean build cache
.\gradlew.bat clean

# Compile and run test suite
.\gradlew.bat compileJava
.\gradlew.bat runServer -PrunTests

# Build production JAR
.\gradlew.bat build
```

The compiled mod JAR will be located at:
```text
build/libs/jarstacker-0.6.0.jar
```

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
