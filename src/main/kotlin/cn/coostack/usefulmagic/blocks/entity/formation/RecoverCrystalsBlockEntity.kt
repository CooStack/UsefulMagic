package cn.coostack.usefulmagic.blocks.entity.formation

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.formation.api.BlockFormation
import cn.coostack.usefulmagic.formation.api.FormationCrystal
import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.particles.style.formation.crystal.CrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.DefendCrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.RecoverCrystalStyle
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.sqrt

class RecoverCrystalsBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.RECOVER_CRYSTAL, pos, state), FormationCrystal {
    var recoverSpeed = 10
    override fun writeNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.writeNbt(nbt, registryLookup)
        nbt?.putInt("recover_speed", recoverSpeed)
    }

    override fun readNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.readNbt(nbt, registryLookup)
        nbt?.let {
            recoverSpeed = it.getInt("recover_speed")
        }
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener?> {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound? {
        return createNbt(registryLookup)
    }

    var time = 0

    fun tick(
        world: World,
        pos: BlockPos,
        state: BlockState,
    ) {
        activeFormation ?: return
        if (!activeFormation!!.isActiveFormation()) return
        if (time++ % 20 == 0) {
            val first =
                activeFormation!!.activeCrystals.filter { it is EnergyCrystalsBlockEntity && it.currentMana + 1 <= it.maxMana }
                    .map { it as EnergyCrystalsBlockEntity }
                    .firstOrNull()
            first?.let {
                it.increase(recoverSpeed)
                val start = this.crystalPos
                val end = it.crystalPos

                val line = LightningParticleEmitters(start, world).apply {
                    this.endPos = RelativeLocation.of(start.relativize(end))
                    this.templateData.also { it ->
                        it.speed = 0.0
                        it.color = Math3DUtil.colorOf(100, 255, 180)
                        it.maxAge = 3
                    }
                    maxTick = 1
                }
                ParticleEmittersManager.spawnEmitters(line)

            }
        }
    }

    override var activeFormation: BlockFormation? = null
    override var crystalPos: Vec3d
        get() = pos.toCenterPos()
        set(value) {
        }

    override fun handle(option: FormationTargetOption): FormationTargetOption {
        return option
    }

    var style: CrystalStyle? = null
    override fun onFormationActive(formation: BlockFormation) {
        this.activeFormation = formation
        if (world!!.isClient) return
        style = RecoverCrystalStyle()
        style!!.crystalPos = pos
        ParticleStyleManager.spawnStyle(world!!, pos.toCenterPos().add(0.0, -0.4, 0.0), style!!)
    }

}