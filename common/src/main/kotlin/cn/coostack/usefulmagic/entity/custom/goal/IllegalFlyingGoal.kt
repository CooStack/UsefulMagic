package cn.coostack.usefulmagic.entity.custom.goal

import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.particles.emitters.DirectionShootEmitters
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

/**
 * 禁止升空
 */
class IllegalFlyingGoal(val entity: MagicBookEntity) : Goal() {
    override fun canUse(): Boolean {
        val target = entity.target ?: return false
        if (target !is Player) {
            return false
        }
        return target.abilities.flying && !(target.isCreative || target.isSpectator)
    }

    var time = 0

    override fun start() {
        super.start()
        time = 0
    }


    override fun tick() {
        super.tick()
        val target = entity.target ?: return
        if (target !is Player) {
            return
        }
        time++
        val illegal = target.abilities.flying && !(target.isCreative || target.isSpectator)
        val interval = entity.getHealthState() * 4
        if (time > interval && illegal) {
            val shoot = DirectionShootEmitters(target.position().add(0.0, 5.0, 0.0), target.level()).apply {
                templateData.also { it ->
                    it.maxAge = 10
                    it.speed = 2.0
                }
                shootDirection = Vec3(0.0, -1.0, 0.0)
                count = 100
                randomX = 0.1
                randomZ = 0.1
                randomSpeedOffset = 0.5
                gravity = PhysicConstant.EARTH_GRAVITY
                shootType = EmittersShootTypes.box(HitBox.of(4.0, 1.0, 4.0))
                maxTick = 10
            }
            ParticleEmittersManager.spawnEmitters(shoot)
            target.abilities.flying = false
            target.level().playSound(
                null, target.x, target.y, target.z,
                SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 4f, 1f
            )
            val mobAttack = target.damageSources().mobAttack(entity)
            target.hurt(mobAttack, 30f)
            target.deltaMovement = Vec3(0.0, -1.0, 0.0)
            target.hurtMarked = true
            target.sendSystemMessage(
                Component.literal("[${entity.displayName?.string}]: §c此地禁空!")
            )
            time = 0
        }
    }

}