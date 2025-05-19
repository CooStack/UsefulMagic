package cn.coostack.usefulmagic.blocks.entitiy

import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketCallbacks
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.jvm.optionals.getOrElse
import kotlin.math.max
import kotlin.math.min

class AltarBlockCoreEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.ALTAR_BLOCK_CORE, pos, state), AltarEntity {
    var stack: ItemStack = ItemStack.EMPTY


    override fun getAltarStack(): ItemStack {
        return stack
    }

    override fun setAltarStack(stack: ItemStack) {
        this.stack = stack
        this.markDirty()
        world!!.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
    }

    override fun writeNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.writeNbt(nbt, registryLookup)
        if (stack.isEmpty) {
            nbt?.putBoolean("stack_empty", true)
            return
        }
        nbt?.put("stack", stack.encode(registryLookup))
    }

    override fun readNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.readNbt(nbt, registryLookup)
        val stackElement = nbt?.get("stack") ?: let {
            stack = ItemStack.EMPTY
            return
        }
        stack = ItemStack.fromNbt(registryLookup, stackElement).getOrElse { ItemStack.EMPTY }
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener?> {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound? {
        return createNbt(registryLookup)
    }

    override fun getDownActiveBlocksMaxMana(): Int {
        var current = pos.down()
        var res = 0
        repeat(10) {
            val state = world?.getBlockState(current) ?: return 0
            current = current.down()
            if (!AltarEntity.blockMapper.containsKey(state.block)) {
                return res
            }
            res += AltarEntity.getBlockMaxMana(state.block)
        }
        return res
    }

    override fun getDownActiveBlocksManaReviveSpeed(): Int {
        var current = pos.down()
        var res = 0
        repeat(10) {
            val state = world?.getBlockState(current) ?: return 0
            current = current.down()
            if (!AltarEntity.blockMapper.containsKey(state.block)) {
                return res
            }
            res += AltarEntity.getBlockRevive(state.block)
        }
        return res
    }

    fun tick(
        world: World?,
        pos: BlockPos?,
        state: BlockState?,
    ) {
    }
}