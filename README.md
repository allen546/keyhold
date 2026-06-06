# KeyHold

A Meteor Client addon that holds down user-selected keyboard keys and mouse buttons continuously, bypassing game state — works even when paused or in menus.

## Features

- **4 keyboard keys** — bind any key via the settings
- **3 mouse buttons** — hold left click, right click, or middle click
- **Works when paused** — continues holding even with the pause menu open
- **Gameplay actions** — attack, use, interact, place, block breaking all work
- **No key reassignment** — uses your existing keybindings
- **Periodic attack** — configurable attack interval in ticks (1–200)

## Requirements

- Minecraft 1.21.11
- Meteor Client 1.21.11-SNAPSHOT
- Fabric Loader 0.18.4+
- Java 21+

## Installation

1. Build with `gradle build`
2. Copy `build/libs/keyhold-0.1.0.jar` to your Minecraft `mods/` folder
3. Launch with Meteor Client

## How It Works

The addon injects at `HEAD` of `MinecraftClient.tick()` and temporarily nullifies the `currentScreen` field, allowing Minecraft's own `handleInputEvents()` to run unblocked. This processes all keybind-driven actions (attack, use, interact, movement) through vanilla's full input pipeline — no reimplemented logic needed.
