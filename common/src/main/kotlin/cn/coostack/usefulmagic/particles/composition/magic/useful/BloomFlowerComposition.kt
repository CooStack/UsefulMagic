package cn.coostack.usefulmagic.particles.composition.magic.useful

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.network.particle.composition.AutoParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.CompositionData
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.helper.impl.composition.CompositionAlphaHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI
import kotlin.math.roundToInt

@CooAutoRegister
class BloomFlowerComposition(position: Vec3, world: Level? = null) : AutoParticleComposition(position, world) {
    private val alphaHelper = CompositionAlphaHelper(0.0, 1.0, 10)

    @CodecField
    var tick = 0

    /**
     * 统一确立花朵的大小 和旋转速度
     */
    @CodecField
    var flowerScale = 1.0

    @CodecField
    var flowerRotationSpeed = 0.0

    @CodecField
    var velocity = Vec3.ZERO

    @CodecField
    var drag = 0.01

    @CodecField
    var maxTick = -1

    init {
        setDisabledInterval(10)
        alphaHelper.loadControler(this)
    }


    override fun getParticles(): Map<CompositionData, RelativeLocation> {
        // 多个Part的组合
        val res = HashMap<CompositionData, RelativeLocation>()

        // 最外层叶片
        res[CompositionData()
            .setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    BloomFlowerPartComposition(Vec3.ZERO, null)
                        .apply {
                            // 调节花瓣参数
                            this.flowerPartColor = Math3DUtil.colorOf(80, 117, 32)
                            this.glowTicks = 20
                            this.startGlowTick = 4
                            this.baseRotated = PI / 6
                            this.flowerPartSingleSize = 0.09f
                            this.partScale = 2.5
                            handleComposition(this)

                        }
                )
            }
        ] = RelativeLocation(0.0, -0.2, 0.0)

        // 叶片2
        res[CompositionData()
            .setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    BloomFlowerPartComposition(Vec3.ZERO, null)
                        .apply {
                            // 调节花瓣参数
                            this.flowerPartColor = Math3DUtil.colorOf(80, 117, 32)
                            this.partScale = 1.5
                            this.glowTicks = 15
                            this.cycle = 6.0
                            this.heightProgressMin = 0.1
                            this.heightPow = 2.0
                            this.baseRotated = PI / 6
                            this.startGlowTick = 2
                            this.flowerPartSingleSize = 0.09f
                            handleComposition(this)

                        }
                )
            }
        ] = RelativeLocation(0.0, -0.2, 0.0)

        // 外层花瓣
        res[CompositionData()
            .setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    BloomFlowerPartComposition(Vec3.ZERO, null)
                        .apply {
                            // 调节花瓣参数
                            this.flowerPartColor = Vector3f(0.7f)
                            this.glowTicks = 20
                            this.cycle = 8.0
                            this.partMaxHeightScale = 0.105
                            this.startGlowTick = 3
                            this.heightProgressMin = 0.4
                            this.polarPow = 0.5
                            this.heightPow = 2.0
                            handleComposition(this)

                        }
                )
            }
        ] = RelativeLocation()
        // 花瓣2 粉色外面的花瓣
        res[CompositionData()
            .setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    BloomFlowerPartComposition(Vec3.ZERO, null)
                        .apply {
                            // 调节花瓣参数
                            this.flowerPartColor = Math3DUtil.colorOf(255, 117, 150)
                            this.textureSheets = TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT
                            this.glowTicks = 15
                            this.cycle = 7.0
                            this.heightProgressMin = 0.45
                            this.polarPow = 0.3
                            this.heightPow = 2.0
                            this.startGlowTick = 3
                            this.flowerPartSingleSize = 0.13f
                            handleComposition(this)

                            this.partCount = 240
                        }
                )
            }
        ] = RelativeLocation(0.0, 0.15, 0.0)
        // 花瓣1 粉色里面的花瓣
        res[CompositionData()
            .setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    BloomFlowerPartComposition(Vec3.ZERO, null)
                        .apply {
                            // 调节花瓣参数
                            this.flowerPartColor = Math3DUtil.colorOf(255, 117, 150)
                            this.textureSheets = TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT
                            this.glowTicks = 15
                            this.cycle = 5.0
                            this.heightProgressMin = 0.7
                            this.polarPow = 0.3
                            this.heightPow = 2.0
                            this.partMaxHeightScale = 0.1125
                            this.startGlowTick = 3
                            this.flowerPartSingleSize = 0.13f
                            handleComposition(this)

                            this.partCount = 160
                        }
                )
            }
        ] = RelativeLocation(0.0, 0.1, 0.0)
        // 花蕊
        res[CompositionData()
            .setDisplayerSupplier {
                ParticleDisplayer.withComposition(
                    BloomFlowerPartComposition(Vec3.ZERO, null)
                        .apply {
                            // 调节花瓣参数
                            this.flowerPartColor = Math3DUtil.colorOf(255, 220, 60)
                            this.glowTicks = 15
                            this.cycle = 6.0
                            this.heightProgressMin = 0.8
                            this.polarPow = 0.7
                            this.heightPow = 1.5
                            this.partMaxHeightScale = 0.12
                            this.flowerPartSingleSize = 0.1f
                            this.textureSheets = TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT
                            this.partCount = 120
                            this.startGlowTick = 5
                            handleComposition(this)
                        }
                )
            }
        ] = RelativeLocation(0.0, 0.15, 0.0)
//        res[
//            CompositionData()
//                .setDisplayerSupplier {
//                    ParticleDisplayer.withDisplayEntity(FlowerDisplay(Vec3.ZERO, null).apply {
//                        this.rotateSpeed = PIF / 16
//                        this.scale = 1.4f
//                    })
//                }
//        ] = RelativeLocation(0.0, 0.5, 0.0)
        return res
    }

    private fun handleComposition(composition: BloomFlowerPartComposition) {
        composition.apply {
            this.partScale *= flowerScale
            this.partMaxHeightScale *= flowerScale
            this.rotateSpeed = flowerRotationSpeed
            this.partCount *= flowerScale.roundToInt().coerceAtLeast(1)
        }
    }

    override fun onDisplay() {
        alphaHelper.resetAlphaMin()
        addPreTickAction {
            if (tick++ >= maxTick && maxTick != -1 && status.isEnable()) {
                status.disable()
            }
            teleportTo(position + velocity)
            velocity *= (1 - drag)
            if (status.isDisable()) {
                alphaHelper.decreaseAlpha()
            } else {
                alphaHelper.increaseAlpha()
            }
        }
    }

    override fun remove() {
        if (status.isDisable()) {
            super.remove()
        } else {
            status.disable()
        }
    }
}
