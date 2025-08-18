package cn.coostack.usefulmagic.utils

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import java.util.UUID

object FriendFilterHelper {

    /**
     * 如果是朋友,则返回true
     */
    fun filterFriend(source: LivingEntity, target: UUID): Boolean {
        if (source !is Player) return false
        return filterFriend(source, target)
    }

    /**
     * 如果不是朋友,则返回true
     */
    fun filterNotFriend(source: LivingEntity, target: UUID): Boolean {
        if (source !is Player) return true
        return filterNotFriend(source, target)
    }

    /**
     * 如果是朋友,则返回true
     */
    fun filterFriend(source: Player, target: UUID): Boolean {
        val data = UsefulMagic.state.getDataFromServer(source.uuid)
        return data.isFriend(target)
    }

    /**
     * 如果不是朋友,则返回true
     */
    fun filterNotFriend(source: Player, target: UUID): Boolean {
        val data = UsefulMagic.state.getDataFromServer(source.uuid)
        return !data.isFriend(target)
    }

    fun filterPlayersIfFriend(source: Player, players: Collection<Player>): Collection<Player> {
        return players.filter { filterFriend(source, it.uuid) }
    }

    fun filterPlayersIfNotFriend(source: Player, players: Collection<Player>): Collection<Player> {
        return players.filter { filterNotFriend(source, it.uuid) }
    }
}