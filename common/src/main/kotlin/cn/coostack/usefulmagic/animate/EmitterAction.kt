package cn.coostack.usefulmagic.animate

import cn.coostack.cooparticlesapi.animation.AnimateAction
import cn.coostack.cooparticlesapi.api.controler.Tickable
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager

class EmitterAction<T : ParticleEmitters>(val emitter: T) : AnimateAction(), Tickable<EmitterAction<T>> {
    private val actions = ArrayList<EmitterAction<T>.() -> Unit>()
    private val postActions = ArrayList<EmitterAction<T>.() -> Unit>()
    private var cancelMethod: EmitterAction<T>.(T) -> Unit = {
        it.remove()
    }

    fun cancelMethod(invoker: EmitterAction<T>.(T) -> Unit): EmitterAction<T> {
        this.cancelMethod = invoker
        return this
    }

    override fun checkDone(): Boolean {
        return emitter.canceled
    }

    override fun tick() {
        actions.forEach { it() }
        postActions.forEach { it() }
    }

    override fun onStart() {
        ParticleEmittersManager.spawnEmitters(emitter)
    }

    override fun onDone() {
        cancelMethod(emitter)
    }

    override fun addPreTickAction(action: EmitterAction<T>.() -> Unit): EmitterAction<T> {
        actions.add(action)
        return this
    }

    override fun addPreTickActionPost(action: EmitterAction<T>.() -> Unit): EmitterAction<T> {
        postActions.add(action)
        return this
    }
}
