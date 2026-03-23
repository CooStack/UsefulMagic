package cn.coostack.usefulmagic.meteorite.starry

import cn.coostack.cooparticlesapi.animation.timeline.DoubleConstSpeedAnimator
import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.extend.asRelative
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.meteorite.MeteoriteImpactHelper
import cn.coostack.usefulmagic.meteorite.MeteoriteDisplay
import cn.coostack.usefulmagic.particles.barrages.api.DamagedBarrage
import cn.coostack.usefulmagic.particles.emitters.StarryMeteoriteLocusEmitters
import cn.coostack.usefulmagic.particles.emitters.magic.StarryHugeBarrageExplosionEmitter
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.ArrayDeque
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

class StarrySubBarrage(loc: Vec3, world: ServerLevel, options: BarrageOption, damage: Double) :
    DamagedBarrage(loc, world, options, damage) {
    companion object {
        private const val DEFAULT_SPEED = 1.0
        private const val MIN_SIZE = 0.5
        private const val MAX_SPEED = 2.8
        private const val ALIGNMENT_DOT_THRESHOLD = 0.9925
        private const val EXPLOSION_RADIUS_MULTIPLIER = 2.0
        private const val DAMAGE_RADIUS_MULTIPLIER = 4.0
        private const val SPECIAL_IMPACT_PARTICLE_SCALE = 0.82
    }

    /**
     * 持续把四散的小陨石拉回主陨石的下落方向，直到几乎共线。
     */
    var attractDirection = Vec3(0.0, -1.0, 0.0)
    var meteoriteType = Blocks.MAGMA_BLOCK
    var targetSize = DoubleConstSpeedAnimator(0.8, 1.0)
    private var immediateScale: Double? = null
    private var meteoriteImpactEnabled = false
    private var meteoriteImpactParticleScale = SPECIAL_IMPACT_PARTICLE_SCALE
    private var aligningToMainDirection = false
    private var trailStarted = false
    private val trailEmitter = createTrailEmitter()

    init {
        if (!options.enableSpeed) {
            if (options.speed > 0.0) {
                options.enableSpeed()
            } else {
                options.enableSpeedWithOptions(DEFAULT_SPEED)
            }
        }
        if (options.maxLivingTick < 0) {
            options.maxLivingTick(120)
        }
        if (options.noneHitBoxTick < 6) {
            options.noneHitBoxTick(6)
        }
    }

    override fun onHitDamaged(result: BarrageHitResult) {
        result.entities.forEach {
            it.invulnerableTime = 0
        }
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        val shooter = shooter
        if (shooter == null) {
            return true
        }
        return livingEntity.uuid != shooter.uuid && FriendFilterHelper.filterNotFriend(shooter, livingEntity)
    }

    override fun createHitBox(): HitBox {
        val size = targetSize.current.coerceAtLeast(MIN_SIZE)
        return HitBox.of(size, size, size)
    }

    override fun createControler(): ServerControler<*> {
        val initialScale = (immediateScale ?: 0.0).toFloat()
        return MeteoriteDisplay(loc, world)
            .apply {
                this.state = meteoriteType.defaultBlockState()
                scale = initialScale
                prevScale = initialScale
            }
    }

    override fun tick() {
        syncScale()
        ensureTrail()
        super.tick()
        if (aligningToMainDirection) {
            applyAttraction()
        }
        trailEmitter.pos = loc
        val control = bindControl.get() as MeteoriteDisplay
        if (direction.lengthSqr() > 1e-6) {
            control.rotateToPoint(direction.asRelative())
        }
    }

    override fun onHit(result: BarrageHitResult) {
        val hitSomething = result.hitBlocks.isNotEmpty() || result.entities.isNotEmpty()
        stopTrail()
        if (hitSomething) {
            if (meteoriteImpactEnabled) {
                triggerMeteoriteImpactExplosion()
            } else {
                triggerImpactExplosion()
            }
            damageNearbyEntities()
            spawnExplosionEffect()
        }
        onHitDamaged(result)
    }

    fun setImmediateSize(size: Double): StarrySubBarrage {
        val fixedSize = size.coerceAtLeast(MIN_SIZE)
        immediateScale = fixedSize
        targetSize.targetNum = fixedSize
        targetSize.resetCurrentTo(fixedSize)
        return this
    }

    fun enableMeteoriteImpact(particleScale: Double = SPECIAL_IMPACT_PARTICLE_SCALE): StarrySubBarrage {
        meteoriteImpactEnabled = true
        meteoriteImpactParticleScale = particleScale
        return this
    }

    fun enableAttractionToMainDirection(direction: Vec3 = attractDirection): StarrySubBarrage {
        attractDirection = if (direction.lengthSqr() > 1e-6) direction.normalize() else Vec3(0.0, -1.0, 0.0)
        aligningToMainDirection = true
        return this
    }

    private fun syncScale() {
        val nextScale = targetSize.next().coerceAtLeast(MIN_SIZE)
        val control = bindControl.get() as MeteoriteDisplay
        if (abs(control.scale.toDouble() - nextScale) > 1e-4) {
            control.scale = nextScale.toFloat()
            hitBox.resetMemo()
        }
    }

    private fun applyAttraction() {
        val pullDirection =
            if (attractDirection.lengthSqr() > 1e-6) attractDirection.normalize() else Vec3(0.0, -1.0, 0.0)
        val currentDirection = if (direction.lengthSqr() > 1e-6) direction.normalize() else pullDirection
        val alignment = currentDirection.dot(pullDirection)
        if (alignment >= ALIGNMENT_DOT_THRESHOLD) {
            direction = pullDirection
            aligningToMainDirection = false
            return
        }
        val sizeFactor = targetSize.current.coerceAtLeast(MIN_SIZE)
        val steerFactor = (0.08 + sizeFactor * 0.035).coerceIn(0.08, 0.2)
        val blendedDirection = currentDirection.scale(1.0 - steerFactor).add(pullDirection.scale(steerFactor))
        if (blendedDirection.lengthSqr() <= 1e-6) {
            return
        }
        val currentSpeed = options.speed.coerceAtLeast(DEFAULT_SPEED)
        val extraAcceleration = (0.025 + sizeFactor * 0.02).coerceAtMost(0.1)
        direction = blendedDirection.normalize()
        options.speed((currentSpeed + extraAcceleration).coerceAtMost(MAX_SPEED))
    }

    private fun damageNearbyEntities() {
        val explosionRadius = calculateDamageRadius()
        val source =
            shooter?.let { UsefulMagicDamageSources.entityMagic(world, it, it) } ?: world.damageSources().magic()
        val candidates = LinkedHashSet<LivingEntity>()
        candidates.addAll(
            world.getEntitiesOfClass(
                LivingEntity::class.java,
                AABB.ofSize(loc, explosionRadius * 2.0, explosionRadius * 2.0, explosionRadius * 2.0)
            ) { filterHitEntity(it) }
        )
        candidates.forEach { entity ->
            val distance = entity.position().distanceTo(loc)
            if (distance > explosionRadius) {
                return@forEach
            }
            val rate = (1.0 - distance / explosionRadius).coerceIn(0.25, 1.0)
            entity.invulnerableTime = 0
            entity.hurt(source, (damage * rate).toFloat())
            entity.invulnerableTime = 0
        }
    }

    private fun triggerImpactExplosion() {
        world.explode(
            shooter,
            loc.x,
            loc.y,
            loc.z,
            calculateExplosionPower(),
            false,
            Level.ExplosionInteraction.MOB
        )
    }

    private fun triggerMeteoriteImpactExplosion() {
        val impactDirection = if (direction.lengthSqr() > 1e-6) direction.normalize() else Vec3(0.0, -1.0, 0.0)
        val impactSize = currentSize()
        val explosionCenter = loc.add(impactDirection.scale(impactSize * 0.9))
        shooter?.let { attacker ->
            val pendingBlocks = ArrayDeque<BlockPos>()
            val impactRadius = calculateExplosionRadius().roundToInt().coerceAtLeast(1)
            val blocksPerTick = MeteoriteImpactHelper.queueImpactExplosion(
                pendingBlocks = pendingBlocks,
                radius = impactRadius,
                center = explosionCenter,
                impactDir = impactDirection
            )
            while (pendingBlocks.isNotEmpty()) {
                MeteoriteImpactHelper.processPendingExplosion(
                    world = world,
                    pendingBlocks = pendingBlocks,
                    explosionBlocksPerTick = blocksPerTick,
                    source = attacker
                )
            }
        }
        spawnMeteoriteImpactEffect(impactDirection, impactSize)
    }

    private fun calculateExplosionRadius(): Double {
        return currentSize() * EXPLOSION_RADIUS_MULTIPLIER
    }

    private fun calculateExplosionPower(): Float {
        return calculateExplosionRadius().toFloat()
    }

    private fun calculateDamageRadius(): Double {
        return currentSize() * DAMAGE_RADIUS_MULTIPLIER
    }

    private fun currentSize(): Double {
        return targetSize.current.coerceAtLeast(MIN_SIZE)
    }

    private fun ensureTrail() {
        if (trailStarted) {
            return
        }
        trailStarted = true
        ParticleEmittersManager.spawnEmitters(trailEmitter)
        spawnTrailEffect()
    }

    private fun stopTrail() {
        if (!trailStarted) {
            return
        }
        trailEmitter.remove()
        trailStarted = false
    }

    private fun createTrailEmitter(): StarryMeteoriteLocusEmitters {
        return StarryMeteoriteLocusEmitters(loc, world).apply {
            maxTick = -1
            templateData.apply {
                color = Vector3f(1.0f, 0.62f, 0.48f)
                size = 0.28f
                maxAge = 12
                alpha = 0.8f
                light = 15
            }
        }
    }

    private fun spawnExplosionEffect() {
        // TODO: 在小陨石落地爆炸时生成粒子效果
    }

    private fun spawnMeteoriteImpactEffect(impactDirection: Vec3, impactSize: Double) {
        val emitter = StarryHugeBarrageExplosionEmitter(loc, world).apply {
            maxTick = 10
        }
        ParticleEmittersManager.spawnEmitters(emitter)
        world.playSound(
            null,
            loc.x,
            loc.y,
            loc.z,
            UsefulMagicSoundEvents.METEOR_IMPACT.get(),
            shooter?.soundSource ?: SoundSource.HOSTILE,
            32f,
            1f
        )
        ServerCameraUtil.sendShake(world, loc, 64 * impactSize, 1.0, 10, 3.0, true)
    }

    private fun spawnTrailEffect() {
        // TODO: 在小陨石开始飞行时生成拖尾粒子效果
    }
}
