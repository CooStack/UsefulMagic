package cn.coostack.usefulmagic.skill.player

import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.particles.emitters.DirectionShootEmitters
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.utils.ComboUtil
import cn.coostack.usefulmagic.utils.FallingBlockHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import kotlin.random.Random

class HeavyHitSkill(val damage: Float) : Skill, ComboCondition {
    override var chance: Double = 1.0

    override fun getSkillCountDown(source: LivingEntity): Int {
        return 0
    }

    override fun onActive(source: LivingEntity) {
    }

    override fun onRelease(source: LivingEntity, holdingTick: Int) {
        if (!canTrigger(source)) {
            return
        }
        val state = ComboUtil.getComboState(source.uuid)
        val combo = state.count
        val hitBox = HitBox.of(
            combo * 2.0, combo * 2.0, combo * 2.0
        )

        val blockPosList = FallingBlockHelper.getBoxIncludeBlockPosList(
            Box.of(
                source.pos.add(0.0, -1.0, 0.0),
                combo * 1.0, 1.0, combo * 1.0
            ), source.world
        )
        val random = Random(System.currentTimeMillis())
        val entities = FallingBlockHelper.conversionBlockToFallingBlocks(blockPosList, false, source.world)
        entities.forEach {
            it.dropItem = true
            val v = it.pos.distanceTo(source.pos) / (combo * 1.5)
            it.velocity = Vec3d(
                random.nextDouble(
                    -0.5, 0.5
                ), v, random.nextDouble(
                    -0.5, 0.5
                )
            )
            it.velocityModified = true
        }
        source as PlayerEntity
        source.velocity = Vec3d(0.0, combo * 0.5 + 0.2, 0.0)
        source.velocityModified = true
        val emitter = DirectionShootEmitters(source.pos, source.world)
            .apply {
                maxTick = 4
                shootDirection = Vec3d(0.0, 1.0, 0.0)
                randomX = 0.2
                randomZ = 0.2
                randomY = 0.2
                speedDrag = 0.98
                count = 10 * combo
                shootType = EmittersShootTypes.box(
                    hitBox
                )
                gravity = PhysicConstant.EARTH_GRAVITY
                templateData.apply {
                    this.speed = 0.5
                    this.maxAge = 25
                    this.effect = ControlableCloudEffect(uuid)
                }
            }
        ParticleEmittersManager.spawnEmitters(emitter)

        ServerCameraUtil.sendShake(
            source.world as ServerWorld,
            source.pos,
            32.0,
            0.3,
            5
        )

        val box = hitBox.ofBox(
            source.pos
        )
        source.world.getEntitiesByClass(
            LivingEntity::class.java,
            box
        ) {
            it.uuid != source.uuid
        }.forEach {
            val attack = it.damageSources.playerAttack(source as PlayerEntity)
            it.damage(attack, damage * combo / 3)
            val len = it.distanceTo(source).coerceAtLeast(0.1f)
            val hitStrength = combo.toDouble() / (len * 2)
            val rel = source.pos.relativize(it.eyePos).normalize().multiply(hitStrength)
            it.velocity = rel
            it.velocityModified = true
        }
        source.world.playSound(
            null, source.x, source.y, source.z,
            SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, SoundCategory.PLAYERS, 5f, 1.2f
        )
        state.reset()
    }

    override fun getMaxHoldingTick(holdingEntity: LivingEntity): Int {
        return 0
    }

    override fun holdingTick(holdingEntity: LivingEntity, holdTicks: Int) {

    }

    override fun stopHolding(entity: LivingEntity, holdTicks: Int) {
    }

    override fun getSkillID(): String {
        return "HeavyHitSkill"
    }

    override val triggerComboMin: Int
        get() = 3

}