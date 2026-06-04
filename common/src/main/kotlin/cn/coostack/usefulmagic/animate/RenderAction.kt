package cn.coostack.usefulmagic.animate

import cn.coostack.cooparticlesapi.animation.AnimateAction
import cn.coostack.cooparticlesapi.api.controler.Tickable
import cn.coostack.cooparticlesapi.renderer.RenderEntity
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager

class RenderAction<T : RenderEntity>(val renderEntity: T) : AnimateAction(), Tickable<RenderAction<T>> {
    private val actions = ArrayList<RenderAction<T>.() -> Unit>()
    private val postActions = ArrayList<RenderAction<T>.() -> Unit>()
    private var cancelMethod: RenderAction<T>.(T) -> Unit = {
        it.remove()
    }

    fun cancelMethod(invoker: RenderAction<T>.(T) -> Unit): RenderAction<T> {
        this.cancelMethod = invoker
        return this
    }

    override fun checkDone(): Boolean {
        return renderEntity.canceled
    }

    override fun tick() {
        actions.forEach { it() }
        postActions.forEach { it() }
    }

    override fun onStart() {
        ServerRenderEntityManager.spawn(renderEntity)
    }

    override fun onDone() {
        cancelMethod(renderEntity)
    }

    override fun addPreTickAction(action: RenderAction<T>.() -> Unit): RenderAction<T> {
        actions.add(action)
        return this
    }

    override fun addPreTickActionPost(action: RenderAction<T>.() -> Unit): RenderAction<T> {
        postActions.add(action)
        return this
    }
}
