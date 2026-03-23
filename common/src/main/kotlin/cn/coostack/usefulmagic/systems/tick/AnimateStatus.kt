package cn.coostack.usefulmagic.systems.tick

import cn.coostack.cooparticlesapi.animation.Animate
import cn.coostack.usefulmagic.systems.tick.api.EntryStatus

class AnimateStatus(val animate: Animate) : EntryStatus<Animate> {
    override fun get(): Animate {
        return animate
    }

    override fun type(): Class<out Animate> {
        return animate::class.java
    }

    override fun isValid(): Boolean {
        return !animate.done && animate.display
    }

    override fun remove() {
        animate.cancel()
    }
}