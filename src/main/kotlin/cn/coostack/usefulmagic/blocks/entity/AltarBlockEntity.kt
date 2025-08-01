package cn.coostack.usefulmagic.blocks.entity

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.jvm.optionals.getOrElse

class AltarBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.ALTAR_BLOCK, pos, state), AltarEntity {
    var stack: ItemStack = ItemStack.EMPTY
    override fun writeNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.writeNbt(nbt, registryLookup)
        if (stack.isEmpty) {
            // 空nbt不会传到读取
            // 狗屎
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

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener?>? {
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

    override fun getAltarStack(): ItemStack {
        return stack
    }

    override fun setAltarStack(stack: ItemStack) {
        this.stack = stack
        markDirty()
        world!!.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
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