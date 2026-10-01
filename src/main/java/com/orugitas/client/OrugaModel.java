package com.orugitas.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.orugitas.OrugaEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** Orugita: cabeza grande con antenitas y cuatro segmentos que ondulan al andar. */
public class OrugaModel extends EntityModel<OrugaEntity> {
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart antLeft;
	private final ModelPart antRight;
	private final ModelPart[] segs = new ModelPart[4];

	public OrugaModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.antLeft = head.getChild("ant_left");
		this.antRight = head.getChild("ant_right");
		for (int i = 0; i < 4; i++) {
			segs[i] = root.getChild("seg" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition head = root.addOrReplaceChild("head",
			CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5F, -2.5F, 5F, 5F, 5F),
			PartPose.offset(0F, 24F, -5.5F));
		head.addOrReplaceChild("ant_left",
			CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
			PartPose.offset(1.2F, -5F, -1F));
		head.addOrReplaceChild("ant_right",
			CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
			PartPose.offset(-1.2F, -5F, -1F));

		root.addOrReplaceChild("seg0",
			CubeListBuilder.create().texOffs(0, 10).addBox(-2F, -4F, -2F, 4F, 4F, 4F),
			PartPose.offset(0F, 24F, -1.5F));
		root.addOrReplaceChild("seg1",
			CubeListBuilder.create().texOffs(16, 10).addBox(-2F, -4F, -2F, 4F, 4F, 4F),
			PartPose.offset(0F, 24F, 2.5F));
		root.addOrReplaceChild("seg2",
			CubeListBuilder.create().texOffs(32, 10).addBox(-2F, -4F, -2F, 4F, 4F, 4F),
			PartPose.offset(0F, 24F, 6.5F));
		root.addOrReplaceChild("seg3",
			CubeListBuilder.create().texOffs(0, 18).addBox(-1.5F, -3F, -1.5F, 3F, 3F, 3F),
			PartPose.offset(0F, 24F, 9.5F));

		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(OrugaEntity e, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		boolean sit = e.isInSittingPose();
		float amp = sit ? 0F : limbSwingAmount;

		for (int i = 0; i < 4; i++) {
			float idle = Mth.sin(ageInTicks * 0.12F - i * 0.7F) * (sit ? 0.15F : 0.35F);
			float hump = Math.max(0F, Mth.sin(limbSwing * 1.4F - i * 1.1F)) * 1.8F * amp;
			segs[i].y = 24F + idle - hump;
		}

		float headIdle = Mth.sin(ageInTicks * 0.12F + 0.7F) * 0.3F;
		float headHump = Math.max(0F, Mth.sin(limbSwing * 1.4F + 1.1F)) * 1.5F * amp;
		head.y = 24F + headIdle - headHump;
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		head.xRot = headPitch * Mth.DEG_TO_RAD;
		if (e.eatAnim > 0) {
			// masticando: cabeza que sube y baja
			head.xRot = 0.35F + Mth.sin(ageInTicks * 1.4F) * 0.3F;
		}

		float wiggle = Mth.sin(ageInTicks * 0.2F) * 0.15F;
		antLeft.zRot = 0.2F + wiggle;
		antRight.zRot = -0.2F - wiggle;
	}

	@Override
	public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
		root.render(pose, buffer, light, overlay, r, g, b, a);
	}
}
