package cn.coostack.usefulmagic.display.skills

import cn.coostack.cooparticlesapi.animation.timeline.Eases
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.display.AutoDisplayEntity
import cn.coostack.cooparticlesapi.platform.CooParticlesServices
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Camera
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.roundToInt

@CooAutoRegister
class SkillRangeDisplay(pos: Vec3, world: Level?) : AutoDisplayEntity(pos, world) {
    companion object {
        private val TEXTURE = ResourceLocation.fromNamespaceAndPath(
            UsefulMagic.MOD_ID,
            "textures/gui/tracked.png"
        )
        private val MODEL_AXIS = Vec3(0.0, 0.0, 1.0)
        private const val BASE_HALF_SIZE = 0.5f
        private const val ENTRY_SPIN_DEGREES = 180f
        private const val EMISSIVE_SCALE_STEP = 0.01f
        private const val EMISSIVE_DEPTH_STEP = 0.0005f
    }

    @CodecField
    var scaled = 1f

    @CodecField
    var color = Vector3f(1f)

    @CodecField
    var axis = RelativeLocation(0, 1, 0)

    @CodecField
    var biggerTick = 30

    @CodecField
    var discardTick = 40

    // 持续时间
    @CodecField
    var lifetime = 20

    @CodecField
    var age = 0

    @CodecField
    var bright = 2f

    init {
        manageRotation = false
    }

    override fun render(
        view: Matrix4f,
        proj: Matrix4f,
        modelMatrixStack: PoseStack,
        buffer: MultiBufferSource,
        delta: Float,
        camera: Camera
    ) {
        val totalTick = totalTick()
        if (totalTick <= 0) {
            return
        }

        val renderAge = (age + delta).coerceAtMost(totalTick.toFloat())
        val currentScale = currentScale(renderAge)
        if (currentScale <= 0.0001f) {
            return
        }
        val axisVector = normalizedAxis()
        val solidConsumer = buffer.getBuffer(
            CooParticlesServices.PLATFORM.getRenderTypesProvider().entityCutoutEmissive(TEXTURE, bright)
        )

        modelMatrixStack.pushPose()
        modelMatrixStack.mulPose(createRotation(axisVector, currentRoll(renderAge)))
        modelMatrixStack.scale(currentScale, currentScale, currentScale)
        drawQuad(modelMatrixStack, solidConsumer, axisVector)
        modelMatrixStack.popPose()
    }

    override fun tick() {
        super.tick()
        age++
        if (age > totalTick()) {
            remove()
        }
    }

    private fun totalTick(): Int {
        return biggerTick.coerceAtLeast(0) + lifetime.coerceAtLeast(0) + discardTick.coerceAtLeast(0)
    }

    private fun normalizedAxis(): Vec3 {
        val axisVector = axis.toVector()
        return if (axisVector.lengthSqr() < 1.0E-6) {
            Vec3(0.0, 1.0, 0.0)
        } else {
            axisVector.normalize()
        }
    }

    private fun currentScale(renderAge: Float): Float {
        val bigger = biggerTick.coerceAtLeast(0)
        val hold = lifetime.coerceAtLeast(0)
        val discard = discardTick.coerceAtLeast(0)
        val target = scaled.coerceAtLeast(0f)
        val ageValue = renderAge.coerceAtLeast(0f)

        if (target <= 0f) {
            return 0f
        }

        if (bigger > 0 && ageValue < bigger) {
            val progress = (ageValue / bigger.toFloat()).coerceIn(0f, 1f)
            return target * Eases.outBack.cal(progress.toDouble()).toFloat()
        }

        val discardStart = bigger + hold
        if (discard > 0 && ageValue > discardStart) {
            val progress = ((ageValue - discardStart.toFloat()) / discard.toFloat()).coerceIn(0f, 1f)
            return target * (1f - Eases.inCubic.cal(progress.toDouble()).toFloat())
        }

        return target
    }

    private fun currentRoll(renderAge: Float): Float {
        val bigger = biggerTick.coerceAtLeast(0)
        if (bigger <= 0 || renderAge >= bigger) {
            return 0f
        }
        val progress = (renderAge / bigger.toFloat()).coerceIn(0f, 1f)
        val eased = Eases.outElastic.cal(progress.toDouble()).toFloat()
        return ENTRY_SPIN_DEGREES * (1f - eased)
    }

    private fun createRotation(targetAxis: Vec3, rollDegrees: Float): Quaternionf {
        val axis = Vector3f(targetAxis.x.toFloat(), targetAxis.y.toFloat(), targetAxis.z.toFloat()).normalize()
        val modelAxis = Vector3f(MODEL_AXIS.x.toFloat(), MODEL_AXIS.y.toFloat(), MODEL_AXIS.z.toFloat())
        val align = Quaternionf().rotationTo(modelAxis, axis)
        if (rollDegrees == 0f) {
            return align
        }

        val roll = Quaternionf().rotateAxis(Math.toRadians(rollDegrees.toDouble()).toFloat(), axis)
        return roll.mul(align)
    }

    private fun drawQuad(
        modelMatrixStack: PoseStack,
        consumer: VertexConsumer,
        normal: Vec3
    ) {
        val pose = modelMatrixStack.last()
        val r = (color.x.coerceIn(0f, 1f) * 255f).roundToInt()
        val g = (color.y.coerceIn(0f, 1f) * 255f).roundToInt()
        val b = (color.z.coerceIn(0f, 1f) * 255f).roundToInt()
        val normalX = normal.x.toFloat()
        val normalY = normal.y.toFloat()
        val normalZ = normal.z.toFloat()

        putVertex(consumer, pose, -BASE_HALF_SIZE, -BASE_HALF_SIZE, 0f, 0f, 1f, r, g, b, normalX, normalY, normalZ)
        putVertex(consumer, pose, BASE_HALF_SIZE, -BASE_HALF_SIZE, 0f, 1f, 1f, r, g, b, normalX, normalY, normalZ)
        putVertex(consumer, pose, BASE_HALF_SIZE, BASE_HALF_SIZE, 0f, 1f, 0f, r, g, b, normalX, normalY, normalZ)
        putVertex(consumer, pose, -BASE_HALF_SIZE, BASE_HALF_SIZE, 0f, 0f, 0f, r, g, b, normalX, normalY, normalZ)

        putVertex(consumer, pose, -BASE_HALF_SIZE, BASE_HALF_SIZE, 0f, 0f, 0f, r, g, b, -normalX, -normalY, -normalZ)
        putVertex(consumer, pose, BASE_HALF_SIZE, BASE_HALF_SIZE, 0f, 1f, 0f, r, g, b, -normalX, -normalY, -normalZ)
        putVertex(consumer, pose, BASE_HALF_SIZE, -BASE_HALF_SIZE, 0f, 1f, 1f, r, g, b, -normalX, -normalY, -normalZ)
        putVertex(consumer, pose, -BASE_HALF_SIZE, -BASE_HALF_SIZE, 0f, 0f, 1f, r, g, b, -normalX, -normalY, -normalZ)
    }

    private fun putVertex(
        consumer: VertexConsumer,
        pose: PoseStack.Pose,
        x: Float,
        y: Float,
        z: Float,
        u: Float,
        v: Float,
        r: Int,
        g: Int,
        b: Int,
        normalX: Float,
        normalY: Float,
        normalZ: Float
    ) {
        consumer.addVertex(pose, x, y, z)
            .setColor(r, g, b, 255)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(pose, normalX, normalY, normalZ)
    }
}
