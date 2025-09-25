package net.ookasamoti.gstationmod.lunchbox.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT, modid = "gstationmod")
public final class ClientModels {

    public static final ModelResourceLocation MRL_FALLBACK_T1 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "item/lunch_box_t1_fallback"), "standalone");
    public static final ModelResourceLocation MRL_FALLBACK_T2 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "item/lunch_box_t2_fallback"), "standalone");
    public static final ModelResourceLocation MRL_FALLBACK_T3 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "item/lunch_box_t3_fallback"), "standalone");
    public static final ModelResourceLocation MRL_FALLBACK_T4 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "item/lunch_box_t4_fallback"), "standalone");

    private static final ModelResourceLocation MRL_INV_T1 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "lunch_box_tier1"), "inventory");
    private static final ModelResourceLocation MRL_INV_T2 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "lunch_box_tier2"), "inventory");
    private static final ModelResourceLocation MRL_INV_T3 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "lunch_box_tier3"), "inventory");
    private static final ModelResourceLocation MRL_INV_T4 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "lunch_box_tier4"), "inventory");

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional e) {

        e.register(MRL_FALLBACK_T1);
        e.register(MRL_FALLBACK_T2);
        e.register(MRL_FALLBACK_T3);
        e.register(MRL_FALLBACK_T4);
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult e) {

        Map<ModelResourceLocation, BakedModel> models = e.getModels();
        wrapIfPresent(models, MRL_INV_T1);
        wrapIfPresent(models, MRL_INV_T2);
        wrapIfPresent(models, MRL_INV_T3);
        wrapIfPresent(models, MRL_INV_T4);
    }

    private static void wrapIfPresent(Map<ModelResourceLocation, BakedModel> models, ModelResourceLocation key) {
        BakedModel orig = models.get(key);
        if (orig != null) {

            models.put(key, new LunchBoxParticleModel(orig));
        }
    }

    private ClientModels() {}
}
