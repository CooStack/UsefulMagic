package cn.coostack.usefulmagic.particles.barrages.skill

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.ServerControler
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.SimpleParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.particles.barrages.PlayerDamagedBarrage
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.style.skill.GiantSwordStyle
import cn.coostack.usefulmagic.particles.style.skill.SwordLightStyle
import cn.coostack.usefulmagic.utils.FallingBlockHelper
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import kotlin.random.Random

class GiantSwordLightBarrage(
    val controlStyle: SwordLightStyle?,
    val entityID: Int,
    loc: Vec3d,
    world: ServerWorld,
    options: BarrageOption,
    damage: Double,
    shooter: PlayerEntity
) : PlayerDamagedBarrage(loc, world, HitBox.of(12.0, 12.0, 12.0), GiantSwordStyle(), options, damage, shooter) {
    val countDouble: Int
        get() = ParticleOption.getParticleCounts()

    val tailEffect = SimpleParticleEmitters(loc, world, ControlableParticleData().apply {
        this.effect = ControlableCloudEffect(this.uuid)
        maxAge = 30
        speed = 0.05
        size = 0.3f
    }).apply {
        maxTick = -1
        this.count = 10
    }
    var tick = 0
    override fun tick() {
        if (options.speed > 0.8) {
            if (tick == 0) {
                ParticleEmittersManager.spawnEmitters(tailEffect)
            }
            tick++
        }
        super.tick()
        tailEffect.teleportTo(loc)
        bindControl as GiantSwordStyle
        bindControl.rotateParticlesToPoint(RelativeLocation.of(direction))
        val entity = world.getEntityById(entityID) ?: return
        direction = direction.add(loc.relativize(entity.eyePos.add(0.0, -0.5, 0.0)).normalize()).normalize()
    }

    override fun onHitDamaged(result: BarrageHitResult) {
        val random = Random(System.currentTimeMillis())
        controlStyle?.status?.setStatus(2)
        shooter ?: return
        result.entities.forEach {
            it.timeUntilRegen = 0
            it.hurtTime = 0
        }
        val blocks = FallingBlockHelper.getBoxIncludeBlockPosList(
            Box.of(loc, 8.0, 1.0, 8.0), world
        )

        FallingBlockHelper.conversionBlockToFallingBlocks(
            blocks, false, world
        ).forEach {
            val direction = loc.relativize(it.pos).multiply(1.0 / 9)
            it.velocity = direction.add(
                random.nextDouble(-0.5, 0.5),
                random.nextDouble(0.2, 0.5),
                random.nextDouble(-0.5, 0.5)
            )
        }
        world.playSound(
            null, loc.x, loc.y, loc.z,
            SoundEvents.ENTITY_GENERIC_EXPLODE, shooter!!.soundCategory, 10f, 2f
        )
        world.playSound(
            null, loc.x, loc.y, loc.z,
            SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, shooter!!.soundCategory, 10f, 2f
        )
        val explosion = ExplodeMagicEmitters(loc.add(direction.normalize().multiply(5.0)), world).apply {
            this.templateData.also {
                it.color = Math3DUtil.colorOf(255, 255, 255)
                it.effect = ControlableCloudEffect(it.uuid)
                it.size = 0.2f
            }
            randomParticleAgeMin = 30
            randomParticleAgeMax = 80
            precentDrag = 0.95
            maxTick = 5
            ballCountPow = countDouble * 10
            minSpeed = 0.5
            maxSpeed = 7.0
            randomCountMin = 100 * countDouble
            randomCountMax = 200 * countDouble
        }
        tailEffect.cancelled = true
        ParticleEmittersManager.spawnEmitters(explosion)
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid
    }
}