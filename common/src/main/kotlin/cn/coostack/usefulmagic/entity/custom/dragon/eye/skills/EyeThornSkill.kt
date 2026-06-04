package cn.coostack.usefulmagic.entity.custom.dragon.eye.skills

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.display.DisplayEntityManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.display.skills.SkillRangeDisplay
import cn.coostack.usefulmagic.display.skills.ThornDisplay
import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.effects.asHolder
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.EyeOrbitCloserPhase
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.particles.emitters.entity.eye.MagicThornEmitter
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

/**
 * 发射多个地刺， 从地面向上发射， 有推力
 *
 * 这个还要改一下美观设计
 *
 * @constructor Create empty Eye thorn skill
 */
class EyeThornSkill(val searchedRange: Double, val damage: Float) :
    Skill<MagicEyeEntity>,
    SkillCancelCondition<MagicEyeEntity> {
    companion object {
        const val ID = "eye_thorn_skill"
    }

    private var offsetTick = 0
    private var arriving = false
    override var chance: Double = 0.5
    override fun getSkillCountDown(source: MagicEyeEntity): Int {
        return 20 * 22
    }

    override fun testCancel(entity: MagicEyeEntity): Boolean {
        return entity.health <= 1f
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = true
    override fun onActive(source: MagicEyeEntity) {
        // 这里要设置他的phase状态
        offsetTick = 0
        arriving = false
        source.phaseManager.forceSetPhase(
            EyeOrbitCloserPhase.HOLDER.get()
        ) {
            addTarget(source.spawnPos.add(0.0, 2.0, 0.0))
            listenContinue {
                peekTarget().ifPresent {
                    if (it.distanceToSqr(source.positionOnEye()) <= 1) {
                        arriving = true
                    }
                }
            }
        }
    }

    override fun onRelease(source: MagicEyeEntity, holdingTick: Int) {
        // 这里要重置他的phase
        source.phaseManager.resetDefaultPhase()
    }

    override fun getMaxHoldingTick(holdingEntity: MagicEyeEntity): Int {
        return 30 * 5 + offsetTick
    }

    override fun holdingTick(
        holdingEntity: MagicEyeEntity,
        holdTicks: Int
    ) {
        if (!arriving) {
            if (offsetTick++ > 60) {
                arriving = true
            }
            return
        }

        if (holdTicks % 12 != 0) {
            return
        }
        // 寻找玩家
        val searchBox = holdingEntity.boundingBox.inflate(searchedRange)
        val world = holdingEntity.level()
        val players = world.getEntitiesOfClass(Player::class.java, searchBox) {
            !(it.isSpectator || it.isCreative) && it.isAlive
        }.take(3)
        var leastPlayer: Player? = null
        players.forEach { p ->
            // 召唤display
            prickingOnPosition(p.position(), world, holdingEntity)
            leastPlayer = p
        }


        val countReminding = 3 - players.count()
        if (countReminding > 0) {
            repeat(countReminding) {
                // 在范围内随机
                val pos = if (leastPlayer != null) {
                    leastPlayer.position().offsetRandomlyHorizontal(Random.nextDouble(8.0, 24.0))
                } else {
                    holdingEntity.position().offsetRandomlyHorizontal(Random.nextDouble(searchedRange))
                }
                val finalPos = findBlockTopPosition(pos, world)?.add(0.0, 0.5, 0.0)
                if (finalPos != null) {
                    val fast = holdingEntity.target?.let {
                        it is Player && (it.abilities.flying || it.abilities.mayfly)
                    } ?: false
                    prickingOnPosition(finalPos, world, holdingEntity, fast)
                }
            }
        }

    }

    private fun findBlockTopPosition(position: Vec3, world: Level): Vec3? {
        val mutable = BlockPos.containing(position).mutable()
        val minY = world.minBuildHeight

        while (mutable.y > minY && world.getBlockState(mutable).isAir) {
            mutable.move(0, -1, 0)
        }
        if (world.getBlockState(mutable).isAir) {
            return null
        }

        val top = mutable.above()
        if (!world.getBlockState(top).isAir) {
            return null
        }
        return top.bottomCenter
    }


    private fun prickingOnPosition(position: Vec3, world: Level, shooter: LivingEntity, fast: Boolean = false) {
        SkillRangeDisplay(position, world).apply {
            biggerTick = 10
            lifetime = 10
            discardTick = 5
            scaled = 4f
            color = Vector3f(1f, 0.3f, 0.4f)
            bright = 20f
            DisplayEntityManager.spawn(this)
        }


        CooParticlesAPI.scheduler.runTask(if (fast) 8 else 16) {
            repeat(Random.nextInt(16, 24)) {
                ThornDisplay(position.add(0.0, -1.0, 0.0).offsetRandomlyHorizontal(2.0), world).apply {
                    blockMaterial = Blocks.END_STONE.defaultBlockState()
                    lifetime = Random.nextInt(2, 4)
                    thrustTick = Random.nextInt(3, 6)
                    baseSize = 0.4
                    heapCount = Random.nextInt(6, 12)
                    topSize = 0.1
                    height = Random.nextDouble(3.0, 12.0)
                    this.direction = Vec3(0.0, 1.0, 0.0).offsetRandomlyHorizontal(0.25)
                    DisplayEntityManager.spawn(this)
                }
            }
            ThornDisplay(position.add(0.0, -1.0, 0.0), world).apply {
                blockMaterial = Blocks.END_STONE.defaultBlockState()
                thrustTick = Random.nextInt(4, 7)
                lifetime = 5
                baseSize = 1.0
                heapCount = 16
                topSize = 0.2
                height = Random.nextDouble(8.0, 16.0)
                DisplayEntityManager.spawn(this)
            }
            MagicThornEmitter(position, world).apply {
                size = 4.0
                ParticleEmittersManager.spawnEmitters(this)
            }
            playDragonSoundOnce(
                world,
                position,
                UsefulMagicSoundEvents.THORN_HIT.get(),
                SoundSource.HOSTILE,
                0.5f,
                0.9f + Random.nextFloat() * 0.3f,
                24.0,
            )
            // 攻击范围内的所有实体
            val box = AABB.ofSize(position.add(0.0, 4.0, 0.0), 5.0, 16.0, 5.0)
            world.getEntitiesOfClass(LivingEntity::class.java, box) {
                !(it.isSpectator || it.hasInfiniteMaterials()) &&
                        it.isAlive &&
                        (shooter as? MagicEyeEntity)?.canAttackWithEyeMagic(it) != false
            }.forEach {
                val source = UsefulMagicDamageSources.entityMagic(world, shooter, shooter)
                it.hurt(source, damage)
                it.addDeltaMovement(Vec3(0.0, 0.3, 0.0))
                it.addEffect(
                    MobEffectInstance(UsefulMagicEffects.MAGIC_SEALED.asHolder(), 20 * 3)
                )
                it.hurtMarked = true
            }
        }
    }

    override fun stopHolding(entity: MagicEyeEntity, holdTicks: Int) {
    }

    override fun getSkillID(): String {
        return ID
    }
}
