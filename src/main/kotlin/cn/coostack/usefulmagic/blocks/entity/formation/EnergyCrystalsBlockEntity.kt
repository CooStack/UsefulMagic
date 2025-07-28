package cn.coostack.usefulmagic.blocks.entity.formation

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.formation.api.BlockFormation
import cn.coostack.usefulmagic.formation.api.FormationCrystal
import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import cn.coostack.usefulmagic.packet.s2c.PacketS2CEnergyCrystalChange
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class EnergyCrystalsBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.ENERGY_CRYSTAL, pos, state), FormationCrystal {
    var maxMana = 1000
    var currentMana: Int = 1000
        set(value) {
            field = value
            markDirty()
        }

    override fun writeNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.writeNbt(nbt, registryLookup)
        nbt?.let {
            it.putInt("maxMana", maxMana)
            it.putInt("currentMana", currentMana)
        }
    }

    override fun readNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.readNbt(nbt, registryLookup)
        nbt?.let {
            maxMana = it.getInt("maxMana")
            currentMana = it.getInt("currentMana")
        }
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener?> {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound? {
        return createNbt(registryLookup)
    }

    fun increase(mana: Int) {
        currentMana = (currentMana + mana).coerceAtMost(maxMana)
        if (world?.isClient == false) {
            world!!.players.forEach {
                it as ServerPlayerEntity
                val change = PacketS2CEnergyCrystalChange(pos, currentMana, maxMana)
                ServerPlayNetworking.send(it, change)
            }
        }
        markDirty()
    }

    fun decrease(mana: Int) {
        currentMana = (currentMana - mana).coerceAtLeast(0)
        if (world?.isClient == false) {
            world!!.players.forEach {
                it as ServerPlayerEntity
                val change = PacketS2CEnergyCrystalChange(pos, currentMana, maxMana)
                ServerPlayNetworking.send(it, change)
            }
        }
        markDirty()
    }

    fun tick(
        world: World,
        pos: BlockPos,
        state: BlockState,
    ) {
    }

    override var activeFormation: BlockFormation? = null
    override var crystalPos: Vec3d
        get() = pos.toCenterPos()
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