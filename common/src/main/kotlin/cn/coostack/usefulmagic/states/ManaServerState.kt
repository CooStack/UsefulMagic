package cn.coostack.usefulmagic.states

import cn.coostack.cooparticlesapi.platform.CooParticlesServices
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.beans.MagicPlayerData
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataToggle
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID
import java.util.function.BiFunction

class ManaServerState : SavedData() {
    val magicPlayerData = HashMap<UUID, MagicPlayerData>()

    fun getDataFromServer(uuid: UUID): MagicPlayerData {
        return magicPlayerData.getOrPut(uuid) { MagicPlayerData(uuid) }
    }

    fun sendToggle() {
        UsefulMagic.server.playerList.players.forEach {
            val data = magicPlayerData.getOrPut(it.uuid) {
                MagicPlayerData(it.uuid)
            }
            CooParticlesServices.SERVER_NETWORK.send(
                PacketS2CManaDataToggle(data, it.uuid), it
            )
        }
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
                    maxMana = value.getInt("maxMana")
                    mana = value.getInt("mana")
                    manaRegeneration = value.getInt("manaRegeneration")
                    val friendsNBT = value.getCompound("friends")
                    friendsNBT.allKeys.forEach { index ->
                        val uuid = friendsNBT.getUUID(index)
                        data.addFriend(uuid)
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
            dataNbt.putInt("mana", value.mana)
            dataNbt.putInt("maxMana", value.maxMana)
            dataNbt.putInt("manaRegeneration", value.manaRegeneration)
            val friends = CompoundTag()
            value.friends.forEachIndexed { index, f ->
                friends.putUUID("$index", f)
            }
            dataNbt.put("friends", friends)
            playersNBT.put(key, dataNbt)
        }
        nbt.put("players", playersNBT)
        return nbt
    }
}