# Torcherino-GTNH

**English** | [简体中文](README.zh-CN.md)

> A time acceleration mod specially optimized for GregTech: New Horizons (GTNH) modpack

## 📖 Introduction

Torcherino is a magical torch mod that can accelerate the tick rate of surrounding blocks and machines. This version is specially optimized for the GTNH modpack, with perfect support for GregTech machine acceleration to make your automated factory run faster!

### ✨ Core Features

- **Time Acceleration**: Place Torcherino torches to accelerate machines and blocks in the surrounding area
- **GregTech Optimization**: Special support for GregTech machines without causing energy consumption issues or power failures
- **Multiblock Support**: Perfect support for both GregTech multiblock structures and single-block machines
- **Adjustable Range**: Freely adjust acceleration range and speed through GUI interface
- **Redstone Control**: Support redstone signal control (Classic version)
- **Time Vial**: Portable single-block acceleration tool that stores time for on-demand use

## 🔥 Block Types

The mod provides multiple tiers of Torcherino torches with different acceleration strengths:

### Standard Version (with GUI)

1. **Torcherino (Normal Torch)**
   - Basic version of time acceleration torch
   - Adjustable acceleration rate via GUI (0% ~ 400%)
   - Customizable X, Y, Z directional acceleration range

2. **Compressed Torcherino**
   - Mid-tier version with stronger acceleration
   - Adjustable acceleration rate (0% ~ 900%)
   - Same range and control as normal version

3. **Double Compressed Torcherino**
   - High-tier version with maximum acceleration
   - Adjustable acceleration rate (0% ~ 8100%)
   - Suitable for scenarios requiring extreme production speed

### Classic Version (Traditional Mode)

Each torch has a corresponding Classic version (Torcherino Classic, Compressed Torcherino Classic, Double Compressed Torcherino Classic):
- Uses traditional mode switching (right-click to toggle)
- Supports redstone signal control (stops when receiving redstone signal)
- Preset multiple fixed acceleration rate tiers

## ⏳ Time Vial

The Time Vial is a portable time acceleration tool that can temporarily accelerate individual blocks or machines.

### Features

- **Time Storage**: The Time Vial automatically accumulates time (1 second per second of game time)
- **Single-Point Acceleration**: Right-click on a block to create an acceleration entity at that location
- **Adjustable Rate**: Acceleration rate starts at 4x and can be upgraded to 128x (4x → 8x → 16x → 32x → 64x → 128x)
- **Time Consumption**: Creating acceleration entities and upgrading rates consume stored time
- **Time Recycling**: Shift + right-click on existing acceleration entities to recycle remaining time

### Usage

1. **Create Acceleration Entity**
   - Hold Time Vial and right-click on target block
   - Consumes 2,400 seconds of stored time (40 minutes)
   - Creates a 4x acceleration entity lasting 600 ticks (30 seconds)
   - **Shift + Right-Click**: Creates normal mode entity (without GregTech optimization)

2. **Upgrade Acceleration Rate**
   - Right-click on existing acceleration entity
   - Doubles the rate (4x → 8x → 16x → 32x → 64x → 128x)
   - Time consumed = current rate × remaining time
   - Maximum upgrade to 128x speed

3. **Recycle Time**
   - **Shift + Right-Click** on existing acceleration entity
   - Recycles remaining time back to Time Vial
   - Recycled amount = current rate × remaining time

4. **View Stored Time**
   - Hover mouse over Time Vial
   - Display format: `time: X hours X minutes X seconds`

### How It Works

- Acceleration entity performs multiple updates per tick on the TileEntity at target location
- Uses GregTech optimization mode by default to prevent abnormal energy consumption
- Acceleration entity is invisible with minimal size (0.02×0.02 blocks)
- Each acceleration entity lasts 600 ticks (30 seconds), accelerating target block at set rate

### Comparison with Torches

| Feature | Torcherino Torch | Time Vial |
|---------|------------------|-----------|
| Acceleration Range | Configurable area (multi-block) | Single block |
| Duration | Permanent (while placed) | Temporary (600 ticks / 30 seconds) |
| Portability | Requires block placement | Can be carried |
| Max Rate | Up to 8100% | Up to 128x |
| Resource Cost | None (after placement) | Consumes stored time |

## 🎮 Usage

### Standard Version Torches

1. Place Torcherino torch near the area you want to accelerate
2. Right-click the torch to open GUI interface
3. Adjust using sliders:
   - **Speed**: Adjust acceleration rate
   - **X/Y/Z Radius**: Adjust acceleration radius in each direction
4. Machines and blocks within range will accelerate at the set rate

### Classic Version Torches

1. Place Classic version Torcherino torch
2. Right-click to switch acceleration mode and speed tier
3. Shift + Right-Click to toggle different speed settings
4. Use redstone signals to remotely control torch on/off state

## ⚙️ Configuration

The mod provides a configuration file (located at `config/Torcherino.cfg`) for advanced users:

- **enableAccelerateGregTechMachine**: Enable advanced acceleration for GregTech machines (Default: enabled)
- **accelerateGregTechMachineDiscount**: Discount factor for GregTech machine acceleration (Default: 0.8, Range: 0.0-1.0)
  - This parameter balances performance and gameplay experience, preventing excessive acceleration from causing server lag

## 🔧 Technical Features

### GregTech Special Optimization

This mod uses advanced Mixin technology to optimize GregTech machine acceleration:
- **Prevent Energy Anomalies**: No extra energy consumption or power failures during acceleration
- **Smart Progress Management**: Properly handles machine work progress to avoid logic errors
- **Performance Limits**: Built-in performance protection with 1ms cap per acceleration to prevent server lag
- **Multiblock Support**: Special support for large GregTech multiblock structures (such as Advanced Assembly Line)

### Supported Special Machines

- GregTech base machines and multiblock structures
- TecTech Research Station
- Other Tile Entities implementing acceleration interfaces

## 📦 Dependencies

- Minecraft 1.7.10
- Minecraft Forge 10.13.4.1614 or higher
- GregTech 5 (GTNH version) recommended but not required

## 🙏 Credits

- Thanks to [MockTurtle7/Torcherino](https://github.com/MockTurtle7/Torcherino) for the original code
- Thanks to NH-Utilities for acceleration optimization solutions
- Thanks to GTNH development team for support

## 📝 License

This project follows the open source license of the original project. See [LICENSE](LICENSE) file for details.

## 🔗 Links

- Repository: https://github.com/czqwq/Torcherino-GTNH
- Issue Tracker: https://github.com/czqwq/Torcherino-GTNH/issues

---

**Note**: Please use reasonable acceleration rates. Excessive acceleration may affect server performance. It's recommended to test in single-player before using on servers.
