package cn.coostack.usefulmagic.entity.custom.dragon.skills

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.data.IntRangeData
import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.supports.sound.ServerManagedSoundInstance
import cn.coostack.cooparticlesapi.supports.sound.ServerSoundManager
import cn.coostack.cooparticlesapi.supports.sound.SoundVolumeFalloff
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.PhysicsUtil
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.emitters.TrackingTailEmitter
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicSubEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.phases.DragonHoverFlightPhase
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.entity.custom.dragon.skills.barrage.DragonTrackedBarrage
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.searchEntities
import cn.coostack.usefulmagic.extend.searchLivingEntities
import cn.coostack.usefulmagic.extend.serverLevel
import cn.coostack.usefulmagic.meteorite.MeteoriteBarrage
import cn.coostack.usefulmagic.meteorite.MeteoriteDisplay
import cn.coostack.usefulmagic.particles.composition.magic.attack.MeteoriteChargingComposition
import cn.coostack.usefulmagic.particles.emitters.magic.MeteoriteExplosionEmitter
import cn.coostack.usefulmagic.particles.emitters.magic.MeteoriteShockwaveEmitter
import cn.coostack.usefulmagic.particles.emitters.meteorite.MeteoriteTailEmitter
import cn.coostack.usefulmagic.renderer.MeteoriteAtmosphereFireRenderEntity
import cn.coostack.usefulmagic.renderer.UsefulMagicPostEffects
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.EntityUtil
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * 这里需要一个魔法阵
 * 一个粒子轨迹 （复用）
 * 爆炸特效 （复用）
 *
 * 爆炸后会放出很多小石头粒子 发射向玩家
 *
 * 然后用来展现出陨石特效的
 *
 * @constructor 创建陨石技能。
 */
class DragonMeteoriteSkill : DragonSkill() {
    companion object {
        const val ID = "dragon_meteorite_skill"
        private const val ARRIVE_GRACE_TICKS = 80
        private const val MERGE_TICKS = 100
        private const val RELEASE_WAIT_TICKS = 80
        private const val TARGET_SIZE = 16.0
        private const val SUB_METEORITES_PER_WAVE = 2
    }

    override var chance: Double = 100.0
    private var tick = 0
    private var arrive = false
    private var released = false
    private var mainMeteorite: MeteoriteBarrage? = null
    private var mainMeteoriteWorld: ServerLevel? = null
    private var mainTailEmitter: MeteoriteTailEmitter? = null
    private var mainMeteoriteRenderer: MeteoriteAtmosphereFireRenderEntity? = null

    // 使用玩家的composition
    private var chargingComposition: MeteoriteChargingComposition? = null
    private var loopSound: ServerManagedSoundInstance? = null

    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 20 * 72000
    }

    override fun onActive(source: MagicDragonEntity) {
        clearStatus()
        source.phaseManager.forceSetPhase(
            DragonHoverFlightPhase()
        ) {
            setCurrentTarget(source.spawnPosition + Vec3(0.0, 15.0, 0.0))
            listenContinue {
                if (!arrive && isArriveCurrentTarget(source, 2.0)) {
                    arrive = true
                    spawnMainBarrage(source)
                }
            }
        }
        chargingComposition = MeteoriteChargingComposition(source.boxCenterPosition(), source.level()).apply {
            ParticleCompositionManager.spawn(this)
        }
    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {
        releaseMeteorite(source)
        source.phaseManager.resetDefaultPhase()
        clearStatus(removeMeteorite = false)
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        return ARRIVE_GRACE_TICKS + MERGE_TICKS + RELEASE_WAIT_TICKS
    }

    override fun holdingTick(
        holdingEntity: MagicDragonEntity,
        holdTicks: Int
    ) {
        chargingComposition?.teleportTo(holdingEntity.boxCenterPosition())
        // 陨石的蓄力 （变大）
        if (tick++ < ARRIVE_GRACE_TICKS && !arrive) {
            return
        }
        if (mainMeteorite == null) {
            spawnMainBarrage(holdingEntity)
        }
        // 先召唤主陨石
        // 然后召唤次陨石去吸
        // 吸到 max - 40 后等待， 然后用release发射出去
        if (tick >= getMaxHoldingTick(holdingEntity) - RELEASE_WAIT_TICKS) {
            return
        }
        if (tick % 2 != 0) {
            return
        }

        repeat(SUB_METEORITES_PER_WAVE) {
            spawnSubMeteorite(holdingEntity)
        }
        if (tick % 8 != 0) {
            return
        }
        val randomCount = (2 minRangeTo 5).random()
        val color = Math3DUtil.colorOf(255, 150, 130)
        repeat(randomCount) {
            val targetPos = mainMeteorite!!.loc
            val randomPos = targetPos.offsetRandomly(Random.nextDouble(16.0, 64.0))
            val emitter = TrackingTailEmitter(randomPos, mainMeteorite!!.world).apply {
                trackingTarget = targetPos
                this.arriveCanceled = true
                particleConfig.apply {
                    this.color = color
                    this.visibleRange = 256F
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

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
        clearStatus()
    }

    override fun getSkillID(): String {
        return ID
    }

    private fun spawnMainBarrage(source: MagicDragonEntity) {
        val world = source.serverLevel ?: return
        val loc = source.position() + Vec3(0.0, 24.0, 0.0)
        // 播放循环音效。
        submitTaskServer(20) {
            loopSound = ServerSoundManager.instance(
                UsefulMagicSoundEvents.ROCK_LOOP.get(),
                SoundSource.HOSTILE
            )
                .entity(source)
                .layer("meteorite_rock")
                .volume(0F)
                .pitch(1F)
                .visibleRange(256.0)
                .volumeFalloff(SoundVolumeFalloff.LINEAR)
                .syncEveryTick(true)
                .relative()
                .lifetime(-1)
                .looping()
                .spawn().apply {
                    fadeTo(1F, 20)
                }
        }
        mainMeteoriteWorld = world
        mainMeteorite = MeteoriteBarrage(
            loc,
            world,
            MeteoriteDisplay(loc, world).apply {
                state = Blocks.MAGMA_BLOCK.defaultBlockState()
                scale = 0F
                prevScale = 0F
            },
            BarrageOption()
                .noneHitBoxTick(5)
                .speed(0.0)
                .acrossBlock()
                .acrossLiquid()
                .acrossable()
                .maxAcrossCount(1145141919)
        ).ignore(MagicDragonEntity::class.java)
            .ignore(MagicEyeEntity::class.java)
            .ignore(MagicSubEyeEntity::class.java)
            .apply {
                targetSize.targetNum = 0.2 // 先很小
                targetSize.speed = 0.2
                freeze = true
                shooter = source
                BarrageManager.spawn(this)
            }
    }

    private fun spawnSubMeteorite(source: MagicDragonEntity) {
        val mainBarrage = mainMeteorite ?: return
        val world = source.serverLevel ?: return
        val spawnOffset = Vec3.ZERO.random() *
            Random.nextDouble(
                TARGET_SIZE + 24.0,
                TARGET_SIZE + 48.0
            )
        val spawnPos = mainBarrage.loc + spawnOffset
        val mergeIncrease = TARGET_SIZE / (MERGE_TICKS * SUB_METEORITES_PER_WAVE)
        val subMeteorite = MeteoriteBarrage(
            spawnPos,
            world,
            MeteoriteDisplay(spawnPos, world).apply {
                state = Blocks.NETHERRACK.defaultBlockState()
                scale = 0F
                prevScale = 0F
            },
            BarrageOption()
                .acrossBlock(true)
                .acrossLiquid(true)
                .acrossable(true)
        ).apply {
            mergeSign = true
            targetSize.targetNum = mergeIncrease + 1.0
            addPreTickAction {
                val next = PhysicsUtil.nextAttractVelocityNullable(
                    loc,
                    direction.normalize() * options.speed,
                    mainBarrage.loc,
                    falloffPow = 1,
                    strength = 5.0,
                    maxSpeed = 12.0,
                    arriveRadius = mainBarrage.targetSize.targetNum.toDouble()
                ) ?: let {
                    remove()
                    mainBarrage.mergeCount--
                    mainBarrage.targetSize.targetNum = mainBarrage.targetSize.targetNum.toDouble() + mergeIncrease
                    return@addPreTickAction
                }
                direction = next.normalize()
                options.speed(next.length())
            }
        }
        BarrageManager.spawn(subMeteorite)
        mainBarrage.mergeCount++
    }

    private fun releaseMeteorite(source: MagicDragonEntity) {
        if (released) return
        val world = source.serverLevel ?: return
        val mainBarrage = mainMeteorite ?: return
        val target = source.getCombatTarget()?.position() ?: source.spawnPosition
        val dir = (target - mainBarrage.loc).normalize()
        mainBarrage.direction = dir.withY { -y.absoluteValue } // 避免飞天陨石
        mainBarrage.options.enableSpeedWithOptions(2.8)
            .acrossBlock(false)
            .noneHitBoxTick(0)
            .maxLivingTick(20 * 6)
        mainBarrage.freeze = false
        mainMeteoriteRenderer = MeteoriteAtmosphereFireRenderEntity.spawn(
            world, mainBarrage.loc, mainBarrage.direction, mainBarrage.targetSize.targetNum.toFloat(),
            lifetime = 20 * 20
        )
        var farFlySoundStarted = false
        var nearFlySoundPlayed = false
        fun stopFarFlySound(soundPos: Vec3) {
            if (!farFlySoundStarted) return
            stopMeteorFarFlySound(world, soundPos)
            farFlySoundStarted = false
        }

        val tailEmitter = bindMainMeteoriteTrail(world, mainBarrage, dir)
        playMeteorFarFlySound(world, mainBarrage.loc)
        farFlySoundStarted = true
        mainBarrage.addHitOnServer {
            val fallDir = if (direction.lengthSqr() <= 1e-6) dir else direction.normalize()
            val explosionCenter = loc + fallDir * (mainBarrage.targetSize.targetNum.toDouble() * 1.25)
            tailEmitter.canceled = true
            mainMeteoriteRenderer?.discard(12)
            mainMeteoriteRenderer = null
            stopFarFlySound(loc)
            playMeteorImpactSound(world, loc)
            explosion(source, this)
            applyMeteoriteEntityDamage(
                source = source,
                center = explosionCenter,
                radius = (TARGET_SIZE * 2.0).coerceAtLeast(1.0),
                fullDamageRadius = TARGET_SIZE,
                spellDamage = 32F
            )
            mainMeteoriteRenderer?.discard()
            remove()
        }.addPreTickAction {
            mainMeteoriteRenderer?.moveTo(loc, direction)
        }
        mainBarrage.addPreTickAction {
            if (nearFlySoundPlayed) return@addPreTickAction
            if (loc.distanceTo(target) > 12.0) return@addPreTickAction
            stopFarFlySound(loc)
            playMeteorNearFlySound(world, loc)
            nearFlySoundPlayed = true
        }
        released = true
    }

    private fun bindMainMeteoriteTrail(
        world: ServerLevel,
        mainBarrage: MeteoriteBarrage,
        fallbackDirection: Vec3
    ): MeteoriteTailEmitter {
        val tailEmitter = MeteoriteTailEmitter(mainBarrage.loc, world).apply {
            radius = mainBarrage.targetSize.targetNum.toDouble().coerceAtLeast(1.0)
            direction = -fallbackDirection
        }
        mainTailEmitter = tailEmitter
        ParticleEmittersManager.spawnEmitters(tailEmitter)
        mainBarrage.addPreTickAction {
            val fallDir = if (direction.lengthSqr() <= 1e-6) {
                fallbackDirection
            } else {
                direction.normalize()
            }
            tailEmitter.teleportTo(loc)
            tailEmitter.radius = targetSize.targetNum.toDouble().coerceAtLeast(1.0)
            tailEmitter.direction = -fallDir
            tailEmitter.markDirty()
            ServerCameraUtil.sendShake(world, loc, 256.0, 4.0, 10, 100.0, true)
        }
        return tailEmitter
    }

    private fun cancelMainTailEmitter() {
        mainTailEmitter?.canceled = true
        mainTailEmitter = null
    }

    private fun explosion(source: MagicDragonEntity, meteorite: MeteoriteBarrage) {
        source.level().getEntitiesOfClass(Player::class.java, source.boundingBox.inflate(256.0)).forEach {
            UsefulMagicPostEffects.playFlameExplodeFlash(
                it as ServerPlayer,
                30,
                1F,
                1F,
            )
        }
        // 召唤多个小陨石 + 粒子
        splitChildBarrage(meteorite.world, meteorite.loc, source, 30 minRangeTo 48, 8.0)

        repeat(Random.nextInt(12, 18)) {
            // 小陨石
            val smallMeteorite = MeteoriteBarrage(
                meteorite.loc, meteorite.world,
                MeteoriteDisplay(Vec3.ZERO, null).apply {
                    this.scale = 4F
                },
                BarrageOption().enableSpeedWithOptions(3.5)
            ).apply {
                direction = Vec3(0.0, 1.0, 0.0).offsetRandomlyHorizontal(Random.nextDouble(3.0))
                targetSize.targetNum = 4
                BarrageManager.spawn(this)
            }
            val renderer = MeteoriteAtmosphereFireRenderEntity.spawn(
                meteorite.world,
                smallMeteorite.loc,
                smallMeteorite.direction,
                4F,
                color = Vector3f(1.0F, 0.16F, 0.58F),
                lifetime = 20 * 100,
                fadeInTicks = 3,
                fadeOutTicks = 8,
                alpha = 0.56,
            )
            smallMeteorite.addPreTickAction {
                // 重力
                direction = direction.normalize() * options.speed + Vec3(0.0, -0.1, 0.0)
                options.speed = direction.length()
                renderer.moveTo(loc, direction)
            }.addHitOnServer {
                renderer.discard(6)
                sendWeakSubMeteoriteFlash(world, loc)
                ParticleEmittersManager.spawnEmitters(
                    MeteoriteShockwaveEmitter(loc, world).apply {
                        effectScale = (targetSize.targetNum.toDouble() / 5.0).coerceAtLeast(0.2)
                    }
                )
                val ds = UsefulMagicDamageSources.entityDamage(world, source, source)
                world.searchEntities(loc, 32.0, EntityUtil.filterDragon).forEach {
                    it.hurt(ds, 12F)
                }
                playDragonSoundOnce(
                    world,
                    loc,
                    SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.HOSTILE,
                    10F,
                    1F,
                    256.0,
                )
                splitChildBarrage(world, loc, source, 8 minRangeTo 12, 5.0)
            }
        }

        ParticleEmittersManager.spawnEmitters(
            MeteoriteExplosionEmitter(
                meteorite.loc,
                meteorite.world
            )
        )
        ParticleEmittersManager.spawnEmitters(
            MeteoriteShockwaveEmitter(
                meteorite.loc,
                meteorite.world
            ).apply {
                effectScale = meteorite.targetSize.targetNum.toDouble().coerceAtLeast(1.0)
            }
        )
    }

    private fun sendWeakSubMeteoriteFlash(world: ServerLevel, loc: Vec3) {
        world.players().filter {
            it.position().distanceTo(loc) <= 96.0
                    && it canSee loc
        }.forEach {
            UsefulMagicPostEffects.playFlameExplodeFlash(
                it,
                8,
                0.28F,
                0.45F,
                Vec3(1.0, 0.16, 0.58),
            )
        }
    }


    private fun splitChildBarrage(
        world: ServerLevel,
        loc: Vec3,
        source: MagicDragonEntity,
        count: IntRangeData,
        damage: Double
    ) {
        val targets = source.searchLivingEntities(256.0, EntityUtil.filterDragon)
        repeat(Random.nextInt(count.random())) {
            val target = targets.randomOrNull() ?: source.target ?: return
            // 这里要搜索被跟踪的实体。
            DragonTrackedBarrage(target, loc, world, damage, source)
                .apply {
                    // 向上炸开的感觉
                    direction = Vec3(0.0, 1.0, 0.0).offsetRandomlyHorizontal(Random.nextDouble(8.0))
                    trackingTick = 20
                    trackingInternal = 30
                    trackingForce = 0.8
                    trackingMaxSpeed = 1.8
                    options.enableSpeedWithOptions(0.9)
                    BarrageManager.spawn(this)
                }
        }
    }

    private fun applyMeteoriteEntityDamage(
        source: MagicDragonEntity,
        center: Vec3,
        radius: Double,
        fullDamageRadius: Double,
        spellDamage: Float
    ) {
        val world = source.serverLevel ?: return
        val rangeBox = AABB.ofSize(center, radius * 2.0, radius * 2.0, radius * 2.0)
        val damageSource = UsefulMagicDamageSources.entityMagic(world, source, source)
        world.getEntitiesOfClass(LivingEntity::class.java, rangeBox) { target ->
            target.isAlive && target.uuid != source.uuid && FriendFilterHelper.filterNotFriend(source, target)
        }.forEach { target ->
            val distance = target.boundingBox.center.distanceTo(center)
            if (distance > radius) return@forEach
            val damageFactor = if (distance <= fullDamageRadius) {
                1.0F
            } else {
                (1.0 - (distance - fullDamageRadius) / (radius - fullDamageRadius))
                    .coerceIn(0.0, 1.0)
                    .toFloat()
            }
            val finalDamage = spellDamage * damageFactor
            if (finalDamage > 0.05F) {
                target.hurt(damageSource, finalDamage)
            }
        }
    }

    private fun playMeteorFarFlySound(world: ServerLevel, soundPos: Vec3) {
        world.playSound(
            null,
            soundPos.x,
            soundPos.y,
            soundPos.z,
            UsefulMagicSoundEvents.METEOR_FALL_FAR.get(),
            SoundSource.HOSTILE,
            16F,
            1F
        )
    }

    private fun playMeteorNearFlySound(world: ServerLevel, soundPos: Vec3) {
        world.playSound(
            null,
            soundPos.x,
            soundPos.y,
            soundPos.z,
            UsefulMagicSoundEvents.METEOR_FALL_NEAR.get(),
            SoundSource.HOSTILE,
            18F,
            1.1F
        )
    }

    private fun playMeteorImpactSound(world: ServerLevel, soundPos: Vec3) {
        world.playSound(
            null,
            soundPos.x,
            soundPos.y,
            soundPos.z,
            UsefulMagicSoundEvents.METEOR_IMPACT.get(),
            SoundSource.HOSTILE,
            32F,
            1F
        )
    }

    private fun stopMeteorFarFlySound(world: ServerLevel, soundPos: Vec3) {
        val stopPacket = ClientboundStopSoundPacket(
            UsefulMagicSoundEvents.METEOR_FALL_FAR.id,
            SoundSource.HOSTILE
        )
        nearbyPlayers(world, soundPos, 512.0).forEach {
            it.connection.send(stopPacket)
        }
    }

    private fun nearbyPlayers(world: ServerLevel, center: Vec3, range: Double): List<ServerPlayer> {
        val rangeSqr = range * range
        return world.players().filter { it.position().distanceToSqr(center) <= rangeSqr }
    }

    private fun clearStatus(removeMeteorite: Boolean = true) {

        // 这里要判定陨石是否已经发射出去了
        // 如果没有发射出去就要直接删掉
        mainMeteorite?.let {
            if (!released) {
                it.remove()
            }
        }

        if (removeMeteorite) {
            sourceLevelForCleanup()?.let { world ->
                mainMeteorite?.let { stopMeteorFarFlySound(world, it.loc) }
            }
            cancelMainTailEmitter()
            mainMeteoriteRenderer?.discard(10)
            mainMeteoriteRenderer = null
            mainMeteorite?.remove()
        }
        loopSound?.fadeOut(10)
        loopSound = null
        mainMeteorite = null
        mainMeteoriteWorld = null
        arrive = false
        released = false
        tick = 0
        chargingComposition?.remove()
        chargingComposition = null
    }

    private fun sourceLevelForCleanup(): ServerLevel? {
        return mainMeteoriteWorld
    }
}
