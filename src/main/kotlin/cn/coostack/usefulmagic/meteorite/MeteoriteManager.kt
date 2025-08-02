package cn.coostack.usefulmagic.meteorite

import net.minecraft.util.math.BlockPos
import java.util.UUID

object MeteoriteManager {
    private val meteorites = HashMap<UUID, Meteorite>()

    fun clearAll() {
        meteorites.onEach {
            it.value.clear()
        }.clear()
    }

    fun addTicks(meteorite: Meteorite) {
        if (!meteorite.spawned || !meteorite.valid) {
            return
        }
        meteorites[meteorite.uuid] = meteorite
    }

    fun getFromSingleEntity(entity: MeteoriteFallingBlockEntity): Meteorite? {
        return meteorites.filter {
            it.value.entities.containsKey(entity.uuid)
        }.values.firstOrNull()
    }


    fun doTick() {
        val iterator = meteorites.iterator()
        while (iterator.hasNext()) {
            val meteorite = iterator.next().value
            if (!meteorite.valid || !meteorite.spawned || meteorite.hit) {
                iterator.remove()
                continue
            }
            val world = meteorite.world ?: continue
            if (world.shouldTick(BlockPos.ofFloored(meteorite.origin))) {
                meteorite.tick()
            }
        }
    }

}