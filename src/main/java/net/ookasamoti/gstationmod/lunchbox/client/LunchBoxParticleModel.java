// src/main/java/net/ookasamoti/gstationmod/lunchbox/client/LunchBoxParticleModel.java
package net.ookasamoti.gstationmod.lunchbox.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxUtil;

public final class LunchBoxParticleModel implements BakedModel {
    private final BakedModel delegate;

    public LunchBoxParticleModel(BakedModel delegate) {
        this.delegate = delegate;
    }

    private final ItemOverrides overrides = new ItemOverrides() {
        @Override
        public BakedModel resolve(BakedModel originalModel,
                                  ItemStack stack,
                                  @Nullable ClientLevel level,
                                  @Nullable LivingEntity entity,
                                  int seed) {
            if (!(stack.getItem() instanceof LunchBoxItem)) return originalModel;
            ItemStack sel = LunchBoxUtil.getSelectedFood(stack);
            if (sel.isEmpty()) return originalModel;
            return Minecraft.getInstance().getItemRenderer().getModel(sel, level, entity, seed);
        }
    };

    @Override public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return delegate.getQuads(state, side, rand);
    }
    @Override public boolean useAmbientOcclusion() { return delegate.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return delegate.isGui3d(); }
    @Override public boolean usesBlockLight() { return delegate.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return delegate.isCustomRenderer(); }
    @Override public TextureAtlasSprite getParticleIcon() { return delegate.getParticleIcon(); }
    @Override public ItemOverrides getOverrides() { return overrides; }
    @Override public ItemTransforms getTransforms() { return delegate.getTransforms(); }
}
