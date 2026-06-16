package cn.coostack.usefulmagic.entity.custom.dragon.skills

import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.renderer.post.CooPostEffects
import cn.coostack.cooparticlesapi.sound.ServerManagedSoundInstance
import cn.coostack.cooparticlesapi.sound.ServerSoundManager
import cn.coostack.cooparticlesapi.sound.SoundVolumeFalloff
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.effects.asHolder
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.emitters.TrackingTailEmitter
import cn.coostack.usefulmagic.entity.custom.dragon.phases.DragonHoverFlightPhase
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.entity.custom.dragon.skills.emitter.CollectDisplayLineEmitter
import cn.coostack.usefulmagic.entity.custom.dragon.skills.emitter.DragonCollectEmitter
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.serverLevel
import cn.coostack.usefulmagic.renderer.CylinderLaserRenderEntity
import cn.coostack.usefulmagic.renderer.ShotWaveBillboardRenderEntity
import cn.coostack.usefulmagic.renderer.UsefulMagicPostEffects
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.EntityUtil
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random

/**
 * TODO
 *
 * 一个中场面，  释放后会让周围的玩家有60秒的魔力禁止时间
 *
 * 此技能为蓄力技能
 * 会先以spawnPosition为中心释放一个大魔法阵，然后龙会环绕接近（Phase）
 *
 * 最后转换为Hover 技能结束的时候才会转换为Simple
 *
 * 蓄力方案：
 *  - 召唤N个眼睛 （环绕在周围） 旋转速度加快靠拢， 最后放一个激光， 从内部向外直接扩散
 *  - 中心召唤一个较大的魔法阵在龙的下面
 *
 */
class DragonBanMagicSkill : DragonSkill() {
    override var chance: Double = 0.6

    private var emitter: CollectDisplayLineEmitter? = null
    private var sound: ServerManagedSoundInstance? = null
    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 240 * 20
    }

    override fun onActive(source: MagicDragonEntity) {
        source.phaseManager.forceSetPhase(
            DragonHoverFlightPhase()
        ) {
            setCurrentTarget(source.spawnPosition)
        }
    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {
        // 这里要进行 explode 然后给128范围内的所有实体设置 1分钟的 禁魔
        val world = source.serverLevel!!
        val start = source.boxCenterPosition().add(0.0, 256.0, 0.0)
        val end = source.boxCenterPosition().add(0.0, -256.0, 0.0)
        CylinderLaserRenderEntity.spawn(
            world, start, end, 15, 0,
            maxRadius = 120f,
            color = Math3DUtil.colorOf(255, 150, 240).asVec3(),
            shrinkOnFadeOut = false,
        ).apply {
            brightness = 0.4f
        }

        playDragonSoundOnce(
            source,
            UsefulMagicSoundEvents.DRAGON_MAGIC_BAN.get(),
            SoundSource.HOSTILE,
            1.5f,
            1f,
            256.0,
        )

        world.getEntitiesOfClass(LivingEntity::class.java, source.boundingBox.inflate(256.0), EntityUtil.filterDragon).forEach {
            it.hurt(UsefulMagicDamageSources.entityDamage(it.level(), source, source), 10f)
            it.addEffect(MobEffectInstance(UsefulMagicEffects.MAGIC_SEALED.asHolder(), 20 * 60))
            if (it is ServerPlayer) {
                // 发送震动和RGB分离
                // Shake
                ServerCameraUtil.sendShake(world.serverLevel!!, source.boxCenterPosition(), 256.0, 6.2, 40, 5.0)
                CooPostEffects.server.send(it, UsefulMagicPostEffects.flameExplodeFlash(10, 2f, 0.85f))
                CooPostEffects.server.send(it, UsefulMagicPostEffects.rgbDashBlur(20, 2f, 2f))
            }
        }
        // play particles
        // 吸收collect， 然后做几个收缩淡入淡出效果 最后激光爆炸撑开
        clear(source)
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        return 20 * 12
    }

    override fun holdingTick(holdingEntity: MagicDragonEntity, holdTicks: Int) {
        emitter?.teleportTo(holdingEntity.boxCenterPosition())
        if (holdTicks < 20 * 2) {
            return
        }
        if (sound == null && holdTicks <= 20 * 9) {
            sound = ServerSoundManager.instance(
                UsefulMagicSoundEvents.LASER_LOOP.get(),
                SoundSource.HOSTILE
            )
                .volume(0.4f)
                .visibleRange(256.0)
                .pitch(0.2f)
                .syncEveryTick(true)
                .volumeFalloff(SoundVolumeFalloff.LINEAR)
                .layer("dragon_ban_charging")
                .uniqueKey()
                .bindToEntity(holdingEntity)
                .looping()
                .spawn()
        }
        if (emitter == null && holdTicks <= 20 * 9) {
            emitter =
                CollectDisplayLineEmitter(holdingEntity.boxCenterPosition(), holdingEntity.serverLevel!!).apply {
                    simpleData.apply {
                        this.minSpeed = 1.2
                        this.maxSpeed = 1.8
                        this.minCount = 3
                        this.maxCount = 5
                    }
                    this.color = Math3DUtil.colorOf(250, 236, 254)
                    this.radius = 72.0
                    this.maxTick = -1
                    ParticleEmittersManager.spawnEmitters(this)
                }
        }
        if (holdTicks % 5 == 0) {
            // 这里召唤一个内敛的球 作为收缩
            // 或者是一个缩小的一个能量脉冲 billboard
            ShotWaveBillboardRenderEntity.spawn(
                holdingEntity.serverLevel!!, holdingEntity.boxCenterPosition(), 5, 5,
                limitScale = 0.5f,
                timeoutTick = 60,
                scaleSpeed = -16f,
                roll = Random.nextFloat(),
                alpha = 0.1,
                initialScale = 120F,
                useWave2Texture = false
            )
        }
        ServerCameraUtil.sendShake(
            holdingEntity.serverLevel!!, holdingEntity.boxCenterPosition(), 256.0, 0.5, 10, 2.0
        )
        if (holdTicks > 20 * 9) {
            sound?.fadeOut(10)
            sound = null
            emitter?.remove()
            emitter = null
            return
        }

        sound?.pitchMultiplier = (2.0 * (holdTicks - 40) / 140).toFloat()
        if (holdTicks % 4 == 0) {
            repeat(Random.nextInt(2, 3)) {
                // 十秒的冷却， 这里是冷却动画
                DragonCollectEmitter(
                    holdingEntity.boxCenterPosition().offsetRandomly(Random.nextDouble(12.0, 32.0)),
                    holdingEntity.level()
                ).apply {
                    collectTarget = holdingEntity.boxCenterPosition()
                    strength = 1.5
                    particleDefaultVelocity = Vec3.ZERO.random()
                    randomData.apply {
                        minAge = 40
                        maxAge = 80
                        minSize = 0.2
                        maxSize = 0.4
                        minSpeed = 3.0
                        maxSpeed = 6.0
                    }
                    template.apply {
                        setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                    }
                    colorLeft = Math3DUtil.colorOf(190, 61, 237)
                    colorRight = Math3DUtil.colorOf(250, 236, 254)
                    collectNoiseOffset = Random.nextDouble(1.2, 2.3)
                    maxTick = Random.nextInt(20, 40)
                    ParticleEmittersManager.spawnEmitters(this)
                }
            }

            val randomCount = (2 minRangeTo 5).random()
            val color = Math3DUtil.colorOf(255, 150, 240)
            repeat(randomCount) {
                val targetPos = holdingEntity.boxCenterPosition()
                val randomPos = targetPos.offsetRandomly(Random.nextDouble(16.0, 64.0))
                val emitter = TrackingTailEmitter(randomPos, holdingEntity.level()).apply {
                    trackingTarget = targetPos
                    this.arriveCanceled = true
                    particleConfig.apply {
                        this.color = color
                        this.visibleRange = 256f
                        this.setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                    }
                    this.simpleConfig.apply {
                        this.minAge = 5
                        this.maxAge = 9
                        this.minCount = 3
                        this.maxCount = 8
                        this.minSize = 0.1
                        this.maxSize = 0.3
                        this.minSpeed = 2.5
                        this.maxSpeed = 4.6
                    }

                    this.noiseStrength = 0.06
                    this.offsetNoise = 0.5
                    this.strength = 3.0
                    this.velocity = Vec3.ZERO.random()
                }
                ParticleEmittersManager.spawnEmitters(emitter)
            }
        }


//        if (holdTicks > 20 * 5) {
//            sealedTarget?.takeIf { it.isAlive && it.distanceToSqr(holdingEntity) <= 16.0 * 16.0 }?.let {
//                it.hurt(it.damageSources().mobAttack(holdingEntity), 8f)
//                it.deltaMovement = holdingEntity.createImpactVelocity(holdingEntity.getBreathAimDirection(), 0.35, 0.8)
//                it.hurtMarked = true
//                stopHolding(holdingEntity, holdTicks)
//            }
//        }
    }

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
        clear(entity)
    }

    override fun getSkillID(): String {
        return "dragon_ban_magic_skill"
    }

    private fun clear(entity: MagicDragonEntity) {
        entity.phaseManager.resetDefaultPhase()
        emitter?.remove()
        emitter = null
        sound?.fadeOut(10)
        sound = null
    }


    override fun canTrigger(entity: MagicDragonEntity): Boolean {
        return super.canTrigger(entity) && entity.health <= entity.dragonMaxHealth * 0.6
    }

    override fun testCancel(entity: MagicDragonEntity): Boolean {
        return !(!entity.entityDeath && !entity.isDizzying() && !UsefulMagicEffects.isMagicSealed(entity))
    }
}
