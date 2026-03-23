package cn.coostack.usefulmagic.entity.util.phases

import cn.coostack.cooparticlesapi.utils.Math3DUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import java.util.Optional
import java.util.function.Consumer
import kotlin.collections.ArrayDeque
import kotlin.collections.Map
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty

/**
 * - 负责存储数据，
 * - 序列化
 * 目前缺少外部输入参数变体的能力
 */
class PhaseRuntime(
    val id: String,
    private val onChanged: () -> Unit = {}
) {


    var onStarted: PhaseRuntime.() -> Unit = {}
    var onContinue: PhaseRuntime.() -> Unit = {}
    var onReset: PhaseRuntime.(new: PhaseRuntime) -> Unit = {}
    var onChangePhase: PhaseRuntime.(PhaseRuntime) -> Unit = {}
    var onCompleted: PhaseRuntime.(Map<String, Any>) -> Unit = {}

    var phaseAge = 0
    var isBegin = false
    var isEnd = false

    var targets = ArrayDeque<Vec3>()
    var params = CompoundTag()

    private fun markChanged() {
        onChanged()
    }

    fun configure(vararg entries: Pair<String, Tag>): PhaseRuntime {
        entries.forEach {
            params.put(it.first, it.second)
        }
        if (entries.isNotEmpty()) {
            markChanged()
        }
        return this
    }


    fun listenStart(listener: PhaseRuntime.() -> Unit): PhaseRuntime {
        this.onStarted = listener
        return this
    }

    fun listenContinue(listener: PhaseRuntime.() -> Unit): PhaseRuntime {
        this.onContinue = listener
        return this
    }

    fun listenReset(listener: PhaseRuntime.(new: PhaseRuntime) -> Unit): PhaseRuntime {
        this.onReset = listener
        return this
    }

    /**
     * 当修改为其他的phase时执行
     *
     * @param listener
     * @receiver
     * @return
     */
    fun listenChangePhase(listener: PhaseRuntime.(new: PhaseRuntime) -> Unit): PhaseRuntime {
        this.onChangePhase = listener
        return this
    }

    fun listenComplete(listener: PhaseRuntime.(data: Map<String, Any>) -> Unit): PhaseRuntime {
        this.onCompleted = listener
        return this
    }

    operator fun get(key: String): Tag? {
        return params[key]
    }


    fun addTarget(target: Vec3): PhaseRuntime {
        targets.add(target)
        markChanged()
        return this
    }

    fun peekTargetOrNull(): Vec3? = targets.firstOrNull()

    fun peekTarget(): Optional<Vec3> = Optional.ofNullable(targets.firstOrNull())

    fun peekTargetIfPrecent(consumer: Consumer<Vec3>) {
        targets.firstOrNull()?.let {
            consumer.accept(it)
        }
    }

    fun popTarget(): Vec3? {
        val removed = targets.removeFirstOrNull()
        if (removed != null) {
            markChanged()
        }
        return removed
    }

    fun hasTarget() = targets.isNotEmpty()


    fun popTargetIfPrecent(consumer: Consumer<Vec3>) {
        targets.removeFirstOrNull()?.let {
            consumer.accept(it)
            markChanged()
        }
    }

    fun setCurrentTarget(target: Vec3) {
        targets.apply {
            removeFirstOrNull()
            addFirst(target)
        }
        markChanged()
    }


    fun clearTargets() {
        if (targets.isNotEmpty()) {
            targets.clear()
            markChanged()
        }
    }

    fun inheritanceTargets(lastRuntime: PhaseRuntime) {
        targets.addAll(lastRuntime.targets)
        if (lastRuntime.targets.isNotEmpty()) {
            markChanged()
        }
    }

    fun isArriveCurrentTarget(holder: LivingEntity, distance: Double): Boolean {
        val current = peekTargetOrNull() ?: return true
        return Math3DUtil.isPointCrossBySphere(
            holder.position(),
            holder.deltaMovement,
            distance,
            current
        )
    }

    fun init() {
        phaseAge = 0
        isBegin = false
        isEnd = false
        markChanged()
    }
}
