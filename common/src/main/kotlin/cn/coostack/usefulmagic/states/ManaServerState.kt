package cn.coostack.usefulmagic.states

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.beans.MagicPlayerData
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID

class ManaServerState : SavedData() {
    val magicPlayerData = HashMap<UUID, MagicPlayerData>()

    fun getDataFromServer(uuid: UUID): MagicPlayerData {
        return magicPlayerData.getOrPut(uuid) { MagicPlayerData(uuid) }
    }

    fun sendToggle() {
        setDirty()
    }

    companion object {
        @JvmStatic
        val stateFactory = Factory<ManaServerState>({
            ManaServerState()
        }, { nbt, provider ->
            val loader = ManaServerState()
            val players = nbt.getCompound("players")
            players.allKeys.forEach {
                val uuid = UUID.fromString(it)
                val value = players.getCompound(it)
                val data = MagicPlayerData(uuid)
                data.apply {
                    val friendsNBT = value.getCompound("friends")
                    friendsNBT.allKeys.forEach { index ->
                        val uuid = friendsNBT.getUUID(index)
                        data.addFriend(uuid)
                    }
                    if (value.contains("friendly_settings")) {
                        val settings = value.getCompound("friendly_settings")
                        updateFriendlySettings(
                            settings.getBoolean("hostile"),
                            if (settings.contains("neutral")) settings.getBoolean("neutral") else false,
                            settings.getBoolean("non_friend_player"),
                            if (settings.contains("friend_player")) settings.getBoolean("friend_player") else true,
                            settings.getBoolean("animal"),
                            if (settings.contains("friendly_mob")) settings.getBoolean("friendly_mob") else false
                        )
                    }
                }
                loader.magicPlayerData[uuid] = data
            }
            loader
        }, DataFixTypes.STATS)


        @JvmStatic
        fun getFromState(server: MinecraftServer): ManaServerState {
            val pm = server.getLevel(Level.OVERWORLD)!!.dataStorage
            val state = pm.computeIfAbsent(stateFactory, UsefulMagic.MOD_ID)
            state.setDirty()
            return state
        }
    }

    override fun save(
        nbt: CompoundTag,
        registryLookup: HolderLookup.Provider
    ): CompoundTag {
        val playersNBT = CompoundTag()
        magicPlayerData.forEach {
            val key = it.key.toString()
            val value = it.value
            val dataNbt = CompoundTag()
            val friends = CompoundTag()
            value.friends.forEachIndexed { index, f ->
                friends.putUUID("$index", f)
            }
            dataNbt.put("friends", friends)
            val friendlySettings = CompoundTag()
            friendlySettings.putBoolean("hostile", value.treatHostileAsFriend)
            friendlySettings.putBoolean("neutral", value.treatNeutralAsFriend)
            friendlySettings.putBoolean("non_friend_player", value.treatNonFriendPlayerAsFriend)
            friendlySettings.putBoolean("friend_player", value.treatFriendPlayerAsFriend)
            friendlySettings.putBoolean("animal", value.treatAnimalAsFriend)
            friendlySettings.putBoolean("friendly_mob", value.treatFriendlyMobAsFriend)
            dataNbt.put("friendly_settings", friendlySettings)
            playersNBT.put(key, dataNbt)
        }
        nbt.put("players", playersNBT)
        return nbt
    }
}
