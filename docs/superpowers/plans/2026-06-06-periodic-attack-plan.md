# Periodic Attack Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add configurable periodic attack to the KeyHold module — click attack once per tick interval instead of holding continuously.

**Architecture:** Single-file change to `KeyHold.java`. Add a new setting group, a tick counter, and a release flag. The existing `holdTick()` method is extended with periodic attack logic that is mutually exclusive with `hold-left`.

**Tech Stack:** Java 21, Minecraft 1.21.11, Meteor Client, Fabric

---

### Task 1: Add periodic attack settings and fields

**Files:**
- Modify: `src/main/java/com/you/keyhold/modules/KeyHold.java:16-44`

- [ ] **Step 1: Add the periodic attack setting group and settings**

After the `sgMouse` setting group declaration (line 18), add:

```java
private final SettingGroup sgPeriodic = this.settings.createGroup("Periodic Attack");
```

After the `holdMiddle` setting (line 41), add the new settings:

```java
private final Setting<Boolean> periodicEnabled = sgPeriodic.add(new BoolSetting.Builder()
    .name("periodic-attack").description("Click attack once per interval instead of holding.").defaultValue(false).build()
);
private final Setting<Integer> periodicInterval = sgPeriodic.add(new IntSetting.Builder()
    .name("attack-interval").description("Attack interval in ticks (1 tick = 50ms).").defaultValue(13).min(1).max(200).sliderMax(200).build()
);
```

- [ ] **Step 2: Add the tick counter and release flag fields**

After the `originalInput` field (line 44), add:

```java
private int attackTickCounter = 0;
private boolean needsAttackRelease = false;
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/you/keyhold/modules/KeyHold.java
git commit -m "feat: add periodic attack settings and fields"
```

### Task 2: Initialize periodic attack state in onActivate

**Files:**
- Modify: `src/main/java/com/you/keyhold/modules/KeyHold.java:50-56`

- [ ] **Step 1: Reset counter and flag in onActivate()**

In the `onActivate()` method, after `trackedBindings.clear();` (line 52), add:

```java
attackTickCounter = 0;
needsAttackRelease = false;
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/you/keyhold/modules/KeyHold.java
git commit -m "feat: reset periodic attack state on activation"
```

### Task 3: Release periodic attack state in onDeactivate

**Files:**
- Modify: `src/main/java/com/you/keyhold/modules/KeyHold.java:58-76`

- [ ] **Step 1: Clean up periodic attack state in onDeactivate()**

In the `onDeactivate()` method, after `trackedBindings.clear();` (line 66), add:

```java
attackTickCounter = 0;
needsAttackRelease = false;
```

Also, after the `holdLeft` release line (line 68), add release for periodic attack:

```java
if (periodicEnabled.get() && !holdLeft.get()) mc.options.attackKey.setPressed(false);
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/you/keyhold/modules/KeyHold.java
git commit -m "feat: clean up periodic attack state on deactivation"
```

### Task 4: Implement periodic attack logic in holdTick

**Files:**
- Modify: `src/main/java/com/you/keyhold/modules/KeyHold.java:78-100`

- [ ] **Step 1: Replace the attack key line in holdTick()**

Replace the existing line 92:
```java
if (holdLeft.get()) mc.options.attackKey.setPressed(true);
```

With:
```java
// Release attack key from previous periodic click
if (needsAttackRelease) {
    mc.options.attackKey.setPressed(false);
    needsAttackRelease = false;
}

if (periodicEnabled.get() && !holdLeft.get()) {
    attackTickCounter++;
    if (attackTickCounter >= periodicInterval.get()) {
        attackTickCounter = 0;
        mc.options.attackKey.setPressed(true);
        needsAttackRelease = true;
    }
} else if (holdLeft.get()) {
    mc.options.attackKey.setPressed(true);
}
```

- [ ] **Step 2: Verify the full holdTick method looks correct**

The final `holdTick` method should be:

```java
public void holdTick(MinecraftClient mc) {
    if (mc.player == null || mc.world == null) return;

    trackedBindings.clear();

    collectBindings(mc, key1.get());
    collectBindings(mc, key2.get());
    collectBindings(mc, key3.get());
    collectBindings(mc, key4.get());

    for (KeyBinding kb : trackedBindings) {
        kb.setPressed(true);
    }

    // Release attack key from previous periodic click
    if (needsAttackRelease) {
        mc.options.attackKey.setPressed(false);
        needsAttackRelease = false;
    }

    if (periodicEnabled.get() && !holdLeft.get()) {
        attackTickCounter++;
        if (attackTickCounter >= periodicInterval.get()) {
            attackTickCounter = 0;
            mc.options.attackKey.setPressed(true);
            needsAttackRelease = true;
        }
    } else if (holdLeft.get()) {
        mc.options.attackKey.setPressed(true);
    }

    if (holdRight.get()) mc.options.useKey.setPressed(true);
    if (holdMiddle.get()) mc.options.pickItemKey.setPressed(true);

    if (!(mc.player.input instanceof HoldPlayerInput)) {
        mc.player.input = new HoldPlayerInput(mc);
    }
    mc.player.input.tick();
}
```

- [ ] **Step 3: Build and verify compilation**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/you/keyhold/modules/KeyHold.java
git commit -m "feat: implement periodic attack logic in holdTick"
```

### Task 5: Update README

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add periodic attack feature to README**

In the Features section of `README.md`, after the "No key reassignment" bullet, add:

```markdown
- **Periodic attack** — configurable attack interval in ticks (1–200)
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: add periodic attack feature to README"
```
