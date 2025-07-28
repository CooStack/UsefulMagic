package cn.coostack.usefulmagic.states

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.beans.MagicPlayerData
import cn.coostack.usefulmagic.packet.s2c.PacketS2CManaDataToggle
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.component.type.NbtComponent
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.UUID

class ManaServerState : PersistentState() {
    val magicPlayerData = HashMap<UUID, MagicPlayerData>()

    fun getDataFromServer(uuid: UUID): MagicPlayerData {
        return magicPlayerData.getOrPut(uuid) { MagicPlayerData(uuid) }
    }

    fun sendToggle() {
        UsefulMagic.server.playerManager.playerList.forEach {
            val data = magicPlayerData.getOrPut(it.uuid) {
                MagicPlayerData(it.uuid)
            }
            ServerPlayNetworking.send(
                it,
                PacketS2CManaDataToggle(data, it.uuid)
            )
        }
        markDirty()
    }

    companion object {
        @JvmStatic
        fun loadFromNBT(nbt: NbtCompound, lookup: RegistryWrapper.WrapperLookup): ManaServerState {
            val loader = ManaServerState()
            val players = nbt.getCompound("players")
            players.keys.forEach {
                val uuid = UUID.fromString(it)
                val value = players.getCompound(it)
                val data = MagicPlayerData(uuid)
                data.apply {
                    maxMana = value.getInt("maxMana")
                    mana = value.getInt("mana")
                    manaRegeneration = value.getInt("manaRegeneration")
                    val friendsNBT = value.getCompound("friends")
                    friendsNBT.keys.forEach { index ->
                        val uuid = friendsNBT.getUuid(index)
                        data.addFriend(uuid)
                    }
                }
                loader.magicPlayerData[uuid] = data
            }
            return loader
        }

        @JvmStatic
        private val type = Type<ManaServerState>(
            ::ManaServerState,
            ManaServerState::loadFromNBT,
            null
        )

        @JvmStatic
        fun getFromState(server: MinecraftServer): ManaServerState {
            val pm = server.getWorld(World.OVERWORLD)!!.persistentStateManager
            val state = pm.getOrCreate(type, UsefulMagic.MOD_ID)
            state.markDirty()
            return state
        }
    }

    override fun writeNbt(
        nbt: NbtCompound,
        registryLookup: RegistryWrapper.WrapperLookup?
    ): NbtCompound {
        val playersNBT = NbtCompound()
        magicPlayerData.forEach {
            val key = it.key.toString()
            val value = it.value
            val dataNbt = NbtCompound()
            dataNbt.putInt("mana", value.mana)
            dataNbt.putInt("maxMana", value.maxMana)
            dataNbt.putInt("manaRegeneration", value.manaRegeneration)
            val friends = NbtCompound()
            value.friends.forEachIndexed { index, f ->
                friends.putUuid("$index", f)
            }
            dataNbt.put("friends", friends)
            playersNBT.put(key, dataNbt)
        }
        nbt.put("players", playersNBT)
        return nbt
    }
}