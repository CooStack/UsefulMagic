package cn.coostack.usefulmagic.entity.custom.dragon.phases

import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonAnimationState
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.util.EntityPoseUtil
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime
import cn.coostack.usefulmagic.extend.asDouble
import cn.coostack.usefulmagic.extend.asFloat
import cn.coostack.usefulmagic.extend.asInt
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 靠近地面可能会触发走路
 *
 */
class DragonWalkPhase : PhaseDefinition<MagicDragonEntity> {
    companion object {
        const val ID = "walk_phase"
        // 接近地面目标的距离平方阈值参数键，需要传入 Double
        const val ARRIVE_DISTANCE = "arrive_distance"
        // 行走目标点的最小搜索半径参数键，需要传入 Double
        const val WALK_RADIUS_MIN = "walk_radius_min"
        // 行走目标点的最大搜索半径参数键，需要传入 Double
        const val WALK_RADIUS_MAX = "walk_radius_max"
        // 每次寻找地面目标的最大重试次数参数键，需要传入 Int
        const val TARGET_RETRY_COUNT = "target_retry_count"
        // 再次选择目标前的最小等待 tick 参数键，需要传入 Int
        const val SELECT_TARGET_COUNTDOWN_MIN = "select_target_countdown_min"
        // 再次选择目标前的最大等待 tick 参数键，需要传入 Int
        const val SELECT_TARGET_COUNTDOWN_MAX = "select_target_countdown_max"
        // 转身时的速度参数键，需要传入 Float
        const val TURN_SPEED = "turn_speed"
        // 行走速度下限参数键，需要传入 Double
        const val MIN_WALK_SPEED = "min_walk_speed"

        private const val DEFAULT_ARRIVE_DISTANCE = 2.25
        private const val DEFAULT_WALK_RADIUS_MIN = 4.0
        private const val DEFAULT_WALK_RADIUS_MAX = 12.0
        private const val DEFAULT_TARGET_RETRY_COUNT = 8
        private const val DEFAULT_SELECT_TARGET_COUNTDOWN_MIN = 15
        private const val DEFAULT_SELECT_TARGET_COUNTDOWN_MAX = 40
        private const val DEFAULT_TURN_SPEED = 8f
        private const val DEFAULT_MIN_WALK_SPEED = 0.15
        val HOLDER = PhaseRegistries.register(ID) { DragonWalkPhase() }
    }

    private var selectTargetCountdown = 0

    override fun canBegin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): Boolean {
        val level = instance.level()
        // 距离地面不足3个方块
        val blockPosition = instance.blockPosition().mutable()
        for (i in 0 until 5) {
            if (!level.getBlockState(blockPosition).isAir) {
                return true
            }
            blockPosition.move(0, -1, 0)
        }
        return false
    }

    override fun begin(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        instance.isNoGravity = false
        instance.noPhysics = false
    }

    override fun step(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): PhaseResult {
        // 可能会摔下去 然后重新起飞
        if (!canBegin(instance, runtime)) {
            instance.navigation.stop()
            return PhaseResult.Reset
        }

        val target = runtime.peekTargetOrNull() ?: selectRandomGroundTarget(instance, runtime)
        if (target == null) {
            instance.navigation.stop()
            instance.playAnimation(MagicDragonAnimationState.WALK)
            return PhaseResult.Continue
        }

        val offset = target.subtract(instance.position())
        val horizontalOffset = Vec3(offset.x, 0.0, offset.z)
        val arriveDistance = runtime.params[ARRIVE_DISTANCE].asDouble ?: DEFAULT_ARRIVE_DISTANCE
        if (horizontalOffset.lengthSqr() <= arriveDistance) {
            runtime.popTarget()
            instance.navigation.stop()
            return PhaseResult.Continue
        }

        if (horizontalOffset.lengthSqr() > 1.0E-6) {
            val nextYaw = EntityPoseUtil.lerpRotationFromDirectionAsSpeed(
                instance.forward,
                horizontalOffset,
                runtime.params[TURN_SPEED].asFloat ?: DEFAULT_TURN_SPEED
            )
            instance.yRotO = instance.yRot
            instance.yRot = nextYaw
            instance.yHeadRot = nextYaw
            instance.yBodyRot = nextYaw
        }

        val speed = instance.getAttributeValue(Attributes.MOVEMENT_SPEED)
            .coerceAtLeast(runtime.params[MIN_WALK_SPEED].asDouble ?: DEFAULT_MIN_WALK_SPEED)
        instance.navigation.moveTo(target.x, target.y, target.z, speed)
        instance.playAnimation(MagicDragonAnimationState.WALK)

        return PhaseResult.Continue
    }

    override fun end(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ) {
        instance.navigation.stop()
        instance.isNoGravity = true
        instance.noPhysics = true
        // 起飞
        instance.playAnimation(MagicDragonAnimationState.TAKE_OFF)
    }

    override fun id(): String {
        return ID
    }

    private fun selectRandomGroundTarget(
        instance: MagicDragonEntity,
        runtime: PhaseRuntime
    ): Vec3? {
        if (selectTargetCountdown-- > 0) {
            return null
        }
        val minCountdown =
            (runtime.params[SELECT_TARGET_COUNTDOWN_MIN].asInt ?: DEFAULT_SELECT_TARGET_COUNTDOWN_MIN)
                .coerceAtLeast(0)
        val maxCountdown =
            (runtime.params[SELECT_TARGET_COUNTDOWN_MAX].asInt ?: DEFAULT_SELECT_TARGET_COUNTDOWN_MAX)
                .coerceAtLeast(minCountdown + 1)
        selectTargetCountdown = Random.nextInt(minCountdown, maxCountdown)

        val retryCount = (runtime.params[TARGET_RETRY_COUNT].asInt ?: DEFAULT_TARGET_RETRY_COUNT).coerceAtLeast(1)
        repeat(retryCount) {
            val angle = Random.nextDouble(0.0, PI * 2.0)
            val minRadius = runtime.params[WALK_RADIUS_MIN].asDouble ?: DEFAULT_WALK_RADIUS_MIN
            val maxRadius = (runtime.params[WALK_RADIUS_MAX].asDouble ?: DEFAULT_WALK_RADIUS_MAX)
                .coerceAtLeast(minRadius)
            val distance = Random.nextDouble(minRadius, maxRadius)
            val center = if (instance.position().distanceToSqr(instance.spawnPosition) > 144.0) {
                instance.spawnPosition
            } else {
                instance.position()
            }

            val groundTarget = findGroundTarget(
                instance,
                center.x + cos(angle) * distance,
                center.z + sin(angle) * distance
            ) ?: return@repeat

            runtime.addTarget(groundTarget)
            return groundTarget
        }
        return null
    }

    private fun findGroundTarget(
        instance: MagicDragonEntity,
        x: Double,
        z: Double
    ): Vec3? {
        val level = instance.level()
        val mutable = BlockPos.containing(x, instance.y + 3.0, z).mutable()
        val minY = level.minBuildHeight + 1

        while (mutable.y > minY && level.getBlockState(mutable).isAir) {
            mutable.move(0, -1, 0)
        }
        if (level.getBlockState(mutable).isAir) {
            return null
        }

        val stand = mutable.above()
        val head = stand.above()
        if (!level.getBlockState(stand).isAir || !level.getBlockState(head).isAir) {
            return null
        }
        return Vec3(stand.x + 0.5, stand.y.toDouble(), stand.z + 0.5)
    }
}
