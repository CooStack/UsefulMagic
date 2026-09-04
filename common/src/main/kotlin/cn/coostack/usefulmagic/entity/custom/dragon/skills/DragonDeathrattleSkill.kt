package cn.coostack.usefulmagic.entity.custom.dragon.skills

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.display.DisplayEntityManager
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.supports.sound.ServerManagedSoundInstance
import cn.coostack.cooparticlesapi.supports.sound.ServerSoundManager
import cn.coostack.cooparticlesapi.supports.sound.SoundVolumeFalloff
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.barrages.entity.skill.StraightPointBarrage
import cn.coostack.usefulmagic.barrages.entity.skill.TrackedPointBarrage
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.display.skills.SkillRangeDisplay
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicSubEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.phases.DragonHoverFlightPhase
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.entity.custom.dragon.skills.composition.MagicRuneRingComposition
import cn.coostack.usefulmagic.entity.custom.dragon.skills.composition.MagicRuneRingEffects
import cn.coostack.usefulmagic.entity.custom.dragon.skills.emitter.CollectDisplayLineEmitter
import cn.coostack.usefulmagic.entity.custom.dragon.spawn.composition.MagicDragonSpawnLaserComposition
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.lerpAsProgress
import cn.coostack.usefulmagic.extend.searchLivingEntities
import cn.coostack.usefulmagic.extend.serverLevel
import cn.coostack.usefulmagic.particles.emitters.CollectLineParticleEmitter
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.renderer.StraightLaserRenderEntity
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.EntityUtil
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import kotlin.random.Random

/**
 * 龙的亡语
 * 体现倒计时 5秒内让生命值拉满， 然后按照时间设置生命值减少
 *
 * # 方案1 （参考无名光神）
 * 1. 召唤大量陨石 向下砸落
 * 2. 陨石砸落的时候会召唤弹幕 发射向玩家
 * # 方案2 (类似突变者)
 * 1. 释放大量弹幕让玩家躲避 (但是三维空间上可能会导致卡顿)
 * # 方案3
 * ## 技能合集
 * 1. 先发射大量弹幕 （类似于Eye的）
 * 2. 发射大量激光 （LaserSkill）
 * 3. 发射3-4个大激光（LaserSkill)
 * 4. 投掷大量小陨石 （MeteoriteBarrageSkill)
 * 5. 生成大量半血SubEye
 * ## 动画表现 TODO
 * 1. 采用多个眼睛（1层 6个） 不断旋转（和Ban的设计差不多）
 *  如果期间玩家死亡导致技能中断，则重来
 */
class DragonDeathrattleSkill : DragonSkill() {
    companion object {
        const val ID = "dragon_die_skill"
        const val RECOVER_TICK = 20 * 5
        private const val TRACKING_HUGE_LASER_DISTANCE = 220.0
        private const val TRACKING_HUGE_LASER_DAMAGE_RADIUS = 5.0
        private const val TRACKING_HUGE_LASER_DAMAGE = 30f
        private const val TRACKING_HUGE_LASER_COLLECT_TICKS = 20 * 3
        private const val TRACKING_HUGE_LASER_PHASE_TICKS = 20 * 4
        private const val TRACKING_HUGE_LASER_SPEED_OFFSET = 0.3
        private const val TRACKING_HUGE_LASER_MIN_SPEED = 0.05
        private const val DEATHRATTLE_LASER_CHARGE_TICKS = 20
        private const val DEATHRATTLE_LASER_DAMAGE_TICKS = 20
    }

    override var chance: Double = 12.0
    private var trackingHugeComposition: MagicDragonSpawnLaserComposition? = null
    private var trackingHugeLaserEntity: StraightLaserRenderEntity? = null
    private var trackingHugeTargetPosition: Vec3? = null
    private var trackingHugeLaserAge = 0
    private var trackingHugeShootSoundPlayed = false
    private var trackingHugeCollectEmitter: CollectDisplayLineEmitter? = null
    private var trackingHugeLaserLoopSound: ServerManagedSoundInstance? = null
    private var deathrattleRuneRingsDisplayed = false
    private var deathrattleRuneRings: ArrayList<MagicRuneRingComposition>? = null

    private var collectToExplosionEmitter: CollectLineParticleEmitter? = null

    override fun getSkillCountDown(source: MagicDragonEntity): Int {
        return 0
    }

    override fun onActive(source: MagicDragonEntity) {
        clearTrackingHugeLaser()
        clearDeathrattleAll()
        source.phaseManager.forceSetPhase(
            DragonHoverFlightPhase()
        ) {
            setCurrentTarget(source.spawnPosition)
        }
    }

    override fun onRelease(source: MagicDragonEntity, holdingTick: Int) {
        clearTrackingHugeLaser()
        clearDeathrattleAll()
        source.dieSkillPlayed = true
        source.kill()
    }

    override fun getMaxHoldingTick(holdingEntity: MagicDragonEntity): Int {
        return 20 * 85 + RECOVER_TICK // 45秒的亡语 -> 转成75秒
    }

    override fun holdingTick(
        holdingEntity: MagicDragonEntity,
        holdTicks: Int
    ) {

        if (holdTicks < RECOVER_TICK) {
            // 到达位置， 进行续能
            // 蓄能
            val process = holdTicks.toDouble() / RECOVER_TICK
            holdingEntity.health = process.lerpAsProgress(1f, holdingEntity.dragonMaxHealth).toFloat()
            return
        }
        // 这里开始亡语
        if (!deathrattleRuneRingsDisplayed) {
            deathrattleRuneRingsDisplayed = true
            deathrattleRuneRings = MagicRuneRingEffects.spawnRewindRings(
                holdingEntity.boxCenterPosition(),
                holdingEntity.level()
            )
        }
        // 对生命进行计时
        val process = (holdTicks - RECOVER_TICK.toDouble()) / (getMaxHoldingTick(holdingEntity) - RECOVER_TICK)
        holdingEntity.health = process.lerpAsProgress(holdingEntity.dragonMaxHealth, 1f).toFloat()
        // 先对target射很多弹幕
        step(holdingEntity, holdTicks)
    }

    override fun stopHolding(entity: MagicDragonEntity, holdTicks: Int) {
        clearDeathrattleAll()
        entity.phaseManager.resetDefaultPhase()
    }

    override fun getSkillID(): String = ID


    fun step(entity: MagicDragonEntity, tick: Int) {
        val steppingTick = tick - RECOVER_TICK
        // 通过对stepping进行分类 (找到不同类别的技能释放）
        val searchedEntities = entity.searchLivingEntities(256.0, EntityUtil.filterDragon)

        if (steppingTick % 15 == 0 && steppingTick < 20 * 2) {
            placeLighting(entity, searchedEntities)
            return
        }

        if (!(steppingTick % 60 != 0 || steppingTick !in 20 * 2..20 * 75)) {
            placeLaser(entity, searchedEntities, steppingTick >= 20 * 35)
        }

        if (steppingTick % 2 == 0 && steppingTick in 20 * 17..20 * 35) {
            if (steppingTick % 30 == 0) {
                placeLighting(entity, searchedEntities)
            }
            placeBarrage(entity, searchedEntities)
            return
        }

        // 大量释放barrage
        if (steppingTick in 20 * 35..20 * 75) {
            if (steppingTick % 25 == 0) {
                placeLighting(entity, searchedEntities)
            }
            placeAndTrackingHugeLaser(entity, steppingTick, searchedEntities)
        }
        if (steppingTick == 20 * 75) {
            clearTrackingHugeLaser()
        }
        // 十秒钟的尾声
        if (steppingTick > 20 * 75) {
            if (steppingTick % 40 == 0) {
                playDragonSoundOnce(
                    entity.level(),
                    entity.position(),
                    SoundEvents.ENDER_DRAGON_GROWL,
                    entity.soundSource,
                    24F,
                    0.6F + Random.nextFloat() * 0.2F,
                    256.0,
                )
            }
            // 放一个粒子聚集的效果
            placeCollectToExplosion(entity)
        }

    }


    override fun canTrigger(entity: MagicDragonEntity): Boolean {
        return entity.entityDeath && !entity.dieSkillPlayed // 标记为entityDeath之后这是它唯一能播放的技能
    }

    override fun testCancel(entity: MagicDragonEntity): Boolean {
        return !canTrigger(entity)
    }

    private fun placeCollectToExplosion(entity: MagicDragonEntity) {
        collectToExplosionEmitter ?: let {
            collectToExplosionEmitter = CollectLineParticleEmitter(entity.boxCenterPosition(), entity.level()).apply {
                this.maxTick = -1
                this.disappearRadius = 0.1
                this.simpleData.apply {
                    this.minCount = 40
                    this.maxCount = 80
                    this.minSpeed = 2.5
                    this.maxSpeed = 4.5
                    this.leftColor = Vector3f(1f, 0.431613f, 0.431613f)
                    this.rightColor = Vector3f(0.304848f, 0.353003f, 1f)
                }
            }
            ParticleEmittersManager.spawnEmitters(collectToExplosionEmitter!!)
            return
        }
        collectToExplosionEmitter!!.pos = entity.boxCenterPosition()
    }

    private fun placeAndTrackingHugeLaser(entity: MagicDragonEntity, tick: Int, searchedEntities: List<LivingEntity>) {
        val closest = searchedEntities.minByOrNull { entity.distanceTo(it) }
        val world = entity.level()
        val start = entity.boxCenterPosition().add(0.0, 16.0, 0.0)
        val fallbackDirection = normalizedOr(entity.forward, Vec3(0.0, -1.0, 0.0))
        val desiredEnd = closest?.let {
            val targetDirection = normalizedOr(it.boxCenterPosition() - start, fallbackDirection)
            start + targetDirection * TRACKING_HUGE_LASER_DISTANCE
        } ?: trackingHugeTargetPosition ?: (start + fallbackDirection * TRACKING_HUGE_LASER_DISTANCE)
        val end = moveTowards(
            trackingHugeTargetPosition ?: desiredEnd,
            desiredEnd,
            trackingLaserSpeed(closest)
        )
        val direction = normalizedOr(end - start, fallbackDirection)

        if (trackingHugeComposition == null || trackingHugeComposition?.status?.isDisable() == true) {
            trackingHugeComposition = MagicDragonSpawnLaserComposition(start, world).apply {
                this.direction = direction.asRelative()
                this.color = Math3DUtil.colorOf(255, 100, 220)
                ParticleCompositionManager.spawn(this)
            }
            trackingHugeLaserAge = 0
            trackingHugeShootSoundPlayed = false
            playDragonSoundOnce(
                entity,
                UsefulMagicSoundEvents.DRAGON_HUGE_LASER_CHARGE_UP.get(),
                SoundSource.HOSTILE,
                1f,
                0.5f,
                256.0,
            )
        }

        if (trackingHugeCollectEmitter == null) {
            trackingHugeCollectEmitter = CollectDisplayLineEmitter(start, world).apply {
                simpleData.apply {
                    minSpeed = 1.0
                    maxSpeed = 1.6
                    minCount = 2
                    maxCount = 4
                }
                radius = 48.0
                color = Math3DUtil.colorOf(255, 100, 220)
                fadeOutOnEmitterRemoved = true
                maxTick = -1
                ParticleEmittersManager.spawnEmitters(this)
            }
        }
        trackingHugeCollectEmitter?.pos = start

        trackingHugeTargetPosition = end
        trackingHugeComposition?.apply {
            teleportTo(start)
            this.direction = direction.asRelative()
            markDirty()
        }

        if (!trackingHugeShootSoundPlayed && trackingHugeLaserAge >= TRACKING_HUGE_LASER_PHASE_TICKS) {
            trackingHugeShootSoundPlayed = true
            playDragonSoundOnce(
                entity,
                UsefulMagicSoundEvents.LASER_START.get(),
                SoundSource.HOSTILE,
                0.9f,
                1f,
                256.0,
            )
            trackingHugeLaserEntity = StraightLaserRenderEntity(world, start).apply {
                maxRadius = 5.5f
                color = Math3DUtil.colorOf(255, 100, 220)
                brightness = 1.15f
                phaseTicks = 5
                lifetime = 20 * 60
                updateBeam(start, end)
                ServerRenderEntityManager.spawn(this)
            }
            trackingHugeLaserLoopSound = ServerSoundManager.instance(
                UsefulMagicSoundEvents.LASER_LOOP.get(),
                SoundSource.HOSTILE
            ).volume(1f)
                .pitch(0.75f)
                .entity(entity)
                .layer("deathrattle_tracking_laser_shoot")
                .uniqueKey(UUID.randomUUID().toString())
                .visibleRange(256.0)
                .syncEveryTick(true)
                .volumeFalloff(SoundVolumeFalloff.LINEAR)
                .looping()
                .stopWhenBoundEntityMissing(false)
                .relative()
                .spawn()
        }

        trackingHugeLaserEntity?.updateBeam(start, end)
        trackingHugeLaserLoopSound?.pitchMultiplier =
            (0.75 + ((trackingHugeLaserAge - TRACKING_HUGE_LASER_PHASE_TICKS).coerceAtLeast(0) / 150.0)
                .coerceAtMost(0.75)).toFloat()

        if (trackingHugeLaserAge >= TRACKING_HUGE_LASER_PHASE_TICKS) {
            damageWithLines(
                entity,
                TRACKING_HUGE_LASER_DAMAGE,
                start,
                end,
                TRACKING_HUGE_LASER_DAMAGE_RADIUS
            )
        }

        trackingHugeLaserAge++
    }

    private fun placeLighting(entity: MagicDragonEntity, searchedEntity: List<LivingEntity>) {
        // 会先召唤Ranged Display
        // 然后从天而降大概4-8个闪电
        // 闪电召唤时间大概是3个tick
        // 每一个tick都有实质伤害

        // 范围随机选取所有目标3，4格方块附近
        // 最多选择6个目标
        // 不足6个目标则在已选择的目标按照
        // 选中目标的闪电个数 * 3，4 格方块附近随机
        fun place(target: Vec3, up: Vec3, down: Vec3, world: Level) {
            // 先再target放一个tracked entity
            SkillRangeDisplay(target, world).apply {
                this.bright = 3f
                this.color = Math3DUtil.colorOf(255, 100, 125)
                this.lifetime = 10
                this.scaled = 3f
                this.discardTick = 5
                this.biggerTick = 5

                DisplayEntityManager.spawn(this)
            }
            submitTaskServer(8) {
                // 持续3个tick， 每一个tick会在他的周围放一个闪电并且造成一次伤害
                playDragonSoundOnce(
                    entity,
                    UsefulMagicSoundEvents.ELECTRIC_EFFECT.get(),
                    SoundSource.HOSTILE,
                    0.4f,
                    1f,
                    128.0,
                )
                submitTaskTimerMaxTickServer(2, 6) {
                    LightningParticleEmitters(up, world).apply {
                        this.targetPos = down - up
                        this.templateData.apply {
                            color = Math3DUtil.colorOf(255, 150, 170)
                            setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                        }
                        this.simpleData.apply {
                            minCount = 2
                            maxCount = 5
                            minAge = 2
                            maxAge = 6
                        }
                        maxTick = 1
                        ParticleEmittersManager.spawnEmitters(this)
                    }
                    damageWithLines(entity, 25f, up, down, 3.0)
                }
            }
        }


        selectRandomPositions(
            entity, searchedEntity, 5, true
        ).forEach { target ->
            // 从target上方 64-128 方块， 朝着 target - 10
            val up = target.add(0.0, Random.nextDouble(64.0, 128.0), 0.0)
            val down = target.add(0.0, -8.0, 0.0)
            place(target.add(0.0, 0.2, 0.0), up, down, entity.level())
        }
    }

    private fun placeBarrage(entity: MagicDragonEntity, entities: List<LivingEntity>) {
        // 朝着四面八方发射直线弹幕
        // 两个barrage交替发射
        repeat(Random.nextInt(1, 2)) {
            StraightPointBarrage(entity.boxCenterPosition(), entity.serverLevel!!, 10.0, entity)
                .apply {
                    direction = Vec3.ZERO.random()
                    options.enableSpeedWithOptions(3.0)
                        .maxLivingTick(30)
                    BarrageManager.spawn(this)
                    this.particleMinAge = 2
                    this.particleMaxAge = 5
                    this.refiner = 1.5
                    world.playSound(
                        null,
                        loc.x,
                        loc.y,
                        loc.z,
                        SoundEvents.PLAYER_ATTACK_CRIT,
                        SoundSource.HOSTILE,
                        10f,
                        1f + Random.nextFloat()
                    )
                }
        }

        if (entities.isEmpty()) return
        repeat(Random.nextInt(1, 2)) {
            val target = entities.random()
            TrackedPointBarrage(target, entity.boxCenterPosition(), entity.serverLevel!!, 10.0, entity).apply {
                direction = Vec3.ZERO.random()
                options.enableSpeedWithOptions(4.0)
                    .maxLivingTick(40)
                this.startTrackingTick = 15
                this.trackingTick = 20
                this.particleMinAge = 2
                this.particleMaxAge = 5
                this.refiner = 1.8
                world.playSound(
                    null,
                    loc.x,
                    loc.y,
                    loc.z,
                    SoundEvents.PLAYER_ATTACK_CRIT,
                    SoundSource.HOSTILE,
                    10f,
                    1.8f
                )
                BarrageManager.spawn(this)
            }
        }
    }

    private fun placeLaser(
        entity: MagicDragonEntity,
        searchedEntity: List<LivingEntity>,
        hasTrackingHugeLaser: Boolean = false
    ) {
        val count = if (hasTrackingHugeLaser) 3 else 6
        val targetPositions = searchedEntity
            .shuffled()
            .take(count)
            .map { it.boxCenterPosition() }
            .toMutableList()

        if (targetPositions.isEmpty()) {
            entity.target?.boxCenterPosition()?.let { targetPositions += it } ?: return
        }

        while (targetPositions.size < count) {
            targetPositions += targetPositions.random().offsetRandomly(Random.nextDouble(24.0, 32.0))
        }


        ServerSoundManager.instance(
            UsefulMagicSoundEvents.DRAGON_HUGE_LASER_CHARGE_UP.get(),
            entity.soundSource,
        )
            .visibleRange(256.0)
            .pitch(0.75f)
            .bindToEntity(entity)
            .uniqueKey()
            .spawn()
            .fadeOut(30)

        val laserRadius = if (hasTrackingHugeLaser) 2f else 4f
        targetPositions.forEach { target ->
            val dragonCenter = entity.boxCenterPosition()
            val start = dragonCenter
                .offsetRandomlyHorizontal(Random.nextDouble(10.0, 64.0))
                .add(0.0, Random.nextDouble(4.0, 20.0), 0.0)
            var direction = (target - start).normalize()
            val composition = MagicDragonSpawnLaserComposition(start, entity.level()).apply {
                this.direction = direction.asRelative()
                this.color = Math3DUtil.colorOf(255, 100, 200)
                ParticleCompositionManager.spawn(this)
            }
            val laserEntity = StraightLaserRenderEntity(entity.level(), start).apply {
                this.maxRadius = laserRadius
                this.color = Math3DUtil.colorOf(255, 100, 200)
                this.brightness = 1.1f
                this.phaseTicks = 10
                this.lifetime = 20
            }

            submitTaskTimerMaxTickServer(DEATHRATTLE_LASER_CHARGE_TICKS) {
                composition.direction = direction.asRelative()
                composition.markDirty()
                direction = (target - start).normalize()
            }.setCancelPredicate {
                composition.status.isDisable()
            }

            submitTaskServer(DEATHRATTLE_LASER_CHARGE_TICKS) {
                if (entity.isDizzying()) {
                    composition.remove()
                    return@submitTaskServer
                }

                laserEntity.apply {
                    ServerRenderEntityManager.spawn(this)
                    updateBeam(start, start + direction * 200.0)
                }
                submitTaskTimerMaxTickServer(DEATHRATTLE_LASER_DAMAGE_TICKS) {
                    damageWithLines(entity, 30f, start, start + direction * 200.0, laserRadius.toDouble())
                }.setFinishCallback {
                    composition.remove()
                }
            }
        }
        // 只用跑一边而不是所有都跑
        submitTaskServer(DEATHRATTLE_LASER_CHARGE_TICKS) {
            if (entity.isDizzying()) {
                return@submitTaskServer
            }
            playDragonSoundOnce(
                entity,
                UsefulMagicSoundEvents.DRAGON_HUGE_LASER_SHOOT.get(),
                SoundSource.HOSTILE,
                0.5f,
                1f,
                256.0,
            )
        }
    }


    private fun damageWithLines(entity: MagicDragonEntity, damage: Float, start: Vec3, end: Vec3, range: Double) {
        val direction = end - start
        val laserBox = AABB(start, end).inflate(range)
        val damageSource = UsefulMagicDamageSources.entityDamage(
            entity.level(), entity, entity
        )
        entity.level().getEntitiesOfClass(LivingEntity::class.java, laserBox) {
            it.uuid != entity.uuid && it.isAlive && it !is MagicEyeEntity && it !is MagicSubEyeEntity
        }.forEach { entity ->
            val entityCenter = entity.boundingBox.center
            val distanceOnLaser = (entityCenter - start).dot(direction.normalize())
            if (distanceOnLaser !in 0.0..direction.length()) return@forEach

            val closestPoint = start.add(direction.normalize().scale(distanceOnLaser))
            if (entityCenter.distanceToSqr(closestPoint) <= range * range) {
                if (entity.hurt(damageSource, damage)) {
                    entity.invulnerableTime = 0
                }
            }
        }
    }

    private fun clearTrackingHugeLaser() {
        trackingHugeComposition?.remove()
        trackingHugeComposition = null
        trackingHugeLaserEntity?.discard()
        trackingHugeLaserEntity = null
        trackingHugeTargetPosition = null
        trackingHugeLaserAge = 0
        trackingHugeShootSoundPlayed = false
        trackingHugeCollectEmitter?.remove()
        trackingHugeCollectEmitter = null
        trackingHugeLaserLoopSound?.fadeOut(20)
        trackingHugeLaserLoopSound = null
    }

    private fun clearDeathrattleAll() {
        deathrattleRuneRingsDisplayed = false
        deathrattleRuneRings?.forEach {
            it.remove()
        }
        collectToExplosionEmitter?.remove()
        collectToExplosionEmitter = null
        deathrattleRuneRings = null
    }

    private fun moveTowards(current: Vec3, target: Vec3, maxDistance: Double): Vec3 {
        val delta = target - current
        val distanceSqr = delta.lengthSqr()
        if (distanceSqr <= maxDistance * maxDistance) {
            return target
        }
        return current + delta.normalize() * maxDistance
    }

    private fun trackingLaserSpeed(target: LivingEntity?): Double {
        return 3.3
    }

    private fun normalizedOr(vector: Vec3, fallback: Vec3): Vec3 {
        if (vector.lengthSqr() > 1.0E-4) {
            return vector.normalize()
        }
        return if (fallback.lengthSqr() > 1.0E-4) {
            fallback.normalize()
        } else {
            Vec3(0.0, -1.0, 0.0)
        }
    }

    private fun selectRandomPositions(
        entity: MagicDragonEntity,
        searchedEntities: List<LivingEntity>,
        selectedCount: Int,
        alwaysFloor: Boolean = false,
    ): List<Vec3> {
        if (selectedCount <= 0) {
            return emptyList()
        }

        val world = entity.level()
        val selected = ArrayList<Vec3>(selectedCount)
        searchedEntities.shuffled().take(selectedCount).forEach {
            selected += if (alwaysFloor) {
                floorPosition(world, it.position())
            } else {
                it.position()
            }
        }

        while (selected.size < selectedCount) {
            val origin = entity.boxCenterPosition().offsetRandomlyHorizontal(Random.nextDouble(16.0, 64.0))
            selected += floorPosition(world, origin)
        }

        return selected
    }

    private fun floorPosition(world: Level, pos: Vec3): Vec3 {
        val minY = world.minBuildHeight
        val mutable = BlockPos.containing(pos).mutable()
        while (mutable.y > minY && world.getBlockState(mutable).isAir) {
            mutable.move(0, -1, 0)
        }

        if (world.getBlockState(mutable).isAir) {
            return Vec3(pos.x, minY.toDouble(), pos.z)
        }

        return Vec3(pos.x, mutable.y + 1.0, pos.z)
    }


}
