package cn.coostack.usefulmagic.skill.player

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.particles.barrages.entity.skill.GiantSwordLightBarrage
import cn.coostack.usefulmagic.particles.emitters.ParticleWaveEmitters
import cn.coostack.usefulmagic.particles.style.skill.SwordLightStyle
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.utils.ComboUtil
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Box
import java.util.UUID

/**
 * 大剑气
 */
class PlayerSwordLightSkill : Skill, ComboCondition {
    override var chance: Double = 1.0
    override val triggerComboMin: Int = 15

    override fun getSkillCountDown(source: LivingEntity): Int {
        return 0
    }

    var style = SwordLightStyle()
    override fun onActive(source: LivingEntity) {
        val combo = ComboUtil.getComboState(source.uuid)
        combo.count -= triggerComboMin

        val spawnedStyle = SwordLightStyle()
        style = spawnedStyle
        var currentPos = source.eyePos
        val direction = source.rotationVector
        val world = source.world
        var findTarget: LivingEntity? = null
        for (i in 1..50) {
            currentPos = currentPos.add(direction)
            val entities = world.getEntitiesByClass(
                LivingEntity::class.java,
                Box.of(currentPos, 4.0, 4.0, 4.0)
            ) {
                it.uuid != source.uuid
            }
            if (entities.isNotEmpty()) {
                findTarget = entities.random()
                break
            }
        }
        if (findTarget != null) {
            style.lockedEntityID = findTarget.id
        }
        val spawnPos = source.eyePos.add(0.0, 30.0, 0.0)
        val rotateDirection = spawnPos.relativize(currentPos)
        ParticleStyleManager.spawnStyle(world, spawnPos, spawnedStyle)
        CooParticleAPI.scheduler.runTask(20) {
            val barrage = GiantSwordLightBarrage(
                spawnedStyle,
                findTarget?.id ?: -1,
                spawnPos, world as ServerWorld,
                BarrageOption().apply {
                    speed = 1 / 20.0
                    enableSpeed = true
                    maxLivingTick = 20 * 60
                    noneHitBoxTick = 1 * 20
                }, 80.0, source as PlayerEntity
            )
            barrage.apply {
                this.direction = rotateDirection
            }
            BarrageManager.spawn(barrage)
            CooParticleAPI.scheduler.runTask(20) {
                val emitter = ParticleWaveEmitters(barrage.loc, barrage.world).apply {
                    waveSpeed = 0.01
                    waveSize = 5.0
                    maxTick = 1
                    waveAxis = barrage.direction
                    waveCircleCountMin = 50
                    waveCircleCountMax = 150
                    templateData.also {
                        it.effect = ControlableCloudEffect(UUID.randomUUID())
                        it.maxAge = 50
                        it.color = Math3DUtil.colorOf(80, 80, 255)
                    }
                }
                ParticleEmittersManager.spawnEmitters(emitter)
                barrage.options.speed = 1.5
            }
        }
    }

    override fun onRelease(source: LivingEntity, holdingTick: Int) {
    }

    override fun getMaxHoldingTick(holdingEntity: LivingEntity): Int {
        return 0
    }

    override fun holdingTick(holdingEntity: LivingEntity, holdTicks: Int) {
    }

    override fun stopHolding(entity: LivingEntity, holdTicks: Int) {
        onRelease(entity, holdTicks)
    }

    override fun getSkillID(): String {
        return "PlayerSwordLightSkill"
    }

}