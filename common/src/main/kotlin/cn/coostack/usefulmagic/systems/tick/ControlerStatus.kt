package cn.coostack.usefulmagic.systems.tick

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.usefulmagic.systems.tick.api.EntryStatus
import java.util.function.Predicate

class ControlerStatus<T : ServerControler<T>>(val controler: T, val validFunc: Predicate<T>) :
    EntryStatus<T> {
    override fun get(): T {
        return controler
    }

    override fun type(): Class<out T> {
        return controler::class.java
    }

    override fun isValid(): Boolean {
        return validFunc.test(controler)
    }

    override fun remove() {
        controler.remove()
    }
}