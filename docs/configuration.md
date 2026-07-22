# Configuration
Config file: `config/compatmod/advanced.json`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| safe_mode | bool | false | Detect but don't transform |
| debug_mode | bool | false | Verbose transformation logging |
| blacklisted_mods | string[] | [] | Mod IDs to skip |
| patch_cache_size | int | 2048 | Max cache entries |
| max_model_depth | int | 16 | Max parent chain depth |

Commands: `/compatmod status|health|cache|mods|config|reload|test`
