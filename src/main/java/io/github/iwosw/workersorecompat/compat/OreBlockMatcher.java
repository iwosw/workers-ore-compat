package io.github.iwosw.workersorecompat.compat;

import io.github.iwosw.workersorecompat.config.CompatConfigCache;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class OreBlockMatcher {
    private static final Logger LOGGER = LogUtils.getLogger();
    // Written from the server thread, cleared from the config-watcher thread on reload.
    private static final Set<ResourceLocation> LOGGED_BLOCKS = ConcurrentHashMap.newKeySet();

    private OreBlockMatcher() {
    }

    public static MatchResult match(BlockState state) {
        return match(state, CompatConfigCache.get(), true);
    }

    /**
     * Same rules as {@link #match(BlockState)}, but without writing to the block debug-log cache,
     * and against a caller-supplied config snapshot. Used when an item lookup consults the block
     * config, so that item hits are neither reported as block hits nor allowed to consume the
     * block's log-once slot, and so that both halves of the lookup see one config generation.
     */
    static MatchResult matchQuiet(BlockState state, CompatConfigCache.Snapshot config) {
        return match(state, config, false);
    }

    private static MatchResult match(BlockState state, CompatConfigCache.Snapshot config, boolean log) {
        if (state == null) return MatchResult.PASS;

        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (id == null) return MatchResult.PASS;

        // 1. Blacklist check
        if (config.excludedOreBlocks().contains(id)) {
            if (log) logDebugOnce(config, id, "blacklisted in excludedOreBlocks");
            return MatchResult.DENY;
        }

        // 2. Custom block ID check
        if (config.additionalOreBlocks().contains(id)) {
            if (log) logDebugOnce(config, id, "matched additionalOreBlocks ID");
            return MatchResult.ALLOW;
        }

        // 3. Custom block tag check. Indexed loop rather than an enhanced for: this runs for every
        // block a miner scans, and an iterator per call is garbage the scan does not need.
        List<TagKey<Block>> blockTags = config.additionalOreBlockTags();
        for (int i = 0; i < blockTags.size(); i++) {
            TagKey<Block> tag = blockTags.get(i);
            if (state.is(tag)) {
                if (log) logDebugOnce(config, id, "matched additionalOreBlockTag #" + tag.location());
                return MatchResult.ALLOW;
            }
        }

        // 4. Fallback to Workers default logic
        return MatchResult.PASS;
    }

    private static void logDebugOnce(CompatConfigCache.Snapshot config, ResourceLocation id, String reason) {
        if (config.debugLogging() && LOGGED_BLOCKS.add(id)) {
            LOGGER.info("[Workers Ore Compat] Block {} -> {}", id, reason);
        }
    }

    public static void clearLoggedCache() {
        LOGGED_BLOCKS.clear();
    }
}
