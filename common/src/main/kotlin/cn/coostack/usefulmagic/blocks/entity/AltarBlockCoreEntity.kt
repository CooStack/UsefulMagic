package cn.coostack.usefulmagic.blocks.entity

import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.SimpleParticleEmitters
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.items.weapon.wands.WandItem
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import kotlin.jvm.optionals.getOrElse

class AltarBlockCoreEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.ALTAR_BLOCK_CORE.get(), pos, state), AltarEntity {
    var stack: ItemStack = ItemStack.EMPTY


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

    fun getCore(): MagicCoreBlockEntity? {
        val p = worldPosition.above(3)
        val entity = level?.getBlockEntity(p)
        return entity as? MagicCoreBlockEntity
    }

    var time = 0
    fun tick(
        world: Level,
        pos: BlockPos,
        state: BlockState,
    ) {
        time++
        if (world.isClientSide) {
            return
        }
        val core = getCore() ?: return
        if (core.crafting) {
            return
        }
        val item = stack.item
        if (item !is WandItem) return
        if (time % 10 == 0 && core.currentMana >= 10 && stack.damageValue > 0) {
            stack.damageValue--
            core.currentMana -= 10
            val emitter = SimpleParticleEmitters(
                pos.above(3).center,
                world as ServerLevel,
                ControlableParticleData()
                    .also {
                        it.effect = ControlableFireworkEffect(it.uuid)
                        it.velocity = Vec3(0.0, 0.2, 0.0)
                        it.speed = -3 / 30.0
                        it.maxAge = 20
                        it.color = Math3DUtil.colorOf(200, 100, 250)
                    }
            ).also {
                it.maxTick = 5
                it.delay = 3
            }
            ParticleEmittersManager.spawnEmitters(emitter)
        }
    }
}