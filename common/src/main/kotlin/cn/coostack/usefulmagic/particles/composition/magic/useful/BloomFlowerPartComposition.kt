package cn.coostack.usefulmagic.particles.composition.magic.useful

import cn.coostack.cooparticlesapi.animation.timeline.Eases
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin

@CooAutoRegister
class BloomFlowerPartComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    // 这里要记录原有的位置 k - v 记录原先的delta角度
    private val particleRadian = HashMap<UUID, Float>()

    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        return PointsBuilder()
            .addCircle(1.0, partCount)
            .createWithCompositionData {
                CompositionData()
                    .setDisplayerSupplier {
                        ParticleDisplayer.withSingle(ControlableEndRodEffect(it))
                    }
                    .addParticleInstanceInit {
                        this.size = flowerPartSingleSize
                        this.color = flowerPartColor
                        textureSheet = ControlableParticleData.particleTexturesMapper[textureSheets.name]!!
                    }
            }
    }

    @CodecField
    var textureSheets = TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT

    @CodecField
    var flowerPartColor = Math3DUtil.colorOf(255, 210, 230)

    @CodecField
    var flowerPartSingleSize = 0.1f

    @CodecField
    var ticks = 0.0

    @CodecField
    var glowTicks = 40

    @CodecField
    var startGlowTick = 0

    @CodecField
    var partCount = 320

    /**
     * 三角函数的速度偏移 函数零点越多花瓣越多
     */
    @CodecField
    var cycle = 5.0

    @CodecField
    var baseRotated = 0.0

    @CodecField
    var rotateSpeed = 0.0

    @CodecField
    var partScale = 3.0

    @CodecField
    var partMaxHeightScale = 0.125

    @CodecField
    var heightPow = 2.0

    /**
     * 决定花瓣胖瘦的
     */
    @CodecField
    var polarPow = 0.6

    /**
     * 高度变化的最小进度（保持向上）
     *
     * 防止高度不变化的时候花瓣还在变，所以他会同时限制花瓣的本地缩放
     */
    @CodecField
    var heightProgressMin = 0.3


    override fun onDisplay() {

        addPreTickAction {
            ticks++
            baseRotated += rotateSpeed

            handleParticles()
            toggleRelative()
        }
    }

    override fun beforeDisplay(map: Map<CompositionData, RelativeLocation>) {
        super.beforeDisplay(map)
        map.forEach { (k, v) ->
            val angle = Math3DUtil.calculateEulerAnglesToPoint(v.toVector3f())
                .second
            particleRadian[k.uuid] = angle

            // 写入函数计算， 算出他现在应该的半径位置
            val cycle = cycle * angle / (Math.PI * 2)
            val phase = cycle - floor(cycle) // 小数部分 0 - 1
            val raw = if (phase <= 0.5) 2 * phase else 2 * (1 - phase) // 0-1-0
            val func = raw.pow(polarPow)
            val tilt = 1.0
            // 最终的location
            val limitEase = if (0.2 >= 1 - heightProgressMin) 0.2 else 0.0
            v.apply {
                x = func * cos(angle + baseRotated) * partScale * limitEase
                z = func * sin(angle + baseRotated) * partScale * limitEase
                y = (func * 4).pow(heightPow) * tilt.coerceAtLeast(heightProgressMin) * partMaxHeightScale
            }
        }
    }

    private fun handleParticles() {
        particleLocations.forEach { (key, location) ->
            // 通过原始的location， 对位置进行修改
            if (ticks < startGlowTick) {
                return
            }
            val progress = ((ticks - startGlowTick) / glowTicks).coerceIn(0.0, 1.0)
            val changeEase = Eases.outQuad.cal(progress)
            val originRadian = particleRadian[key.controlUUID()] ?: 0f
            // 写入函数计算， 算出他现在应该的半径位置
            val cycle = cycle * originRadian / (Math.PI * 2)
            val phase = cycle - floor(cycle) // 小数部分 0 - 1
            val raw = if (phase <= 0.5) 2 * phase else 2 * (1 - phase) // 0-1-0
            val func = raw.pow(polarPow)
            val tilt = (1 - changeEase)
            // 最终的location
            val limitEase = if (0.2 >= 1 - heightProgressMin) 0.2 else changeEase.coerceIn(0.0, 1 - heightProgressMin)
            location.apply {
                x = func * cos(originRadian + baseRotated) * partScale * limitEase
                z = func * sin(originRadian + baseRotated) * partScale * limitEase
                y = (func * 4).pow(heightPow) * tilt.coerceAtLeast(heightProgressMin) * partMaxHeightScale
            }
        }
    }

}
