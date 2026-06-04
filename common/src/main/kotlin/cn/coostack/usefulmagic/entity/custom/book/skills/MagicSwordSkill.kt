package cn.coostack.usefulmagic.entity.custom.book.skills

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.barrages.entity.MagicBookSwordBarrageMagic
import cn.coostack.usefulmagic.entity.custom.book.MagicBookEntity
import cn.coostack.usefulmagic.particles.emitters.DirectionShootEmitters
import cn.coostack.usefulmagic.particles.emitters.ParticleWaveEmitters
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import cn.coostack.usefulmagic.skill.api.SkillCondition
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import kotlin.math.PI

class MagicSwordSkill : Skill<MagicBookEntity>, SkillCondition<MagicBookEntity>, SkillCancelCondition<MagicBookEntity> {
    override var chance: Double = 1.0
    override fun getSkillCountDown(source: MagicBookEntity): Int {
        return when (source.getHealthState()) {
            3 -> 30 * 20
            2 -> 25 * 20
            1 -> 15 * 20
            else -> 30 * 20
        }
    }

    val barrages = HashMap<MagicBookSwordBarrageMagic, RelativeLocation>()
    override fun onActive(source: MagicBookEntity) {
        barrages.clear()
        // 生成barrages
        PointsBuilder()
            .addPolygonInCircleVertices(3, 9.0)
            .pointsOnEach { it.y += 2.0 }
            .addPolygonInCircleVertices(3, 3.0)
            .pointsOnEach { it.y += 2.0 }
            .addPolygonInCircleVertices(3, 6.0)
            .pointsOnEach { it.y += 2.0 }
            .create()
            .forEach {
                val barrage = MagicBookSwordBarrageMagic(
                    source.position().add(it.toVector()),
                    source.level() as ServerLevel,
                    10.0, 0.0, source
                ).also { it -> it.direction = RelativeLocation.yAxis().toVector() }
                BarrageManager.spawn(barrage)
                barrages[barrage] = it
            }
    }

    override fun onRelease(source: MagicBookEntity, holdingTick: Int) {
        source.setAttacking(false)
        source.attackTick = 0
        val target = source.target ?: return
        var maxLivingTickOrigin = holdingTick
        source.level().playSound(
            null,
            source.x,
            source.y,
            source.z,
            SoundEvents.END_PORTAL_SPAWN,
            SoundSource.HOSTILE,
            10f,
            2f
        )
        ServerCameraUtil.sendShake(source.level() as ServerLevel, source.position(), 64.0, 0.5, 10)
        val shoot = DirectionShootEmitters(source.position(), source.level()).apply {
            templateData.also { it ->
                it.maxAge = 30
                it.speed = 0.0
                it.effect = ControlableCloudEffect(it.uuid)
            }
            count = 150
            randomX = 0.3
            randomZ = 0.3
            randomSpeedOffset = 0.3
            gravity = PhysicConstant.EARTH_GRAVITY
            shootType = EmittersShootTypes.box(HitBox.of(16.0, 16.0, 16.0))
            maxTick = 5
        }

        ParticleEmittersManager.spawnEmitters(shoot)
        val iterator = barrages.iterator()
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(5, barrages.size * 5) {
            if (!iterator.hasNext()) {
                return@runTaskTimerMaxTick
            }
            val entry = iterator.next()
            iterator.remove()
            val it = entry.key
            it.options.maxLivingTick = maxLivingTickOrigin + 120
            it.options.noneHitBoxTick = 0
            it.options.speed = 0.01
            it.options.acceleration = 0.005
            it.options.accelerationMaxSpeedEnabled = true
            it.options.accelerationMaxSpeed = 2.0
            // 设置朝向
            it.target = target
            it.direction = it.loc.relativize(target.position())
            it.world.playSound(
                null,
                it.loc.x,
                it.loc.y,
                it.loc.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE,
                10f,
                1.5f
            )
            val emitter = ParticleWaveEmitters(it.loc, it.world).apply {
                waveSpeed = 0.01
                waveSize = 1.0
                maxTick = 1
                waveAxis = it.direction
                waveCircleCountMin = 10
                waveCircleCountMax = 30
                templateData.also {
                    it.effect = ControlableCloudEffect(UUID.randomUUID())
                    it.maxAge = 30
                    it.color = Math3DUtil.colorOf(80, 80, 255)
                }
            }
            ParticleEmittersManager.spawnEmitters(emitter)
            maxLivingTickOrigin += 5
        }
    }

    override fun getMaxHoldingTick(holdingEntity: MagicBookEntity): Int {
        return when (holdingEntity.getHealthState()) {
            3 -> 6 * 20
            2 -> 4 * 20
            1 -> 2 * 20
            else -> 6 * 20
        }
    }

    override fun holdingTick(holdingEntity: MagicBookEntity, holdTicks: Int) {
        holdingEntity.setAttacking(true)
        // 设置barrages的location旋转
        Math3DUtil.rotateAsAxis(
            barrages.values.toList(),
            RelativeLocation.yAxis(),
            PI / 32
        )
        val iterator = barrages.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
            val barrage = it.key
            barrage.loc = holdingEntity.position().add(it.value.toVector())
            if (!barrage.valid) {
                iterator.remove()
            }
        }
    }

    override fun stopHolding(entity: MagicBookEntity, holdTicks: Int) {
        entity.setAttacking(false)
        entity.attackTick = 0
        barrages.onEach {
            it.key.remove()
        }.clear()
    }

    override fun getSkillID(): String {
        return "magic-sword-skill"
    }

    override fun canTrigger(entity: MagicBookEntity): Boolean {
        return entity.target != null
    }

    override fun testCancel(entity: MagicBookEntity): Boolean {
        return !canTrigger(entity)
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = false
}
