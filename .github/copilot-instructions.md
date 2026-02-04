# Copilot Instructions for Torcherino-GTNH

## Project Overview

Torcherino is a Minecraft 1.7.10 mod designed for GregTech: New Horizons (GTNH). It provides time acceleration blocks that can speed up tile entities, with special support for GregTech machines to prevent power issues during acceleration.

**Key Features:**
- Time acceleration for both single-block and multi-block structures
- Special handling for GregTech machines to prevent power skipping
- Multiple tiers: Torcherino, Compressed Torcherino, and Double Compressed Torcherino
- Configuration system for customizing acceleration rates and recipes

## Technology Stack

- **Minecraft Version:** 1.7.10
- **Forge Version:** 10.13.4.1614
- **Build System:** Gradle with GTNewHorizons conventions
- **Language:** Java (with modern syntax via Jabel, targeting JVM 8)
- **Mixins:** Enabled for runtime code modification

## Project Structure

```
src/main/java/com/czqwq/Torcherino/
├── Torcherino.java          # Main mod class with @Mod annotation
├── Config.java              # Configuration handling
├── ClientProxy.java         # Client-side proxy
├── CommonProxy.java         # Common/server-side proxy
├── block/                   # Block definitions
│   ├── BlockTorcherino.java
│   ├── BlockCompressedTorcherino.java
│   ├── BlockDoubleCompressedTorcherino.java
│   └── ModBlocks.java
├── tile/                    # Tile entity logic
│   ├── TileTorcherinoAccelerated.java
│   ├── TileCompressedTorcherino.java
│   └── TileDoubleCompressedTorcherino.java
└── init/                    # Initialization and registration
```

## Build and Development

### Building the Project
```bash
./gradlew build
```

### Running in Development
```bash
./gradlew runClient  # Start Minecraft client
./gradlew runServer  # Start dedicated server
```

### Code Quality
The project uses GTNH conventions which include:
- **Spotless** for code formatting
- **Checkstyle** for code style validation
- Automatic build and test workflows via GitHub Actions

## Code Style and Conventions

### General Guidelines
- Follow the `.editorconfig` settings (4 spaces, UTF-8, LF line endings)
- Use modern Java syntax where appropriate (enabled via Jabel)
- Follow existing naming conventions in the codebase

### Package Structure
- `com.czqwq.Torcherino` - Root package
- `com.czqwq.Torcherino.block` - Block classes
- `com.czqwq.Torcherino.tile` - Tile entity classes
- `com.czqwq.Torcherino.init` - Registration and initialization
- `com.czqwq.Torcherino.mixin` - Mixin classes (if added)

### Naming Conventions
- Blocks: `Block[Name].java` (e.g., `BlockTorcherino.java`)
- Tile Entities: `Tile[Name].java` (e.g., `TileTorcherino.java`)
- Use clear, descriptive names that reflect functionality

### Logging
Use the Logger from the main Torcherino class:
```java
Torcherino.LOG.info("Message");
Torcherino.LOG.error("Error", exception);
```

## Key Dependencies and Integration

### GregTech Integration
- Check `Torcherino.hasGregTech` to determine if GregTech is loaded
- Special handling is required for GregTech machines to prevent power issues during acceleration

### Forge/FML
- Use `@Mod` annotation on the main class
- Follow the standard FML event lifecycle: preInit → init → postInit
- Use `@SidedProxy` for client/server separation

### Mixins
- Mixins are enabled in this project (`usesMixins = true`)
- Mixin classes should go in the `mixin` package
- Keep mixin debug disabled unless actively debugging

## Common Patterns

### Tile Entity Updates
Tile entities in this mod perform time acceleration by ticking nearby tile entities multiple times. When working with tile entity code:
- Be mindful of performance implications
- Consider chunk loading states
- Handle null checks for neighboring tile entities

### Block Registration
Blocks are registered through the ModBlocks class during initialization. Follow the existing pattern when adding new blocks.

## Testing

The project uses GTNewHorizons GitHub Actions workflows for CI/CD:
- Builds are automatically triggered on PRs and pushes to master/main
- Tagged releases are automatically published
- Timeout for builds is set to 180 minutes

## Configuration

The mod uses Forge's configuration system. Configuration files are managed through the `Config.java` class.

## Important Notes

1. **Minimal Changes:** This is a focused mod - avoid adding unnecessary features or dependencies
2. **GTNH Compatibility:** Always consider compatibility with the GTNH modpack ecosystem
3. **Performance:** Time acceleration can be CPU-intensive - optimize for performance
4. **Null Safety:** Always check for null when working with world and tile entity references
5. **Side Awareness:** Be aware of client vs. server execution contexts

## Resources

- Main Repository: https://github.com/czqwq/Torcherino-GTNH
- Original Torcherino: https://github.com/MockTurtle7/Torcherino
- GTNH Organization: https://github.com/GTNewHorizons

## When Making Changes

1. **Test Thoroughly:** Test changes in both single-player and multiplayer environments
2. **Check GregTech Integration:** If modifying acceleration logic, test with GregTech machines
3. **Follow Existing Patterns:** Match the style and structure of existing code
4. **Update Documentation:** Update comments and documentation when changing functionality
5. **Run Spotless:** Ensure code formatting is correct with `./gradlew spotlessApply`
