package cn.coostack.usefulmagic.animate

import cn.coostack.cooparticlesapi.animation.AnimateAction
import cn.coostack.cooparticlesapi.api.controler.Tickable
import cn.coostack.cooparticlesapi.display.DisplayEntity
import cn.coostack.cooparticlesapi.display.DisplayEntityManager

class DisplayAction<T : DisplayEntity>(val displayEntity: T) : AnimateAction(), Tickable<DisplayAction<T>> {
    private val actions = ArrayList<DisplayAction<T>.() -> Unit>()
    private val postActions = ArrayList<DisplayAction<T>.() -> Unit>()
    private var cancelMethod: DisplayAction<T>.(T) -> Unit = {
        it.remove()
    }

    fun cancelMethod(invoker: DisplayAction<T>.(T) -> Unit): DisplayAction<T> {
        this.cancelMethod = invoker
        return this
    }

    override fun checkDone(): Boolean {
        return !displayEntity.isValid()
    }

    override fun tick() {
        actions.forEach { it() }
        postActions.forEach { it() }
    }

    override fun onStart() {
        DisplayEntityManager.spawn(displayEntity)
    }

    override fun onDone() {
        cancelMethod(displayEntity)
    }

    override fun addPreTickAction(action: DisplayAction<T>.() -> Unit): DisplayAction<T> {
        actions.add(action)
        return this
    }

    override fun addPreTickActionPost(action: DisplayAction<T>.() -> Unit): DisplayAction<T> {
        postActions.add(action)
        return this
    }
}
