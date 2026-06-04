package cn.coostack.usefulmagic.entity.custom.dragon.display

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.display.AutoDisplayEntity
import cn.coostack.usefulmagic.renderer.UsefulMagicRenderTypes
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.*

@CooAutoRegister
class DragonCircleEyeDisplay(
    pos: Vec3 = Vec3.ZERO,
    world: Level? = null
) : AutoDisplayEntity(pos, world) {
    @CodecField
    var r: Double = 4.0

    @CodecField
    var rotateSpeed: Double = 0.08

    @CodecField
    var count: Int = 8

    @CodecField
    var eyeScaled: Float = 0.72f

    @CodecField
    var alpha: Float = 1f

    @CodecField
    var fadeInTick: Int = 10

    @CodecField
    var borderSize: Float = 0.12f

    @CodecField
    var borderColor: Vector3f = Vector3f(0.10f, 0.02f, 0.16f)

    @CodecField
    var direction: Vector3f = Vector3f(0f, 1f, 0f)

    @CodecField
    var heightOffset: Double = 0.0

    @CodecField
    var discarded: Boolean = false

    @CodecField
    var discardTick: Int = 0

    private var age = 0
    private var prevR = r
    private var prevEyeScaled = eyeScaled
    private var prevAlpha = alpha
    private var prevDirection = Vector3f(direction)
    private var prevAngle = 0.0
    private var angle = 0.0

    init {
        manageRotation = false
    }

    override fun tick() {
        prevR = r
        prevEyeScaled = eyeScaled
        prevAlpha = alpha
        prevDirection.set(direction)
        prevAngle = angle
        super.tick()
        angle = normalizeAngle(angle + rotateSpeed)
        age++
        if (discarded) {
            val progress = ((age - discardTick).toFloat() / DISCARD_FADE_TICKS).coerceIn(0f, 1f)
            alpha = 1f - smoothstep(progress)
            if (progress >= 1f) {
                remove()
            }
        }
    }

    override fun render(
        view: Matrix4f,
        proj: Matrix4f,
        modelMatrixStack: PoseStack,
        buffer: MultiBufferSource,
        delta: Float,
        camera: Camera,
    ) {
        val visibleAlpha = (interpolatedAlpha(delta) * fadeInProgress(delta)).coerceIn(0f, 1f)
        val eyeCount = count.coerceAtLeast(0)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA || eyeCount == 0) {
            return
        }

        val radius = lerp(delta, prevR, r)
        val size = lerp(delta, prevEyeScaled, eyeScaled).coerceAtLeast(0f)
        if (radius <= 0.0 || size <= 0f) {
            return
        }

        val orbitAngle = interpolateAngle(delta)
        val step = TAU / eyeCount.toDouble()
        val axis = interpolatedDirection(delta)
        val basis = orbitBasis(axis)
        val minecraft = Minecraft.getInstance()
        val level = world ?: minecraft.level
        val stack = ENDER_EYE_STACK
        val itemRenderer = minecraft.itemRenderer

        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            repeat(eyeCount) { index ->
                val current = orbitAngle + step * index
                val offset = orbitOffset(basis.first, basis.second, radius, current)
                modelMatrixStack.pushPose()
                modelMatrixStack.translate(
                    offset.x.toDouble(),
                    offset.y.toDouble() + heightOffset,
                    offset.z.toDouble(),
                )
                modelMatrixStack.mulPose(camera.rotation())

                val model = itemRenderer.getModel(stack, level, null, index)
                drawSprite(
                    modelMatrixStack,
                    buffer,
                    model,
                    size + borderSize.coerceAtLeast(0f) * 2f,
                    borderColor,
                    visibleAlpha,
                    -EYE_Z_OFFSET,
                    pureColorFromTextureAlpha = true,
                )
                drawSprite(modelMatrixStack, buffer, model, size, WHITE, visibleAlpha, EYE_Z_OFFSET)
                modelMatrixStack.popPose()
            }
        } finally {
            RenderSystem.depthMask(true)
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableBlend()
            RenderSystem.enableCull()
        }
    }

    fun discard() {
        if (discarded) {
            return
        }
        discarded = true
        discardTick = age
    }

    private fun drawSprite(
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        model: BakedModel,
        size: Float,
        color: Vector3f,
        visibleAlpha: Float,
        z: Float,
        pureColorFromTextureAlpha: Boolean = false,
    ) {
        val quadScale = size.coerceAtLeast(0f)
        if (quadScale <= 0f) {
            return
        }
        val sprite = model.particleIcon
        val renderType = if (pureColorFromTextureAlpha) {
            UsefulMagicRenderTypes.dragonCircleEyeBorder(sprite.atlasLocation())
        } else {
            RenderType.entityTranslucent(sprite.atlasLocation())
        }
        val consumer = buffer.getBuffer(renderType)
        val pose = poseStack.last().pose()
        val r = color.x.coerceIn(0f, 1f)
        val g = color.y.coerceIn(0f, 1f)
        val b = color.z.coerceIn(0f, 1f)
        val half = quadScale * 0.5f
        val light = LightTexture.pack(15, 15)

        consumer.addVertex(pose, -half, -half, z).setColor(r, g, b, visibleAlpha).setUv(sprite.u0, sprite.v1)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f)
        consumer.addVertex(pose, half, -half, z).setColor(r, g, b, visibleAlpha).setUv(sprite.u1, sprite.v1)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f)
        consumer.addVertex(pose, half, half, z).setColor(r, g, b, visibleAlpha).setUv(sprite.u1, sprite.v0)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f)
        consumer.addVertex(pose, -half, half, z).setColor(r, g, b, visibleAlpha).setUv(sprite.u0, sprite.v0)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f)
    }

    private fun interpolatedAlpha(delta: Float): Float {
        return lerp(delta, prevAlpha, alpha)
    }

    private fun fadeInProgress(delta: Float): Float {
        val duration = fadeInTick.coerceAtLeast(0)
        if (duration == 0) {
            return 1f
        }
        val progress = ((age - 1f + delta.coerceIn(0f, 1f)) / duration.toFloat()).coerceIn(0f, 1f)
        return smoothstep(progress)
    }

    private fun interpolateAngle(delta: Float): Double {
        val fixedDelta = normalizeAngle(angle - prevAngle)
        return prevAngle + fixedDelta * delta.coerceIn(0f, 1f)
    }

    private fun interpolatedDirection(delta: Float): Vector3f {
        val t = delta.coerceIn(0f, 1f)
        val current = Vector3f(prevDirection).lerp(direction, t)
        return normalizeOrDefault(current, DEFAULT_AXIS)
    }

    companion object {
        private const val DISCARD_FADE_TICKS = 12f
        private const val MIN_VISIBLE_ALPHA = 0.001f
        private const val EYE_Z_OFFSET = 0.01f
        private const val TAU = PI * 2.0
        private val ENDER_EYE_STACK = ItemStack(Items.ENDER_EYE)
        private val WHITE = Vector3f(1f, 1f, 1f)
        private val DEFAULT_AXIS = Vector3f(0f, 1f, 0f)
        private val FALLBACK_PERPENDICULAR = Vector3f(1f, 0f, 0f)

        private fun lerp(delta: Float, from: Double, to: Double): Double {
            val t = delta.coerceIn(0f, 1f).toDouble()
            return from + (to - from) * t
        }

        private fun lerp(delta: Float, from: Float, to: Float): Float {
            val t = delta.coerceIn(0f, 1f)
            return from + (to - from) * t
        }

        private fun normalizeAngle(value: Double): Double {
            var angle = value % TAU
            if (angle > PI) {
                angle -= TAU
            } else if (angle < -PI) {
                angle += TAU
            }
            return angle
        }

        private fun smoothstep(value: Float): Float {
            val x = value.coerceIn(0f, 1f)
            return x * x * (3f - 2f * x)
        }

        private fun orbitBasis(axis: Vector3f): Pair<Vector3f, Vector3f> {
            val firstSeed = if (abs(axis.y) < 0.98f) {
                DEFAULT_AXIS
            } else {
                FALLBACK_PERPENDICULAR
            }
            val first = Vector3f(firstSeed).cross(axis)
            normalizeOrDefault(first, FALLBACK_PERPENDICULAR)
            val second = Vector3f(axis).cross(first)
            normalizeOrDefault(second, DEFAULT_AXIS)
            return first to second
        }

        private fun orbitOffset(first: Vector3f, second: Vector3f, radius: Double, angle: Double): Vector3f {
            val x = cos(angle).toFloat()
            val y = sin(angle).toFloat()
            return Vector3f(first).mul(x).add(Vector3f(second).mul(y)).mul(radius.toFloat())
        }

        private fun normalizeOrDefault(vector: Vector3f, fallback: Vector3f): Vector3f {
            val length = sqrt(vector.x * vector.x + vector.y * vector.y + vector.z * vector.z)
            if (length <= 1.0e-4f || !length.isFinite()) {
                vector.set(fallback)
                return vector
            }
            vector.div(length)
            return vector
        }
    }
}
