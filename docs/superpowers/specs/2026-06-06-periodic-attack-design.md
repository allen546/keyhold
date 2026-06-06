# Periodic Attack — Design Spec

## Overview

Add configurable periodic attack to the existing KeyHold module. Instead of holding left click continuously, the module will click attack once per configurable tick interval.

## Requirements

- Periodic attack is a toggleable feature within the existing KeyHold module
- Single attack click per cycle (not hold-then-release)
- Simple fixed interval in Minecraft ticks (1 tick = 50ms)
- Mutually exclusive with the existing `hold-left` setting
- Default interval: 13 ticks (650ms)

## New Settings

Add a new setting group `sgPeriodic` to KeyHold:

| Setting | Type | Default | Range | Description |
|---------|------|---------|-------|-------------|
| `periodic-attack` | `BoolSetting` | `false` | — | Enable periodic attack mode |
| `attack-interval` | `IntSetting` | `13` | 1–200 | Attack interval in ticks |

## Behavior

1. When `periodic-attack = true` and `hold-left = false`, the module clicks attack once every `attack-interval` ticks.
2. When `hold-left = true`, periodic attack is ignored — left click is held continuously (existing behavior).
3. The tick counter resets to 0 on module activation.

## Code Changes

### `KeyHold.java`

**New fields:**
- `private final SettingGroup sgPeriodic` — setting group for periodic attack
- `private final Setting<Boolean> periodicEnabled` — toggle for periodic attack
- `private final Setting<Integer> periodicInterval` — interval in ticks
- `private int attackTickCounter` — current tick count toward next attack
- `private boolean needsAttackRelease` — flag to release attack key on next tick

**`onActivate()`:**
- Reset `attackTickCounter = 0`
- Reset `needsAttackRelease = false`

**`holdTick(MinecraftClient mc)`:**
```
// Release attack key from previous periodic click
if (needsAttackRelease) {
    mc.options.attackKey.setPressed(false);
    needsAttackRelease = false;
}

// Periodic attack logic
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

**`onDeactivate()`:**
- Reset `attackTickCounter = 0`
- Reset `needsAttackRelease = false`
- Release attack key if it was pressed

## UI Considerations

- The `periodic-attack` toggle and `attack-interval` setting appear in a new "Periodic Attack" setting group in the module settings
- When `hold-left` is enabled, `attack-interval` still shows but has no effect

## Testing

- Enable periodic attack, verify attack clicks occur at the configured interval
- Enable hold-left, verify continuous hold overrides periodic attack
- Change interval at runtime, verify new timing takes effect
- Deactivate module, verify attack key is released
