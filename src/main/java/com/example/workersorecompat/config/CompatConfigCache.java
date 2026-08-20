package com.example.workersorecompat.config;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.slf4j.Logger;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class CompatConfigCache {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Every config value in one immutable object. Readers take the whole thing in a single volatile
     * read, so a reload on the config-watcher thread can never show the server thread new block
     * rules combined with old item rules.
     */
    public record Snapshot(
            Set<ResourceLocation> additionalOreBlocks,
            Set<ResourceLocation> excludedOreBlocks,
            List<TagKey<Block>> additionalOreBlockTags,
            Set<ResourceLocation> additionalOreDrops,
            Set<ResourceLocation> excludedOreDrops,
            List<TagKey<Item>> additionalOreDropTags,
            boolean debugLogging
    ) {
        static final Snapshot EMPTY =
                new Snapshot(Set.of(), Set.of(), List.of(), Set.of(), Set.of(), List.of(), false);
    }

    private static volatile Snapshot snapshot = Snapshot.EMPTY;

    // Tag lookups report a false miss until datapack tags are bound, so validation has to wait.
    private static volatile boolean tagsAvailable = false;

    private CompatConfigCache() {
    }

    public static Snapshot get() {
        return snapshot;
    }

    public static synchronized void rebuild() {
        Snapshot next = new Snapshot(
                parseResourceLocations(WorkersOreCompatConfig.ADDITIONAL_ORE_BLOCKS.get(), "additionalOreBlocks"),
                parseResourceLocations(WorkersOreCompatConfig.EXCLUDED_ORE_BLOCKS.get(), "excludedOreBlocks"),
                parseTags(WorkersOreCompatConfig.ADDITIONAL_ORE_BLOCK_TAGS.get(), "additionalOreBlockTags", BlockTags::create),
                parseResourceLocations(WorkersOreCompatConfig.ADDITIONAL_ORE_DROPS.get(), "additionalOreDrops"),
                parseResourceLocations(WorkersOreCompatConfig.EXCLUDED_ORE_DROPS.get(), "excludedOreDrops"),
                parseTags(WorkersOreCompatConfig.ADDITIONAL_ORE_DROP_TAGS.get(), "additionalOreDropTags", ItemTags::create),
                WorkersOreCompatConfig.DEBUG_LOGGING.get()
        );
        snapshot = next;

        if (next.debugLogging()) {
            LOGGER.info("[Workers Ore Compat] Config cache rebuilt: {} additional blocks, {} excluded blocks, {} block tags, {} additional drops, {} excluded drops, {} drop tags.",
                    next.additionalOreBlocks().size(), next.excludedOreBlocks().size(), next.additionalOreBlockTags().size(),
                    next.additionalOreDrops().size(), next.excludedOreDrops().size(), next.additionalOreDropTags().size());
        }

        validateTags();
    }

    private static Set<ResourceLocation> parseResourceLocations(List<? extends String> rawList, String configKey) {
        Set<ResourceLocation> result = new LinkedHashSet<>();
        for (String raw : rawList) {
            if (raw == null || raw.isBlank()) continue;
            ResourceLocation loc = ResourceLocation.tryParse(raw.trim());
            if (loc == null) {
                LOGGER.warn("[Workers Ore Compat] Invalid resource location in config '{}': \"{}\"", configKey, raw);
            } else {
                result.add(loc);
            }
        }
        return Set.copyOf(result);
    }

    /**
     * Strips the leading '#' users are used to writing for tags in datapacks; '#' is not a legal
     * ResourceLocation character, so without this "#forge:ores" would be dropped with a warning.
     */
    private static String stripTagPrefix(String raw) {
        String trimmed = raw.trim();
        return trimmed.startsWith("#") ? trimmed.substring(1).trim() : trimmed;
    }

    private static <T> List<TagKey<T>> parseTags(List<? extends String> rawList, String configKey,
                                                 Function<ResourceLocation, TagKey<T>> factory) {
        // LinkedHashSet: duplicate entries would otherwise be re-tested against every block scanned.
        Set<ResourceLocation> locations = new LinkedHashSet<>();
        for (String raw : rawList) {
            if (raw == null || raw.isBlank()) continue;
            ResourceLocation loc = ResourceLocation.tryParse(stripTagPrefix(raw));
            if (loc == null) {
                LOGGER.warn("[Workers Ore Compat] Invalid tag in config '{}': \"{}\"", configKey, raw);
            } else {
                locations.add(loc);
            }
        }
        return locations.stream().map(factory).toList();
    }

    /**
     * Called once datapack tags are bound, and again on every /reload, because tag contents change
     * without any config event firing.
     */
    public static void onTagsBound() {
        tagsAvailable = true;
        validateTags();
    }

    /**
     * A tag name that no datapack defines parses perfectly well and then silently matches nothing,
     * which looks exactly like the mod not working. Report those instead.
     */
    public static void validateTags() {
        if (!tagsAvailable) return;

        Snapshot current = snapshot;
        warnUnusableTags(ForgeRegistries.BLOCKS.tags(), current.additionalOreBlockTags(), "additionalOreBlockTags");
        warnUnusableTags(ForgeRegistries.ITEMS.tags(), current.additionalOreDropTags(), "additionalOreDropTags");
    }

    private static <T> void warnUnusableTags(ITagManager<T> manager, List<TagKey<T>> configured, String configKey) {
        if (manager == null) return;

        for (TagKey<T> tag : configured) {
            if (!manager.isKnownTagName(tag)) {
                LOGGER.warn("[Workers Ore Compat] Tag #{} in config '{}' is not defined by any mod or datapack; it will never match. Check for a typo.",
                        tag.location(), configKey);
            } else if (manager.getTag(tag).isEmpty()) {
                LOGGER.warn("[Workers Ore Compat] Tag #{} in config '{}' is defined but empty; it will never match.",
                        tag.location(), configKey);
            }
        }
    }
}
