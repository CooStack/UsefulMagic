package cn.coostack.usefulmagic.profile

import net.minecraft.world.entity.player.Player
import java.util.UUID

class MagicPlayerData(var owner: UUID) {

    /**
     * Explicit friend list saved on the server.
     */
    internal val friends = ArrayList<UUID>()

    /**
     * FriendManager friendly options.
     * Default: only "friend player" is enabled.
     */
    var treatHostileAsFriend = false
    var treatNeutralAsFriend = false
    var treatNonFriendPlayerAsFriend = false
    var treatFriendPlayerAsFriend = true
    var treatAnimalAsFriend = false
    var treatFriendlyMobAsFriend = false

    fun addFriend(uuid: UUID) {
        if (isFriend(uuid)) {
            return
        }
        friends.add(uuid)
    }

    fun isFriend(uuid: UUID): Boolean {
        return friends.contains(uuid) || uuid == owner
    }

    fun isFriend(player: Player): Boolean = isFriend(player.uuid)

    fun removeFriend(uuid: UUID) {
        friends.remove(uuid)
    }

    fun updateFriendlySettings(
        hostile: Boolean,
        neutral: Boolean,
        nonFriendPlayer: Boolean,
        friendPlayer: Boolean,
        animal: Boolean,
        friendlyMob: Boolean
    ) {
        treatHostileAsFriend = hostile
        treatNeutralAsFriend = neutral
        treatNonFriendPlayerAsFriend = nonFriendPlayer
        treatFriendPlayerAsFriend = friendPlayer
        treatAnimalAsFriend = animal
        treatFriendlyMobAsFriend = friendlyMob
    }
}
