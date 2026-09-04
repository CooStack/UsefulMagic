package cn.coostack.usefulmagic.items.weapon.magic.attack

import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.formation.CrystalFormation
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.items.weapon.magic.MagicItem
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.pow
import kotlin.math.roundToInt

class BeamMagic(properties: Properties) : MagicItem(properties) {
    val attenuation = 0.9
    override fun release(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        if (world !is ServerLevel) return

        // 射线法杖
        world.playSound(
            null, shooter.x, shooter.y, shooter.z, SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 6f, 1.3f
        )
        // 释放直线魔法
        // 如果直线(AABB.ofSize(pos,1.0,1.0,1.0) 内存在实体 视为击中 攻击距离为30
        // 然后寻找附近 AABB.ofSize(pos,10.0,10.0,10.0)的实体 2次
        val direction = shooter.forward.normalize()
        var currentPos = shooter.eyePosition
        val damage = MagicHelper.getMagicDamage(wandStack)
        for (i in 1..30) {
            currentPos = currentPos.add(direction)
            val entities = world.getEntitiesOfClass(
                LivingEntity::class.java,
                AABB.ofSize(currentPos, 1.0, 1.0, 1.0)
            ) {
                it.uuid != shooter.uuid
            }
            if (entities.isNotEmpty()) {
                val damagedEntitySet = HashSet<LivingEntity>()
                val entity = entities.first()
                damagedEntitySet.add(entity)
                val source = UsefulMagicDamageSources.entityMagic(world, shooter, shooter)
                entity.hurt(source, damage.toFloat())
                var prePos = currentPos
                var nextPos = prePos
                for (j in 1..2) {
                    val next =
                        world.getEntitiesOfClass(LivingEntity::class.java, AABB.ofSize(prePos, 16.0, 10.0, 16.0)) {
                            val cant = ServerFormationManager.getFormationFromPos(currentPos, world)?.let { it ->
                                it is CrystalFormation && it.hasDefend
                            } ?: false
                            it.uuid != shooter.uuid && it !in damagedEntitySet && !cant
                        }.firstOrNull() ?: continue
                    prePos = nextPos
                    nextPos = next.eyePosition
                    next.hurt(source, (damage * attenuation.pow(j)).toFloat())
                    damagedEntitySet.add(next)
                    val particles = PointsBuilder().addLine(
                        prePos, nextPos, prePos.distanceTo(nextPos).roundToInt() * 10
                    ).create().map { it.toVector() }
                    ServerParticleUtil.spawnBatch(ParticleTypes.ENCHANT, world, particles, Vec3.ZERO, 64.0)
                }
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
        // 生成直线
        val particles = PointsBuilder().addLine(
            shooter.eyePosition, currentPos, currentPos.distanceTo(shooter.eyePosition).roundToInt() * 10
        ).create().map { it.toVector() }
        ServerParticleUtil.spawnBatch(ParticleTypes.ENCHANT, world, particles, Vec3.ZERO, 64.0)
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
