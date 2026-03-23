package cn.coostack.usefulmagic.systems.tick.api

interface EntryStatus<T> {
    fun get(): T
    fun type(): Class<out T>
    fun isValid(): Boolean
    fun remove()
}
