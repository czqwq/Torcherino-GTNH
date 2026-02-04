# Torcherino-GTNH Acceleration Features

This branch includes MTEAdvAssLine and specialized acceleration optimizations imported from NH-Utilities.

## Changes Made

### Mixin Infrastructure
- Enabled mixins in gradle.properties
- Created mixin configuration file mixins.Torcherino.json

### New Interfaces
- `ITileEntityTickAcceleration` - Interface for precise tile entity time acceleration
- `IAccelerationState` - Interface for tracking machine acceleration state

### Mixin Classes
- `MTEAdvAssLineAcceleration_Mixin` - Prevents energy drain during MTEAdvAssLine acceleration
- `BaseMetaTileEntityAcceleration_Mixin` - Advanced acceleration for GregTech machines
- `ResearchStationAcceleration_Mixin` - Acceleration for TecTech Research Station

### Configuration
- Added `enableAccelerateGregTechMachine` config option
- Added `accelerateGregTechMachineDiscount` config option (default 0.8)

## How It Works

The mixin system intercepts method calls in GregTech machines to provide optimized acceleration that:
1. Prevents energy consumption during accelerated ticks
2. Respects machine progress and max progress
3. Includes a 1ms performance limit to prevent server lag
4. Applies a configurable discount factor to balance performance
