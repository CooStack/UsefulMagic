package cn.coostack.usefulmagic.managers.server

import cn.coostack.usefulmagic.skill.api.EntitySkillManager
import java.util.UUID

/**
 * 防止因为傻逼区块卸载导致 SkillManager 被重复创建
 * 从而导致粒子遗留的BUG
 */
object SkillManagerManager {

    val cache = mutableMapOf<UUID, EntitySkillManager>()

    fun loadFromCache(cacheUUID: UUID): EntitySkillManager? {
        return cache[cacheUUID]
    }


    fun setCache(manager: EntitySkillManager) {
        cache[manager.cacheUUID] = manager
    }

    fun removeCache(manager: UUID) {
        cache.remove(manager)
    }

    fun clearCacheIfOwnerDead() {
        val iterator = cache.iterator()
        while (iterator.hasNext()) {
            val manager = iterator.next().value
            if (manager.owner.isDead) {
                iterator.remove()
            }
        }
    }
}