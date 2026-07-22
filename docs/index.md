# CompatMod v7.8.0
**Forge Compatibility Layer for Legacy Minecraft Mods**

## What It Does
- Detects 6 legacy parent patterns in model JSON
- Rewrites them to modern Minecraft format
- Caches results for zero runtime cost
- Never crashes the game (graceful degradation)

## Quick Start
1. Drop `compatmod-7.8.0.jar` in `mods/`
2. Launch Minecraft with Forge 1.20.1+
3. Run `/compatmod status` — should show "4/4 healthy"
