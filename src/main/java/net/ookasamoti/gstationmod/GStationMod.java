package net.ookasamoti.gstationmod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.ookasamoti.gstationmod.command.FoodOverrideDebugCommands;
import net.ookasamoti.gstationmod.command.LunchboxCommands;
import net.ookasamoti.gstationmod.command.NutritionCommands;
import net.ookasamoti.gstationmod.common.ClientViolationReporter;
import net.ookasamoti.gstationmod.common.FoodDefaultApplier;
import net.ookasamoti.gstationmod.common.NameCarryOverHandler;
import net.ookasamoti.gstationmod.lunchbox.client.ClientKeyHandler;
import net.ookasamoti.gstationmod.lunchbox.client.ClientKeybinds;
import net.ookasamoti.gstationmod.lunchbox.client.ClientOpenHandler;
import net.ookasamoti.gstationmod.lunchbox.client.ClientWheelHandler;
import net.ookasamoti.gstationmod.lunchbox.menu.ClientScreens;
import net.ookasamoti.gstationmod.lunchbox.net.LunchboxTierSync;
import net.ookasamoti.gstationmod.lunchbox.net.NetLunchBox;
import net.ookasamoti.gstationmod.net.MovementNet;
import net.ookasamoti.gstationmod.net.SatietySync;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import net.neoforged.fml.loading.FMLPaths;

import net.ookasamoti.gstationmod.event.NutritionEvents;


@Mod(GStationMod.MODID)
public class GStationMod {
    public static final String MODID = "gstationmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GStationMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        // ---- Config ----
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, net.ookasamoti.gstationmod.config.ServerConfig.SPEC);
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, net.ookasamoti.gstationmod.config.ClientConfig.SPEC);
        java.nio.file.Path gameDir = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
        net.ookasamoti.gstationmod.config.FoodOverrideConfig.load(gameDir);

        net.ookasamoti.gstationmod.tan.TanTagPackBuilder.rebuild(FMLPaths.GAMEDIR.get());
        modEventBus.addListener(net.ookasamoti.gstationmod.tan.TanTagPackRegistrar::onAddPackFinders);

        // ---- Gameplay events ----
        NeoForge.EVENT_BUS.addListener(NutritionEvents::onRightClick);
        NeoForge.EVENT_BUS.addListener(NutritionEvents::onFinish);
        NeoForge.EVENT_BUS.addListener(NutritionEvents::onTooltip);
        NeoForge.EVENT_BUS.addListener(NutritionEvents::onLogin);

        // ---- Commands ----
        NeoForge.EVENT_BUS.addListener(NutritionCommands::register);
        NeoForge.EVENT_BUS.addListener(LunchboxCommands::register);
        NeoForge.EVENT_BUS.addListener(FoodOverrideDebugCommands::register);


        // ---- Networking----
        modEventBus.addListener(NetLunchBox::register);
        modEventBus.addListener(SatietySync::register);
        modEventBus.addListener(LunchboxTierSync::register);
        modEventBus.addListener(MovementNet::register);

        // ---- Common ----
        modEventBus.addListener(FoodDefaultApplier::onModifyDefaults);
        NeoForge.EVENT_BUS.addListener(ClientViolationReporter::onClientTick);
        NeoForge.EVENT_BUS.register(NameCarryOverHandler.class);

        // ---- Registries ----
        net.ookasamoti.gstationmod.registry.ModItems.register(modEventBus);
        net.ookasamoti.gstationmod.lunchbox.menu.ModMenus.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ClientScreens::onRegisterScreens);
            modEventBus.addListener(ClientKeybinds::onRegisterKeyMappings);
            NeoForge.EVENT_BUS.register(ClientKeyHandler.class);
            NeoForge.EVENT_BUS.addListener(ClientWheelHandler::onScroll);
            NeoForge.EVENT_BUS.addListener(ClientOpenHandler::onMouse);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {}

    private void addCreative(BuildCreativeModeTabContentsEvent event) {}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
