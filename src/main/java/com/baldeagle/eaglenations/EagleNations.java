package com.baldeagle.eaglenations;

import com.baldeagle.eaglenations.ftb.FTBTeamBridge;
import com.baldeagle.eaglenations.nation.NationManager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

@Mod(EagleNations.MODID)
public class EagleNations {
    public static final String MODID = "eaglenations";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static NationManager nationManager;
    private static FTBTeamBridge ftbTeamBridge;
    private static MinecraftServer server;

    public EagleNations(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Eagle Nations common setup");

        if (!Config.ENABLED.get()) {
            LOGGER.info("Eagle Nations is disabled in config");
            return;
        }

        LOGGER.info("Eagle Nations enabled");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        com.baldeagle.eaglenations.commands.NationCommands.register(event.getDispatcher());
        LOGGER.info("Registered commands");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        LOGGER.info("Initializing Eagle Nations server systems");

        nationManager = new NationManager(server);
        nationManager.load();
        LOGGER.info("Loaded {} nations", nationManager.getAllNations().size());

        ftbTeamBridge = new FTBTeamBridge(nationManager, server);
        FTBTeamBridge.register(NeoForge.EVENT_BUS, nationManager, server);

        LOGGER.info("Eagle Nations server systems initialized");
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        if (nationManager != null) {
            LOGGER.info("Saving nation data");
            nationManager.save();
            nationManager = null;
        }
        server = null;
    }

    public static NationManager getNationManager() {
        return nationManager;
    }

    public static FTBTeamBridge getFTBTeamBridge() {
        return ftbTeamBridge;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static boolean isEnabled() {
        return Config.ENABLED.get();
    }
}