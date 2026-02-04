This is a Minecraft 1.7.10 Forge mod for GregTech: New Horizons (GTNH) that provides time acceleration blocks. The mod accelerates tile entity ticking with special handling for GregTech machines to prevent power issues. Please follow these guidelines when contributing:

## Code Standards

### Required Before Each Commit
- Run `./gradlew spotlessApply` before committing to ensure proper code formatting
- This applies Spotless formatting rules to maintain consistent style across the codebase

### Development Flow
- Build: `./gradlew build`
- Run client: `./gradlew runClient`
- Run server: `./gradlew runServer`
- Format code: `./gradlew spotlessApply`
- Full CI check: Automatic via GitHub Actions on PR/push

## Repository Structure
- `src/main/java/com/czqwq/Torcherino/`: Main source code
  - `Torcherino.java`: Main mod class with @Mod annotation, contains the mod entry point
  - `Config.java`: Forge configuration system integration
  - `ClientProxy.java` / `CommonProxy.java`: Side-specific proxy classes for client/server separation
  - `block/`: Block definitions for Torcherino, CompressedTorcherino, and DoubleCompressedTorcherino
  - `tile/`: Tile entity logic that handles time acceleration mechanics
  - `init/`: Registration and initialization code
  - `mixin/`: Mixin classes for runtime code modification (if needed)
- `src/main/resources/`: Resources including mcmod.info and assets
- `gradle.properties`: Project configuration including Minecraft/Forge versions and build settings

## Key Guidelines

1. **Follow Minecraft Forge 1.7.10 patterns**: Use @Mod annotations, FML event lifecycle (preInit → init → postInit), and @SidedProxy for client/server separation
2. **Maintain existing code structure**: Keep the clean separation between blocks, tile entities, and initialization
3. **Use modern Java syntax with Jabel**: The project uses Jabel to allow modern Java syntax (up to Java 17) while targeting JVM 8
4. **Check GregTech integration**: Use `Torcherino.hasGregTech` boolean flag to detect if GregTech is loaded and apply special handling for GT machines
5. **Follow naming conventions**:
   - Blocks: `Block[Name].java` (e.g., `BlockTorcherino.java`)
   - Tile Entities: `Tile[Name].java` (e.g., `TileTorcherinoAccelerated.java`)
6. **Use centralized logging**: Always log through `Torcherino.LOG` (e.g., `Torcherino.LOG.info("Message")`)
7. **Respect .editorconfig settings**: 4 spaces for indentation, UTF-8 encoding, LF line endings
8. **Handle null safety**: Always check for null when working with World and TileEntity references, especially during time acceleration
9. **Be side-aware**: Consider client vs. server execution contexts; use proxies for side-specific code
10. **Performance matters**: Time acceleration can be CPU-intensive - optimize tile entity ticking and consider chunk loading states
11. **Keep it minimal**: This is a focused mod - avoid adding unnecessary features or dependencies that don't relate to time acceleration
12. **Test with GTNH**: Changes should be compatible with the GregTech: New Horizons modpack ecosystem
