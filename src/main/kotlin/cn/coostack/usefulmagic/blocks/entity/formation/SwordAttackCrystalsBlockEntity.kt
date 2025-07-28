package cn.coostack.usefulmagic.blocks.entity.formation

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.formation.api.AttackCrystal
import cn.coostack.usefulmagic.formation.api.BlockFormation
import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import cn.coostack.usefulmagic.formation.target.BarrageTargetOption
import cn.coostack.usefulmagic.formation.target.ProjectileEntityTargetOption
import cn.coostack.usefulmagic.particles.barrages.SwordAttackFormationBarrage
import cn.coostack.usefulmagic.particles.style.EndRodSwordStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.CrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.RecoverCrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.SwordAttackCrystalStyle
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.random.Random

class SwordAttackCrystalsBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.SWORD_ATTACK_CRYSTAL, pos, state), AttackCrystal {
    override var currentTime: Int = 0
    override fun writeNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.writeNbt(nbt, registryLookup)
    }

    override fun readNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.readNbt(nbt, registryLookup)
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener?> {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound? {
        return createNbt(registryLookup)
    }

    override var activeFormation: BlockFormation? = null
    override var crystalPos: Vec3d
        get() = pos.toCenterPos()
        set(value) {
        }

    var style: CrystalStyle? = null
    override fun onFormationActive(formation: BlockFormation) {
        this.activeFormation = formation
        if (world!!.isClient) return
        style = SwordAttackCrystalStyle()
        style!!.crystalPos = pos
        ParticleStyleManager.spawnStyle(world!!, pos.toCenterPos().add(0.0, -0.4, 0.0), style!!)
    }

    val random = Random(System.nanoTime())
    override fun shoot(option: FormationTargetOption) {
        if (option is BarrageTargetOption) {
            option.hit()
            return
        }
        if (option is ProjectileEntityTargetOption) return
        // 在range范围内 (y > origin)
        // 随机一个位置生成一把剑 并且指向目标实体发射 击中时造成普通伤害 (不追踪)

        val range = (activeFormation?.getFormationTriggerRange() ?: 0.01) / 2
        val spawnLocation = Vec3d(
            random.nextDouble(-range, range),
            random.nextDouble(0.0, range),
            random.nextDouble(-range, range),
        ).add(activeFormation!!.formationCore)
//        val spawnLocation = activeFormation!!.formationCore.add(0.0,2.0,0.0)
        val barrage =
            SwordAttackFormationBarrage(spawnLocation, world as ServerWorld, HitBox.of(2.0, 2.0, 2.0), 5.0, option)
        val owner = activeFormation!!.owner?.let(UsefulMagic.server.playerManager::getPlayer)
        if (activeFormation!!.owner != null && owner == null) {
            barrage.offlineShooter = activeFormation!!.owner
        }
        barrage.apply {
            direction = spawnLocation.relativize(option.pos())
            shooter = owner
            bindControl as EndRodSwordStyle
            (bindControl as EndRodSwordStyle).addPreTickAction {
                rotateParticlesToPoint(RelativeLocation.of(direction))
            }
        }
        BarrageManager.spawn(barrage)
    }

    override fun take(): Int {
        return 15
    }

    override fun duration(): Int {
        return 13
    }

}