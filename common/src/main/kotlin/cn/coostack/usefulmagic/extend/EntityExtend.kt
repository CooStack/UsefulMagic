package cn.coostack.usefulmagic.extend

import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.data.tracked.CooTrackerHolder
import cn.coostack.usefulmagic.data.tracked.TrackerManager
import cn.coostack.usefulmagic.entity.MagicEntityDataInit
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate

fun Entity.asHolder(): CooTrackerHolder = this as CooTrackerHolder

var Entity.charging: Boolean
    get() = this.asHolder().getCooTracker().getOrDefault(MagicEntityDataInit.CHARGING, false)
    set(value) = this.asHolder().getCooTracker().set(MagicEntityDataInit.CHARGING, value)

var Entity.chargingTick: Int
    get() = this.asHolder().getCooTracker().getOrDefault(MagicEntityDataInit.CHARGE_TICK, 0)
    set(value) = this.asHolder().getCooTracker().set(MagicEntityDataInit.CHARGE_TICK, value)


var Entity.chargedItem: ItemStack
    get() = this.asHolder().getCooTracker().getOrDefault(MagicEntityDataInit.CHARGED_ITEM, ItemStack.EMPTY)
    set(value) = this.asHolder().getCooTracker().set(MagicEntityDataInit.CHARGED_ITEM, value)

var Entity.chargedItemIdentity: Int
    get() = this.asHolder().getCooTracker().getOrDefault(MagicEntityDataInit.CHARGED_ITEM_IDENTITY, 0)
    set(value) = this.asHolder().getCooTracker().set(MagicEntityDataInit.CHARGED_ITEM_IDENTITY, value)

fun Entity.boxCenterPosition(): Vec3 = boundingBox.center

fun Entity.lookAtPos(lookAtPos: Vec3, yawSpeed: Float, pitchSpeed: Float) {
    val delta = lookAtPos.subtract(eyePosition)
    if (delta.lengthSqr() <= 1.0E-6) {
        return
    }
    val angles = Math3DUtil.calculateEulerAnglesToPoint(delta.toVector3f())
    val targetYaw = -90f - Math.toDegrees(angles.second.toDouble()).toFloat()
    val targetPitch = Math.toDegrees(angles.first.toDouble()).toFloat()
    val newYaw = rotateTowards(yRot, targetYaw, yawSpeed)
    val newPitch = rotateTowards(xRot, targetPitch, pitchSpeed)

    yRotO = yRot
    xRotO = xRot
    yRot = newYaw
    xRot = newPitch
    if (this is LivingEntity) {
        yHeadRot = newYaw
        yBodyRot = newYaw
    }
}

val Entity.serverLevel: ServerLevel?
    get() = level() as? ServerLevel


fun <E : Entity> E.serverLevelApply(consumer: E.(ServerLevel) -> Unit): E {
    val world = serverLevel ?: return this
    consumer(world)
    return this
}

fun <E : Entity, V> E.invokeIfServerLevel(consumer: E.(ServerLevel) -> V?): V? {
    val world = serverLevel ?: return null
    return consumer(world)
}

fun <E : Entity> E.invokeIfServerLevel(consumer: E.(ServerLevel) -> Unit?) {
    invokeIfServerLevel<E, Unit>(consumer)
}

private fun rotateTowards(current: Float, target: Float, maxStep: Float): Float {
    val step = wrapDegrees(target - current).coerceIn(-maxStep, maxStep)
    return current + step
}

private fun wrapDegrees(degrees: Float): Float {
    var wrapped = degrees % 360f
    if (wrapped >= 180f) wrapped -= 360f
    if (wrapped < -180f) wrapped += 360f
    return wrapped
}

var Entity.vec3Target: Vec3
    get() = this.asHolder().getCooTracker().getOrDefault(MagicEntityDataInit.VEC3_TARGET, Vec3.ZERO)
    set(value) {
        this.asHolder().getCooTracker().set(MagicEntityDataInit.VEC3_TARGET, value)
    }

var Entity.hasVec3Target: Boolean
    get() = this.asHolder().getCooTracker().getOrDefault(MagicEntityDataInit.HAS_VEC3_TARGET, false)
    set(value) {
        this.asHolder().getCooTracker().set(MagicEntityDataInit.HAS_VEC3_TARGET, value)
    }

fun Entity.resetChargeState() {
    charging = false
    chargingTick = 0
    chargedItem = ItemStack.EMPTY
    chargedItemIdentity = 0
    if (!level().isClientSide) {
        TrackerManager.applyHolder(this)
        if (this is Player) {
            TrackerManager.schedulePlayerChargeStateResync(this)
        }
    }
}

fun Entity.canSee(another: Entity): Boolean {
    if (level() != another.level()) return false

    val to = another.eyePosition
    val ctx = ClipContext(
        eyePosition, to,
        ClipContext.Block.COLLIDER,
        ClipContext.Fluid.NONE,
        this
    )
    val res = level().clip(ctx)

    return when (res.type) {
        HitResult.Type.MISS -> true
        HitResult.Type.ENTITY -> true
        else -> false
    }
}

fun Entity.canSee(to: Vec3): Boolean {
    val ctx = ClipContext(
        eyePosition, to,
        ClipContext.Block.COLLIDER,
        ClipContext.Fluid.NONE,
        this
    )
    val res = level().clip(ctx)

    return when (res.type) {
        HitResult.Type.MISS -> true
        HitResult.Type.ENTITY -> true
        else -> false
    }
}

inline fun <reified T : LivingEntity> Entity.searchEntities(boxSize: Double, filter: Predicate<T>): List<T> {
    return level().getEntitiesOfClass<T>(T::class.java, boundingBox.inflate(boxSize), filter)
}


fun Entity.searchLivingEntities(boxSize: Double, filter: Predicate<LivingEntity>): List<LivingEntity> {
    return level().getEntitiesOfClass(LivingEntity::class.java, boundingBox.inflate(boxSize), filter)
}
