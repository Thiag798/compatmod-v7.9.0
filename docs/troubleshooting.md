# Troubleshooting
## Models still broken
1. `/compatmod cache` — check Transformed count
2. Enable `debug_mode: true` in advanced.json
3. Check log for `[COMPAT_TRANSFORM]`

## Crash on startup
1. Add offending mod to `blacklisted_mods`
2. Enable `safe_mode: true`
3. Report: github.com/compatmod/issues
