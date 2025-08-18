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
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.usefulmagic.particles.style.formation.crystal.RecoverCrystalStyle
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt

class RecoverCrystalsBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.RECOVER_CRYSTAL.get(), pos, state), FormationCrystal {
    var recoverSpeed = 10


    override fun saveAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        nbt.putInt("recover_speed", recoverSpeed)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        recoverSpeed = tag.getInt("recover_speed")

    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener?>? {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    var time = 0

    fun tick(
        world: Level,
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
    override var crystalPos: Vec3
        get() = worldPosition.center
        set(value) {
        }

    override fun handle(option: FormationTargetOption): FormationTargetOption {
        return option
    }

    var style: CrystalStyle? = null
    override fun onFormationActive(formation: BlockFormation) {
        this.activeFormation = formation
        if (level!!.isClientSide) return
        style = RecoverCrystalStyle()
        style!!.crystalPos = worldPosition
        ParticleStyleManager.spawnStyle(level!!, worldPosition.center.add(0.0, -0.4, 0.0), style!!)
    }

}