package cn.coostack.usefulmagic.items.weapon.magic

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.sound.ServerSoundManager
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.formation.CrystalFormation
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import kotlin.math.pow
import kotlin.random.Random

class LightningMagic(properties: Properties) : MagicItem(properties) {
    companion object {
        const val ATTENUATION = 0.9
    }

    override fun release(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        // 仅在服务端执行
        if (world.isClientSide) {
            return
        }

        // 读取法术伤害
        val damage = MagicHelper.getMagicDamage(wandStack)

        world as ServerLevel
        ServerSoundManager.instance(
            UsefulMagicSoundEvents.ELECTRIC_EFFECT.get(),
            SoundSource.PLAYERS,
        )
            .entity(shooter)
            .uniqueKey(UUID.randomUUID().toString())
            .layer("lightning_magic")
            .pitch(0.9F + Random.nextFloat() * 0.2F)
            .volume(0.5f)
            .visibleRange(128.0)
            .relative()
            .spawn()
        // 沿施法者视线逐步扫描，命中首个目标后触发连锁伤害
        // AABB.ofSize(center, dx, dy, dz) 的参数是完整尺寸，不是半径
        // 例如 AABB.ofSize(pos, 4.0, 4.0, 4.0) 实际覆盖中心点周围各 2 格
        val direction = shooter.forward.normalize()
        var currentPos = shooter.eyePosition
        for (i in 1..50) {
            currentPos = currentPos.add(direction)
            val entities = world.getEntitiesOfClass(
                LivingEntity::class.java,
                AABB.ofSize(currentPos, 4.0, 4.0, 4.0)
            ) {
                it.uuid != shooter.uuid && !FriendFilterHelper.filterFriend(shooter, it)
            }
            // 找到首个目标后开始连锁处理
            if (entities.isNotEmpty()) {
                val damagedEntitySet = HashSet<LivingEntity>()
                var entity = entities.first()
                damagedEntitySet.add(entity)
                val source = UsefulMagicDamageSources.entityMagic(world, shooter, shooter)
                currentPos = entity.boxCenterPosition()
                entity.remainingFireTicks = 60
                entity.hurt(source, damage.toFloat())
                entity.invulnerableTime = 0
                var prePos = currentPos
                var nextPos = prePos
                // 继续寻找下一个最近目标，最多连锁 5 次
                for (j in 1..5) {
                    val next =
                        world.getEntitiesOfClass(LivingEntity::class.java, AABB.ofSize(prePos, 48.0, 48.0, 48.0)) {
                            val cant = ServerFormationManager.getFormationFromPos(currentPos, world)?.let { it ->
                                it is CrystalFormation && it.hasDefend
                            } ?: false
                            it.uuid != shooter.uuid && it !in damagedEntitySet && !cant && !FriendFilterHelper.filterFriend(
                                shooter,
                                it
                            )
                        }.minByOrNull {
                            prePos.distanceTo(it.boxCenterPosition())
                        } ?: continue
                    prePos = nextPos
                    nextPos = next.boxCenterPosition()
                    val source = UsefulMagicDamageSources.entityMagic(world, entity, shooter)
                    next.remainingFireTicks = 60
                    next.hurt(source, (damage * ATTENUATION.pow(j)).toFloat())
                    next.invulnerableTime = 0
                    entity = next
                    damagedEntitySet.add(next)
                    // 生成当前链路的闪电特效
                    summonLightningEffect(
                        prePos, next.boxCenterPosition() - prePos, world, true
                    )
                }
                break
            }
            val blockPos = ofFloored(currentPos)
            val state = world.getBlockState(blockPos)
            if (!state.isAir && !state.getCollisionShape(world, blockPos).isEmpty) {
                break
            }
            val cant = ServerFormationManager.getFormationFromPos(currentPos, world)?.let {
                val option = LivingEntityTargetOption(shooter, false)
                if (it is CrystalFormation && it.hasDefend && !it.isFriendly(option)) {
                    it.attack(5f, option, currentPos)
                    true
                } else false
            } ?: false
            if (cant) break
        }
        val dir = (currentPos - shooter.eyePosition)
        summonLightningEffect(
            shooter.eyePosition.add(
                Vec3.ZERO.random()
            ), dir, world
        )
    }


    private fun summonLightningEffect(spawnPos: Vec3, dir: Vec3, world: Level, sub: Boolean = false) {
        val lightning = LightningParticleEmitters(spawnPos, world).apply {
            targetPos = dir
            maxTick = 1
            templateData.apply {
                setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                color = Math3DUtil.colorOf(
                    121, 211, 249
                )
            }
            simpleData.apply {
                if (!sub) {
                    minAge = 3
                    maxAge = 7
                    minCount = 2
                    maxCount = 5
                } else {
                    minAge = 2
                    maxAge = 5
                    minCount = 1
                    maxCount = 3
                }
                minSize = 0.1
                maxSize = 0.3
            }
        }
        ParticleEmittersManager.spawnEmitters(lightning)
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
    }

    override fun startUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack
    ) {
    }

}

