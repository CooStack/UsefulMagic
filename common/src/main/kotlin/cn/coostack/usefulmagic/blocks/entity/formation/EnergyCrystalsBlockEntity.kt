package cn.coostack.usefulmagic.blocks.entity.formation

import cn.coostack.cooparticlesapi.platform.CooParticlesServices
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.formation.api.BlockFormation
import cn.coostack.usefulmagic.formation.api.FormationCrystal
import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import cn.coostack.usefulmagic.packet.s2c.PacketS2CEnergyCrystalChange
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

class EnergyCrystalsBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.ENERGY_CRYSTAL.get(), pos, state), FormationCrystal {
    var maxMana = 1000
    var currentMana: Int = 1000
        set(value) {
            field = value
            setChanged()
        }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        tag.let {
            it.putInt("maxMana", maxMana)
            it.putInt("currentMana", currentMana)
        }

    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        tag.let {
            maxMana = it.getInt("maxMana")
            currentMana = it.getInt("currentMana")
        }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener?>? {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    fun increase(mana: Int) {
        currentMana = (currentMana + mana).coerceAtMost(maxMana)
        if (level?.isClientSide == false) {
            level!!.players().forEach {
                it as ServerPlayer
                val change = PacketS2CEnergyCrystalChange(worldPosition, currentMana, maxMana)
                CooParticlesServices.SERVER_NETWORK.send(change, it)
            }
        }
    }

    fun decrease(mana: Int) {
        currentMana = (currentMana - mana).coerceAtLeast(0)
        if (level?.isClientSide == false) {
            level!!.players().forEach {
                it as ServerPlayer
                val change = PacketS2CEnergyCrystalChange(worldPosition, currentMana, maxMana)
                CooParticlesServices.SERVER_NETWORK.send(change, it)
            }
        }
    }

    fun tick(
        world: Level,
        pos: BlockPos,
        state: BlockState,
    ) {
    }

    override var activeFormation: BlockFormation? = null
    override var crystalPos: Vec3
        get() = worldPosition.center
        set(value) {
        }

    override fun handle(option: FormationTargetOption): FormationTargetOption {
        return option
    }

    override fun onFormationActive(formation: BlockFormation) {
        this.activeFormation = formation
        return
    }
}