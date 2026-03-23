package cn.coostack.usefulmagic.entity.util

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

abstract class MobSpawner {
    var currentTick = 0
        protected set
    var start = false
        private set
    var cancel = false
        private set

    var spawned = false
        private set

    abstract fun spawnCondition(): Boolean

    abstract fun getSpawnTicks(): Int

    /**
     * 此方法需要将spawnCondition返回的结果设置为false 否则会不断生成
     */
    abstract fun onSpawn(entity: LivingEntity)

    abstract fun getSpawnedEntity(): LivingEntity

    abstract fun getSpawnLocation(): Vec3

    /**
     * 达成开始条件时进行
     */
    abstract fun onStartSpawn()

    abstract fun doSpawnTick()

    /**
     * 召唤过程中被打断
     */
    abstract fun onCancelSpawn()

    fun spawn() {
        val entity = getSpawnedEntity()
        val world = entity.level()
        world.addFreshEntity(entity)
        spawned = true
        onSpawn(entity)
    }

    fun cancel() {
        if (!start) {
            return
        }
        cancel = true
    }

    /**
     * 在 block 或者其他的 tick方法内执行
     */
    fun tick() {
        if (spawnCondition()) {
            if (!start) onStartSpawn()
            start = true
            doSpawnTick()
            if (currentTick++ > getSpawnTicks()) {
                spawn()
                currentTick = 0
                start = false
            }
            return
        }
        if (cancel) {
            onCancelSpawn()
        }

        currentTick = 0
        start = false
        cancel = false
    }

}