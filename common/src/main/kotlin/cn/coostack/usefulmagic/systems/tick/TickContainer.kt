package cn.coostack.usefulmagic.systems.tick

import cn.coostack.usefulmagic.systems.tick.api.EntryStatus
import java.util.UUID
import java.util.function.Supplier

@Suppress("UNCHECKED_CAST")
class TickContainer {
    private val container = HashMap<String, EntryStatus<*>>()

    private fun <T> getEntry(type: Class<T>): EntryStatus<T>? {
        val exact = container[type.name] as? EntryStatus<T>
        if (exact != null) {
            return exact
        }
        // Fallback: allow requesting a super type (e.g., inferred base class).
        @Suppress("UNCHECKED_CAST")
        return container.values.firstOrNull {
            type.isAssignableFrom(it.type())
        } as? EntryStatus<T>
    }

    private fun removeEntry(type: Class<*>): EntryStatus<*>? {
        val exact = container.remove(type.name)
        if (exact != null) {
            return exact
        }
        val key = container.entries.firstOrNull { type.isAssignableFrom(it.value.type()) }?.key
        return if (key != null) container.remove(key) else null
    }

    fun <T> get(type: Class<T>): T? {
        return getEntry(type)?.get()
    }

    fun <T> getOrCreate(
        type: Class<T>,
        supplier: Supplier<EntryStatus<T>>
    ): T {
        val existing = getEntry(type)
        if (existing != null) {
            return existing.get()
        }
        val build = supplier.get()
        put(build)
        return build.get()
    }

    inline fun <reified T> getOrCreate(
        supplier: Supplier<EntryStatus<T>>
    ): T {
        return getOrCreate(T::class.java, supplier)
    }

    fun <T> remove(type: Class<T>): T? {
        return removeEntry(type)?.get() as? T
    }

    inline fun <reified T> remove(): T? {
        return remove(T::class.java)
    }

    fun <T> cancel(type: Class<T>): Boolean {
        val entry = removeEntry(type) ?: return false
        entry.remove()
        return true
    }

    inline fun <reified T> cancel(): Boolean {
        return cancel(T::class.java)
    }

    fun remove(value: Any): Boolean {
        val key = container.entries.firstOrNull { it.value.get() === value }?.key ?: return false
        container.remove(key)
        return true
    }

    fun cancel(value: Any): Boolean {
        val key = container.entries.firstOrNull { it.value.get() === value }?.key ?: return false
        val entry = container.remove(key) ?: return false
        entry.remove()
        return true
    }

    fun put(status: EntryStatus<*>) {
        container[status.type().name] = status
    }

    fun cancelAll() {
        container.values.forEach {
            it.remove()
        }
    }

    fun isEmpty(): Boolean {
        return container.isEmpty()
    }

    fun clearNotValid() {
        val iterator = container.values.iterator()
        while (iterator.hasNext()) {
            val status = iterator.next()
            if (!status.isValid()) {
                iterator.remove()
            }
        }
    }

    inline fun <reified T> get(): T? {
        val type = T::class.java
        return get(type)
    }
}
