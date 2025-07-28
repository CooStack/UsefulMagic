package cn.coostack.usefulmagic.utils

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import java.util.UUID

object FriendFilterHelper {

    /**
     * 如果是朋友,则返回true
     */
    fun filterFriend(source: LivingEntity, target: UUID): Boolean {
        if (source !is PlayerEntity) return false
        return filterFriend(source, target)
    }

    /**
     * 如果不是朋友,则返回true
     */
    fun filterNotFriend(source: LivingEntity, target: UUID): Boolean {
        if (source !is PlayerEntity) return true
        return filterNotFriend(source, target)
    }

    /**
     * 如果是朋友,则返回true
     */
    fun filterFriend(source: PlayerEntity, target: UUID): Boolean {
        val data = UsefulMagic.state.getDataFromServer(source.uuid)
        return data.isFriend(target)
    }

    /**
     * 如果不是朋友,则返回true
     */
    fun filterNotFriend(source: PlayerEntity, target: UUID): Boolean {
        val data = UsefulMagic.state.getDataFromServer(source.uuid)
        return !data.isFriend(target)
    }

    fun filterPlayersIfFriend(source: PlayerEntity, players: Collection<PlayerEntity>): Collection<PlayerEntity> {
        return players.filter { filterFriend(source, it.uuid) }
    }

    fun filterPlayersIfNotFriend(source: PlayerEntity, players: Collection<PlayerEntity>): Collection<PlayerEntity> {
        return players.filter { filterNotFriend(source, it.uuid) }
    }
}