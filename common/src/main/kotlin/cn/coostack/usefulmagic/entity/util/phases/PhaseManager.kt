package cn.coostack.usefulmagic.entity.util.phases

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*

typealias PhaseSetup = PhaseRuntime.() -> Unit

/**
 * 路径移动状态机
 * # 设定：
 *  - 当 A的definition的tick返回了next result时 且 next 的 canBegin返回false， 那么就会执行默认的Definition
 */
class PhaseManager<T : LivingEntity>(
    var defaultPhase: PhaseDefinition<T>,
    val instance: T,
    private val onPhaseChanged: () -> Unit = {},
    private val collisionTargetPredicate: (LivingEntity) -> Boolean = { it !== instance }
) {
    private var currentPhase = defaultPhase
    private var runtime = createRuntime(currentPhase.id())

    companion object {
        private const val KEY_CURRENT_PHASE_ID = "current_phase_id"
        private const val KEY_RUNTIME = "runtime"
        private const val KEY_PHASE_AGE = "phase_age"
        private const val KEY_IS_BEGIN = "is_begin"
        private const val KEY_IS_END = "is_end"
        private const val KEY_TARGETS = "targets"
        private const val KEY_PARAMS = "params"
        private const val KEY_TARGET_X = "x"
        private const val KEY_TARGET_Y = "y"
        private const val KEY_TARGET_Z = "z"
    }

    private fun syncPhaseState() {
        onPhaseChanged()
    }

    private fun createRuntime(id: String): PhaseRuntime {
        return PhaseRuntime(id) {
            syncPhaseState()
        }
    }

    fun forceSetPhase(phase: PhaseDefinition<T>) {
        if (runtime.isBegin && !runtime.isEnd) {
            currentPhase.end(instance, runtime)
        }
        currentPhase = phase
        runtime = createRuntime(phase.id())
        beginOnRuntime()
        syncPhaseState()
    }

    fun forceSetPhase(phase: PhaseDefinition<T>, runtimeConsumer: PhaseSetup) {
        if (runtime.isBegin && !runtime.isEnd) {
            currentPhase.end(instance, runtime)
        }
        currentPhase = phase
        runtime = createRuntime(phase.id()).apply {
            runtimeConsumer()
        }
        beginOnRuntime()
        syncPhaseState()
    }

    fun getCurrentPhaseID() = currentPhase.id()

    fun getCurrentPhase(): PhaseDefinition<T> = currentPhase

    @Suppress("UNCHECKED_CAST")
    fun <P : PhaseDefinition<T>> getCurrentPhaseAs(): P = currentPhase as P

    /**
     * 如果输入的phase不能begin 则不设置
     *
     * @param phase
     */
    fun trySetPhase(phase: PhaseDefinition<T>): Boolean {
        val new = createRuntime(phase.id())
        if (!phase.canBegin(instance, new)) {
            return false
        }
        if (runtime.isBegin && !runtime.isEnd) {
            currentPhase.end(instance, runtime)
        }
        currentPhase = phase
        runtime = new
        syncPhaseState()
        return true
    }

    /**
     * 如果输入的phase不能begin 则不设置
     *
     * @param phase
     */
    fun trySetPhase(phase: PhaseDefinition<T>, runtimeConsumer: PhaseSetup): Boolean {
        val new = createRuntime(phase.id()).apply {
            runtimeConsumer()
        }
        if (!phase.canBegin(instance, new)) {
            return false
        }
        if (runtime.isBegin && !runtime.isEnd) {
            currentPhase.end(instance, runtime)
        }
        currentPhase = phase
        runtime = new
        syncPhaseState()
        return true
    }

    /**
     * 从runtime中插入一个目标位置
     *
     */
    fun insertTarget(target: Vec3) {
        runtime.addTarget(target)
    }

    fun peekTargetOrNull() = runtime.peekTargetOrNull()

    fun peekTarget() = runtime.peekTarget()

    fun popTargetOrNull() = runtime.popTarget()

    /**
     * 直接修改当前位置
     *
     */
    fun setCurrentTarget(target: Vec3) {
        runtime.setCurrentTarget(target)
    }

    fun tickPhase() {
        if (runtime.isBegin && !runtime.isEnd) {
            val res = currentPhase.step(instance, runtime)

            // 碰撞检测
            currentPhase.runAsIfType<PhaseCollisionEntity<T>> {
                val box = instance.boundingBox.inflate(0.8)
                val level = instance.level()
                val targets = level.getEntitiesOfClass(LivingEntity::class.java, box) {
                    collisionTargetPredicate(it)
                }
                collisionEntity(targets.toSet(), instance, runtime)
            }

            runtime.phaseAge++

            if (res is PhaseResult.Reset) {
                val old = runtime
                forceSetPhase(defaultPhase)
                old.onReset(old, runtime)
                return
            }
            if (res is PhaseResult.Complete) {
                runtime.onCompleted(runtime, res.data)
                forceSetPhase(defaultPhase)
                return
            }
            if (res is PhaseResult.Next) {
                val old = runtime
                @Suppress("UNCHECKED_CAST")
                if (!trySetPhase(res.phase as PhaseDefinition<T>, res.setup)) {
                    forceSetPhase(defaultPhase)
                }
                old.onChangePhase(old, runtime)
                if (res.inheritTargets) {
                    runtime.targets.addAll(old.targets)
                }
            } else {
                runtime.onContinue(runtime)
                syncPhaseState()
            }
        } else if (currentPhase.canBegin(instance, runtime)) {
            beginOnRuntime()
        }
    }

    fun resetDefaultPhase() {
        forceSetPhase(defaultPhase)
    }


    private fun beginOnRuntime() {
        runtime.init()
        runtime.isBegin = true
        currentPhase.begin(instance, runtime)
        runtime.onStarted(runtime)
        syncPhaseState()
    }


    /**
     * 保存phase的一些实例化信息
     */
    fun loadFromCompound(tag: CompoundTag) {
        val phaseId = if (tag.contains(KEY_CURRENT_PHASE_ID)) {
            tag.getString(KEY_CURRENT_PHASE_ID)
        } else {
            defaultPhase.id()
        }
        currentPhase = if (phaseId == defaultPhase.id()) {
            defaultPhase
        } else {
            PhaseRegistries.buildOrThrow(phaseId)
        }
        runtime = createRuntime(currentPhase.id())
        if (!tag.contains(KEY_RUNTIME, Tag.TAG_COMPOUND.toInt())) {
            return
        }

        val runtimeTag = tag.getCompound(KEY_RUNTIME)
        val shouldRestart = runtimeTag.getBoolean(KEY_IS_BEGIN) && !runtimeTag.getBoolean(KEY_IS_END)
        runtime.phaseAge = runtimeTag.getInt(KEY_PHASE_AGE)
        runtime.isBegin = runtimeTag.getBoolean(KEY_IS_BEGIN)
        runtime.isEnd = runtimeTag.getBoolean(KEY_IS_END)
        runtime.clearTargets()

        val targetList = runtimeTag.getList(KEY_TARGETS, Tag.TAG_COMPOUND.toInt())
        for (i in targetList.indices) {
            val targetTag = targetList.getCompound(i)
            runtime.addTarget(
                Vec3(
                    targetTag.getDouble(KEY_TARGET_X),
                    targetTag.getDouble(KEY_TARGET_Y),
                    targetTag.getDouble(KEY_TARGET_Z)
                )
            )
        }

        if (shouldRestart) {
            val savedPhaseAge = runtime.phaseAge
            val savedIsEnd = runtime.isEnd
            currentPhase.begin(instance, runtime)
            runtime.phaseAge = savedPhaseAge
            runtime.isBegin = true
            runtime.isEnd = savedIsEnd
            runtime.onStarted(runtime)
            syncPhaseState()
        }
    }

    fun saveAsCompound(): CompoundTag {
        val tag = CompoundTag()
        tag.putString(KEY_CURRENT_PHASE_ID, currentPhase.id())

        val runtimeTag = CompoundTag()
        runtimeTag.putInt(KEY_PHASE_AGE, runtime.phaseAge)
        runtimeTag.putBoolean(KEY_IS_BEGIN, runtime.isBegin)
        runtimeTag.putBoolean(KEY_IS_END, runtime.isEnd)

        val targetList = ListTag()
        for (target in runtime.targets) {
            val targetTag = CompoundTag()
            targetTag.putDouble(KEY_TARGET_X, target.x)
            targetTag.putDouble(KEY_TARGET_Y, target.y)
            targetTag.putDouble(KEY_TARGET_Z, target.z)
            targetList.add(targetTag)
        }
        runtimeTag.put(KEY_TARGETS, targetList)
        runtimeTag.put(KEY_PARAMS, runtime.params)
        tag.put(KEY_RUNTIME, runtimeTag)
        return tag
    }

}
