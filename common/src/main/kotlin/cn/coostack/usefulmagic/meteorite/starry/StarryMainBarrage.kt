package cn.coostack.usefulmagic.meteorite.starry

import cn.coostack.cooparticlesapi.animation.timeline.FloatConstSpeedAnimator
import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.extend.asRelative
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.meteorite.MeteoriteDisplay
import cn.coostack.usefulmagic.particles.barrages.api.DamagedBarrage
import cn.coostack.usefulmagic.particles.emitters.StarryMeteoriteLocusEmitters
import cn.coostack.usefulmagic.particles.emitters.magic.StarryMeteoriteSplitEmitter
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.random.Random

/**
 * @param loc 生成的位置
 * @param world 生成的世界
 * @param options 陨石的下坠设置
 * @param damage 陨石的总体伤害， 分裂时 所有子陨石的伤害总和
 */
class StarryMainBarrage(loc: Vec3, world: ServerLevel, options: BarrageOption, damage: Double) :
    DamagedBarrage(loc, world, options, damage) {
    companion object {
        private const val MIN_PREPARE_TICKS = 10
        private const val MAX_PREPARE_TICKS = 20
        private const val MAX_FLIGHT_TICKS = 100
        private const val SPLIT_DISTANCE = 60.0
        private const val DEFAULT_MAIN_SPEED = 1.4
        private const val MIN_SUB_SIZE = 0.5
        private const val MAX_SUB_SIZE = 2.5
        private const val MIN_SPLIT_COUNT = 4
        private const val MAX_SPLIT_COUNT = 5
        private const val BASE_REVERSE_FORCE_MIN = 0.35
        private const val REVERSE_FORCE_PER_SIZE = 0.08
        private const val MAX_REVERSE_FORCE_MIN = 1.65
        private const val REVERSE_FORCE_VARIANCE = 0.7
        private const val SPLIT_DAMAGE_RADIUS_MULTIPLIER = 1.5
        private const val SPECIAL_SUB_DAMAGE_RATIO = 3.0 / 5.0
        private const val SPECIAL_SUB_SIZE_RATIO = 3.0 / 7.0
    }

    var targetSize = FloatConstSpeedAnimator(0.5f, damage.toFloat() / 10f)
    private val spawnPos = loc
    private val prepareDuration = Random.nextInt(MIN_PREPARE_TICKS, MAX_PREPARE_TICKS + 1)
    private var prepareTicks = 0
    private var flightTicks = 0
    private var launched = false
    private var splitTriggered = false
    private var trailStarted = false
    private val trailEmitter = createTrailEmitter()

    init {
        options.acrossBlock(true)
        options.acrossLiquid(true)
    }

    override fun onHitDamaged(result: BarrageHitResult) {
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return false
    }

    override fun createHitBox(): HitBox {
        val size = targetSize.current.coerceAtLeast(MIN_SUB_SIZE.toFloat()).toDouble()
        return HitBox.of(size, size, size)
    }

    override fun createControler(): ServerControler<*> {
        return MeteoriteDisplay(loc, world)
            .apply {
                this.state = Blocks.NETHERRACK.defaultBlockState()
                scale = 0f
                prevScale = 0f
            }
    }

    private val preTickOnServer = ArrayDeque<StarryMainBarrage.() -> Unit>()
    private val onSplit = ArrayDeque<StarryMainBarrage.() -> Unit>()
    fun addSplitAction(action: StarryMainBarrage.() -> Unit): StarryMainBarrage {
        onSplit.add(action)
        return this
    }

    fun addPreTickAction(action: StarryMainBarrage.() -> Unit): StarryMainBarrage {
        preTickOnServer.add(action)
        return this
    }

    override fun tick() {
        syncScale()
        preTickOnServer.forEach {
            it()
        }
        if (splitTriggered) {
            return
        }
        if (!launched) {
            tryLaunch()
            return
        }
        ensureTrail()
        super.tick()
        flightTicks++
        trailEmitter.pos = loc
        val control = bindControl.get() as MeteoriteDisplay
        if (direction.lengthSqr() > 1e-6) {
            control.rotateToPoint(direction.asRelative())
        }
        if (shouldSplit()) {
            splitIntoSubBarrages()
        }
    }

    override fun onHit(result: BarrageHitResult) {
        splitIntoSubBarrages()
    }

    private fun syncScale() {
        val nextScale = targetSize.next()
        val control = bindControl.get() as MeteoriteDisplay
        if (abs(control.scale - nextScale) > 1e-4f) {
            control.scale = nextScale
            hitBox.resetMemo()
        }
    }

    private fun tryLaunch() {
        if (++prepareTicks < prepareDuration) {
            return
        }
        launched = true
        if (direction.lengthSqr() <= 1e-6) {
            direction = Vec3(0.0, -1.0, 0.0)
        }
        if (!options.enableSpeed) {
            if (options.speed > 0.0) {
                options.enableSpeed()
            } else {
                options.enableSpeedWithOptions(DEFAULT_MAIN_SPEED)
            }
        }
    }

    private fun ensureTrail() {
        if (trailStarted) {
            return
        }
        trailStarted = true
        ParticleEmittersManager.spawnEmitters(trailEmitter)
    }

    private fun stopTrail() {
        if (!trailStarted) {
            return
        }
        trailEmitter.remove()
        trailStarted = false
    }

    private fun shouldSplit(): Boolean {
        return flightTicks >= MAX_FLIGHT_TICKS || loc.distanceTo(spawnPos) >= SPLIT_DISTANCE
    }

    private fun splitIntoSubBarrages() {
        if (splitTriggered) {
            return
        }
        onSplit.forEach { it() }
        splitTriggered = true
        stopTrail()
        spawnSplitExplosionEffect()

        val splitCount = Random.nextInt(MIN_SPLIT_COUNT, MAX_SPLIT_COUNT + 1)
        val mainDirection = if (direction.lengthSqr() > 1e-6) direction.normalize() else Vec3(0.0, -1.0, 0.0)
        val currentMainSize = targetSize.current.coerceAtLeast(MIN_SUB_SIZE.toFloat()).toDouble()
        damageNearbyEntitiesOnSplit(currentMainSize)
        val maxSubSize = minOf(targetSize.current.toDouble().coerceAtLeast(MIN_SUB_SIZE), MAX_SUB_SIZE)
        val damageWeights = MutableList(splitCount) { Random.nextDouble(0.2, 1.0) }
        val damageWeightSum = damageWeights.sum()
        val inheritedSpeed = options.speed.takeIf { it > 0.0 } ?: DEFAULT_MAIN_SPEED

        repeat(splitCount) { index ->
            val damageWeight = damageWeights[index] / damageWeightSum
            val randomSize = if (maxSubSize <= MIN_SUB_SIZE + 1e-6) {
                MIN_SUB_SIZE
            } else {
                Random.nextDouble(MIN_SUB_SIZE, maxSubSize)
            }
            val randomSpeed = if (options.speed > 0.0) {
                (options.speed * Random.nextDouble(0.35, 0.85)).coerceIn(0.45, 1.8)
            } else {
                Random.nextDouble(0.6, 1.4)
            }
            val spawnOffset = randomUnitVector().scale(Random.nextDouble(0.0, 1.5))
            val subBarrage = StarrySubBarrage(
                loc.add(spawnOffset),
                world,
                BarrageOption()
                    .enableSpeedWithOptions(randomSpeed)
                    .noneHitBoxTick(6)
                    .maxLivingTick(120),
                damage * damageWeight
            ).apply {
                shooter = this@StarryMainBarrage.shooter
                direction = randomSpreadDirection(mainDirection, currentMainSize)
                enableAttractionToMainDirection(mainDirection)
                targetSize.targetNum = randomSize
            }
            BarrageManager.spawn(subBarrage)
        }
        spawnSpecialSubBarrage(mainDirection, currentMainSize, inheritedSpeed)

        remove()
    }

    private fun createTrailEmitter(): StarryMeteoriteLocusEmitters {
        return StarryMeteoriteLocusEmitters(loc, world).apply {
            maxTick = -1
            templateData.apply {
                color = Vector3f(1.0f, 0.45f, 0.55f)
                size = 0.45f
                maxAge = 16
                alpha = 0.85f
                light = 15
            }
        }
    }

    private fun randomSpreadDirection(mainDirection: Vec3, mainSize: Double): Vec3 {
        val basis = if (mainDirection.lengthSqr() > 1e-6) mainDirection.normalize() else Vec3(0.0, -1.0, 0.0)
        val randomVec = randomUnitVector().scale(Random.nextDouble(0.45, 1.35))
        val minRandomForce = (
                BASE_REVERSE_FORCE_MIN +
                        mainSize.coerceAtLeast(MIN_SUB_SIZE) * REVERSE_FORCE_PER_SIZE
                ).coerceIn(BASE_REVERSE_FORCE_MIN, MAX_REVERSE_FORCE_MIN)
        val randomForce = Random.nextDouble(minRandomForce, minRandomForce + REVERSE_FORCE_VARIANCE)
        val subDirection = randomVec.add(
            basis.scale(-randomForce)
        )
        return if (subDirection.lengthSqr() <= 1e-6) basis.scale(-1.0) else subDirection.normalize()
    }

    private fun randomUnitVector(): Vec3 {
        while (true) {
            val candidate = Vec3(
                Random.nextDouble(-1.0, 1.0),
                Random.nextDouble(-1.0, 1.0),
                Random.nextDouble(-1.0, 1.0)
            )
            if (candidate.lengthSqr() > 1e-6) {
                return candidate.normalize()
            }
        }
    }

    private fun damageNearbyEntitiesOnSplit(mainSize: Double) {
        val explosionRadius = (mainSize * SPLIT_DAMAGE_RADIUS_MULTIPLIER).coerceAtLeast(MIN_SUB_SIZE)
        val attacker = shooter
        val source =
            attacker?.let { UsefulMagicDamageSources.entityMagic(world, it, it) } ?: world.damageSources().magic()
        val rangeBox = AABB.ofSize(loc, explosionRadius * 2.0, explosionRadius * 2.0, explosionRadius * 2.0)
        world.getEntitiesOfClass(LivingEntity::class.java, rangeBox) { target ->
            target.isAlive && (
                    attacker == null ||
                            (target.uuid != attacker.uuid && FriendFilterHelper.filterNotFriend(attacker, target))
                    )
        }.forEach { entity ->
            val distance = entity.boundingBox.center.distanceTo(loc)
            if (distance > explosionRadius) {
                return@forEach
            }
            val damageRate = (1.0 - distance / explosionRadius).coerceIn(0.35, 1.0)
            entity.invulnerableTime = 0
            entity.hurt(source, (damage * damageRate).toFloat())
            entity.invulnerableTime = 0
        }
    }

    private fun spawnSpecialSubBarrage(mainDirection: Vec3, currentMainSize: Double, inheritedSpeed: Double) {
        val specialSize = (currentMainSize * SPECIAL_SUB_SIZE_RATIO).coerceAtLeast(MIN_SUB_SIZE)
        val specialBarrage = StarrySubBarrage(
            loc,
            world,
            BarrageOption()
                .enableSpeedWithOptions(inheritedSpeed)
                .noneHitBoxTick(6)
                .maxLivingTick(120),
            damage * SPECIAL_SUB_DAMAGE_RATIO
        ).apply {
            shooter = this@StarryMainBarrage.shooter
            direction = mainDirection
            enableAttractionToMainDirection(mainDirection)
            setImmediateSize(specialSize)
            enableMeteoriteImpact()
        }
        BarrageManager.spawn(specialBarrage)
    }

    private fun spawnSplitExplosionEffect() {
        val emitter = StarryMeteoriteSplitEmitter(loc, world)
        ParticleEmittersManager.spawnEmitters(emitter)
        world.playSound(
            null,
            BlockPos.containing(loc),
            SoundEvents.GENERIC_EXPLODE.value(),
            shooter?.soundSource ?: SoundSource.PLAYERS,
            10f, 0.7f
        )
    }
}
