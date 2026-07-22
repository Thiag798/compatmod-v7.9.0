# Changelog

## [7.8.0] — 2026-07-04
### Added
- MixinModelBakery — intercepts ModelBakery.getModel()
- ModelTransformCache — thread-safe LRU cache
- ForgeCompatMod — unified startup with all subsystems
- VirtualModelLoaderTest — 6 patterns + edge cases + concurrency
- /compatmod command — 7 subcommands
- 4 docs: index, installation, configuration, troubleshooting
- RegistryMappingTable — 17 sample mappings

### Changed
- Config format: advanced.json in config/compatmod/
- Logging: 6 Log4j2 Markers

### Fixed
- VirtualModelLoader recursion via ThreadLocal guard
- Removed dead code: MixinRegistryAccess
- Version string consistency across all files
