package cn.coostack.usefulmagic.animate

import cn.coostack.cooparticlesapi.animation.AnimateAction
import cn.coostack.cooparticlesapi.api.controler.Tickable
import cn.coostack.cooparticlesapi.network.particle.composition.ParticleComposition
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager

class CompositionAction<T : ParticleComposition>(val composition: T) : AnimateAction(), Tickable<CompositionAction<T>> {
    private val actions = ArrayList<CompositionAction<T>.() -> Unit>()
    private var cancelMethod: CompositionAction<T>.(T) -> Unit = {
        it.remove()
    }

    fun cancelMethod(invoker: CompositionAction<T>.(T) -> Unit): CompositionAction<T> {
        this.cancelMethod = invoker
        return this
    }

    override fun checkDone(): Boolean {
        return composition.canceled
    }

    override fun tick() {
        actions.forEach { it() }
    }

    override fun onStart() {
        ParticleCompositionManager.spawn(composition)
    }

    override fun onDone() {
        cancelMethod(composition)
    }

    override fun addPreTickAction(action: CompositionAction<T>.() -> Unit): CompositionAction<T> {
        actions.add(action)
        return this
    }
}
