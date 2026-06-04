package cn.coostack.usefulmagic.display

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.display.AutoDisplayEntity
import cn.coostack.cooparticlesapi.utils.MinecraftRendererUtil
import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Camera
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.roundToInt

@CooAutoRegister
class CylinderBillboardLineDisplay() : AutoDisplayEntity(Vec3.ZERO, null) {
    constructor(pos: Vec3, world: Level?) : this() {
        this.pos = pos
        this.world = world
    }

    @CodecField
    var direction: Vec3 = Vec3(0.0, 1.0, 0.0)

    @CodecField
    var width: Float = 0.08f

    @CodecField
    var length: Float = 1.0f

    @CodecField
    var color: Vector3f = Vector3f(1f, 1f, 1f)

    @CodecField
    var alpha: Float = 1.0f

    @CodecField
    var age = 0

    init {
        manageRotation = false
    }

    override fun render(
        view: Matrix4f,
        proj: Matrix4f,
        modelMatrixStack: PoseStack,
        buffer: MultiBufferSource,
        delta: Float,
        camera: Camera,
    ) {
        val alpha = currentAlpha()
        val lineLength = length.coerceAtLeast(MIN_SIZE)
        val lineWidth = width.coerceAtLeast(MIN_SIZE)
        if (alpha <= MIN_VISIBLE_ALPHA || lineLength <= MIN_SIZE || lineWidth <= MIN_SIZE) {
            return
        }

        val lineDirection = normalizedDirection()
        val basis = MinecraftRendererUtil.axialBillboardBasis(lineDirection, camera, pos)
        val consumer = buffer.getBuffer(
            RenderType.entityTranslucentCull(TEXTURE),
        )
        val halfLength = lineLength.toDouble() * 0.5
        val halfWidth = lineWidth.toDouble() * 0.5
        val top = basis.axis.scale(halfLength)
        val side = basis.right.scale(halfWidth)

        RenderSystem.disableCull()
        try {
            addBoard(
                consumer = consumer,
                stack = modelMatrixStack,
                p1 = top.scale(-1.0).subtract(side),
                p2 = top.scale(-1.0).add(side),
                p3 = top.add(side),
                p4 = top.subtract(side),
                normal = basis.face,
            )
        } finally {
            RenderSystem.enableCull()
        }
    }

    fun configure(
        pos: Vec3 = this.pos,
        direction: Vec3 = this.direction,
        width: Float = this.width,
        length: Float = this.length,
        color: Vector3f = this.color,
        alpha: Float = this.alpha,
    ): CylinderBillboardLineDisplay {
        this.pos = pos
        this.direction = direction
        this.width = width.coerceAtLeast(MIN_SIZE)
        this.length = length.coerceAtLeast(MIN_SIZE)
        this.color = Vector3f(color)
        this.alpha = alpha.coerceIn(0f, 1f)
        return this
    }

    fun setDirection(direction: Vec3): CylinderBillboardLineDisplay {
        this.direction = direction
        return this
    }

    fun setDimensions(width: Float = this.width, length: Float = this.length): CylinderBillboardLineDisplay {
        this.width = width.coerceAtLeast(MIN_SIZE)
        this.length = length.coerceAtLeast(MIN_SIZE)
        return this
    }

    fun setColor(color: Vector3f): CylinderBillboardLineDisplay {
        this.color = Vector3f(color.x, color.y, color.z)
        return this
    }

    fun setAlpha(alpha: Float): CylinderBillboardLineDisplay {
        this.alpha = alpha.coerceIn(0f, 1f)
        return this
    }

    override fun tick() {
        super.tick()
        age++
    }


    private fun normalizedDirection(): Vec3 {
        return if (direction.lengthSqr() <= MIN_DIRECTION_LENGTH_SQR) {
            DEFAULT_DIRECTION
        } else {
            direction.normalize()
        }
    }

    private fun currentAlpha(): Float {
        return alpha.coerceIn(0f, 1f)
    }


    private fun addBoard(
        consumer: VertexConsumer,
        stack: PoseStack,
        p1: Vec3,
        p2: Vec3,
        p3: Vec3,
        p4: Vec3,
        normal: Vec3,
    ) {
        addVertex(consumer, stack, p1, normal, 0f, 1f)
        addVertex(consumer, stack, p2, normal, 1f, 1f)
        addVertex(consumer, stack, p3, normal, 1f, 0f)
        addVertex(consumer, stack, p4, normal, 0f, 0f)

        addVertex(consumer, stack, p4, normal.scale(-1.0), 0f, 0f)
        addVertex(consumer, stack, p3, normal.scale(-1.0), 1f, 0f)
        addVertex(consumer, stack, p2, normal.scale(-1.0), 1f, 1f)
        addVertex(consumer, stack, p1, normal.scale(-1.0), 0f, 1f)
    }

    private fun addVertex(
        consumer: VertexConsumer,
        stack: PoseStack,
        point: Vec3,
        normal: Vec3,
        u: Float,
        v: Float,
    ) {
        consumer.addVertex(stack.last(), point.x.toFloat(), point.y.toFloat(), point.z.toFloat())
            .setColor(
                colorChannel(color.x),
                colorChannel(color.y),
                colorChannel(color.z),
                colorChannel(currentAlpha())
            )
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(stack.last(), normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat())
    }

    companion object {
        private const val MIN_SIZE = 0.001f
        private const val MIN_VISIBLE_ALPHA = 0.001f
        private const val MIN_DIRECTION_LENGTH_SQR = 1.0E-6
        private const val MIN_TARGET_DISTANCE = 0.05
        private const val TARGET_FADE_DISTANCE = 1.0
        private const val COLLECT_FADE_TICKS = 5
        private val DEFAULT_DIRECTION = Vec3(0.0, 1.0, 0.0)
        private val TEXTURE = ResourceLocation.fromNamespaceAndPath(
            UsefulMagic.MOD_ID,
            "textures/effect/color/white.png",
        )

        private fun colorChannel(value: Float): Int {
            return (value.coerceIn(0f, 1f) * 255f).roundToInt()
        }
    }
}
