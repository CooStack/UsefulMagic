package cn.coostack.usefulmagic.entity.custom.renderer

import cn.coostack.cooparticlesapi.platform.CooParticlesServices
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicHeartEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3f
import kotlin.math.sin

class MagicHeartEntityRenderer(context: EntityRendererProvider.Context) : EntityRenderer<MagicHeartEntity>(context) {
    companion object {
        private val HEART_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/item/heart_of_the_sea.png"
        )
        private val TRACKED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            UsefulMagic.MOD_ID,
            "textures/gui/tracked.png"
        )

        private const val VISUAL_SCALE = 2.1f
        private const val BILLBOARD_TRACK_HALF_SIZE = 0.78f
        private const val INNER_SHELL_HALF_SIZE = 1.02f
        private const val CALM_SPIN_SPEED = 1.35f
        private const val HURT_SPIN_BONUS = 3.15f
        private const val INNER_SHELL_ORBIT_SPEED = 0.48f
        private const val INNER_SHELL_HURT_BONUS = 0.36f
        private const val INNER_SHELL_DRIFT_SPEED = 0.12f
    }

    init {
        shadowRadius = 0f
    }

    override fun render(
        entity: MagicHeartEntity,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        val age = entity.tickCount + partialTick
        val bob = sin(age * 0.16f) * 0.12f
        val heartSpin = age * (CALM_SPIN_SPEED + HURT_SPIN_BONUS)
        val billboardSpin = heartSpin * 0.72f
        val innerOrbit = age * (INNER_SHELL_ORBIT_SPEED + INNER_SHELL_HURT_BONUS)
        val innerDrift = age * INNER_SHELL_DRIFT_SPEED
        val overlay = LivingEntityRenderer.getOverlayCoords(entity, 0f)

        poseStack.pushPose()
        poseStack.translate(0.0, (entity.bbHeight * 0.52f + bob).toDouble(), 0.0)
        poseStack.scale(VISUAL_SCALE, VISUAL_SCALE, VISUAL_SCALE)

        renderShell(
            poseStack,
            buffer,
            overlay,
            INNER_SHELL_HALF_SIZE,
            innerOrbit,
            64f,
            innerDrift,
            255,
            -0.014f,
            bright = 2f
        )
        renderBillboardLayer(
            poseStack,
            buffer,
            TRACKED_TEXTURE,
            BILLBOARD_TRACK_HALF_SIZE,
            billboardSpin,
            overlay,
            255,
            0.012f,
            Math3DUtil.colorOf(72, 220, 255),
            2f
        )
        renderBillboardLayer(
            poseStack,
            buffer,
            HEART_TEXTURE,
            0.6f,
            0f,
            overlay,
            255,
            0.026f, bright = 1.5f
        )
        poseStack.popPose()

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight)
    }

    override fun getTextureLocation(entity: MagicHeartEntity): ResourceLocation {
        return HEART_TEXTURE
    }

    private fun renderBillboardLayer(
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        texture: ResourceLocation,
        halfSize: Float,
        rollDegrees: Float,
        overlay: Int,
        alpha: Int,
        depthOffset: Float,
        color: Vector3f = Vector3f(1f),
        bright: Float = 1f
    ) {
        poseStack.pushPose()
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation())
        poseStack.mulPose(Axis.ZP.rotationDegrees(rollDegrees))
        drawQuadLayer(poseStack, buffer, texture, halfSize, overlay, alpha, depthOffset, color, bright)
        poseStack.popPose()
    }

    private fun renderShell(
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        overlay: Int,
        halfSize: Float,
        primaryRotation: Float,
        tiltDegrees: Float,
        secondaryRotation: Float,
        alpha: Int,
        depthOffset: Float,
        color: Vector3f = Vector3f(1f),
        bright: Float = 1f
    ) {
        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotationDegrees(primaryRotation))
        poseStack.mulPose(Axis.XP.rotationDegrees(tiltDegrees))
        poseStack.mulPose(Axis.ZP.rotationDegrees(secondaryRotation))
        drawQuadLayer(poseStack, buffer, TRACKED_TEXTURE, halfSize, overlay, alpha, depthOffset, color, bright)
        poseStack.popPose()
    }

    private fun drawQuadLayer(
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        texture: ResourceLocation,
        halfSize: Float,
        overlay: Int,
        alpha: Int,
        depthOffset: Float,
        color: Vector3f = Vector3f(1f),
        bright: Float = 1f
    ) {
        val solid = buffer.getBuffer(
            CooParticlesServices.PLATFORM.getRenderTypesProvider().entityCutoutEmissive(texture, bright)
        )

        poseStack.pushPose()
        poseStack.translate(0.0, 0.0, depthOffset.toDouble())
        drawQuad(poseStack, solid, halfSize, overlay, alpha, color)
        poseStack.popPose()
    }

    private fun drawQuad(
        poseStack: PoseStack,
        consumer: VertexConsumer,
        halfSize: Float,
        overlay: Int,
        alpha: Int,
        color: Vector3f = Vector3f(1f),
    ) {
        val pose = poseStack.last()
        putVertex(consumer, pose, -halfSize, -halfSize, 0f, 0f, 1f, overlay, alpha, 0f, 0f, 1f, color)
        putVertex(consumer, pose, halfSize, -halfSize, 0f, 1f, 1f, overlay, alpha, 0f, 0f, 1f, color)
        putVertex(consumer, pose, halfSize, halfSize, 0f, 1f, 0f, overlay, alpha, 0f, 0f, 1f, color)
        putVertex(consumer, pose, -halfSize, halfSize, 0f, 0f, 0f, overlay, alpha, 0f, 0f, 1f, color)

        putVertex(consumer, pose, -halfSize, halfSize, 0f, 0f, 0f, overlay, alpha, 0f, 0f, 1f, color)
        putVertex(consumer, pose, halfSize, halfSize, 0f, 1f, 0f, overlay, alpha, 0f, 0f, 1f, color)
        putVertex(consumer, pose, halfSize, -halfSize, 0f, 1f, 1f, overlay, alpha, 0f, 0f, 1f, color)
        putVertex(consumer, pose, -halfSize, -halfSize, 0f, 0f, 1f, overlay, alpha, 0f, 0f, 1f, color)
    }

    private fun putVertex(
        consumer: VertexConsumer,
        pose: PoseStack.Pose,
        x: Float,
        y: Float,
        z: Float,
        u: Float,
        v: Float,
        overlay: Int,
        alpha: Int,
        normalX: Float,
        normalY: Float,
        normalZ: Float,
        color: Vector3f = Vector3f(1f),
    ) {
        consumer.addVertex(pose, x, y, z)
            .setColor((color.x * 255).toInt(), (color.y * 255).toInt(), (color.z * 255).toInt(), alpha)
            .setOverlay(overlay)
            .setUv(u, v)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(pose, normalX, normalY, normalZ)
    }
}
