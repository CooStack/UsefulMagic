package cn.coostack.usefulmagic.test

import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.particles.impl.ControlableFlashEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableHappyVillagerEffect
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.test.SimpleCompositionOption
import cn.coostack.cooparticlesapi.test.SimpleEmitterOption
import cn.coostack.cooparticlesapi.test.SimpleRendererEntityOption
import cn.coostack.cooparticlesapi.test.TestManager
import cn.coostack.cooparticlesapi.test.api.*
import cn.coostack.cooparticlesapi.test.block.BlockTestGroup
import cn.coostack.cooparticlesapi.test.block.BlockTestPlayer
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.extend.serverLevel
import cn.coostack.usefulmagic.particles.composition.magic.useful.BloomFlowerComposition
import cn.coostack.usefulmagic.particles.emitters.CollectLineParticleEmitter
import cn.coostack.usefulmagic.particles.emitters.magic.useful.BloomEffectEmitter
import cn.coostack.usefulmagic.particles.emitters.magic.useful.BloomFlowerEmitter
import cn.coostack.usefulmagic.renderer.StraightLaserRenderEntity
import cn.coostack.usefulmagic.utils.BloomUtil
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI

/**
 * 构建 UsefulMagic 的方块测试组。
 *
 * @param player 执行测试的玩家
 */
class UsefulMagicBlockTestBuilder(player: Player) : TestGroupBuilder {
    /** 测试组实际使用的方块测试玩家。 */
    private val player = player as? BlockTestPlayer ?: BlockTestPlayer(player)

    /** 方块测试组的注册入口。 */
    companion object {
        /** 方块测试组的注册路径。 */
        private const val ID_PATH = "usefulmagic-block-test"

        /** 方块测试组的稳定注册 ID。 */
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, ID_PATH)

        /** 向 CooParticlesAPI 测试管理器注册方块测试组。 */
        fun init() {
            TestManager.register(ID) {
                UsefulMagicBlockTestBuilder(it)
            }
        }
    }

    override fun groupID(): ResourceLocation {
        return ID
    }

    /**
     * 创建包含开花组合、粒子发射器和方块扩散测试的测试组。
     *
     * @return 可由测试控制器执行的方块测试组
     */
    override fun build(): TestGroup {
        return BlockTestGroup(player, ID)
            .appendOption {
                SimpleEmitterOption(CollectLineParticleEmitter(player.position, player.level), -1, "聚集测试")
                    .applyParam(Vector3fTestOptionValue("leftColor", "起始颜色").asColor(), Vector3f(1f))
                    .applyParam(Vector3fTestOptionValue("rightColor", "结束颜色").asColor(), Vector3f(1f))
                    .applyParam(IntTestOptionValue("minCount", "最少个数"), 60)
                    .applyParam(IntTestOptionValue("maxCount", "最大个数"), 120)
                    .applyParam(IntTestOptionValue("minAge", "最小生命周期"), 10)
                    .applyParam(IntTestOptionValue("maxAge", "最大生命周期"), 40)
                    .applyParam(DoubleTestOptionValue("minSpeed", "最小速度"), 0.5)
                    .applyParam(DoubleTestOptionValue("maxSpeed", "最大速度"), 2.6)
                    .applyParam(
                        TextureSheetEnumTestOptionValue("textureSheet", "渲染方式"),
                        TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT
                    )
                    .applyParam(
                        ControlableParticleEffectTestOptionValue("effects", "纹理"),
                        ControlableParticleEffectBuilder("ControlableFlashEffect") { u, b ->
                            ControlableFlashEffect(u, b)
                        }
                    )
                    .applyTo {
                        it.template.apply {
                            this.effect = getParamOrThrow<ControlableParticleEffectBuilder>("effects").build(uuid)
                            setTextureSheet(getParamOrThrow<TextureSheetsEnum>("textureSheet"))
                        }
                        it.simpleData.apply {
                            leftColor = getParamOrThrow("leftColor")
                            rightColor = getParamOrThrow("rightColor")
                            minCount = getParamOrThrow("minCount")
                            maxCount = getParamOrThrow("maxCount")
                            minAge = getParamOrThrow("minAge")
                            maxAge = getParamOrThrow("maxAge")
                            minSpeed = getParamOrThrow("minSpeed")
                            maxSize = getParamOrThrow("maxSpeed")
                        }
                    }.onPlayerUpdate { player, emitter ->
                        emitter.pos = player.position()
                    }
            }
            .appendOption {
                SimpleRendererEntityOption(
                    StraightLaserRenderEntity(player.level, player.position), -1, "激光测试"
                ).applyParam(
                    Vec3TestOptionValue("target", "目标位置"), Vec3(0.0, 12.0, 0.0)
                )
                    .applyParam(
                        DoubleTestOptionValue("size", "大小"), 3.0
                    )
                    .applyParam(
                        Vector3fTestOptionValue("color", "颜色").asColor(), Vector3f(1f)
                    )
                    .applyParam(
                        IntTestOptionValue("lifetime", "存活时间"), 20
                    )
                    .applyParam(DoubleTestOptionValue("bright", "亮度"), 1.0)
                    .applyTo {
                        it.brightness = getParamOrThrow("bright")
                        it.updateBeam(player.position, player.position + getParamOrThrow<Vec3>("target"))
                        it.maxRadius = getParamOrThrow("size")
                        it.color = getParamOrThrow("color")
                        it.lifetime = getParamOrThrow("lifetime")
                    }
            }
            .appendOption {
                SimpleCompositionOption(
                    BloomFlowerComposition(player.position(), player.level()).apply {
                        velocity = Vec3(0.0, 0.12, 0.0)
                        drag = 0.05
                        flowerRotationSpeed = PI / 32
                        flowerScale = 1.8
                    },
                    100
                )
            }.appendOption {
                TickOption(200)
                    .addTickAction {
                        if (testingTick % 20 == 0) {
                            val world = player.serverLevel ?: return@addTickAction
                            BloomUtil.bloomAround(world, player.blockPos, 16, 5, 999)
                        }
                    }
            }.appendOption {
                SimpleCompositionOption(
                    BloomFlowerComposition(player.position(), player.level()).apply {
                        velocity = Vec3(0.0, 0.12, 0.0)
                        drag = 0.1
                        flowerScale = 1.0
                    },
                    100
                )
            }.appendOption {
                SimpleEmitterOption(BloomEffectEmitter(player.position(), player.level).apply {
                    maxRadius = 16.0
                    maxTick = -1
                    simpleData.apply {
                        minCount = 60
                        maxCount = 120
                        minAge = 5
                        maxAge = 12
                    }
                    yawSpeed = PI.toFloat() / 64F minRangeTo PI.toFloat() / 48F
                    pitchSpeed = PI.toFloat() / 64F minRangeTo PI.toFloat() / 32F
                    rollSpeed = PI.toFloat() / 64F minRangeTo PI.toFloat() / 48F
                    templateData.apply {
                        faceToCamera = false
                        setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                    }
                }, -1)
            }.appendOption {
                SimpleEmitterOption(BloomEffectEmitter(player.position(), player.level).apply {
                    maxRadius = 16.0
                    maxTick = -1
                    simpleData.apply {
                        minCount = 60
                        maxCount = 120
                        minAge = 5
                        maxAge = 12
                    }
                    templateData.apply {
                        effect = ControlableHappyVillagerEffect(uuid)
                        setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                    }
                }, -1)
            }.appendOption {
                SimpleEmitterOption(BloomEffectEmitter(player.position(), player.level).apply {
                    maxRadius = 16.0
                    maxTick = -1
                    simpleData.apply {
                        minCount = 60
                        maxCount = 120
                        minAge = 5
                        maxAge = 12
                    }
                    templateData.apply {
                        setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                    }
                }, -1)
            }.appendOption {
                SimpleEmitterOption(BloomFlowerEmitter(player.position(), player.level))
            }
    }
}
