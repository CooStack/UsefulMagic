package cn.coostack.usefulmagic.gui.magicexchange

object MagicExchangeOpenSignal {
    private var requested = false

    fun request() {
        requested = true
    }

    fun consume(): Boolean {
        val value = requested
        requested = false
        return value
    }
}
