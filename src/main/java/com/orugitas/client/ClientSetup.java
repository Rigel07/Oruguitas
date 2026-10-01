package com.orugitas.client;

import com.orugitas.Orugitas;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Orugitas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(Orugitas.MOD_ID, "oruga"), "main");

	@SubscribeEvent
	public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(LAYER, OrugaModel::createBodyLayer);
	}

	@SubscribeEvent
	public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(Orugitas.ORUGA.get(), OrugaRenderer::new);
	}
}
