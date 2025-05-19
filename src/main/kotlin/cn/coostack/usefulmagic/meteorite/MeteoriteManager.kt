package cn.coostack.usefulmagic.meteorite

import java.util.UUID

object MeteoriteManager {
    private val meteorites = HashSet<Meteorite>()

    fun clearAll() {
        meteorites.onEach {
            it.clear()
        }.clear()
    }

    fun addTicks(meteorite: Meteorite) {
        if (!meteorite.spawned || !meteorite.valid) {
            return
        }
        meteorites.add(meteorite)
    }

    fun doTick() {
        val iterator = meteorites.iterator()
        while (iterator.hasNext()) {
            val meteorite = iterator.next()
            if (!meteorite.valid || !meteorite.spawned || meteorite.hit) {
                iterator.remove()
                continue
            }
            meteorite.tick()
        }
    }

}