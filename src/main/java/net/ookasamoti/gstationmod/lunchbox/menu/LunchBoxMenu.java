package net.ookasamoti.gstationmod.lunchbox.menu;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.ookasamoti.gstationmod.config.FoodOverrideConfig;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem.Size;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxUtil;

import java.util.ArrayList;
import java.util.List;

public class LunchBoxMenu extends AbstractContainerMenu {

    private final int BOX_START = 0;
    private final int BOX_COUNT;
    private final int INV_START;
    private final int INV_COUNT = 27;
    private final int HOT_START;
    private final int HOT_COUNT = 9;

    private final ItemStack boxRef;
    private final Container cont;

    private final Size boxSize;
    private final int boxCols;
    private final int boxRows;

    public LunchBoxMenu(int id, Inventory inv) {
        this(ModMenus.LUNCH_BOX.get(), id, inv, findHeldLunchBox(inv.player));
    }

    public LunchBoxMenu(int id, Inventory inv, ItemStack boxRef) {
        this(ModMenus.LUNCH_BOX.get(), id, inv, boxRef);
    }

    public LunchBoxMenu(MenuType<?> type, int id, Inventory inv, ItemStack boxRef) {
        super(type, id);
        this.boxRef = boxRef;

        if (boxRef.getItem() instanceof LunchBoxItem lb) {
            this.boxSize = lb.size();
        } else {
            this.boxSize = Size.T3_27;
        }

        int cols, rows;
        switch (this.boxSize) {
            case T1_5  -> { cols = 5; rows = 1; }
            case T2_9  -> { cols = 3; rows = 3; }
            case T3_27 -> { cols = 9; rows = 3; }
            case T4_54 -> { cols = 9; rows = 6; }
            default    -> { cols = 9; rows = 3; }
        }
        this.boxCols = cols;
        this.boxRows = rows;

        this.BOX_COUNT = cols * rows;
        this.INV_START = BOX_START + BOX_COUNT;
        this.HOT_START = INV_START + INV_COUNT;

        ItemContainerContents contents = LunchBoxUtil.getContents(boxRef);
        List<ItemStack> list = LunchBoxUtil.contentsAsList(contents);
        this.cont = new SimpleContainer(this.BOX_COUNT);
        for (int i = 0; i < this.BOX_COUNT; i++) {
            this.cont.setItem(i, (i < list.size() ? list.get(i) : ItemStack.EMPTY).copy());
        }

        switch (this.boxSize) {
            case T1_5 -> {
                int baseX = 44, baseY = 20;
                for (int c = 0; c < 5; c++) {
                    addSlot(new FoodSlot(this.cont, c, baseX + c * 18, baseY));
                }
            }
            case T2_9 -> {
                int baseX = 62, baseY = 17;
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        int idx = c + r * 3;
                        addSlot(new FoodSlot(this.cont, idx, baseX + c * 18, baseY + r * 18));
                    }
                }
            }
            case T3_27 -> {
                int baseX = 8, baseY = 18;
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 9; c++) {
                        int idx = c + r * 9;
                        addSlot(new FoodSlot(this.cont, idx, baseX + c * 18, baseY + r * 18));
                    }
                }
            }
            case T4_54 -> {
                int baseX = 8, baseY = 18;
                for (int r = 0; r < 6; r++) {
                    for (int c = 0; c < 9; c++) {
                        int idx = c + r * 9;
                        addSlot(new FoodSlot(this.cont, idx, baseX + c * 18, baseY + r * 18));
                    }
                }
            }
        }

        int invY;
        int hotY;
        switch (this.boxSize) {
            case T1_5  -> { invY = 51;  hotY = 109; }
            case T2_9  -> { invY = 84;  hotY = 142; }
            case T3_27 -> { invY = 84;  hotY = 142; }
            case T4_54 -> { invY = 140; hotY = 198; }
            default    -> { invY = 84;  hotY = 142; }
        }

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, invY + r * 18));
            }
        }

        for (int c = 0; c < 9; c++) {
            addSlot(new Slot(inv, c, 8 + c * 18, hotY));
        }
    }

    private static ItemStack findHeldLunchBox(Player p) {
        if (p == null) return ItemStack.EMPTY;
        var main = p.getMainHandItem();
        if (main.getItem() instanceof LunchBoxItem) return main;
        var off = p.getOffhandItem();
        if (off.getItem() instanceof LunchBoxItem) return off;
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player == null || player.isRemoved()) return false;
        return player.getMainHandItem().getItem() instanceof LunchBoxItem
                || player.getOffhandItem().getItem() instanceof LunchBoxItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        if (index < 0 || index >= this.slots.size()) return empty;

        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return empty;

        ItemStack src = slot.getItem();
        ItemStack copy = src.copy();

        if (index >= BOX_START && index < BOX_START + BOX_COUNT) {
            if (!moveItemStackTo(src, INV_START, INV_START + INV_COUNT, false)) {
                if (!moveItemStackTo(src, HOT_START, HOT_START + HOT_COUNT, false)) return empty;
            }
        } else {
            if (!moveItemStackTo(src, BOX_START, BOX_START + BOX_COUNT, false)) return empty;
        }

        if (src.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide()) return;

        List<ItemStack> list = new ArrayList<>(this.BOX_COUNT);
        for (int i = 0; i < this.BOX_COUNT; i++) list.add(this.cont.getItem(i).copy());
        ItemContainerContents newCont = ItemContainerContents.fromItems(list);
        LunchBoxUtil.setContents(this.boxRef, newCont);

        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            sp.getInventory().setChanged();
        }
    }

    private static final class FoodSlot extends Slot {
        public FoodSlot(Container cont, int index, int x, int y) {
            super(cont, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return false;
            if (stack.get(DataComponents.FOOD) == null) return false;
            if (FoodOverrideConfig.reliefOnlyFor(stack.getItem())) return false;
            return true;
        }
    }

    public Size getBoxSize() { return boxSize; }
    public int  getBoxCols() { return boxCols; }
    public int  getBoxRows() { return boxRows; }
    public int  getBoxCount(){ return BOX_COUNT; }
}
