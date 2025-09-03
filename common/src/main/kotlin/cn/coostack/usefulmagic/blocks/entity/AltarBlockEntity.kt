package cn.coostack.usefulmagic.blocks.entity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import kotlin.jvm.optionals.getOrElse

class AltarBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.ALTAR_BLOCK.get(), pos, state), AltarEntity {
    var stack: ItemStack = ItemStack.EMPTY

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        if (stack.isEmpty) {
            tag.putBoolean("stack_empty", true)
            return
        }
        val stackTag = stack.save(registries, CompoundTag())
        tag.put("stack", stackTag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        val stackElement = tag.get("stack") ?: let {
            stack = ItemStack.EMPTY
            return
        }
        stack = ItemStack.parse(registries, stackElement).getOrElse { ItemStack.EMPTY }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener?>? {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    override fun getDownActiveBlocksMaxMana(): Int {
        var current = worldPosition.below()
        var res = 0
        repeat(10) {
            val state = level?.getBlockState(current) ?: return 0
            current = current.below()
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
        setChanged()
        level!!.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_ALL)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        // 当 block entity 需要同步时，这里会被发给客户端
        val tag = CompoundTag()
        saveAdditional(tag, level!!.registryAccess())
        return tag
    }

    override fun getDownActiveBlocksManaReviveSpeed(): Int {
        var current = worldPosition.below()
        var res = 0
        repeat(10) {
            val state = level?.getBlockState(current) ?: return 0
            current = current.below()
            if (!AltarEntity.blockMapper.containsKey(state.block)) {
                return res
            }
            res += AltarEntity.getBlockRevive(state.block)
        }
        return res
    }

    fun tick(
        world: Level?,
        pos: BlockPos?,
        state: BlockState?,
    ) {
    }

    override fun getSlotsForFace(p0: Direction): IntArray {
        return intArrayOf(0)
    }

    override fun canPlaceItemThroughFace(
        p0: Int,
        p1: ItemStack,
        p2: Direction?
    ): Boolean {
        return true
    }

    override fun canTakeItemThroughFace(
        p0: Int,
        p1: ItemStack,
        p2: Direction
    ): Boolean {
        return true
    }

    override fun getContainerSize(): Int {
        return 1
    }

    override fun isEmpty(): Boolean {
        return stack.isEmpty
    }

    override fun getItem(p0: Int): ItemStack {
        return stack
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        val old = stack
        setAltarStack(ItemStack.EMPTY)
        return old
    }

    override fun removeItemNoUpdate(p0: Int): ItemStack {
        val old = stack
        stack = ItemStack.EMPTY
        return old
    }

    override fun setItem(p0: Int, stack: ItemStack) {
        setAltarStack(stack)
    }

    override fun stillValid(p0: Player): Boolean {
        return true
    }

    override fun clearContent() {
        setAltarStack(ItemStack.EMPTY)
    }


    override fun getMaxStackSize(): Int {
        return 1
    }
}