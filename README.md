# Workers Ore Compat

Forge mod for Minecraft 1.20.1 that teaches Villager Recruits' Workers about
ores and drops from other mods.

## Requirements

- Minecraft 1.20.1
- Forge 47.3.x
- Villager Recruits 2.0.3 or newer

## Features

- Adds configured blocks to the worker miner's ore scan.
- Adds configured items and item tags to the miner's pickup rules.
- Supports block and item exclusions.
- Rebuilds its cache when the config or datapack tags change.

## Configuration

The common config is generated at:

`config/workers_ore_compat-common.toml`

The default lists cover the ore mods used during development. Exact IDs and
tags can be added without changing the mod jar. Set `debugLogging = true` to
log custom matches while testing a pack.

## Building

Use Java 17 and run:

```text
gradlew build
```

The release jar is written to `build/libs/`.

The local `libs/` directory contains development-only third-party mod jars and
is not part of this repository. A full development environment needs the
matching jars listed in `build.gradle`.

## License

This project is licensed under the MIT License. See `LICENSE`.
