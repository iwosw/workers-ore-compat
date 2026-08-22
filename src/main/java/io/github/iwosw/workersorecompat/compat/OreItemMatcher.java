package io.github.iwosw.workersorecompat.compat;

import io.github.iwosw.workersorecompat.config.CompatConfigCache;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class OreItemMatcher {
    private static final Logger LOGGER = LogUtils.getLogger();
    // Written from the server thread, cleared from the config-watcher thread on reload.
    private static final Set<ResourceLocation> LOGGED_ITEMS = ConcurrentHashMap.newKeySet();

    private OreItemMatcher() {
    }

    public static MatchResult match(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return MatchResult.PASS;

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return MatchResult.PASS;

        // One read for the whole decision: a reload midway through must not mix rule generations.
        CompatConfigCache.Snapshot config = CompatConfigCache.get();

        // 1. Blacklist check
        if (config.excludedOreDrops().contains(id)) {
            logDebugOnce(config, id, "blacklisted in excludedOreDrops");
            return MatchResult.DENY;
        }

        // Rules are ordered by how specific they are: an entry naming this exact item outranks a
        // block-level rule, which in turn outranks a broad tag.

        // 2. Custom item ID check
        if (config.additionalOreDrops().contains(id)) {
            logDebugOnce(config, id, "matched additionalOreDrops ID");
            return MatchResult.ALLOW;
        }

        // 3. If this is the item form of a blacklisted block, this mod must not promote it at all.
        //    Checked before the tag rule below: a broad tag such as forge:ores contains the ore
        //    block items themselves, so it would otherwise promote the item before
        //    excludedOreBlocks is ever consulted. PASS rather than DENY, so Workers' own
        //    wantsToPickUp keeps the final say (including its MinerPickup config list).
        MatchResult blockMatch = matchBackingBlock(stack, config);
        if (blockMatch == MatchResult.DENY) {
            logDebugOnce(config, id, "not promoted; block is in excludedOreBlocks");
            return MatchResult.PASS;
        }

        // 4. Custom item tag check. Indexed loop rather than an enhanced for: Workers tests every
        // nearby item entity once per tick, so an iterator per call is garbage on a hot path.
        List<TagKey<Item>> dropTags = config.additionalOreDropTags();
        for (int i = 0; i < dropTags.size(); i++) {
            TagKey<Item> tag = dropTags.get(i);
            if (stack.is(tag)) {
                logDebugOnce(config, id, "matched additionalOreDropTag #" + tag.location());
                return MatchResult.ALLOW;
            }
        }

        // 5. BlockItem behavior: promote when the backing block is a configured ore. Driven purely
        //    by the block config, so removing forge:ores from additionalOreBlockTags actually
        //    takes effect here.
        if (blockMatch == MatchResult.ALLOW) {
            logDebugOnce(config, id, "matched as BlockItem of compatible ore");
            return MatchResult.ALLOW;
        }

        // 6. Fallback to Workers default logic
        logDebugOnce(config, id, "no custom match; falling back to Workers logic");
        return MatchResult.PASS;
    }

    private static MatchResult matchBackingBlock(ItemStack stack, CompatConfigCache.Snapshot config) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            // Quiet variant: this is an item lookup, so it must not log as a block hit.
            return OreBlockMatcher.matchQuiet(blockItem.getBlock().defaultBlockState(), config);
        }
        return MatchResult.PASS;
    }

    private static void logDebugOnce(CompatConfigCache.Snapshot config, ResourceLocation id, String reason) {
        if (config.debugLogging() && LOGGED_ITEMS.add(id)) {
            LOGGER.info("[Workers Ore Compat] Item {} -> {}", id, reason);
        }
    }

    public static void clearLoggedCache() {
        LOGGED_ITEMS.clear();
    }
}
