package cn.coostack.usefulmagic.entity.custom.skills

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.SimpleParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.particles.emitters.DirectionShootEmitters
import cn.coostack.usefulmagic.particles.style.skill.BookShootSkillStyle
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import net.fabricmc.loader.impl.lib.sat4j.core.Vec
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

class BookShootSkill(val maxEntityTarget: Int) : Skill, SkillCancelCondition {
    override var chance: Double = 0.5
    var tick = 0
    var style: BookShootSkillStyle? = null
    override fun onActive(source: LivingEntity) {
        tick = 0
        style = BookShootSkillStyle()
        ParticleStyleManager.spawnStyle(source.world, source.pos, style!!)
    }

    override fun onRelease(source: LivingEntity, holdingTick: Int) {
        if (source is MagicBookEntity) {
            source.isAttacking = false
            source.attackTick = 0
        }
        style?.status?.setStatus(2)
        style = null
    }

    override fun getMaxHoldingTick(holdingEntity: LivingEntity): Int {
        if (holdingEntity !is MagicBookEntity) return 10 * 20
        return when (holdingEntity.getHealthState()) {
            3 -> 10 * 20
            2 -> 12 * 20
            1 -> 15 * 20
            else -> 10 * 20
        }
    }

    override fun getSkillCountDown(source: LivingEntity): Int {
        if (source !is MagicBookEntity) return 30 * 20
        return when (source.getHealthState()) {
            3 -> 30 * 20
            2 -> 15 * 20
            1 -> 10 * 20
            else -> 30 * 20
        }
    }

    override fun holdingTick(holdingEntity: LivingEntity, holdTicks: Int) {
        if (holdingEntity is MagicBookEntity) {
            holdingEntity.isAttacking = true
        }
        style?.teleportTo(holdingEntity.pos)
        var interval = 20
        if (holdingEntity is MagicBookEntity) {
            interval = when (holdingEntity.getHealthState()) {
                1 -> 5
                2 -> 10
                3 -> 20
                else -> 20
            }
        }
        if (tick % interval == 0) {
            handleOnceAttack(holdingEntity)
        }
        tick++
    }

    private fun handleOnceAttack(holdingEntity: LivingEntity) {
        val world = holdingEntity.world
        // 寻找周围的实体
        val entities = world.getEntitiesByClass(
            LivingEntity::class.java,
            Box.of(holdingEntity.pos, 128.0, 32.0, 128.0)
        ) {
            if (it !is MobEntity && it !is PlayerEntity) return@getEntitiesByClass false
            if (holdingEntity is MobEntity) {
                holdingEntity.target?.uuid == it.uuid || it.type != holdingEntity.type
            } else it.type != holdingEntity.type
        }
        repeat(maxEntityTarget) { index ->
            val it = entities.randomOrNull() ?: return@repeat
            entities.remove(it)
            // 设置方形攻击范围
            val cube = SimpleParticleEmitters(
                it.pos, world, ControlableParticleData()
                    .apply {
                        color = Math3DUtil.colorOf(255, 140, 10)
                        speed = 0.0
                        maxAge = 10
                    }
            ).apply {
                count = 30
                shootType = EmittersShootTypes.box(HitBox.of(4.0, 1.0, 4.0))
                maxTick = 10
            }
            world.playSound(null, it.x, it.y, it.z, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.VOICE, 5f, 1f)
            ParticleEmittersManager.spawnEmitters(cube)
            val attackPos = it.pos
            // 延时触发攻击
            CooParticleAPI.scheduler.runTask(10) {
                // 向上喷射的粒子
                val shoot = DirectionShootEmitters(attackPos, world).apply {
                    templateData.also { it ->
                        it.maxAge = 10
                        it.speed = 1.5
                        it.effect = ControlableCloudEffect(it.uuid)
                    }
                    count = 50
                    randomX = 4.0
                    randomY = 8.0
                    randomZ = 4.0
                    shootDirection = Vec3d(0.0, 12.0, 0.0)
                    randomSpeedOffset = 0.5
                    gravity = PhysicConstant.EARTH_GRAVITY
                    shootType = EmittersShootTypes.box(HitBox.of(4.0, 1.0, 4.0))
                    maxTick = 20
                }
                ParticleEmittersManager.spawnEmitters(shoot)
                world.getEntitiesByClass(LivingEntity::class.java, Box.of(attackPos, 4.0, 20.0, 4.0)) { it ->
                    if (it !is PlayerEntity && it !is MobEntity) return@getEntitiesByClass false
                    if (holdingEntity is MobEntity) {
                        holdingEntity.target?.uuid == it.uuid || it.type != holdingEntity.type
                    } else it.type != holdingEntity.type
                }.forEach { it ->
                    val source = it.damageSources.mobAttack(holdingEntity)
                    it.damage(source, 15f)
                }
                world.playSound(null, it.x, it.y, it.z, SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.VOICE, 5f, 1f)
            }

        }
    }

    override fun stopHolding(entity: LivingEntity, holdTicks: Int) {
        if (entity is MagicBookEntity) {
            entity.isAttacking = false
            entity.attackTick = 0
        }
        style?.status?.setStatus(2)
        style = null
    }

    override fun getSkillID(): String {
        return "book-shoot-skill"
    }

    override fun testCancel(entity: LivingEntity): Boolean {
        if (entity !is MobEntity) return false
        return entity.target == null
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = true
}