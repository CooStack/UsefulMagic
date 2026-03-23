package cn.coostack.usefulmagic.display.skills

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.display.AutoDisplayEntity
import cn.coostack.usefulmagic.extend.lerpAsProgress
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
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.max
import kotlin.math.pow

/**
 * 采用方块渲染
 * 从下(position)到上(direction)
 * 向上刺的时间和收回的时间一样 tick
 * 停留时间为 lifetime
 * 刺的材质为blockMaterial
 * 底座大小为baseSize（单位方块）
 * 顶部大小为topSize（单位方块）
 * 刺的最大高度为height （单位方块）
 * 然后它是棱锥的形式 （地面为正方形的方块）
 * 用多个从下到上大小递减的方块堆叠进行模拟 (count为堆叠个数)
 * @constructor
 * @param pos
 * @param world
 */
@CooAutoRegister
class ThornDisplay(pos: Vec3, world: Level?) : AutoDisplayEntity(pos, world) {
    companion object {
        private const val MIN_SHRINK_PROGRESS = 0.04f
    }

    private var age = 0

    @CodecField
    var direction = Vec3(0.0, 1.0, 0.0)

    @CodecField
    var thrustTick = 10

    @CodecField
    var lifetime = 10

    @CodecField
    var blockMaterial = Blocks.IRON_BLOCK.defaultBlockState()

    @CodecField
    var baseSize = 1.0

    @CodecField
    var topSize = 0.0

    @CodecField
    var heapCount = 4

    @CodecField
    var height = 2.0

    override fun render(
        view: Matrix4f,
        proj: Matrix4f,
        modelMatrixStack: PoseStack,
        buffer: MultiBufferSource,
        delta: Float,
        camera: Camera
    ) {
        if (height <= 0.0 || baseSize <= 0.0 || topSize < 0.0) {
            return
        }

        val currentAge = age + delta
        val thrustDuration = max(1, thrustTick)
        val totalAge = totalAge(thrustDuration)
        if (currentAge >= totalAge) {
            remove()
            return
        }

        val growProgress = when {
            currentAge <= thrustDuration -> easeOutCubic(currentAge / thrustDuration)
            currentAge <= thrustDuration + lifetime -> 1f
            else -> shrinkProgress(currentAge, thrustDuration)
        }.coerceIn(0f, 1f)
        if (growProgress <= 0f) {
            return
        }

        val renderDirection = Vector3f(direction.x.toFloat(), direction.y.toFloat(), direction.z.toFloat())
        if (renderDirection.lengthSquared() <= 0.0001f) {
            return
        }
        renderDirection.normalize()

        val count = max(1, heapCount)
        val visibleHeight = height.toFloat() * growProgress
        val segmentHeight = height.toFloat() / count
        val renderer = Minecraft.getInstance().blockRenderer
        val model = Minecraft.getInstance().modelManager.blockModelShaper.getBlockModel(blockMaterial)
        val vertex = buffer.getBuffer(RenderType.cutout())

        modelMatrixStack.pushPose()
        modelMatrixStack.mulPose(Quaternionf().rotationTo(Vector3f(0f, 1f, 0f), renderDirection))
        for (index in 0 until count) {
            val segmentProgress = ((visibleHeight / segmentHeight) - index).coerceIn(0f, 1f)
            if (segmentProgress <= 0f) {
                break
            }

            val sizeProgress = if (count == 1) 0.0 else index.toDouble() / (count - 1)
            val size = sizeProgress.lerpAsProgress(baseSize, topSize).toFloat()
            val segmentBottom = index * segmentHeight
            val renderHeight = segmentProgress.lerpAsProgress(0.01, segmentHeight).toFloat()
            modelMatrixStack.pushPose()
            modelMatrixStack.translate((-size / 2f).toDouble(), segmentBottom.toDouble(), (-size / 2f).toDouble())
            modelMatrixStack.scale(size, renderHeight, size)
            renderer.modelRenderer.renderModel(
                modelMatrixStack.last(),
                vertex,
                blockMaterial,
                model,
                1f, 1f, 1f,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY
            )
            modelMatrixStack.popPose()
        }
        modelMatrixStack.popPose()
    }

    override fun tick() {
        super.tick()
        age++
        if (age >= totalAge(max(1, thrustTick))) {
            remove()
        }
    }

    private fun easeOutCubic(progress: Float): Float {
        return 1f - (1f - progress.coerceIn(0f, 1f)).pow(3)
    }

    private fun shrinkProgress(currentAge: Float, thrustDuration: Int): Float {
        val shrinkAge = currentAge - thrustDuration - max(0, lifetime)
        val remaining = 1f - (shrinkAge / thrustDuration)
        if (remaining <= 0f) {
            return 0f
        }
        return max(MIN_SHRINK_PROGRESS, smoothStep(remaining))
    }

    private fun totalAge(thrustDuration: Int): Int {
        return thrustDuration * 2 + max(0, lifetime)
    }

    private fun smoothStep(progress: Float): Float {
        val value = progress.coerceIn(0f, 1f)
        return value * value * (3f - 2f * value)
    }
}
