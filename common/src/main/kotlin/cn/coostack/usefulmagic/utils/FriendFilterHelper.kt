package cn.coostack.usefulmagic.utils

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.profile.MagicPlayerData
import net.minecraft.world.entity.NeutralMob
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.AbstractGolem
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.npc.AbstractVillager
import net.minecraft.world.entity.player.Player
import java.util.UUID

object FriendFilterHelper {

    fun filterFriend(source: LivingEntity, target: UUID): Boolean {
        if (source !is Player) {
            return false
        }
        return filterFriend(source, target)
    }

    fun filterNotFriend(source: LivingEntity, target: UUID): Boolean {
        if (source !is Player) {
            return true
        }
        return filterNotFriend(source, target)
    }

    fun filterFriend(source: LivingEntity, target: LivingEntity): Boolean {
        if (source !is Player) {
            return false
        }
        return filterFriend(source, target)
    }

    fun filterNotFriend(source: LivingEntity, target: LivingEntity): Boolean {
        if (source !is Player) {
            return true
        }
        return filterNotFriend(source, target)
    }

    fun filterFriend(source: Player, target: UUID): Boolean {
        val data = getPlayerData(source) ?: return source.uuid == target
        val targetEntity = findTargetEntity(source, target)
        if (targetEntity != null) {
            return isFriendByRules(source, targetEntity, data)
        }
        // Fallback when the target entity cannot be resolved.
        return data.isFriend(target) && data.treatFriendPlayerAsFriend
    }

    fun filterNotFriend(source: Player, target: UUID): Boolean {
        return !filterFriend(source, target)
    }

    /**
     * Determine whether [target] should be treated as a friend of [source]
     * according to FriendManager settings.
     *
     * Possible cases:
     * 1. Self target: always friend.
     * 2. Player in friend list: controlled by "friend player" setting.
     * 3. Player not in friend list: controlled by "non-friend player" setting.
     * 4. Hostile mob (Enemy): controlled by "hostile mob" setting.
     * 5. Friendly mob (villager/golem etc.): controlled by "friendly mob" setting.
     * 6. Neutral mob (NeutralMob): controlled by "neutral mob" setting.
     * 7. Animal (Animal): controlled by "animal" setting.
     * 8. Other entity types: not treated as friend by default.
     *
     * Priority note:
     * friendly mob is checked before neutral/animal, so villagers and golems
     * can be protected by the dedicated "friendly mob" setting.
     */
    fun filterFriend(source: Player, target: LivingEntity): Boolean {
        val data = getPlayerData(source) ?: return source.uuid == target.uuid
        return isFriendByRules(source, target, data)
    }

    fun filterNotFriend(source: Player, target: LivingEntity): Boolean {
        return !filterFriend(source, target)
    }

    fun filterPlayersIfFriend(source: Player, players: Collection<Player>): Collection<Player> {
        return players.filter { filterFriend(source, it) }
    }

    fun filterPlayersIfNotFriend(source: Player, players: Collection<Player>): Collection<Player> {
        return players.filter { filterNotFriend(source, it) }
    }

    private fun getPlayerData(source: Player): MagicPlayerData? {
        return runCatching {
            UsefulMagic.state.getDataFromServer(source.uuid)
        }.getOrNull()
    }

    private fun findTargetEntity(source: Player, target: UUID): LivingEntity? {
        source.server?.playerList?.getPlayer(target)?.let { return it }
        val level = source.level()
        if (level is ServerLevel) {
            return level.getEntity(target) as? LivingEntity
        }
        return level.players().firstOrNull { it.uuid == target }
    }

    private fun isFriendByRules(source: Player, target: LivingEntity, data: MagicPlayerData): Boolean {
        if (target.uuid == source.uuid) {
            return true
        }
        if (target is Player) {
            val inFriendList = data.isFriend(target.uuid)
            if (inFriendList) {
                return data.treatFriendPlayerAsFriend
            }
            return data.treatNonFriendPlayerAsFriend
        }
        if (target is Enemy) {
            return data.treatHostileAsFriend
        }
        if (isFriendlyMob(target)) {
            return data.treatFriendlyMobAsFriend
        }
        if (target is NeutralMob) {
            return data.treatNeutralAsFriend
        }
        if (target is Animal) {
            return data.treatAnimalAsFriend
        }
        return false
    }

    private fun isFriendlyMob(target: LivingEntity): Boolean {
        return target is AbstractVillager || target is AbstractGolem
    }
}
