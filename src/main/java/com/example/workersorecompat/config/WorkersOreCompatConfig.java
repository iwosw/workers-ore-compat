package com.example.workersorecompat.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class WorkersOreCompatConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_ORE_BLOCKS = BUILDER
            .comment("Blocks counted as ore when a miner scans surrounding walls for ores (e.g. \"weirdmod:uranium_ore\").",
                    "Workers only consults this during wall scanning; blocks inside the mining area are broken either way.")
            .defineListAllowEmpty("additionalOreBlocks", List.of(
                    "immersiveengineering:ore_aluminum", "immersiveengineering:ore_lead", "immersiveengineering:ore_nickel",
                    "immersiveengineering:ore_uranium", "immersiveengineering:ore_silver", "immersiveengineering:ore_apatite",
                    "mekanism:osmium_ore", "mekanism:tin_ore", "mekanism:lead_ore", "mekanism:uranium_ore", "mekanism:fluorite_ore",
                    "geolosys:hematite_ore", "geolosys:cassiterite_ore", "geolosys:bauxite_ore", "geolosys:autunite_ore", "geolosys:platinum_ore",
                    "create:zinc_ore", "ae2:quartz_cluster", "powah:uraninite_ore", "bigreactors:yellorite_ore",
                    "forbidden_arcanus:arcane_crystal_ore",
                    "undergarden:depthrock_cloggrum_ore", "undergarden:shiverstone_cloggrum_ore",
                    "mysticalagriculture:prosperity_ore", "mysticalagriculture:deepslate_prosperity_ore",
                    "ad_astra:moon_desh_ore"
            ), WorkersOreCompatConfig::validateEntry);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_ORE_BLOCK_TAGS = BUILDER
            .comment("Block tags counted as ore during wall scanning (e.g. \"forge:ores\" or \"#forge:ores\").",
                    "Also decides which block items get promoted for pickup; dropping forge:ores here disables that promotion.")
            .defineListAllowEmpty("additionalOreBlockTags", List.of(
                    "forge:ores",
                    "ad_astra:desh_ores", "ad_astra:ostrum_ores", "ad_astra:calorite_ores", "ad_astra:ice_shard_ores"
            ), WorkersOreCompatConfig::validateEntry);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_ORE_DROPS = BUILDER
            .comment("Item drops that miners should pick up (e.g. \"weirdmod:uranium_chunk\")")
            .defineListAllowEmpty("additionalOreDrops", List.of(
                    "immersiveengineering:raw_aluminum", "immersiveengineering:raw_lead", "immersiveengineering:raw_nickel",
                    "immersiveengineering:raw_uranium", "immersiveengineering:raw_silver",
                    "mekanism:raw_osmium", "mekanism:raw_tin", "mekanism:raw_lead", "mekanism:raw_uranium", "mekanism:fluorite_gem",
                    "geolosys:iron_cluster", "geolosys:copper_cluster", "geolosys:gold_cluster", "geolosys:aluminum_cluster",
                    "geolosys:tin_cluster", "geolosys:lead_cluster", "geolosys:nickel_cluster", "geolosys:silver_cluster",
                    "geolosys:uranium_cluster", "geolosys:zinc_cluster", "geolosys:platinum_cluster", "geolosys:nether_gold_cluster",
                    "geolosys:ancient_debris_cluster", "geolosys:anthracite_coal", "geolosys:bituminous_coal", "geolosys:lignite_coal",
                    "create:raw_zinc", "ae2:certus_quartz_crystal", "powah:uraninite_raw",
                    "bigreactors:raw_yellorium", "bigreactors:anglesite_crystal", "bigreactors:benitoite_crystal",
                    "forbidden_arcanus:arcane_crystal", "forbidden_arcanus:xpetrified_orb", "forbidden_arcanus:clibano_core",
                    "undergarden:raw_cloggrum", "undergarden:raw_froststeel", "undergarden:regalium_crystal", "undergarden:utherium_crystal",
                    "mysticalagriculture:prosperity_shard", "mysticalagriculture:inferium_essence", "mysticalagriculture:soulium_dust",
                    "ad_astra:raw_desh", "ad_astra:raw_ostrum", "ad_astra:raw_calorite", "ad_astra:ice_shard"
            ), WorkersOreCompatConfig::validateEntry);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_ORE_DROP_TAGS = BUILDER
            .comment("Item tags whose contents miners should pick up (e.g. \"forge:raw_materials\").",
                    "An entry naming an exact item in additionalOreDrops takes precedence over these.")
            .defineListAllowEmpty("additionalOreDropTags", List.of(
                    "forge:raw_materials", "forge:ores"
            ), WorkersOreCompatConfig::validateEntry);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_ORE_BLOCKS = BUILDER
            .comment("Never count these blocks as ore during wall scanning, and never promote their item form",
                    "for pickup (e.g. \"mod:decorative_ore_bricks\"). Miners still break them inside the mining area,",
                    "and Workers' own pickup rules still apply to the item; use excludedOreDrops to hard-deny pickup.")
            .defineListAllowEmpty("excludedOreBlocks", List.of(), WorkersOreCompatConfig::validateEntry);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_ORE_DROPS = BUILDER
            .comment("Never pick these items up as ore drops")
            .defineListAllowEmpty("excludedOreDrops", List.of(), WorkersOreCompatConfig::validateEntry);

    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Enable debug logging when miners match custom ore blocks or drops")
            .define("debugLogging", false);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private static boolean validateEntry(final Object obj) {
        return obj instanceof String;
    }
}
