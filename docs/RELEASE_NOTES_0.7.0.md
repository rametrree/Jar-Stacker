# Jar Stacker 0.7.0 Release Notes

**Release Version:** `0.7.0`  
**Release Date:** September 15, 2026  
**Target Platform:** Fabric Loader (`>=0.19.3`)  
**Supported Minecraft Versions:** Minecraft Java Edition `1.21.1` through `26.2`  

---

## Highlights

Jar Stacker **v0.7.0** is our largest update yet, expanding entity stacking and logical virtualization across the entire modern Minecraft lifecycle—from **Minecraft 1.21.1 through 26.2**.

- **Full Multi-Version Support (1.21.1–26.2)**: 9 dedicated compile artifacts covering 15 distinct Minecraft versions with 100% test parity.
- **Latest Entity Wins Item Stacking**: Dropped items smoothly consolidate into the newest item with natural Vanilla physics, preserving exact position and flight velocity with zero survivor teleportation or visual pop.
- **Adaptive 2/10 Scan Cadence**: High-frequency 2-tick scans for moving and freshly spawned items during mining, paired with an ultra-lightweight 10-tick baseline for settled items.
- **Complete Per-Logical-Entity Status Effects & Persistent Burn**: Independent duration, amplifier, and fire tick counters per stacked mob, with exact death and hurt callbacks for event-driven effects (`INFESTED`, `OOZING`, `WEAVING`, `WIND_CHARGED`).
- **Real Daylight Sunlight & Environmental Ignition**: Stacked undead mobs naturally detect and respond to real Vanilla sunlight and environmental hazards with zero desynchronization.
- **Native Combat & MC-3304 Parity**: Decoupled weapon Looting evaluation, sweeping edge damage propagation, and projectile kill credit matching native Minecraft.
- **100% Automated Matrix Verification**: All 15 supported runtimes pass our comprehensive 360-test automated server suite (**5,400 / 5,400 tests passing**).

---

## Minecraft Compatibility & Artifact Matrix

> [!IMPORTANT]
> **Install ONLY ONE Jar Stacker JAR matching your Minecraft version.** Do not install multiple Jar Stacker JARs simultaneously.

| Minecraft Version | Required Mod Artifact | Java | Status / Details |
| :--- | :--- | :---: | :--- |
| **1.21.1** | `jarstacker-0.7.0+mc1.21.1.jar` | Java 21 | Dedicated Target (Loom Remap) |
| **1.21.2 – 1.21.3** | `jarstacker-0.7.0+mc1.21.2.jar` | Java 21 | Compatibility Band Anchor |
| **1.21.4** | `jarstacker-0.7.0+mc1.21.4.jar` | Java 21 | Dedicated Target (Loom Remap) |
| **1.21.5** | `jarstacker-0.7.0+mc1.21.5.jar` | Java 21 | Dedicated Target (Loom Remap) |
| **1.21.6 – 1.21.8** | `jarstacker-0.7.0+mc1.21.6.jar` | Java 21 | Compatibility Band Anchor |
| **1.21.9 – 1.21.10** | `jarstacker-0.7.0+mc1.21.9.jar` | Java 21 | Compatibility Band Anchor |
| **1.21.11** | `jarstacker-0.7.0+mc1.21.11.jar` | Java 21 | Dedicated Target (Loom Remap) |
| **26.1 – 26.1.2** | `jarstacker-0.7.0+mc26.1.jar` | Java 25 | Compatibility Band Anchor (Unobfuscated Loom) |
| **26.2** | `jarstacker-0.7.0+mc26.2.jar` | Java 25 | Dedicated Target (Java 25, EntityTypes, Unobfuscated Loom) |

---

## What's New & Improved

### 1. Item Stacking UX ("Latest Entity Wins")
- **Natural Motion Preservation**: Newly mined drops retain their natural position, trajectory, and momentum. The newest entity becomes the physical survivor, eliminating jarring anchor snaps back to old piles.
- **Adaptive Cadence**: Items in motion or recently spawned (`age <= 40`) are evaluated every 2 ticks (~100 ms), delivering immediate merge responsiveness during rapid block breaking while maintaining sub-0.3 ms tick overhead on settled piles.
- **Visual Smoothness**: Operates strictly through native entity state without fragile client-side render interpolation or lerp offsets.
- **Clean Nametags**: Clean single-item display hides floating labels for unstacked items (`count == 1`), preserving pure Vanilla visuals.

### 2. Mob Stacking & Logical Virtualization
- **Strict State Invariant**: Guaranteed count balance: $\text{logicalCount} == \text{healthRecordCount} == \text{statusRecordCount} == \text{burnRecordCount}$.
- **Compile-Time Typed Variant Safety**: Typed compatibility checks across all 16 Vanilla variant entities (Mooshroom, Cow, Pig, Sheep, Wolf, Cat, Frog, Horse, Llama, Rabbit, Fox, Axolotl, Parrot, Salmon, Tropical Fish, and Villager professions).
- **Fail-Safe Unknown Variant Policy**: Custom or modded mobs with unknown variant semantics safely fail closed (`UNKNOWN_VARIANT_COMPATIBILITY`), preventing silent attribute desynchronization.
- **Dynamic Equipment Extraction**: Mobs picking up damageable weapons or armor automatically extract the active member as an independent singleton, protecting durability mechanics and preventing exploits.
- **Separated Lifecycle Groups**: Dedicated grouping for breeding readiness, mating cooldowns, baby maturity progression, and single-increment sheep wool regrowth.

### 3. Combat Attribution & Death Parity
- **Decoupled Combat Context (`CombatDeathContext`)**: Full player attribution, damage type tracking, and enchantment levels matching native Vanilla mechanics.
- **MC-3304 Parity**: Projectiles (arrows, tridents) and player-ignited TNT evaluate the player's held weapon at the exact moment of impact/death.
- **Sweeping Attacks**: Primary targets receive full weapon Looting and damage; sweeping attacks propagate secondary sweep damage cleanly across stack members.
- **Multi-Death Batching**: When $K$ members die from high-damage attacks (explosions, sweep strikes), loot and XP are awarded exactly $K$ times without mass physical entity spawning.

### 4. Logical Status Effects & Persistent Burn
- **Authoritative Classification**: All Vanilla status effects classified into 7 behavior classes, verified with 0 unknown and 0 unsupported effects across the entire registry.
- **Event-Driven Parity**: Native hurt callbacks for `INFESTED` (spawning silverfish); native death callbacks for `OOZING` (spawning slimes), `WEAVING` (placing cobwebs), and `WIND_CHARGED` (triggering wind bursts).
- **Persistent Burn Countdown**: Logical members store independent `remainingFireTicks`. Fire damage ticks authoritatively every 20 ticks unless protected by Fire Resistance.
- **Daylight Sunlight Detection**: Undead stacks accurately check daytime and sky clearance, igniting and sustaining fire with full physical-logical continuity.

### 5. In-Game Configuration GUI & Commands
- Seamless configuration UI powered by [YetAnotherConfigLib (YACL v3)](https://github.com/isXander/YetAnotherConfigLib) and [Mod Menu](https://modrinth.com/mod/modmenu).
- Built-in fallback screen available when YACL is not installed.
- Real-time diagnostic inspect command (`/jarstacker inspect`) raycasting live entity state.

---

## Important Bug Fixes

- **Sunlight Burn Extinguish Fix**: Fixed an issue where daylight sunlight burn checks did not properly synchronize shared ignition across logical undead stack records.
- **Targeted Ignition Context Isolation**: Fixed fire aspect melee and flaming arrow impacts improperly spreading single-target burn to unhit stack members.
- **Item Merge Discard Race**: Resolved rare timing conditions where older item entities remained active after count transfer during rapid multi-pile merges.
- **Attribute Modifier Staling**: Fixed residual speed and strength modifiers lingering on representative mobs after active member death.

---

## Installation

1. Install **Fabric Loader** (`>=0.19.3`) and **Fabric API** for your target Minecraft version.
2. Ensure you have the appropriate Java runtime installed:
   - **Java 21** for Minecraft `1.21.1` – `1.21.11`
   - **Java 25** for Minecraft `26.1` – `26.2`
3. Download the **single** Jar Stacker JAR matching your Minecraft version from the matrix table above.
4. Place the `.jar` file into your `.minecraft/mods` directory.
5. *(Optional)* Install **YACL v3** and **Mod Menu** to configure settings in-game.

---

## Upgrade Notes

- **Existing Worlds & Configs**: Worlds and configurations created with Jar Stacker 0.6.0 on supported versions are fully compatible and will load seamlessly.
- **Verified Cross-Version Save Upgrade (`26.1.2` $\to$ `26.2`)**: Worlds generated on Minecraft 26.1.2 containing multi-member stacks with varied health, status effects, and items were upgraded to Minecraft 26.2 and verified with 100% data fidelity.
- **Downgrade Notice**: World downgrades (e.g. 26.2 to 26.1 or 1.21.x) are not supported by Minecraft and are not supported by Jar Stacker. Always backup your worlds before performing major Minecraft version upgrades.

---

## Compatibility Notes

- **Modded Entities**: Authentic Vanilla entities registered in the `minecraft` namespace are fully supported. Custom modded entities with unknown variant mechanics safely fail closed to prevent state corruption.
- **Server-Side Operation**: Jar Stacker is fully functional on dedicated servers without requiring client-side installation. Clients connecting to a Jar Stacker server do not require the mod.
- **Optional Client Libraries**: YACL and Mod Menu are purely optional client-side enhancements; their absence has zero effect on server or client functionality.

---

## Known Limitations

- Modded entities that define non-standard custom NBT variants are not stacked by default unless added to explicit stacking rules.
- World downgrades to older Minecraft versions will cause Vanilla data loss and are unsupported.

