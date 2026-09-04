package cn.coostack.usefulmagic.display.magic.useful

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.display.AutoDisplayEntity
import cn.coostack.cooparticlesapi.utils.MinecraftRendererUtil
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f

/**
 * 倒过来的孢子花
 */
@CooAutoRegister
class FlowerDisplay(pos: Vec3, world: Level?) : AutoDisplayEntity(pos, world) {
    init {
        manageRotation = false
    }

    @CodecField
    var rotateSpeed = 0f

    override fun render(
        view: Matrix4f,
        proj: Matrix4f,
        modelMatrixStack: PoseStack,
        buffer: MultiBufferSource,
        delta: Float,
        camera: Camera
    ) {

        val flowerState = Blocks.SPORE_BLOSSOM.defaultBlockState()
        val renderer = Minecraft.getInstance().blockRenderer
        val model = renderer.getBlockModel(flowerState)
        val cutout = buffer.getBuffer(RenderType.cutout())

        modelMatrixStack.pushPose()
        try {
            modelMatrixStack.scale(scale, scale, scale)
            MinecraftRendererUtil.applyRotation(modelMatrixStack, yaw, 180f, 0f)
            modelMatrixStack.translate(-MODEL_CENTER_XZ, -MODEL_CENTER_Y, -MODEL_CENTER_XZ)
            renderer.modelRenderer.renderModel(
                modelMatrixStack.last(),
                cutout,
                flowerState,
                model,
                1f,
                1f,
                1f,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY
            )
        } finally {
            modelMatrixStack.popPose()
        }
    }

    override fun tick() {
        super.tick()
        yaw += rotateSpeed
    }

    companion object {
        private const val MODEL_CENTER_XZ = 0.5
        private const val MODEL_CENTER_Y = 0.8
    }
}
