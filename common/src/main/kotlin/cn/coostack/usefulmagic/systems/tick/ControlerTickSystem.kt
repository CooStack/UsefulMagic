package cn.coostack.usefulmagic.systems.tick

import java.util.UUID

object ControlerTickSystem {
    val controls = HashMap<UUID, TickContainer>()

    fun get(uuid: UUID): TickContainer {
        if (controls.containsKey(uuid)) {
            return controls[uuid]!!
        }
        val control = TickContainer()
        controls[uuid] = control
        return control
    }


    fun clearNotValid() {
        val iterator = controls.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val value = entry.value
            value.clearNotValid()
            if (value.isEmpty()) {
                iterator.remove()
            }
        }
    }
}