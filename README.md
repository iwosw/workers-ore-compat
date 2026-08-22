# Workers Ore Compat

**Villager Workers** miners only recognise ores that carry the `forge:ores` tag. Every ore that misses that tag — crystals, clusters, essences, planet ores — is invisible to them, and the drops get left on the ground.

This mod fixes that with a config file. No datapack, no scripting, no jar edits.

## What it changes

Two things, and nothing else:

- **Wall scanning.** When a miner checks the walls of its mining area for ore worth digging out, it now also counts the blocks and block tags you list.
- **Pickup.** The miner now picks up the items and item tags you list.

Both lists work in reverse too: anything in the exclusion lists is never treated as ore and never picked up.

## Requirements

- Minecraft 1.20.1
- Forge 47.x
- [Villager Workers](https://www.curseforge.com/minecraft/mc-mods/workers-mod) 2.0.3 or newer (which needs Villager Recruits)

Install on both client and server.

## Configuration

Generated on first launch at:
```
config/workers_ore_compat-common.toml
```

Seven options:

| Option                   | Effect                                          |
| ------------------------ | ----------------------------------------------- |
| `additionalOreBlocks`    | Block IDs counted as ore during wall scanning   |
| `additionalOreBlockTags` | Block tags counted as ore                       |
| `additionalOreDrops`     | Item IDs the miner picks up                     |
| `additionalOreDropTags`  | Item tags the miner picks up                    |
| `excludedOreBlocks`      | Never counted as ore                            |
| `excludedOreDrops`       | Never picked up                                 |
| `debugLogging`           | Log every custom match once, for pack debugging |

Example:
```toml
additionalOreBlocks = ["weirdmod:uranium_ore", "weirdmod:deepslate_uranium_ore"]
additionalOreDrops = ["weirdmod:raw_uranium"]
additionalOreDropTags = ["forge:raw_materials/uranium"]
excludedOreBlocks = ["weirdmod:decorative_ore_bricks"]
```

**Edits apply immediately.** Save the file and the cache rebuilds — no restart, no `/reload`. Tag contents are re-read on `/reload` as well, so a datapack change is picked up too.

An ID for a mod you do not have is skipped silently, so one config can cover a whole pack. A tag that nothing defines is reported in the log instead of failing quietly.

## Covered out of the box

The default config already lists the ores and drops of:

Immersive Engineering · Mekanism · Create · Applied Energistics 2 · Geolosys · Powah · Extreme Reactors · Forbidden & Arcanus · The Undergarden · Mystical Agriculture · Ad Astra

None of them are required. Add your own IDs for anything else — press F3 while looking at a block to read its ID, or F3+H to put item IDs on tooltips.

## Notes

- Only **wall** scanning is affected. Blocks inside the marked mining area are broken either way; this mod decides what is worth digging *out of the walls*.
- Removing `forge:ores` from `additionalOreBlockTags` turns off the automatic promotion of ore block items for pickup, if you want tighter control.
- An ID naming an exact item outranks a broad tag, and exclusions outrank everything.

## Tips

**Ready-made configs.** Two are kept in the repository, drop either one in as `config/workers_ore_compat-common.toml`:

- [base.toml](https://github.com/iwosw/workers-ore-compat/blob/main/configs/base.toml) — the stock defaults, for a normal pack
- [many-mods.toml](https://github.com/iwosw/workers-ore-compat/blob/main/configs/many-mods.toml) — a wide list for kitchen-sink packs, adding Thermal, AllTheModium, Silent Gear, Draconic Evolution, Occultism, Aether, Galosphere, RFTools and more

**Writing a config for your own pack.** Handing your mod list to an AI assistant and asking it for the block and item IDs is a fast way to get a first draft — but treat that draft as a guess. Language models invent plausible-looking IDs, and a wrong ID is **skipped silently**, so the miner simply ignores the ore with nothing in the log to explain why.

Always verify: set `debugLogging = true`, look at the block in game with F3 open to read its real ID, and use F3+H to show item IDs on tooltips. Fix the entries that never show up in the log.

## Building

Use Java 17 and run:

```text
gradlew build
```

The release jar is written to `build/libs/`.

The local `libs/` directory contains development-only third-party mod jars and is not part of this repository. A full development environment needs the matching jars listed in `build.gradle`.

## License

MIT.
