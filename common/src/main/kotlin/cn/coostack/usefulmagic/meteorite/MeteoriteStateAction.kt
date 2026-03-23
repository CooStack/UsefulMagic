package cn.coostack.usefulmagic.meteorite

interface MeteoriteStateAction {
    fun tick(context: MeteoriteAnimateAction)

    fun canNext(context: MeteoriteAnimateAction): Boolean

    fun next(context: MeteoriteAnimateAction)

    fun start(context: MeteoriteAnimateAction)
}