package cn.coostack.usefulmagic.entity.util.phases.dragon

import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.usefulmagic.entity.custom.MagicDragonAnimationState
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.entity.util.EntityPoseUtil
import cn.coostack.usefulmagic.entity.util.phases.*
import cn.coostack.usefulmagic.extend.asDouble
import cn.coostack.usefulmagic.extend.asFloat
import cn.coostack.usefulmagic.extend.asInt
import cn.coostack.usefulmagic.extend.serverLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.random.Random

/**
 * 随机穿过目标点的飞行姿态
 */
class DragonSimpleFlightPhase : PhaseDefinition<MagicDragonEntity>, PhaseCollisionEntity<MagicDragonEntity> {

    companion object {
        const val ID = "simple-flight-phase"
        // 目标缓冲长度参数键，需要传入 Int，控制 runtime 中预先保留几个目标点
        const val TARGET_BUFFER_SIZE = "target_buffer_size"
        // 转向速度参数键，需要传入 Float，控制实体转头快慢
        const val TURN_SPEED = "turn_speed"
        // 接近目标时额外补偿距离参数键，需要传入 Double
        const val ARRIVE_DISTANCE_EXTRA = "arrive_distance_extra"
        // 判定是否已经面向目标的夹角阈值参数键，需要传入 Double
        const val FACING_DOT_THRESHOLD = "facing_dot_threshold"
        // 面向敌方目标时的最大环绕半径参数键，需要传入 Double
        const val ENEMY_TARGET_RADIUS = "enemy_target_radius"
        // 随机目标的最小水平距离参数键，需要传入 Double
        const val RANDOM_MIN_DISTANCE = "random_min_distance"
        // 随机目标的最大水平距离参数键，需要传入 Double
        const val RANDOM_MAX_DISTANCE = "random_max_distance"
        // 随机目标的最小高度参数键，需要传入 Double
        const val RANDOM_MIN_HEIGHT = "random_min_height"
        // 随机目标的最大高度参数键，需要传入 Double
        const val RANDOM_MAX_HEIGHT = "random_max_height"
        // 实际飞行速度倍率参数键，需要传入 Double
        const val SPEED_SCALE = "speed_scale"
        // 抵达目标后的基础冷却 tick 参数键，需要传入 Int
        const val ARRIVAL_COOLDOWN_BASE_TICKS = "arrival_cooldown_base_ticks"
        // 抵达目标后的随机额外冷却 tick 参数键，需要传入 Int
        const val ARRIVAL_COOLDOWN_RANDOM_TICKS = "arrival_cooldown_random_ticks"
        // 进入环绕时的角度步进参数键，需要传入 Double
        const val ORBIT_ANGLE_STEP = "orbit_angle_step"
        // 环绕半径基础值参数键，需要传入 Double
        const val ORBIT_RADIUS_BASE = "orbit_radius_base"
        // 环绕半径最大值参数键，需要传入 Double
        const val ORBIT_RADIUS_MAX = "orbit_radius_max"
        // 返回环绕点时的前向推进速度参数键，需要传入 Double
        const val ORBIT_RETURN_SPEED = "orbit_return_speed"
        // 返回环绕点时的切向速度参数键，需要传入 Double
        const val ORBIT_TANGENT_SPEED = "orbit_tangent_speed"
        // 方向插值系数参数键，需要传入 Double，控制飞行转向平滑度
        const val DIRECTION_LERP = "direction_lerp"
        // 每次重新挑选目标的最大尝试次数参数键，需要传入 Int
        const val RANDOM_TARGET_ATTEMPTS = "random_target_attempts"
        // 靠近目标后多久判定超时参数键，需要传入 Int
        const val NEAR_TARGET_TIMEOUT_TICKS = "near_target_timeout_ticks"
        // 单个目标最多使用多久参数键，需要传入 Int
        const val TARGET_MAX_USE_TICKS = "target_max_use_ticks"
        // 修正目标高度时向下探测的方块偏移参数键，需要传入 Int
        const val BLOCK_CHECK_Y_OFFSET = "block_check_y_offset"
        // 目标被阻挡时向上抬升的最小方块数参数键，需要传入 Int
        const val BLOCKED_TARGET_RISE_MIN = "blocked_target_rise_min"
        // 目标被阻挡时向上抬升的最大方块数参数键，需要传入 Int
        const val BLOCKED_TARGET_RISE_MAX = "blocked_target_rise_max"
        // 地面附近与高空随机高度切换的距离参数键，需要传入 Double
        const val GROUND_HEIGHT_SWITCH_DISTANCE = "ground_height_switch_distance"
        // 地面附近额外抬高的基础高度参数键，需要传入 Double
        const val GROUND_LOW_EXTRA_HEIGHT = "ground_low_extra_height"
        // 地面附近随机高度的最小值参数键，需要传入 Double
        const val GROUND_RANDOM_MIN_HEIGHT = "ground_random_min_height"
        // 地面附近随机高度的最大值参数键，需要传入 Double
        const val GROUND_RANDOM_MAX_HEIGHT = "ground_random_max_height"

        private const val DEFAULT_TARGET_BUFFER_SIZE = 2
        private const val DEFAULT_TURN_SPEED = 7f
        private const val DEFAULT_ARRIVE_DISTANCE_EXTRA = 0.25
        private const val DEFAULT_FACING_DOT_THRESHOLD = 0.985
        private const val DEFAULT_ENEMY_TARGET_RADIUS = 18.0
        private const val DEFAULT_RANDOM_MIN_DISTANCE = 16.0
        private const val DEFAULT_RANDOM_MAX_DISTANCE = 64.0
        private const val DEFAULT_RANDOM_MIN_HEIGHT = 8.0
        private const val DEFAULT_RANDOM_MAX_HEIGHT = 48.0
        private const val DEFAULT_SPEED_SCALE = 0.75
        private const val DEFAULT_ARRIVAL_COOLDOWN_BASE_TICKS = 10
        private const val DEFAULT_ARRIVAL_COOLDOWN_RANDOM_TICKS = 10
        private const val DEFAULT_ORBIT_ANGLE_STEP = 0.22
        private const val DEFAULT_ORBIT_RADIUS_BASE = 14.0
        private const val DEFAULT_ORBIT_RADIUS_MAX = 28.0
        private const val DEFAULT_ORBIT_RETURN_SPEED = 1.25
        private const val DEFAULT_ORBIT_TANGENT_SPEED = 0.18
        private const val DEFAULT_DIRECTION_LERP = 0.28
        private const val DEFAULT_RANDOM_TARGET_ATTEMPTS = 12
        private const val DEFAULT_NEAR_TARGET_TIMEOUT_TICKS = 40
        private const val DEFAULT_TARGET_MAX_USE_TICKS = 120
        private const val DEFAULT_BLOCK_CHECK_Y_OFFSET = 15
        private const val DEFAULT_BLOCKED_TARGET_RISE_MIN = 10
        private const val DEFAULT_BLOCKED_TARGET_RISE_MAX = 25
        private const val DEFAULT_GROUND_HEIGHT_SWITCH_DISTANCE = 16.0
        private const val DEFAULT_GROUND_LOW_EXTRA_HEIGHT = 5.0
        private const val DEFAULT_GROUND_RANDOM_MIN_HEIGHT = -16.0
        private const val DEFAULT_GROUND_RANDOM_MAX_HEIGHT = 32.0

        val HOLDER = PhaseRegistries.register(ID) { DragonSimpleFlightPhase() }

        private val RANDOM_ANCHORS = listOf(
            FlightAnchor("quadrant_1", Vec3(1.0, 0.0, 1.0)),
            FlightAnchor("quadrant_2", Vec3(-1.0, 0.0, 1.0)),
            FlightAnchor("quadrant_3", Vec3(-1.0, 0.0, -1.0)),
            FlightAnchor("quadrant_4", Vec3(1.0, 0.0, -1.0)),
            FlightAnchor("axis_x_positive", Vec3(1.0, 0.0, 0.0)),
            FlightAnchor("axis_x_negative", Vec3(-1.0, 0.0, 0.0)),
            FlightAnchor("axis_z_positive", Vec3(0.0, 0.0, 1.0)),
            FlightAnchor("axis_z_negative", Vec3(0.0, 0.0, -1.0))
        )
    }

    var selectTargetCountdown = 0

    /**
     * 飞到目标后的延迟选择
     */
    var flightTargetDelay = 0
    private var activeTarget: Vec3? = null
    private var activeTargetUseTicks = 0
    private var nearTargetTicks = 0

    override fun canBegin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): Boolean {
        return true
    }

    override fun begin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        selectTargetCountdown = 0
        flightTargetDelay = 0
        activeTarget = null
        activeTargetUseTicks = 0
        nearTargetTicks = 0
        instance.resetSimpleFlightOrbitState()
    }

    override fun step(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        ensureTargetBuffer(instance, runtime)
        applySpeed(instance, runtime)


        instance.serverLevel?.let {
            val spawn = instance.spawnPosition
            it.players().forEach { p ->
                it.sendParticles(
                    p, ParticleTypes.END_ROD, true, spawn.x, spawn.y, spawn.z, 32, 0.5, 0.5, 0.5, 0.0
                )
                val pos = runtime.peekTargetOrNull() ?: return@forEach
                it.sendParticles(
                    p, ParticleTypes.CLOUD, true, pos.x, pos.y, pos.z, 32, 0.5, 0.5, 0.5, 0.0
                )
            }
        }

        return PhaseResult.Continue
    }

    override fun end(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
    }

    override fun id(): String {
        return ID
    }

    private fun applySpeed(instance: MagicDragonEntity, runtime: PhaseRuntime) {
        ensureTargetBuffer(instance, runtime)
        val flightSpeed =
            instance.getAttributeValue(Attributes.FLYING_SPEED) *
                    (runtime.params[SPEED_SCALE].asDouble ?: DEFAULT_SPEED_SCALE)
        var target = runtime.peekTargetOrNull() ?: instance.spawnPosition
        var targetDirection = target - instance.position()
        val arriveDistance =
            (flightSpeed + (runtime.params[ARRIVE_DISTANCE_EXTRA].asDouble ?: DEFAULT_ARRIVE_DISTANCE_EXTRA)) * 2.0
        updateTargetTracking(target)
        if (instance.simpleFlightArrivalCooldownTick > 0) {
            instance.simpleFlightArrivalCooldownTick--
            val orbitPoint = updateOrbitPoint(instance, runtime, target, arriveDistance)
            moveTowardOrbitPoint(instance, runtime, orbitPoint)
            if (instance.simpleFlightArrivalCooldownTick == 0) {
                runtime.popTarget()
                clearTargetTracking(instance)
                ensureTargetBuffer(instance, runtime)
            }
            return
        }

        val targetMaxUseTicks =
            (runtime.params[TARGET_MAX_USE_TICKS].asInt ?: DEFAULT_TARGET_MAX_USE_TICKS).coerceAtLeast(0)
        if (activeTargetUseTicks > targetMaxUseTicks) {
            skipCurrentTarget(instance, runtime)
            return
        }

        if (targetDirection.lengthSqr() <= arriveDistance * arriveDistance) {
            val baseCooldown =
                (runtime.params[ARRIVAL_COOLDOWN_BASE_TICKS].asInt ?: DEFAULT_ARRIVAL_COOLDOWN_BASE_TICKS)
                    .coerceAtLeast(0)
            val randomCooldown =
                (runtime.params[ARRIVAL_COOLDOWN_RANDOM_TICKS].asInt ?: DEFAULT_ARRIVAL_COOLDOWN_RANDOM_TICKS)
                    .coerceAtLeast(0)
            instance.simpleFlightArrivalCooldownTick =
                baseCooldown + Random.nextInt(randomCooldown + 1)
            instance.simpleFlightOrbitCenter = target
            val offset = instance.position() - target
            instance.simpleFlightOrbitAngle = if (offset.lengthSqr() <= 1.0E-6) {
                atan2(instance.z - target.z, instance.x - target.x)
            } else {
                atan2(offset.z, offset.x)
            }
            instance.simpleFlightOrbitDirection = if (targetDirection.x * instance.forward.z - targetDirection.z * instance.forward.x >= 0.0) 1 else -1
            val orbitRadiusBase = runtime.params[ORBIT_RADIUS_BASE].asDouble ?: DEFAULT_ORBIT_RADIUS_BASE
            val orbitRadiusMax = (runtime.params[ORBIT_RADIUS_MAX].asDouble ?: DEFAULT_ORBIT_RADIUS_MAX)
                .coerceAtLeast(orbitRadiusBase)
            instance.simpleFlightOrbitRadius = (arriveDistance * 2.5).coerceIn(orbitRadiusBase, orbitRadiusMax)
            val orbitPoint = updateOrbitPoint(instance, runtime, target, arriveDistance)
            moveTowardOrbitPoint(instance, runtime, orbitPoint)
            return
        }

        val nearTargetDistance = arriveDistance * 2.0
        if (targetDirection.lengthSqr() <= nearTargetDistance * nearTargetDistance) {
            nearTargetTicks++
            val nearTargetTimeoutTicks =
                (runtime.params[NEAR_TARGET_TIMEOUT_TICKS].asInt ?: DEFAULT_NEAR_TARGET_TIMEOUT_TICKS).coerceAtLeast(0)
            if (nearTargetTicks > nearTargetTimeoutTicks) {
                skipCurrentTarget(instance, runtime)
                return
            }
        } else {
            nearTargetTicks = 0
        }

        val desiredDirection = if (targetDirection.lengthSqr() <= 1.0E-6) {
            horizontalForward(instance)
        } else {
            targetDirection.normalize()
        }

        val forward = horizontalForward(instance)
        val horizontalDesired = Vec3(desiredDirection.x, 0.0, desiredDirection.z)
        val isFacingTarget = horizontalDesired.lengthSqr() <= 1.0E-6 ||
                forward.dot(horizontalDesired.normalize()) >=
                (runtime.params[FACING_DOT_THRESHOLD].asDouble ?: DEFAULT_FACING_DOT_THRESHOLD)

        if (!isFacingTarget) {
            playTurnAnimation(instance, forward, desiredDirection)
            EntityPoseUtil.rotationFix(
                forward,
                desiredDirection,
                instance,
                runtime.params[TURN_SPEED].asFloat ?: DEFAULT_TURN_SPEED
            )
        } else {
            instance.playAnimation(MagicDragonAnimationState.FLY)
        }

        val currentVelocity = if (instance.deltaMovement.lengthSqr() <= 1.0E-6) {
            forward.scale(flightSpeed)
        } else {
            instance.deltaMovement
        }
        val normalizedVelocity = if (currentVelocity.lengthSqr() <= 1.0E-6) {
            forward
        } else {
            currentVelocity.normalize()
        }
        val normalizedDesired = desiredDirection
        val finalVelocity = GraphMathHelper.lerp(
            (runtime.params[DIRECTION_LERP].asDouble ?: DEFAULT_DIRECTION_LERP).coerceIn(0.0, 1.0),
            normalizedVelocity,
            normalizedDesired
        ).normalize().scale(flightSpeed)

        instance.deltaMovement = finalVelocity
        instance.addDeltaMovement(EntityPoseUtil.stayAwayFloor(instance.level(), instance.position(), finalVelocity))
    }

    private fun updateOrbitPoint(instance: MagicDragonEntity, runtime: PhaseRuntime, target: Vec3, orbitRadius: Double): Vec3 {
        val center = instance.simpleFlightOrbitCenter ?: target.also { instance.simpleFlightOrbitCenter = it }
        val horizontalOffset = Vec3(instance.x - center.x, 0.0, instance.z - center.z)
        val radius = if (instance.simpleFlightOrbitRadius > 0.0) instance.simpleFlightOrbitRadius else orbitRadius
        if (horizontalOffset.lengthSqr() > 1.0E-6) {
            instance.simpleFlightOrbitAngle = atan2(horizontalOffset.z, horizontalOffset.x)
        }
        instance.simpleFlightOrbitAngle +=
            (runtime.params[ORBIT_ANGLE_STEP].asDouble ?: DEFAULT_ORBIT_ANGLE_STEP) *
                    instance.simpleFlightOrbitDirection
        val offset = Vec3(
            cos(instance.simpleFlightOrbitAngle) * radius,
            0.0,
            sin(instance.simpleFlightOrbitAngle) * radius
        )
        val currentPoint = center.add(offset)
        return currentPoint
    }

    private fun moveTowardOrbitPoint(instance: MagicDragonEntity, runtime: PhaseRuntime, orbitPoint: Vec3) {
        val position = instance.position()
        val toPoint = orbitPoint - position
        val desiredDirection = if (toPoint.lengthSqr() <= 1.0E-6) {
            horizontalForward(instance)
        } else {
            toPoint.normalize()
        }
        val forward = horizontalForward(instance)
        playTurnAnimation(instance, forward, desiredDirection)
        EntityPoseUtil.rotationFix(
            forward,
            desiredDirection,
            instance,
            runtime.params[TURN_SPEED].asFloat ?: DEFAULT_TURN_SPEED
        )
        val forwardComponent =
            forward.scale(runtime.params[ORBIT_RETURN_SPEED].asDouble ?: DEFAULT_ORBIT_RETURN_SPEED)
        val tangentSpeed = runtime.params[ORBIT_TANGENT_SPEED].asDouble ?: DEFAULT_ORBIT_TANGENT_SPEED
        val tangentComponent =
            Vec3(-forward.z, 0.0, forward.x).scale(instance.simpleFlightOrbitDirection.toDouble() * tangentSpeed)
        val finalVelocity = forwardComponent.add(tangentComponent)
        instance.deltaMovement = finalVelocity
        instance.addDeltaMovement(EntityPoseUtil.stayAwayFloor(instance.level(), position, finalVelocity))
        instance.yBodyRot = instance.yRot
        instance.yHeadRot = instance.yRot
    }

    private fun ensureTargetBuffer(instance: MagicDragonEntity, runtime: PhaseRuntime) {
        if (!runtime.hasTarget()) {
            runtime.addTarget(createNextTarget(instance, runtime))
        }
        val targetBufferSize =
            (runtime.params[TARGET_BUFFER_SIZE].asInt ?: DEFAULT_TARGET_BUFFER_SIZE).coerceAtLeast(1)
        while (runtime.targets.size < targetBufferSize) {
            runtime.addTarget(createNextTarget(instance, runtime))
        }
    }

    private fun createNextTarget(instance: MagicDragonEntity, runtime: PhaseRuntime): Vec3 {
        val attempts = (runtime.params[RANDOM_TARGET_ATTEMPTS].asInt ?: DEFAULT_RANDOM_TARGET_ATTEMPTS)
            .coerceAtLeast(1)
        repeat(attempts) {
            val target = createRandomTarget(instance, runtime)
            val fixedTarget = fixTargetHeight(instance, runtime, target)
            if (canFlyToTargetHeight(instance, fixedTarget)) {
                return fixedTarget
            }
        }

        return instance.position().add(
            0.0,
            runtime.params[RANDOM_MIN_HEIGHT].asDouble ?: DEFAULT_RANDOM_MIN_HEIGHT,
            0.0
        )
    }

    private fun createRandomTarget(instance: MagicDragonEntity, runtime: PhaseRuntime?): Vec3 {
        val combatTarget = instance.getCombatTarget()
        return if (combatTarget != null) {
            createEnemyBiasedTarget(instance, runtime, combatTarget)
        } else {
            createSpawnBiasedTarget(instance, runtime)
        }
    }

    private fun createEnemyBiasedTarget(instance: MagicDragonEntity, runtime: PhaseRuntime?, target: LivingEntity): Vec3 {
        val history = instance.simpleFlightTargetHistory
        val enemyAnchor = "enemy_${target.id}"
        if (enemyAnchor !in history.usedAnchors) {
            history.mark(enemyAnchor)
            val angle = Random.nextDouble(0.0, PI * 2.0)
            val enemyTargetRadius =
                (runtime?.params?.get(ENEMY_TARGET_RADIUS).asDouble ?: DEFAULT_ENEMY_TARGET_RADIUS).coerceAtLeast(8.0)
            val distance = Random.nextDouble(8.0, enemyTargetRadius)
            val height = randomFlightHeight(instance, runtime)
            return target.position().add(cos(angle) * distance, height, sin(angle) * distance)
        }

        val anchor = selectUnusedSpawnAnchor(history)
        history.mark(anchor.id)
        return positionFromAnchor(instance, runtime, instance.spawnPosition, anchor)
    }

    private fun createSpawnBiasedTarget(instance: MagicDragonEntity, runtime: PhaseRuntime?): Vec3 {
        val history = instance.simpleFlightTargetHistory
        val anchor = selectUnusedSpawnAnchor(history)
        history.mark(anchor.id)
        return positionFromAnchor(instance, runtime, instance.spawnPosition, anchor)
    }

    private fun selectUnusedSpawnAnchor(history: MagicDragonEntity.SimpleFlightTargetHistory): FlightAnchor {
        val available = RANDOM_ANCHORS.filter { it.id !in history.usedAnchors }
        if (available.isNotEmpty()) {
            return available.random()
        }
        history.resetKeepingLast()
        return RANDOM_ANCHORS.filter { it.id !in history.usedAnchors }.ifEmpty { RANDOM_ANCHORS }.random()
    }

    private fun positionFromAnchor(instance: MagicDragonEntity, runtime: PhaseRuntime?, origin: Vec3, anchor: FlightAnchor): Vec3 {
        val direction = anchor.direction.normalize()
        val minDistance = runtime?.params?.get(RANDOM_MIN_DISTANCE).asDouble ?: DEFAULT_RANDOM_MIN_DISTANCE
        val maxDistance = (runtime?.params?.get(RANDOM_MAX_DISTANCE).asDouble ?: DEFAULT_RANDOM_MAX_DISTANCE)
            .coerceAtLeast(minDistance)
        val distance = Random.nextDouble(minDistance, maxDistance)
        val height = randomFlightHeight(instance, runtime)
        return origin.add(direction.x * distance, height, direction.z * distance)
    }

    private fun randomFlightHeight(instance: MagicDragonEntity, runtime: PhaseRuntime?): Double {
        val groundY = instance.getOrResolveSimpleFlightSpawnGroundY()
            ?: return randomRange(
                runtime?.params?.get(RANDOM_MIN_HEIGHT).asDouble ?: DEFAULT_RANDOM_MIN_HEIGHT,
                runtime?.params?.get(RANDOM_MAX_HEIGHT).asDouble ?: DEFAULT_RANDOM_MAX_HEIGHT
            )

        val switchDistance =
            runtime?.params?.get(GROUND_HEIGHT_SWITCH_DISTANCE).asDouble ?: DEFAULT_GROUND_HEIGHT_SWITCH_DISTANCE
        val groundRandomMin =
            runtime?.params?.get(GROUND_RANDOM_MIN_HEIGHT).asDouble ?: DEFAULT_GROUND_RANDOM_MIN_HEIGHT
        val groundRandomMax =
            runtime?.params?.get(GROUND_RANDOM_MAX_HEIGHT).asDouble ?: DEFAULT_GROUND_RANDOM_MAX_HEIGHT
        return if (abs(instance.y - groundY) > switchDistance) {
            randomRange(groundRandomMin, groundRandomMax)
        } else {
            val lowerBound =
                groundY - instance.spawnPosition.y +
                        (runtime?.params?.get(GROUND_LOW_EXTRA_HEIGHT).asDouble ?: DEFAULT_GROUND_LOW_EXTRA_HEIGHT)
            if (lowerBound >= groundRandomMax) {
                groundRandomMax
            } else {
                Random.nextDouble(lowerBound, groundRandomMax)
            }
        }
    }

    private fun fixTargetHeight(instance: MagicDragonEntity, runtime: PhaseRuntime?, target: Vec3): Vec3 {
        val world = instance.level()
        val blockCheckYOffset =
            (runtime?.params?.get(BLOCK_CHECK_Y_OFFSET).asInt ?: DEFAULT_BLOCK_CHECK_Y_OFFSET).coerceAtLeast(0)
        val targetBeforeFix = BlockPos.containing(target).mutable().move(0, -blockCheckYOffset, 0)
        var blocked = false
        while (!world.getBlockState(targetBeforeFix).isAir) {
            targetBeforeFix.move(0, Random.nextInt(1, 3), 0)
            blocked = true
        }

        if (blocked) {
            val riseMin =
                (runtime?.params?.get(BLOCKED_TARGET_RISE_MIN).asInt ?: DEFAULT_BLOCKED_TARGET_RISE_MIN)
                    .coerceAtLeast(0)
            val riseMax =
                (runtime?.params?.get(BLOCKED_TARGET_RISE_MAX).asInt ?: DEFAULT_BLOCKED_TARGET_RISE_MAX)
                    .coerceAtLeast(riseMin + 1)
            targetBeforeFix.move(0, Random.nextInt(riseMin, riseMax), 0)
        }
        return targetBeforeFix.center
    }

    private fun randomRange(min: Double, max: Double): Double {
        val safeMax = max.coerceAtLeast(min)
        return if (safeMax == min) min else Random.nextDouble(min, safeMax)
    }

    private fun canFlyToTargetHeight(instance: MagicDragonEntity, target: Vec3): Boolean {
        val world = instance.level()
        val fromY = BlockPos.containing(instance.position()).y
        val toY = BlockPos.containing(target).y
        val minY = minOf(fromY, toY).coerceAtLeast(world.minBuildHeight)
        val maxY = maxOf(fromY, toY).coerceAtMost(world.maxBuildHeight - 1)
        for (y in minY..maxY) {
            val pos = BlockPos.containing(target.x, y.toDouble(), target.z)
            if (!world.getBlockState(pos).isAir) {
                return false
            }
        }
        return true
    }

    private fun updateTargetTracking(target: Vec3) {
        if (activeTarget == null || activeTarget!!.distanceToSqr(target) > 1.0E-6) {
            activeTarget = target
            activeTargetUseTicks = 0
            nearTargetTicks = 0
        }
        activeTargetUseTicks++
    }

    private fun skipCurrentTarget(instance: MagicDragonEntity, runtime: PhaseRuntime) {
        runtime.popTarget()
        clearTargetTracking(instance)
        ensureTargetBuffer(instance, runtime)
    }

    private fun clearTargetTracking(instance: MagicDragonEntity) {
        activeTarget = null
        activeTargetUseTicks = 0
        nearTargetTicks = 0
        instance.resetSimpleFlightOrbitState()
    }

    private fun horizontalForward(instance: MagicDragonEntity): Vec3 {
        val forward = instance.forward
        val horizontal = Vec3(forward.x, 0.0, forward.z)
        return if (horizontal.lengthSqr() <= 1.0E-6) Vec3(0.0, 0.0, 1.0) else horizontal.normalize()
    }

    private fun playTurnAnimation(instance: MagicDragonEntity, forward: Vec3, desiredDirection: Vec3) {
        val turnCross = forward.x * desiredDirection.z - forward.z * desiredDirection.x
        val animation = if (turnCross > 0.0) {
            MagicDragonAnimationState.BREATH_TURN_LEFT
        } else {
            MagicDragonAnimationState.BREATH_TURN_RIGHT
        }
        instance.playAnimation(animation)
    }

    override fun collisionEntity(
        targets: Set<LivingEntity>,
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        // 撞飞
        targets.forEach {
            it.addDeltaMovement(instance.deltaMovement.add(0.0, 1.0, 0.0))
            val source = it.damageSources().mobAttack(instance)
            // 造成伤害
            it.hurt(source, 15f)
            // 撕咬音效

        }
    }

    private data class FlightAnchor(
        val id: String,
        val direction: Vec3
    )
}
