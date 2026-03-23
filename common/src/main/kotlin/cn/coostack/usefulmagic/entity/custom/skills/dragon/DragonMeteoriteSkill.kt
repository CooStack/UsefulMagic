package cn.coostack.usefulmagic.entity.custom.skills.dragon

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.renderer.post.CooPostEffects
import cn.coostack.cooparticlesapi.sound.ServerManagedSoundInstance
import cn.coostack.cooparticlesapi.sound.ServerSoundManager
import cn.coostack.cooparticlesapi.sound.SoundVolumeFalloff
import cn.coostack.cooparticlesapi.utils.PhysicsUtil
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.MagicSubEyeEntity
import cn.coostack.usefulmagic.entity.util.phases.dragon.DragonHoverFlightPhase
import cn.coostack.usefulmagic.extend.serverLevel
import cn.coostack.usefulmagic.meteorite.MeteoriteBarrage
import cn.coostack.usefulmagic.meteorite.MeteoriteDisplay
import cn.coostack.usefulmagic.particles.emitters.meteorite.MeteoriteTailEmitter
import cn.coostack.usefulmagic.renderer.UsefulMagicPostEffects
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * 这里需要一个魔法阵
 * 一个粒子轨迹 （复用）
 * 爆炸特效 （复用）
 * wave （自制）
 * 然后用来展现出陨石特效的
 *
 * @constructor Create empty Dragon meteorite skill
 */
class DragonMeteoriteSkill : DragonSkill() {
    companion object {
        const val ID = "dragon_meteorite_skill"
        private const val ARRIVE_GRACE_TICKS = 80
        private const val MERGE_TICKS = 100
        private const val RELEASE_WAIT_TICKS = 80
        private const val TARGET_SIZE = 16.0
        private const val SUB_METEORITES_PER_WAVE = 2
        private const val SPELL_DAMAGE = 32.0f
        private const val METEOR_SOUND_RANGE = 512.0
        private const val METEOR_NEAR_DISTANCE = 12.0
    }

    override var chance: Double = 10.0
    private var tick = 0
    private var arrive = false
    private var released = false
    private var mainMeteorite: MeteoriteBarrage? = null
    private var mainMeteoriteWorld: ServerLevel? = null
    private var mainTailEmitter: MeteoriteTailEmitter? = null

    private var loopSound: ServerManagedSoundInstance? = null

    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 20 * 3600
    }

    override fun onActive(source: MagicDragonEntity) {
        clearStatus()
        source.phaseManager.forceSetPhase(
            DragonHoverFlightPhase()
        ) {
            setCurrentTarget(source.spawnPosition.add(0.0, 15.0, 0.0))
            listenContinue {
                if (!arrive && isArriveCurrentTarget(source, 2.0)) {
                    arrive = true
                    spawnMainBarrage(source)
                }
            }
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
    }

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
        clearStatus()
    }

    override fun getSkillID(): String {
        return ID
    }

    private fun spawnMainBarrage(source: MagicDragonEntity) {
        val world = source.serverLevel ?: return
        val loc = source.position().add(0.0, 24.0, 0.0)
        //  播放loops音效
        submitTaskServer(20) {
            loopSound = ServerSoundManager.instance(
                UsefulMagicSoundEvents.ROCK_LOOP.get(),
                SoundSource.HOSTILE
            )
                .entity(source)
                .layer("meteorite_rock")
                .volume(0f)
                .pitch(1f)
                .visibleRange(256.0)
                .volumeFalloff(SoundVolumeFalloff.LINEAR)
                .syncEveryTick(true)
                .relative()
                .looping()
                .spawn().apply {
                    fadeTo(1f, 20)
                }
        }
        mainMeteoriteWorld = world
        mainMeteorite = MeteoriteBarrage(
            loc,
            world,
            MeteoriteDisplay(loc, world).apply {
                state = Blocks.MAGMA_BLOCK.defaultBlockState()
                scale = 0f
                prevScale = 0f
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
        val spawnOffset = Vec3.ZERO.random().scale(
            Random.nextDouble(
                TARGET_SIZE + 24.0,
                TARGET_SIZE + 48.0
            )
        )
        val spawnPos = mainBarrage.loc.add(spawnOffset)
        val mergeIncrease = TARGET_SIZE / (MERGE_TICKS * SUB_METEORITES_PER_WAVE)
        val subMeteorite = MeteoriteBarrage(
            spawnPos,
            world,
            MeteoriteDisplay(spawnPos, world).apply {
                state = Blocks.NETHERRACK.defaultBlockState()
                scale = 0f
                prevScale = 0f
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
                    direction.normalize().scale(options.speed),
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
        mainBarrage.options.enableSpeedWithOptions(0.8)
            .acrossBlock(false)
            .noneHitBoxTick(0)
            .maxLivingTick(20 * 16)
        mainBarrage.freeze = false
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
            val explosionCenter = loc.add(fallDir.scale(mainBarrage.targetSize.targetNum.toDouble() * 1.25))
            tailEmitter.cancelled = true
            stopFarFlySound(loc)
            playMeteorImpactSound(world, loc)
            explosion(source)
            applyMeteoriteEntityDamage(
                source = source,
                center = explosionCenter,
                radius = (TARGET_SIZE * 2.0).coerceAtLeast(1.0),
                fullDamageRadius = TARGET_SIZE,
                spellDamage = SPELL_DAMAGE
            )
            remove()
        }
        mainBarrage.addPreTickAction {
            if (nearFlySoundPlayed) return@addPreTickAction
            if (loc.distanceTo(target) > METEOR_NEAR_DISTANCE) return@addPreTickAction
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
            ServerCameraUtil.sendShake(world, loc, 256.0, 4.0, 10, 100.0, true)
        }
        return tailEmitter
    }

    private fun cancelMainTailEmitter() {
        mainTailEmitter?.cancelled = true
        mainTailEmitter = null
    }

    private fun explosion(source: MagicDragonEntity) {
        // 预留：后续可在这里接入不破坏方块的爆炸表现
        source.level().getEntitiesOfClass(Player::class.java, source.boundingBox.inflate(256.0)).forEach {
            CooPostEffects.server.send(
                it as ServerPlayer,
                UsefulMagicPostEffects.flameExplodeFlash(30, 1f, 1f)
            )
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
                1.0f
            } else {
                (1.0 - (distance - fullDamageRadius) / (radius - fullDamageRadius))
                    .coerceIn(0.0, 1.0)
                    .toFloat()
            }
            val finalDamage = spellDamage * damageFactor
            if (finalDamage > 0.05f) {
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
            16f,
            1f
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
            18f,
            1.1f
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
            32f,
            1f
        )
    }

    private fun stopMeteorFarFlySound(world: ServerLevel, soundPos: Vec3) {
        val stopPacket = ClientboundStopSoundPacket(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "meteor_fall_far"),
            SoundSource.HOSTILE
        )
        nearbyPlayers(world, soundPos, METEOR_SOUND_RANGE).forEach {
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
            mainMeteorite?.remove()
        }
        loopSound?.fadeOut(10)
        loopSound = null
        mainMeteorite = null
        mainMeteoriteWorld = null
        arrive = false
        released = false
        tick = 0
    }

    private fun sourceLevelForCleanup(): ServerLevel? {
        return mainMeteoriteWorld
    }
}
