package com.orugitas.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.orugitas.OrugaEntity;
import com.orugitas.Orugitas;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class OrugaRenderer extends MobRenderer<OrugaEntity, OrugaModel> {
	private static final ResourceLocation[] TEXTURES = {
		new ResourceLocation(Orugitas.MOD_ID, "textures/entity/oruga_0.png"),
		new ResourceLocation(Orugitas.MOD_ID, "textures/entity/oruga_1.png"),
		new ResourceLocation(Orugitas.MOD_ID, "textures/entity/oruga_2.png")
	};

	public OrugaRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new OrugaModel(ctx.bakeLayer(ClientSetup.LAYER)), 0.3F);
	}

	@Override
	protected void scale(OrugaEntity entity, PoseStack pose, float partialTick) {
		float s = entity.isPassenger() ? 0.45F : 0.75F; // mas pequenita en el hombro
		pose.scale(s, s, s);
	}

	@Override
	public ResourceLocation getTextureLocation(OrugaEntity entity) {
		return TEXTURES[Math.max(0, Math.min(2, entity.getVariant()))];
	}
}
