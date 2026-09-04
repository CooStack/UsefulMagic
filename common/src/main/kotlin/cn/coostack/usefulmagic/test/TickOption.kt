package cn.coostack.usefulmagic.test

import cn.coostack.cooparticlesapi.test.TickingTestOption

class TickOption(testingTick: Int = 100) : TickingTestOption<Unit>(testingTick) {
    val tickActions = arrayListOf<TickOption.() -> Unit>()
    val failedActions = arrayListOf<TickOption.() -> Unit>()
    val successActions = arrayListOf<TickOption.() -> Unit>()
    val startActions = arrayListOf<TickOption.() -> Unit>()
    val stopActions = arrayListOf<TickOption.() -> Unit>()


    companion object {
        const val ID = "tick_option"
    }

    fun addTickAction(tickAction: TickOption.() -> Unit) = apply {
        tickActions.add(tickAction)
    }
    fun addFailedAction(tickAction: TickOption.() -> Unit) = apply {
        failedActions.add(tickAction)
    }
    fun addSuccessAction(tickAction: TickOption.() -> Unit) = apply {
        successActions.add(tickAction)
    }
    fun addStartAction(tickAction: TickOption.() -> Unit) = apply {
        startActions.add(tickAction)
    }
    fun addStopAction(tickAction: TickOption.() -> Unit) = apply {
        stopActions.add(tickAction)
    }

    override fun start() {
        startActions.forEach {
            it.invoke(this)
        }
    }

    override fun stop() {
        stopActions.forEach {
            it.invoke(this)
        }
    }

    override fun onFailed() {
        failedActions.forEach {
            it.invoke(this)
        }
    }

    override fun onSuccess() {
        successActions.forEach {
            it.invoke(this)
        }
    }

    override fun doTick() {
        super.doTick()
        runCatching {
            tickActions.forEach {
                it.invoke(this)
            }
        }.onFailure {
            onFailed()
        }
        if (testingTick <= 0 && testingTick != -1) {
            onSuccess()
        }
    }

    override fun paramTarget() {
    }

    override fun optionID(): String {
        return ID
    }
}