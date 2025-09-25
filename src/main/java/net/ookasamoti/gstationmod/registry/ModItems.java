package net.ookasamoti.gstationmod.registry;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ookasamoti.gstationmod.GStationMod;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GStationMod.MODID);

    public static final DeferredItem<Item> LUNCH_BOX_T1 = ITEMS.register("lunch_box_tier1",
            () -> new LunchBoxItem(LunchBoxItem.Size.T1_5, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LUNCH_BOX_T2 = ITEMS.register("lunch_box_tier2",
            () -> new LunchBoxItem(LunchBoxItem.Size.T2_9, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LUNCH_BOX_T3 = ITEMS.register("lunch_box_tier3",
            () -> new LunchBoxItem(LunchBoxItem.Size.T3_27, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LUNCH_BOX_T4 = ITEMS.register("lunch_box_tier4",
            () -> new LunchBoxItem(LunchBoxItem.Size.T4_54, new Item.Properties().stacksTo(1)));

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);

        modBus.addListener((BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
                e.accept(LUNCH_BOX_T1);
                e.accept(LUNCH_BOX_T2);
                e.accept(LUNCH_BOX_T3);
                e.accept(LUNCH_BOX_T4);
            }
        });
    }

    private ModItems() {}
}
