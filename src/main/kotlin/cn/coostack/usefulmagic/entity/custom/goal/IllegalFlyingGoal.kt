package cn.coostack.usefulmagic.entity.custom.goal

import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.particles.emitters.DirectionShootEmitters
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

/**
 * 禁止升空
 */
class IllegalFlyingGoal(val entity: MagicBookEntity) : Goal() {
    override fun canStart(): Boolean {
        val target = entity.target ?: return false
        if (target !is PlayerEntity) {
            return false
        }
        return target.abilities.flying && !(target.abilities.creativeMode || target.isSpectator)
    }

    var time = 0
    override fun start() {
        super.start()
        time = 0
    }


    override fun tick() {
        super.tick()
        val target = entity.target ?: return
        if (target !is PlayerEntity) {
            return
        }
        time++
        val illegal = target.abilities.flying && !(target.abilities.creativeMode || target.isSpectator)
        val interval = entity.getHealthState() * 4
        if (time > interval && illegal) {
            val shoot = DirectionShootEmitters(target.pos.add(0.0, 5.0, 0.0), target.world).apply {
                templateData.also { it ->
                    it.maxAge = 10
                    it.speed = 2.0
                }
                shootDirection = Vec3d(0.0, -1.0, 0.0)
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
            target.world.playSound(
                null, target.x, target.y, target.z,
                SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, SoundCategory.PLAYERS, 4f, 1f
            )
            val mobAttack = target.damageSources.mobAttack(entity)
            target.damage(mobAttack, 30f)
            target.velocity = Vec3d(0.0, -1.0, 0.0)
            target.velocityModified = true
            target.sendMessage(
                Text.of {
                    "[${entity.displayName?.string}]: §c此地禁空!"
                }
            )
            time = 0
        }
    }

}