package cn.coostack.usefulmagic.blocks.entitiy

import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.SimpleParticleEmitters
import cn.coostack.cooparticlesapi.particles.impl.ControlableFireworkEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.items.weapon.wands.WandItem
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.jvm.optionals.getOrElse

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

    fun getCore(): MagicCoreBlockEntity? {
        val p = pos.up(3)
        val entity = world?.getBlockEntity(p)
        return entity as? MagicCoreBlockEntity
    }

    var time = 0
    fun tick(
        world: World,
        pos: BlockPos,
        state: BlockState,
    ) {
        time++
        if (world.isClient) {
            return
        }
        val core = getCore() ?: return
        if (core.crafting) {
            return
        }
        val item = stack.item
        if (item !is WandItem) return
        if (time % 10 == 0 && core.currentMana >= 10 && stack.damage > 0) {
            stack.damage--
            core.currentMana -= 10
            val emitter = SimpleParticleEmitters(
                pos.up(3).toCenterPos(),
                world as ServerWorld,
                ControlableParticleData()
                    .also {
                        it.effect = ControlableFireworkEffect(it.uuid)
                        it.velocity = Vec3d(0.0, 0.2, 0.0)
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