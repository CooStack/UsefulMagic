package cn.coostack.usefulmagic.entity.util

import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import java.util.concurrent.StructuredTaskScope

abstract class MobSpawner {
    var currentTick = 0
    var start = false
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
        onSpawn(entity)
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
        if (start) {
            onCancelSpawn()
        }
        currentTick = 0
        start = false
    }

}