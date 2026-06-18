# Mixin Architecture and TimeVial Feature Documentation

## Early vs Late Mixin Architecture

This document explains the mixin architecture learned from NH-Utilities and implemented in Torcherino-GTNH.

### Early Mixins
- **Loading Phase**: FML Core Mod phase (before mod initialization)
- **Loader Interface**: `IEarlyMixinLoader` 
- **Implementation**: Requires a core mod class implementing `IFMLLoadingPlugin`
- **Use Cases**: 
  - Low-level Minecraft/Forge modifications
  - Changes needed before other mods load
  - Game rules, entity behaviors, world generation
- **Example**: Minecraft entity invulnerability, weather cycle rules

### Late Mixins
- **Loading Phase**: After all mods are loaded
- **Loader Interface**: `ILateMixinLoader` with `@LateMixin` annotation
- **Implementation**: Simple class with `@LateMixin` annotation
- **Use Cases**:
  - Mod-specific changes (GregTech, EnderIO, TecTech)
  - Cross-mod integrations
  - Machine optimizations and accelerations
- **Example**: GregTech machine acceleration, EnderIO energy scaling

### Dynamic Mixin Loading Pattern

Instead of hardcoding mixin classes in JSON configuration files, we use a dynamic loading system:

```java
@LateMixin
public class LateMixinPlugin implements ILateMixinLoader {
    @Override
    public String getMixinConfig() {
        return "mixins.Torcherino.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return Mixins.getLateMixins(loadedMods);
    }
}
```

The `Mixins.java` enum uses a builder pattern to define mixins with:
- **Config conditions**: Check if feature is enabled in config
- **Mod dependencies**: Check if required mods are loaded
- **Package organization**: Automatic package path construction
- **Side filtering**: Client/Server/Both (currently simplified to Both)

**Benefits:**
1. No hardcoded mixin lists in JSON
2. Conditional loading based on config and loaded mods
3. Better organization by feature groups
4. Easy to add new mixins

## Specialized Machine Accelerations

### GregTech Machines
- **BaseMetaTileEntityAcceleration_Mixin**: 
  - Direct progress manipulation for basic/multiblock machines
  - Special handling for MTEAdvAssLine (prevents energy drain)
  - Special handling for Research Station (computation acceleration)
  - 1ms execution limit per tick

### EnderIO Machines  
- **AccelerateTileEntity_Mixin**:
  - Forces TileEntityEnder updates by resetting lastUpdate to -1
  - Implements ITileEntityTickAcceleration interface
  
- **AccelerateEnergyReceive_Mixin**:
  - Scales max energy received by acceleration rate
  - Ensures machines can receive enough power when accelerated

### TecTech Research Station
- **ResearchStationAcceleration_Mixin**:
  - Direct manipulation of computationRemaining field
  - Multiplies computation by eAvailableData × acceleration rate

## TimeVial Feature

### Overview
The TimeVial is a portable time acceleration item that spawns EntityTimeAccelerator entities at specific locations.

### EntityTimeAccelerator

**Entity Properties:**
- Size: 0.02F × 0.02F (very small)
- Invisible (no shadow, can't burn, invulnerable)
- Fixed position (no movement, collision, or rotation)
- Duration: 600 ticks (30 seconds)
- Acceleration rates: 4x, 8x, 16x, 32x, 64x, 128x

**Acceleration Logic:**
1. **GregTech Machine Mode** (default):
   - Checks if TileEntity implements `ITileEntityTickAcceleration`
   - If yes, calls `tickAcceleration(timeRate)` and returns
   - This uses optimized mixins for progress manipulation
   
2. **Traditional Mode**:
   - Calls `tileEntity.updateEntity()` multiple times
   - Includes 1ms performance limit
   
3. **Block Acceleration**:
   - Accelerates blocks with random ticks
   - Runs every 2 ticks (configurable in NH-Utilities)

**Performance Protection:**
- 1ms time limit per tick prevents server lag
- Breaks acceleration loop if time exceeded
- Catches and logs exceptions without crashing

### ItemTimeVial

**Time Storage:**
- Stored in NBT as ticks (20 ticks = 1 second)
- Accumulates passively: +20 ticks per second when in inventory
- Display format: HH:MM:SS

**Usage Modes:**

1. **Spawn New Accelerator** (Right-click on block):
   - Cost: 2400 ticks (2 minutes)
   - Creates 4x accelerator for 30 seconds
   - Shift+right-click: Spawn with GregTech mode disabled

2. **Upgrade Speed** (Right-click on existing accelerator):
   - Cost: current_rate × remaining_time ticks
   - Doubles acceleration rate (4x → 8x → 16x → 32x → 64x → 128x)
   - Max rate: 128x
   - Plays musical note with pitch based on rate

3. **Recycle Time** (Shift+right-click on existing accelerator):
   - Returns: current_rate × remaining_time ticks to vial
   - Removes the accelerator entity
   - No cost, pure recovery

**Sound Effects:**
- Uses "note.harp" sound
- Pitch array: [0.749, 0.794, 0.891, 1.059, 0.944, 0.891, 0.691]
- Pitch corresponds to log₂(rate) - 2
- Volume: 0.5F (configurable in NH-Utilities)

### Configuration Options (From NH-Utilities)

Available in NH-Utilities but simplified in Torcherino:
- `enableTimeAcceleratorBoost`: Enable 256x max rate (default false)
- `enableBlockMode`: Enable block acceleration (default true)
- `accelerateBlockInterval`: Block acceleration interval (default 2 ticks)
- `enableResetRemainingTime`: Reset timer on upgrade (default false)
- `limitOneTimeVial`: Merge vials in inventory (default true)
- `timeVialDiscountValue`: Time cost multiplier (default 0.9965)
- `defaultTimeVialVolumeValue`: Sound volume (default 0.5)
- `disableShiftModification`: Disable shift mode toggle (default false)

## Integration with Existing Torcherino

The TimeVial system is fully compatible with existing Torcherino blocks:

1. **Shared Interface**: Both use `ITileEntityTickAcceleration`
2. **Independent Systems**: Torcherino blocks and TimeVial can work simultaneously
3. **No Conflicts**: Different acceleration methods, no interference
4. **Complementary**: Torcherino for permanent acceleration, TimeVial for portable/temporary

## Implementation Notes

### From NH-Utilities
- Complete logic ported from NH-Utilities as requested
- Simplified config (no Avaritia integration, no EternityVial variant)
- Removed advanced features (cosmic rendering, helper utilities)
- Core functionality preserved: time storage, acceleration, recycling

### Key Differences from NH-Utilities
1. No ItemBase inheritance (Torcherino uses vanilla Item)
2. No Avaritia EntityImmortalItem (standard item drops)
3. Simplified config (fewer options)
4. No client-side rendering effects (can be added later)
5. No EternityVial variant (infinite duration version)

### Future Enhancements
Possible additions from NH-Utilities:
- Client-side particle effects
- Custom renderer for EntityTimeAccelerator
- EternityVial (infinite duration variant)
- More config options
- Time vial crafting recipes
- Integration with other mods

## Testing Recommendations

1. **Basic Functionality**:
   - Place TimeVial on various blocks
   - Verify entity spawns and accelerates
   - Check time consumption

2. **Upgrade System**:
   - Test speed doubling (4x → 8x → 16x, etc.)
   - Verify sound effects
   - Check max rate limit (128x)

3. **Recycling**:
   - Test time recovery
   - Verify entity removal
   - Check NBT persistence

4. **GregTech Integration**:
   - Test with GregTech machines
   - Verify mixin activation
   - Check progress acceleration
   - Test mode toggle (shift+click)

5. **Performance**:
   - Monitor TPS with multiple accelerators
   - Verify 1ms limit works
   - Test with heavy machines

6. **Edge Cases**:
   - Multiple accelerators on same block
   - Very long durations
   - NBT persistence across saves
   - Creative mode time generation
