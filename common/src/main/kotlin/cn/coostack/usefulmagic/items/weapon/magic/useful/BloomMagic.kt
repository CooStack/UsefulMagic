package cn.coostack.usefulmagic.items.weapon.magic.useful

import cn.coostack.cooparticlesapi.data.cache.CacheKey
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.supports.sound.ServerSoundManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.extend.addMultilineTranslatable
import cn.coostack.usefulmagic.extend.serverLevel
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.weapon.magic.MagicItem
import cn.coostack.usefulmagic.particles.composition.magic.useful.BloomFlowerComposition
import cn.coostack.usefulmagic.particles.emitters.magic.useful.BloomEffectEmitter
import cn.coostack.usefulmagic.particles.emitters.magic.useful.BloomFlowerEmitter
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.BloomUtil
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI
import kotlin.random.Random


// 康复训练 我再也不能一直依赖AI了
class BloomMagic(properties: Properties) : MagicItem(properties) {
    companion object {
        val CENTER_FLOWER = CacheKey.of<BloomFlowerComposition>(
            ResourceLocation.fromNamespaceAndPath(
                UsefulMagic.MOD_ID,
                "center-flower"
            )
        )

        val BLOOM_EFFECT_EMITTER = CacheKey.of<BloomEffectEmitter>(
            ResourceLocation.fromNamespaceAndPath(
                UsefulMagic.MOD_ID,
                "bloom-emitter"
            )
        )
        val BLOOM_FLOWER_EMITTER = CacheKey.of<BloomFlowerEmitter>(
            ResourceLocation.fromNamespaceAndPath(
                UsefulMagic.MOD_ID,
                "bloom-flower-emitter"
            )
        )
        val BLOOM_TARGET = CacheKey.of<Vec3>(
            ResourceLocation.fromNamespaceAndPath(
                UsefulMagic.MOD_ID,
                "bloom-target"
            )
        )

        const val MAGIC_SPREAD_TICK = 40
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        val level = stack.get(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get()) ?: return
        val baseCost = stack.get(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get()) ?: return
        val baseCD = stack.get(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get()) ?: return
        val baseUsageTime = stack.get(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get()) ?: return
        tooltipComponents.add(Component.translatable("item.usefulmagic.magic.level", Component.literal("$level")))
        tooltipComponents.add(
            Component.translatable(
                "item.usefulmagic.magic.base_cost",
                Component.literal("$baseCost")
            )
        )
        tooltipComponents.add(
            Component.translatable(
                "item.usefulmagic.magic.base_usage",
                Component.literal("$baseUsageTime")
            )
        )
        tooltipComponents.add(Component.translatable("item.usefulmagic.magic.base_cd", Component.literal("$baseCD")))
        tooltipComponents.addMultilineTranslatable("item.usefulmagic.items.bloom_magic")
    }



    override fun release(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        // 每20tick催熟周围一次农作物， 并且消耗魔力值
        // 他的偏好伤害越高，催熟的半径越大 最大是32 -> 分布式环变化， 每一个被催熟都有极低的概率生成一个花
        // 花粒子的周围有更好的催熟能力

        // 在周围召唤一个绽开的花朵， 花朵存在的时候会对周围的农作物进行催熟
        // 要搜索周围的农田
        cleanEffect(shooter)
        val radius = calculateFlowerRange(wandStack, ballStack, shooter)
        if (radius <= 0) {
            return
        }
        if (world.isClientSide) return
        var t = 0
        val target = shooter.cacher[BLOOM_TARGET]!!
        val level = ballStack.get(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get()) ?: return

        val step = radius / MAGIC_SPREAD_TICK

        ServerSoundManager.instance(
            UsefulMagicSoundEvents.BLOOMING.get(),
            shooter.soundSource
        )
            .volume(1f)
            .pitch(1f)
            .visibleRange(radius + 16.0)
            .bindToEntity(shooter, true)
            .spawn()
            .fadeOut(MAGIC_SPREAD_TICK)
        submitTaskTimerMaxTickServer(1, MAGIC_SPREAD_TICK) {
            ServerSoundManager.instance(
                SoundEvents.BONE_MEAL_USE,
                shooter.soundSource
            )
                .volume(1f)
                .pitch(1f)
                .visibleRange(radius + 16.0)
                .bindToEntity(shooter, true)
                .spawn()
                .fadeOut(5)
            BloomUtil.bloomGradientStep(
                shooter.serverLevel!!,
                BlockPos.containing(target),
                t++,
                radius,
                radius / MAGIC_SPREAD_TICK,
                20,
                -1,
                radius / MAGIC_SPREAD_TICK + 1.2
            )
            val flowerOffsetMax = step * t
            val flowerOffsetMin = step * (t - 1)

            if (t >= 5) {
                BloomFlowerComposition(
                    randomHorizontalVec3() * Random.nextDouble(
                        flowerOffsetMin,
                        flowerOffsetMax
                    ) + target + Vec3(0.0, Random.nextDouble(0.0, step), 0.0), world
                ).apply {
                    velocity = Vec3(0.0, -0.12, 0.0)
                    drag = 0.08
                    maxTick = Random.nextInt(18, 25)
                    flowerScale = 1.0
                    flowerRotationSpeed = Random.nextDouble(-PI / 32, PI / 32)
                    ParticleCompositionManager.spawn(this)
                }
            }
        }
        BloomEffectEmitter(target, world).apply {
            maxRadius = radius
            spreadSpeed = radius / 20
            spreadDiscreteSpeed = radius / 50
            maxDiscrete = 12.0
            yawSpeed = PIF / 64 minRangeTo PIF / 48
            pitchSpeed = PIF / 64 minRangeTo PIF / 32
            rollSpeed = PIF / 64 minRangeTo PIF / 48
            simpleData.apply {
                minCount = 60 * level
                maxCount = 120 * level
                minAge = 18
                maxAge = 32
            }
            templateData.apply {
                faceToCamera = false
                setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
            }
            ParticleEmittersManager.spawnEmitters(this)
        }
    }

    /**
     * 计算花田影响范围
     */
    private fun calculateFlowerRange(wand: ItemStack, ball: ItemStack, shooter: LivingEntity): Double {
        val level = wand.get(UsefulMagicDataComponentTypes.WAND_LEVEL.get()) ?: return 0.0
        // 按照法杖等级，偏好程度, 法杖对偏好的加成 计算出最终的范围
        // 法杖等级加成
        val prefer = wand.get(UsefulMagicDataComponentTypes.WAND_PREFER.get()) ?: return 0.0
        val preferLevel = prefer.preferItems[ball.item] ?: 0
        val preferFactor = preferLevel * prefer.damageFactor
        val finalRange = (level - 2) * 16 + (preferFactor * 1.5) * 32

        return finalRange
    }

    override fun usingTick(
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
    }

    override fun stopUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        chargingTick: Int,
        max: Boolean
    ) {
        // 这里要删除掉所有的内容
        cleanEffect(shooter)
    }

    private fun cleanEffect(shooter: LivingEntity) {
        shooter.cacher[CENTER_FLOWER]?.remove()
        shooter.cacher[BLOOM_EFFECT_EMITTER]?.remove()
        shooter.cacher[BLOOM_FLOWER_EMITTER]?.remove()
    }

    override fun startUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack
    ) {
        val range = calculateFlowerRange(wandStack, ballStack, shooter)
        val level = ballStack.get(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get()) ?: return
        val bloomFlower = BloomFlowerEmitter(shooter.eyePosition.add(0.0, 0.5, 0.0), world)
            .apply {
                randomData.apply {
                    minCount = 30 * level
                    maxCount = 60 * level
                }
                template.apply {
                    color = Math3DUtil.colorOf(239, 194, 219)
                }
                boxSize = range
                ParticleEmittersManager.spawnEmitters(this)
            }
        val bloomFlowerComposition = BloomFlowerComposition(shooter.eyePosition.add(0.0, 0.5, 0.0), world)
            .apply {
                velocity = Vec3(0.0, 0.12, 0.0)
                drag = 0.05
                flowerRotationSpeed = PI / 32
                flowerScale = 1.8
                ParticleCompositionManager.spawn(this)
            }

        shooter.cacher[BLOOM_FLOWER_EMITTER] = bloomFlower
        shooter.cacher[CENTER_FLOWER] = bloomFlowerComposition
        shooter.cacher[BLOOM_TARGET] = shooter.position()
    }

}
