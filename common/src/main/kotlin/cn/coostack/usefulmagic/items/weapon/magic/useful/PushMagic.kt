package cn.coostack.usefulmagic.items.weapon.magic.useful

import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.usefulmagic.extend.addMultilineTranslatable
import cn.coostack.usefulmagic.extend.mana
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.weapon.magic.ChargingMagic
import cn.coostack.usefulmagic.particles.composition.magic.useful.PushMagicComposition
import cn.coostack.usefulmagic.particles.emitters.magic.useful.PushCloudEmitter
import cn.coostack.usefulmagic.utils.MagicHelper
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class PushMagic(properties: Properties) : ChargingMagic<PushMagicComposition>(properties) {
    companion object {
        private const val MIN_CHARGE_PROGRESS = 0.05
        private const val CONE_DOT_MIN = 0.25
        private const val BASE_RANGE = 32.0
        private const val RANGE_DAMAGE_SCALE = 1.0
        private const val MAX_RANGE = 256.0
        private const val BASELINE_DAMAGE = 15.0
        private const val BASELINE_PUSH_STRENGTH = 4.8
        private const val PUSH_DAMAGE_EXPONENT = 0.55
        private const val MIN_PUSH_STRENGTH = 0.5
        private const val MAX_PUSH_STRENGTH = 18.0
        private const val DISTANCE_FALLOFF = 0.65
        private const val DISTANCE_FALLOFF_EXPONENT = 1.4
        private const val MIN_DISTANCE_PUSH_FACTOR = 0.35
        private const val PUSH_UPWARD_BIAS = 0.16
        private const val SELF_REACTION_RATE = 0.5
        private const val KINETIC_CHECK_TICKS = 20
        private const val KINETIC_DAMAGE_MIN_SPEED = 0.35
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        val level = stack.get(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get()) ?: return
        val baseCost = stack.get(UsefulMagicDataComponentTypes.MAGIC_BASE_MANA_COST.get()) ?: return
        val baseCD = stack.get(UsefulMagicDataComponentTypes.MAGIC_RELEASE_CD.get()) ?: return
        val baseUsageTime = stack.get(UsefulMagicDataComponentTypes.MAGIC_BASE_USAGE.get()) ?: return
        tooltipComponents.add(Component.translatable("item.usefulmagic.magic.level", Component.literal("$level")))
        tooltipComponents.add(
            Component.translatable(
                "item.usefulmagic.magic.base_cost",
                Component.literal("$baseCost")
            )
        )
        tooltipComponents.add(
            Component.translatable(
                "item.usefulmagic.magic.base_usage",
                Component.literal("$baseUsageTime")
            )
        )
        tooltipComponents.add(Component.translatable("item.usefulmagic.magic.base_cd", Component.literal("$baseCD")))
        tooltipComponents.addMultilineTranslatable("item.usefulmagic.items.push_magic")

    }

    override fun onRelease(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        releasePush(shooter, world, wandStack, time, consumePartialCost = false)
    }

    override fun stopUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        chargingTick: Int,
        max: Boolean
    ) {
        super.stopUse(shooter, world, wandStack, ballStack, chargingTick, max)
        if (!max) {
            releasePush(shooter, world, wandStack, chargingTick, consumePartialCost = true)
        }
    }

    override fun onCompositionTick(
        composition: PushMagicComposition,
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {
        val direction = safeDirection(shooter.lookAngle)
        composition.teleportTo(shooter.eyePosition + direction * 1.2)
        composition.direction = direction
        composition.markDirty()
    }

    override fun getComposition(shooter: LivingEntity): PushMagicComposition? =
        getOrCreateContainer(shooter).get()

    override fun getOrCreateComposition(
        shooter: LivingEntity,
        world: Level
    ): PushMagicComposition =
        getOrCreateContainer(shooter).getOrCreate {
            controlEntryOf(shooter) {
                val direction = safeDirection(shooter.lookAngle)
                PushMagicComposition(shooter.eyePosition.add(direction.scale(0.8)), world).also {
                    it.rotateToPoint(direction.asRelative())
                    ParticleCompositionManager.spawn(it)
                }
            }
        } as PushMagicComposition

    private fun releasePush(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        chargingTick: Int,
        consumePartialCost: Boolean
    ) {
        if (world !is ServerLevel || chargingTick <= 0) {
            return
        }
        world.playSound(
            null, shooter.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), shooter.soundSource, 16f, 1f
        )
        val progress = chargeProgress(wandStack, chargingTick)
        if (progress < MIN_CHARGE_PROGRESS) {
            return
        }
        if (consumePartialCost && !consumePartialCostAndCooldown(shooter, wandStack, progress)) {
            return
        }

        val damage = MagicHelper.getMagicDamage(wandStack).coerceAtLeast(0.0)
        if (damage <= 0.0) {
            return
        }

        val look = safeDirection(shooter.lookAngle)
        val range = (calculateRange(damage) * progress).coerceAtLeast(2.0)
        val strength = (calculatePushStrength(damage) * progress).coerceAtLeast(0.1)
        val reactionStrength = strength * SELF_REACTION_RATE
        val start = shooter.eyePosition
        val end = start.add(look.scale(range))
        val targets = findPushTargets(world, shooter, start, end, look, range)

        targets.forEach { target ->
            val distance = start.distanceTo(target.boundingBox.center)
            val distanceFactor = calculateDistanceFactor(distance, range)
            val targetStrength = strength * distanceFactor
            val direction = calculateTargetPushDirection(start, target.boundingBox.center, look)
            val pushVelocity = direction.scale(targetStrength)
            target.deltaMovement = target.deltaMovement.add(pushVelocity)
            target.hurtMarked = true
            target.hasImpulse = true
            watchKineticDamage(target, targetStrength)
        }

        val reactionVelocity = look.scale(-reactionStrength)
        shooter.deltaMovement = shooter.deltaMovement.add(reactionVelocity)
        shooter.hurtMarked = true
        shooter.hasImpulse = true
        watchKineticDamage(shooter, reactionStrength)

        spawnPushParticles(world, start.add(look.scale(0.6)), look, strength, range)
    }

    private fun chargeProgress(wandStack: ItemStack, chargingTick: Int): Double {
        val maxTick = MagicHelper.getMaxChargingTick(wandStack)
        if (maxTick <= 0) {
            return 0.0
        }
        return (chargingTick.toDouble() / maxTick.toDouble()).coerceIn(0.0, 1.0)
    }

    private fun calculateRange(damage: Double): Double {
        return (BASE_RANGE + sqrt(damage) * RANGE_DAMAGE_SCALE).coerceAtMost(MAX_RANGE)
    }

    private fun calculatePushStrength(damage: Double): Double {
        val scaledDamage = (damage / BASELINE_DAMAGE).coerceAtLeast(0.0)
        return (BASELINE_PUSH_STRENGTH * scaledDamage.pow(PUSH_DAMAGE_EXPONENT))
            .coerceIn(MIN_PUSH_STRENGTH, MAX_PUSH_STRENGTH)
    }

    private fun calculateDistanceFactor(distance: Double, range: Double): Double {
        if (range <= 0.0) {
            return 1.0
        }
        val progress = (distance / range).coerceIn(0.0, 1.0)
        return (1.0 - progress.pow(DISTANCE_FALLOFF_EXPONENT) * DISTANCE_FALLOFF)
            .coerceIn(MIN_DISTANCE_PUSH_FACTOR, 1.0)
    }

    private fun calculateTargetPushDirection(start: Vec3, targetCenter: Vec3, fallback: Vec3): Vec3 {
        val horizontal = Vec3(targetCenter.x - start.x, 0.0, targetCenter.z - start.z)
        val base = if (horizontal.lengthSqr() > 1.0E-8) {
            horizontal.normalize()
        } else {
            horizontalDirection(fallback)
        }
        return safeDirection(base.add(0.0, PUSH_UPWARD_BIAS, 0.0))
    }

    private fun horizontalDirection(direction: Vec3): Vec3 {
        val horizontal = Vec3(direction.x, 0.0, direction.z)
        if (horizontal.lengthSqr() > 1.0E-8) {
            return horizontal.normalize()
        }
        return safeDirection(direction)
    }

    private fun findPushTargets(
        world: ServerLevel,
        shooter: LivingEntity,
        start: Vec3,
        end: Vec3,
        look: Vec3,
        range: Double
    ): List<Entity> {
        return world.getEntitiesOfClass(
            Entity::class.java,
            AABB(start, end).inflate(range * 0.5)
        ) { target ->
            target.uuid != shooter.uuid &&
                    target.isAlive &&
                    !target.isRemoved
            isInPushCone(start, look, range, target) &&
                    canPushSee(world, shooter, start, target)
        }
    }

    private fun isInPushCone(start: Vec3, look: Vec3, range: Double, target: Entity): Boolean {
        val toTarget = target.boundingBox.center.subtract(start)
        val distance = toTarget.length()
        if (distance <= 0.0 || distance > range) {
            return false
        }
        return toTarget.normalize().dot(look) >= CONE_DOT_MIN
    }

    private fun canPushSee(world: ServerLevel, shooter: LivingEntity, start: Vec3, target: Entity): Boolean {
        val targetCenter = target.boundingBox.center
        val hit = world.clip(
            ClipContext(
                start,
                targetCenter,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                shooter
            )
        )
        return hit.type == HitResult.Type.MISS || hit.location.distanceTo(start) >= targetCenter.distanceTo(start)
    }

    private fun consumePartialCostAndCooldown(
        shooter: LivingEntity,
        wandStack: ItemStack,
        progress: Double
    ): Boolean {
        if (shooter !is Player) {
            return true
        }
        val manaCost = scaledPositive(MagicHelper.getManaCost(wandStack), progress)
        if (!shooter.hasInfiniteMaterials()) {
            if (shooter.mana < manaCost) {
                return false
            }
            shooter.mana -= manaCost
        }
        val cooldown = scaledPositive(MagicHelper.getFinalCD(wandStack), progress)
        if (cooldown > 0) {
            shooter.cooldowns.addCooldown(wandStack.item, cooldown)
        }
        return true
    }

    private fun scaledPositive(value: Int, progress: Double): Int {
        if (value <= 0) {
            return 0
        }
        return (value * progress).roundToInt().coerceAtLeast(1)
    }

    private fun watchKineticDamage(entity: Entity, speed: Double) {
        val damage = calculateKineticDamage(speed)
        if (damage <= 0f) {
            return
        }
        submitTaskTimerMaxTickServer(1, KINETIC_CHECK_TICKS) {
            if (!entity.isAlive || entity.isRemoved) {
                cancel()
                return@submitTaskTimerMaxTickServer
            }
            if (!entity.horizontalCollision) {
                return@submitTaskTimerMaxTickServer
            }
            entity.invulnerableTime = 0
            entity.hurt(entity.damageSources().flyIntoWall(), damage)
            cancel()
        }
    }

    private fun calculateKineticDamage(speed: Double): Float {
        if (speed < KINETIC_DAMAGE_MIN_SPEED) {
            return 0f
        }
        return (speed * 10.0 - 3.0).coerceAtLeast(0.0).toFloat()
    }

    private fun spawnPushParticles(
        world: ServerLevel,
        pos: Vec3,
        direction: Vec3,
        strength: Double,
        range: Double
    ) {
        PushCloudEmitter(pos, world).apply {
            this.direction = direction
            this.strength = strength
            this.visibleRange = (range + 24.0).toFloat()
            this.maxTick = 4 + (strength * 1.5).roundToInt().coerceIn(0, 8)
            ParticleEmittersManager.spawnEmitters(this)
        }
    }

    private fun safeDirection(direction: Vec3): Vec3 {
        if (direction.lengthSqr() <= 1.0E-8) {
            return Vec3(0.0, 0.0, 1.0)
        }
        return direction.normalize()
    }
}
