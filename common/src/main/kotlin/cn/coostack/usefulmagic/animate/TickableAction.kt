package cn.coostack.usefulmagic.animate

import cn.coostack.cooparticlesapi.animation.AnimateAction
import cn.coostack.cooparticlesapi.api.controler.Tickable

class TickableAction(val cancelPredicate: TickableAction.() -> Boolean) : AnimateAction(), Tickable<TickableAction> {
    private val actions = ArrayList<TickableAction.() -> Unit>()
    private val postActions = ArrayList<TickableAction.() -> Unit>()
    private val doneActions = ArrayList<TickableAction.() -> Unit>()
    private val startActions = ArrayList<TickableAction.() -> Unit>()


    override fun checkDone(): Boolean {
        return cancelPredicate()
    }

    override fun tick() {
        actions.forEach { it() }
        postActions.forEach { it() }
    }

    override fun onStart() {
        startActions.forEach { it() }
    }

    override fun onDone() {
        doneActions.forEach { it() }
    }

    override fun addPreTickAction(action: TickableAction.() -> Unit): TickableAction {
        actions.add(action)
        return this
    }

    override fun addPreTickActionPost(action: TickableAction.() -> Unit): TickableAction {
        postActions.add(action)
        return this
    }

    fun addDoneAction(action: TickableAction.() -> Unit): TickableAction {
        doneActions.add(action)
        return this
    }

    fun addStartActions(action: TickableAction.() -> Unit): TickableAction {
        startActions.add(action)
        return this
    }
}
