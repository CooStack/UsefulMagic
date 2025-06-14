package cn.coostack.usefulmagic.entity.util

import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import java.util.concurrent.StructuredTaskScope

abstract class MobSpawner {
    var currentTick = 0
    var start = false
    abstract fun spawnCondition(): Boolean

    abstract fun getSpawnTicks(): Int

    /**
     * 此方法需要将spawnCondition返回的结果设置为false 否则会不断生成
     */
    abstract fun onSpawn()

    abstract fun getSpawnedEntity(): LivingEntity

    abstract fun getSpawnLocation(): Vec3d

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
        val spawnLoc = getSpawnLocation()
        val world = entity.world
        world.spawnEntity(entity)
        entity.setPosition(spawnLoc)
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
                onSpawn()
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