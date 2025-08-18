// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package cn.coostack.usefulmagic.entity;

import cn.coostack.usefulmagic.entity.animation.MagicBookEntityAnimation;
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class MagicBookEntityModel extends HierarchicalModel<MagicBookEntity> {
    private final ModelPart root;
    private final ModelPart book_left;
    private final ModelPart left_arm;
    private final ModelPart left;
    private final ModelPart body;
    private final ModelPart book_right;
    private final ModelPart right_arm;
    private final ModelPart right;

    public MagicBookEntityModel(ModelPart root) {
        this.root = root.getChild("root");
        this.book_left = this.root.getChild("book_left");
        this.left_arm = this.book_left.getChild("left_arm");
        this.left = this.book_left.getChild("left");
        this.body = this.root.getChild("body");
        this.book_right = this.root.getChild("book_right");
        this.right_arm = this.book_right.getChild("right_arm");
        this.right = this.book_right.getChild("right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 22.0F, 2.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition book_left = root.addOrReplaceChild("book_left", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm = book_left.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(0.0449F, -1.4064F, -0.0518F));

        PartDefinition left_r1 = left_arm.addOrReplaceChild("left_r1", CubeListBuilder.create().texOffs(53, 51).addBox(-0.5999F, 1.5795F, -6.0F, 9.0F, 0.1F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2022F, -1.7332F, 0.0F, 0.0F, 0.0F, -0.4189F));

        PartDefinition left = book_left.addOrReplaceChild("left", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition left_pi_r1 = left.addOrReplaceChild("left_pi_r1", CubeListBuilder.create().texOffs(0, 37).addBox(-6.0F, 0.5F, -8.0F, 11.0F, 0.1F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2F, -2.5F, 0.0F, 0.0F, 0.0F, -0.4363F));

        PartDefinition left_r2 = left.addOrReplaceChild("left_r2", CubeListBuilder.create().texOffs(0, 17).addBox(-0.6058F, 1.555F, -6.0F, 9.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1573F, -2.0896F, -0.0518F, 0.0F, 0.0F, -0.4189F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(48, 13).addBox(-2.0F, -2.5F, -6.0F, 2.0F, 0.1F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 2.0F, -2.0F));

        PartDefinition book_right = root.addOrReplaceChild("book_right", CubeListBuilder.create(), PartPose.offset(-0.9F, -0.6F, -2.0F));

        PartDefinition right_arm = book_right.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(0.8708F, -0.8192F, 0.8F));

        PartDefinition right_r1 = right_arm.addOrReplaceChild("right_r1", CubeListBuilder.create().texOffs(53, 36).addBox(-8.9734F, -0.0593F, -5.4306F, 9.0F, 0.1F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0292F, 0.0124F, 0.5788F, 0.0F, 0.0F, 0.4189F));

        PartDefinition right = book_right.addOrReplaceChild("right", CubeListBuilder.create(), PartPose.offset(-0.1F, 0.2645F, 2.0F));

        PartDefinition right_r2 = right.addOrReplaceChild("right_r2", CubeListBuilder.create().texOffs(0, 1).addBox(-8.9734F, -0.0593F, -5.4306F, 9.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -1.0462F, -0.6212F, 0.0F, 0.0F, 0.4189F));

        PartDefinition right_pi_r1 = right.addOrReplaceChild("right_pi_r1", CubeListBuilder.create().texOffs(0, 59).addBox(-5.0F, 0.5F, -8.0F, 11.0F, 0.1F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.2F, -3.1645F, 0.0F, 0.0F, 0.0F, 0.4363F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(@NotNull MagicBookEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        setHead(netHeadYaw, headPitch);
        this.animate(
                entity.getAttackAnimateState(),
                MagicBookEntityAnimation.ATTACKING, ageInTicks, 1f
        );

    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgba) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgba);
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }

    //}
    private void setHead(float yaw, float pitch) {
        yaw = Mth.clamp(yaw, -30f, 30f);
        pitch = Mth.clamp(pitch, -25f, 45f);
        this.root.xRot = pitch * 0.017f;
        this.root.yRot = yaw * 0.017f;
    }
}