package net.ookasamoti.gstationmod.lunchbox.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ookasamoti.gstationmod.GStationMod;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, GStationMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<LunchBoxMenu>> LUNCH_BOX =
            MENUS.register("lunch_box",
                    () -> new MenuType<>(LunchBoxMenu::new, FeatureFlags.VANILLA_SET));

    public static void register(IEventBus modBus) { MENUS.register(modBus); }
    private ModMenus() {}
}

