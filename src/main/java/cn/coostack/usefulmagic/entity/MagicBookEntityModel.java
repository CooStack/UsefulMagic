// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package cn.coostack.usefulmagic.entity;

import cn.coostack.usefulmagic.entity.animation.MagicBookEntityAnimation;
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class MagicBookEntityModel extends SinglePartEntityModel<MagicBookEntity> {
    private final ModelPart rootModel;
    private final ModelPart book_left;
    private final ModelPart left_arm;
    private final ModelPart left;
    private final ModelPart body;
    private final ModelPart book_right;
    private final ModelPart right_arm;
    private final ModelPart right;

    public MagicBookEntityModel(ModelPart root) {
        this.rootModel = root.getChild("root");
        this.book_left = rootModel.getChild("book_left");
        this.left_arm = book_left.getChild("left_arm");
        this.left = book_left.getChild("left");
        this.body = rootModel.getChild("body");
        this.book_right = rootModel.getChild("book_right");
        this.right_arm = book_right.getChild("right_arm");
        this.right = book_right.getChild("right");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData root = modelPartData.addChild("root", ModelPartBuilder.create(), ModelTransform.of(-1.0F, 22.0F, 2.0F, 0.2618F, 0.0F, 0.0F));

        ModelPartData book_left = root.addChild("book_left", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData left_arm = book_left.addChild("left_arm", ModelPartBuilder.create(), ModelTransform.pivot(0.0449F, -1.4064F, -0.0518F));

        ModelPartData left_r1 = left_arm.addChild("left_r1", ModelPartBuilder.create().uv(53, 51).cuboid(-0.5999F, 1.5795F, -6.0F, 9.0F, 0.1F, 12.0F, new Dilation(0.0F)), ModelTransform.of(-0.2022F, -1.7332F, 0.0F, 0.0F, 0.0F, -0.4189F));

        ModelPartData left = book_left.addChild("left", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -1.0F, 0.0F));

        ModelPartData left_pi_r1 = left.addChild("left_pi_r1", ModelPartBuilder.create().uv(0, 37).cuboid(-6.0F, 0.5F, -8.0F, 11.0F, 0.1F, 16.0F, new Dilation(0.0F)), ModelTransform.of(6.2F, -2.5F, 0.0F, 0.0F, 0.0F, -0.4363F));

        ModelPartData left_r2 = left.addChild("left_r2", ModelPartBuilder.create().uv(0, 17).cuboid(-0.6058F, 1.555F, -6.0F, 9.0F, 1.0F, 12.0F, new Dilation(0.0F)), ModelTransform.of(-0.1573F, -2.0896F, -0.0518F, 0.0F, 0.0F, -0.4189F));

        ModelPartData body = root.addChild("body", ModelPartBuilder.create().uv(48, 13).cuboid(-2.0F, -2.5F, -6.0F, 2.0F, 0.1F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(1.0F, 2.0F, -2.0F));

        ModelPartData book_right = root.addChild("book_right", ModelPartBuilder.create(), ModelTransform.pivot(-0.9F, -0.6F, -2.0F));

        ModelPartData right_arm = book_right.addChild("right_arm", ModelPartBuilder.create(), ModelTransform.pivot(0.8708F, -0.8192F, 0.8F));

        ModelPartData right_r1 = right_arm.addChild("right_r1", ModelPartBuilder.create().uv(53, 36).cuboid(-8.9734F, -0.0593F, -5.4306F, 9.0F, 0.1F, 12.0F, new Dilation(0.0F)), ModelTransform.of(0.0292F, 0.0124F, 0.5788F, 0.0F, 0.0F, 0.4189F));

        ModelPartData right = book_right.addChild("right", ModelPartBuilder.create(), ModelTransform.of(-0.1F, 0.2645F, 2.0F, 0.0F, 0.0F, 0.0F));

        ModelPartData right_r2 = right.addChild("right_r2", ModelPartBuilder.create().uv(0, 1).cuboid(-8.9734F, -0.0593F, -5.4306F, 9.0F, 1.0F, 12.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, -1.0462F, -0.6212F, 0.0F, 0.0F, 0.4189F));

        ModelPartData right_pi_r1 = right.addChild("right_pi_r1", ModelPartBuilder.create().uv(0, 59).cuboid(-5.0F, 0.5F, -8.0F, 11.0F, 0.1F, 16.0F, new Dilation(0.0F)), ModelTransform.of(-5.2F, -3.1645F, 0.0F, 0.0F, 0.0F, 0.4363F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        matrices.translate(0d, -4.5d, -0.2d);
        matrices.scale(4f, 4f, 4f);
        rootModel.render(matrices, vertexConsumer, light, overlay, color);
    }

    private void setHead(float yaw, float pitch) {
        yaw = MathHelper.clamp(yaw, -30f, 30f);
        pitch = MathHelper.clamp(pitch, -25f, 45f);
        this.rootModel.pitch = pitch * 0.017f;
        this.rootModel.yaw = yaw * 0.017f;
    }

    @Override
    public ModelPart getPart() {
        return rootModel;
    }

    @Override
    public void setAngles(MagicBookEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.setHead(headYaw, headPitch);
        this.updateAnimation(entity.getAttackAnimateState(),
                MagicBookEntityAnimation.ATTACKING, animationProgress, 1f);
    }
}