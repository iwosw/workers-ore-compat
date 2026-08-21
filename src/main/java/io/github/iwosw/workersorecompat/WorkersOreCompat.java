package io.github.iwosw.workersorecompat;

import io.github.iwosw.workersorecompat.compat.OreBlockMatcher;
import io.github.iwosw.workersorecompat.compat.OreItemMatcher;
import io.github.iwosw.workersorecompat.config.CompatConfigCache;
import io.github.iwosw.workersorecompat.config.WorkersOreCompatConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(WorkersOreCompat.MODID)
public class WorkersOreCompat {
    public static final String MODID = "workers_ore_compat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public WorkersOreCompat() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoading);
        modEventBus.addListener(this::onConfigReloading);

        // Tag contents come from datapacks, not from our config, so a /reload changes what the
        // matchers see without any ModConfigEvent ever firing.
        MinecraftForge.EVENT_BUS.addListener(this::onTagsUpdated);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WorkersOreCompatConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[Workers Ore Compat] Initialized successfully.");
    }

    private void onConfigLoading(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == WorkersOreCompatConfig.SPEC) {
            reloadCaches();
            LOGGER.info("[Workers Ore Compat] Configuration loaded and cache built.");
        }
    }

    private void onConfigReloading(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == WorkersOreCompatConfig.SPEC) {
            reloadCaches();
            LOGGER.info("[Workers Ore Compat] Configuration reloaded and cache rebuilt.");
        }
    }

    private void onTagsUpdated(final TagsUpdatedEvent event) {
        // Cached "why did this match" decisions are only valid for one set of tag contents.
        OreBlockMatcher.clearLoggedCache();
        OreItemMatcher.clearLoggedCache();
        CompatConfigCache.onTagsBound();
    }

    private void reloadCaches() {
        CompatConfigCache.rebuild();
        OreBlockMatcher.clearLoggedCache();
        OreItemMatcher.clearLoggedCache();
    }
}
