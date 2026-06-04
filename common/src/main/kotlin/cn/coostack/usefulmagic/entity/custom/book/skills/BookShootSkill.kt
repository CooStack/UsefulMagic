package cn.coostack.usefulmagic.entity.custom.book.skills

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.display.DisplayEntityManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.display.skills.SkillRangeDisplay
import cn.coostack.usefulmagic.entity.custom.book.MagicBookEntity
import cn.coostack.usefulmagic.particles.emitters.DirectionShootEmitters
import cn.coostack.usefulmagic.particles.composition.skill.BookShootSkillComposition
import cn.coostack.usefulmagic.skill.api.Skill
import cn.coostack.usefulmagic.skill.api.SkillCancelCondition
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

class BookShootSkill(val maxEntityTarget: Int) : Skill<MagicBookEntity>, SkillCancelCondition<MagicBookEntity> {
    override var chance: Double = 0.5
    var tick = 0
    var style: BookShootSkillComposition? = null
    override fun onActive(source: MagicBookEntity) {
        tick = 0
        style = BookShootSkillComposition(source.position(), source.level())
        ParticleCompositionManager.spawn(style!!)
    }

    override fun onRelease(source: MagicBookEntity, holdingTick: Int) {
        source.setAttacking(false)
        source.attackTick = 0
        style?.status?.setStatus(2)
        style = null
    }

    override fun getMaxHoldingTick(holdingEntity: MagicBookEntity): Int {
        return when (holdingEntity.getHealthState()) {
            3 -> 10 * 20
            2 -> 12 * 20
            1 -> 15 * 20
            else -> 10 * 20
        }
    }

    override fun getSkillCountDown(source: MagicBookEntity): Int {
        return when (source.getHealthState()) {
            3 -> 30 * 20
            2 -> 15 * 20
            1 -> 10 * 20
            else -> 30 * 20
        }
    }

    override fun holdingTick(holdingEntity: MagicBookEntity, holdTicks: Int) {
        holdingEntity.setAttacking(true)
        style?.teleportTo(holdingEntity.position())
        val interval = when (holdingEntity.getHealthState()) {
            1 -> 5
            2 -> 10
            3 -> 20
            else -> 20
        }
        if (tick % interval == 0) {
            handleOnceAttack(holdingEntity)
        }
        tick++
    }

    private fun handleOnceAttack(holdingEntity: MagicBookEntity) {
        val world = holdingEntity.level()
        // 寻找周围的实体
        val entities = world.getEntitiesOfClass(
            LivingEntity::class.java,
            AABB.ofSize(holdingEntity.position(), 128.0, 32.0, 128.0)
        ) {
            if (it is Player && (it.isCreative || it.isSpectator)) return@getEntitiesOfClass false
            it.uuid != holdingEntity.uuid && it.type != holdingEntity.type
        }
        repeat(maxEntityTarget) { index ->
            val it = entities.randomOrNull() ?: return@repeat
            entities.remove(it)
            val cube = SkillRangeDisplay(it.position(), world)
                .apply {
                    this.scaled = 4f
                    discardTick = 10
                    biggerTick = 10
                    lifetime = 20
                    color = Math3DUtil.colorOf(255, 140, 10)
                }
            DisplayEntityManager.spawn(cube)
            world.playSound(null, it.x, it.y, it.z, SoundEvents.BEACON_ACTIVATE, SoundSource.VOICE, 5f, 1f)
//            ParticleEmittersManager.spawnEmitters(cube)
            val attackPos = it.position()
            // 延时触发攻击
            CooParticlesAPI.scheduler.runTask(10) {
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
                    shootDirection = Vec3(0.0, 12.0, 0.0)
                    randomSpeedOffset = 0.5
                    gravity = PhysicConstant.EARTH_GRAVITY
                    shootType = EmittersShootTypes.box(HitBox.of(4.0, 1.0, 4.0))
                    maxTick = 20
                }
                ParticleEmittersManager.spawnEmitters(shoot)
                world.getEntitiesOfClass(LivingEntity::class.java, AABB.ofSize(attackPos, 4.0, 20.0, 4.0)) { it ->
                    if (it is Player && (it.isCreative || it.isSpectator)) return@getEntitiesOfClass false
                    it.uuid != holdingEntity.uuid && it.type != holdingEntity.type
                }.forEach { it ->
                    val source = it.damageSources().mobAttack(holdingEntity)
                    it.hurt(source, 15f)
                }
                world.playSound(null, it.x, it.y, it.z, SoundEvents.BREEZE_SHOOT, SoundSource.HOSTILE, 5f, 1f)
            }

        }
    }

    override fun stopHolding(entity: MagicBookEntity, holdTicks: Int) {
        entity.setAttacking(false)
        entity.attackTick = 0
        style?.status?.setStatus(2)
        style = null
    }

    override fun getSkillID(): String {
        return "book-shoot-skill"
    }

    override fun testCancel(entity: MagicBookEntity): Boolean {
        return entity.target == null
    }

    override var canceled: Boolean = false
    override var cancelSetCD: Boolean = true
}
