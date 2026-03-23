package cn.coostack.usefulmagic.data.tracked


/**
 * 数据跟踪器，
 * 通过实体class注册，他和他的子类均有此方法，需要手动写ID用来区分
 *
 * 被实体持有
 */
class CooDataTracker {
    companion object {
        val registerEntityTypes = HashMap<String, CooTrackedData<*>>()

        fun <T> register(data: CooTrackedData<T>): CooTrackedData<T> {
            registerEntityTypes[data.id] = data
            return data
        }
    }

    val trackedData: MutableMap<String, Any> = HashMap()
    val trackedDirties = HashMap<String, Boolean>()
    val trackedTypes = HashMap<String, CooTrackedData<*>>()
    fun <T> set(data: CooTrackedData<T>, value: T) {
        val old = trackedData.put(data.id, value as Any)
        if (old !== value) {
            // 防止重复赋值时进行额外的操作
            trackedDirties[data.id] = true
        }
        trackedTypes[data.id] = data
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(data: CooTrackedData<T>): T {
        val value = trackedData[data.id]
        return value as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getOrDefault(data: CooTrackedData<T>, default: T): T {
        val value = trackedData[data.id] ?: default
        return runCatching { value as T }.getOrDefault(default)
    }


    fun getDirtiesDataAndClean(): Map<String, Any> {
        return trackedData.filter { (key, value) -> trackedDirties[key] ?: false }.onEach {
            trackedDirties[it.key] = false
        }
    }

    /**
     * client only
     *
     * @param other
     */
    fun applyChange(other: CooDataTracker) {
        trackedData.putAll(other.trackedData)
        // 设置dirty 为 false
        trackedDirties.putAll(other.trackedDirties)
        // 如果这里出现了新的那就塞
        trackedTypes.putAll(other.trackedTypes)
    }

}