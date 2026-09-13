# Jar Stacker Multi-Version Architecture and Compatibility Matrix

## 1. Overview and Architecture

Jar Stacker uses Stonecutter (`dev.kikugie.stonecutter:0.9.8`) together with Fabric Loom (`1.17.20`) to deliver multi-version support across modern Minecraft releases:
- One unified codebase in `src/main/java`
- One unified feature set (Items, Mobs, Combat, Transformations, Logical Health/Status Effects, Config, Test Suite)
- One unified test suite (343 automated in-game tests)
- Minimum safe number of versioned JAR artifacts

Stonecutter enables selective version preprocessing via comments (`//? if >=1.21.2 { ... } else { ... }`), while version-specific properties and dependencies reside in `versions/<mc_version>/gradle.properties`.

---

## 2. Version Targets and Output Artifacts

| Target Minecraft Version | Mod Dependency | Build Artifact | Automated Tests |
| :--- | :--- | :--- | :--- |
| **1.21.1** | `~1.21.1` | `jarstacker-0.7.0+mc1.21.1.jar` | **343 / 343 PASS** |
| **1.21.2** | `~1.21.2` | `jarstacker-0.7.0+mc1.21.2.jar` | **343 / 343 PASS** |
| **1.21.4** | `~1.21.4` | `jarstacker-0.7.0+mc1.21.4.jar` | **343 / 343 PASS** |

Each target produces:
1. Main mod JAR: `versions/<version>/build/libs/jarstacker-0.7.0+mc<version>.jar`
2. Sources JAR: `versions/<version>/build/libs/jarstacker-0.7.0+mc<version>-sources.jar`

---

## 3. Core vs Adapter Classification

To maintain zero gameplay regression and strict isolation of version divergence, version-sensitive operations are segregated into dedicated adapter classes:

### Adapters
- `com.jar.jarstacker.adapter.EntityAdapter`:
  - `create(EntityType<T>, Level)`: Delegates to `EntityType.create(Level)` on 1.21.1, and `EntityType.create(Level, EntitySpawnReason.TRIGGERED)` on >=1.21.2.
  - `setMooshroomVariant(MushroomCow, boolean)`: Handles `MushroomCow.MushroomType` on 1.21.1 vs `MushroomCow.Variant` on >=1.21.2.
  - `createThrownPotion(Level, double, double, double)`: Creates and positions `ThrownPotion` using `EntityType.POTION` constructor supported across all versions.
- `com.jar.jarstacker.adapter.EffectAdapter`:
  - `applyEffectTick(Holder<MobEffect>, LivingEntity, int)`: Calls `applyEffectTick(LivingEntity, int)` on 1.21.1, and `applyEffectTick(ServerLevel, LivingEntity, int)` on >=1.21.2.
  - `onMobHurt(Holder<MobEffect>, LivingEntity, int, DamageSource, float)`: Calls `onMobHurt(LivingEntity, ...)` on 1.21.1, and `onMobHurt(ServerLevel, LivingEntity, ...)` on >=1.21.2.
  - `onMobRemoved(Holder<MobEffect>, LivingEntity, int, Entity.RemovalReason)`: Calls `onMobRemoved(LivingEntity, ...)` on 1.21.1, and `onMobRemoved(ServerLevel, LivingEntity, ...)` on >=1.21.2.

### Mixin Adaptations
- `LivingEntityMixin`:
  - Conditioned `@Inject` on `actuallyHurt`: Signature is `actuallyHurt(DamageSource, float)` on 1.21.1, and `actuallyHurt(ServerLevel, DamageSource, float)` on >=1.21.2.
  - Conditioned `@Shadow` on `dropFromLootTable`: Signature is `dropFromLootTable(DamageSource, boolean)` on 1.21.1, and `dropFromLootTable(ServerLevel, DamageSource, boolean)` on >=1.21.2.
  - Removed unused shadows (`dropAllDeathLoot`, `dropExperience`) to eliminate remapping warnings.
- `MushroomCowMixin`:
  - On 1.21.1: Intercepts `shear(SoundSource)` via `@ModifyArg` on `Level.addFreshEntity`.
  - On >=1.21.2: Intercepts `shear(ServerLevel, SoundSource, ItemStack)` via `@ModifyArg` on `MushroomCow.convertTo` modifying `ConversionParams.AfterConversion` to record direct transformation into `LogicalEntityTransformer`.
- `ThrownPotionMixin`:
  - Conditioned `@Inject` on `applySplash`: Signature is `applySplash(Iterable<MobEffectInstance>, Entity)` on 1.21.1, and `applySplash(ServerLevel, Iterable<MobEffectInstance>, Entity)` on >=1.21.2.

### Direct Porting
- `JarStackerClientMod`: Uses `mc.execute(...)` instead of deprecated `mc.tell(...)`.
- `LogicalEffectClassifier`: Iterates `BuiltInRegistries.MOB_EFFECT` directly instead of deprecated `holders()`.
- `MobInteractionResolver`: Uses string comparison `brown.equals(mc.getVariant().getSerializedName())` for variant-agnostic comparison.

---

## 4. Compatibility Matrix Findings

### Question 1: Can 1.21.1 and 1.21.2 share a universal JAR?
**No.**
1.21.2 introduces fundamental binary breaking changes in Mojang/Minecraft classes that prevent a single raw JAR without complex reflection or runtime bytecode generation:
- `actuallyHurt` parameter count and order changed (`ServerLevel` added).
- `ThrownPotion.applySplash` parameter count changed (`ServerLevel` added).
- `MobEffect` methods (`applyEffectTick`, `onMobHurt`, `onMobRemoved`) added `ServerLevel`.
- `EntityType.create` added `EntitySpawnReason` parameter.
Attempting a universal JAR would require reflective call-sites on hot combat/tick loops, severely compromising performance and safety. Generating separate, clean JARs via Stonecutter guarantees maximum performance and safety.

### Question 2: Do 1.21.2 and 1.21.3 share binary/runtime compatibility?
**Yes.**
Minecraft 1.21.3 is a maintenance release that does not alter any of the entity, effect, or mixin hook signatures touched by Jar Stacker. The `jarstacker-0.7.0+mc1.21.2.jar` artifact (configured with dependency `"minecraft": "~1.21.2"`) runs on both 1.21.2 and 1.21.3.

### Question 3: Does 1.21.4 require another branch or can it share source code?
**It shares source code completely.**
Minecraft 1.21.4 maintains identical API signatures with 1.21.2 for all of Jar Stacker's adapters and mixin injection points.
With Stonecutter, 1.21.4 is built from the exact same shared source tree without code duplication, generating `jarstacker-0.7.0+mc1.21.4.jar` and passing all 343 / 343 automated tests.

---

## 5. Developer Workflow

### Switching Active Project in IDE
```bash
# Switch active editing project to 1.21.1
./gradlew stonecutterSwitchTo1.21.1

# Switch active editing project to 1.21.2
./gradlew stonecutterSwitchTo1.21.2

# Switch active editing project to 1.21.4
./gradlew stonecutterSwitchTo1.21.4
```

### Compiling and Building All Versions
```bash
# Compile specific target
./gradlew :1.21.1:compileJava
./gradlew :1.21.2:compileJava
./gradlew :1.21.4:compileJava

# Build all JAR packages
./gradlew :1.21.1:build
./gradlew :1.21.2:build
./gradlew :1.21.4:build
```

### Running In-Game Automated Tests
```bash
# Run 343 tests on 1.21.1
./gradlew :1.21.1:runServer -PrunTests

# Run 343 tests on 1.21.2
./gradlew :1.21.2:runServer -PrunTests

# Run 343 tests on 1.21.4
./gradlew :1.21.4:runServer -PrunTests
```

