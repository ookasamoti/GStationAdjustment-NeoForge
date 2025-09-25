package net.ookasamoti.gstationmod.lunchbox.menu;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxUtil;

public final class ClientScreens {
    public static void onRegisterScreens(RegisterMenuScreensEvent e) {
        e.register(ModMenus.LUNCH_BOX.get(), net.ookasamoti.gstationmod.lunchbox.menu.LunchBoxScreen::new);
    }
    private ClientScreens(){}

    @EventBusSubscriber(value = Dist.CLIENT, modid = "gstationmod")
    public static final class LunchBoxEatingVisuals {
        private static final String NBT_ROOT = "gstation_lunch";
        private static final String NBT_PARTICLE_BLANK = "particle_blank";

        private LunchBoxEatingVisuals(){}
        @SubscribeEvent
        public static void onUseStart(LivingEntityUseItemEvent.Start e) {
            if (!e.getEntity().level().isClientSide()) return;
            if (!(e.getEntity() instanceof Player)) return;
            ItemStack stack = e.getItem();
            if (!(stack.getItem() instanceof LunchBoxItem)) return;

            ItemStack sel = LunchBoxUtil.getSelectedFood(stack);
            if (sel.isEmpty()) return;

            setParticleBlankFlag(stack, true);
        }

        @SubscribeEvent
        public static void onUseStop(LivingEntityUseItemEvent.Stop e) {
            if (!e.getEntity().level().isClientSide()) return;
            clearFlagIfLunchBox(e.getItem());
        }
        @SubscribeEvent
        public static void onUseFinish(LivingEntityUseItemEvent.Finish e) {
            if (!e.getEntity().level().isClientSide()) return;
            clearFlagIfLunchBox(e.getItem());
        }

        private static void clearFlagIfLunchBox(ItemStack stack) {
            if (!(stack.getItem() instanceof LunchBoxItem)) return;
            setParticleBlankFlag(stack, false);
        }

        private static final double MOUTH_FORWARD = 0.60;
        private static final double MOUTH_SIDE    = 0.18;
        private static final double MOUTH_DOWN    = 0.10;

        @SubscribeEvent
        public static void onUseTick(LivingEntityUseItemEvent.Tick e) {
            if (!e.getEntity().level().isClientSide()) return;
            if (!(e.getEntity() instanceof Player p)) return;

            ItemStack box = e.getItem();
            if (!(box.getItem() instanceof LunchBoxItem)) return;

            ItemStack sel = LunchBoxUtil.getSelectedFood(box);
            if (sel.isEmpty()) return;

            if (p.getRandom().nextInt(4) != 0) return;

            var mc = net.minecraft.client.Minecraft.getInstance();
            var level = mc.level;
            if (level == null) return;

            var look = p.getViewVector(1.0f).normalize(); // 前
            var up   = new net.minecraft.world.phys.Vec3(0, 1, 0);
            var rightRaw = look.cross(up);
            var right = rightRaw.lengthSqr() < 1.0e-6 ? new net.minecraft.world.phys.Vec3(1, 0, 0) : rightRaw.normalize();

            var hand = p.getUsedItemHand();
            boolean mainArmRight = (p.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT);

            boolean rightSide = (hand == net.minecraft.world.InteractionHand.MAIN_HAND) ? mainArmRight : !mainArmRight;
            double sideSign = rightSide ? 1.0 : -1.0;

            var eye = p.getEyePosition(1.0f);
            var pos = eye
                    .add(look.scale(MOUTH_FORWARD))
                    .add(0, -MOUTH_DOWN, 0)
                    .add(right.scale(MOUTH_SIDE * sideSign));

            var r = p.getRandom();
            double spread = 0.06;
            double vx = look.x * 0.08 + (r.nextDouble() - 0.5) * spread;
            double vy = look.y * 0.02 + 0.03 + (r.nextDouble() - 0.5) * spread;
            double vz = look.z * 0.08 + (r.nextDouble() - 0.5) * spread;

            level.addParticle(
                    new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM, sel),
                    pos.x, pos.y, pos.z, vx, vy, vz
            );
        }

        private static void setParticleBlankFlag(ItemStack stack, boolean on) {
            CompoundTag cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            CompoundTag root = cd.getCompound(NBT_ROOT).copy();
            root.putBoolean(NBT_PARTICLE_BLANK, on);
            cd.put(NBT_ROOT, root);
            stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(cd));
        }
    }
}
