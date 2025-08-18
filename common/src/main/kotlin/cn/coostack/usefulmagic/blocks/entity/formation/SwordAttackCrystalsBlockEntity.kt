package cn.coostack.usefulmagic.blocks.entity.formation

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.extend.relativize
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
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

class SwordAttackCrystalsBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.SWORD_ATTACK_CRYSTAL.get(), pos, state), AttackCrystal {
    override var currentTime: Int = 0

    override fun getUpdatePacket(): Packet<ClientGamePacketListener?>? {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    override var activeFormation: BlockFormation? = null
    override var crystalPos: Vec3
        get() = worldPosition.center
        set(value) {
        }

    var style: CrystalStyle? = null
    override fun onFormationActive(formation: BlockFormation) {
        this.activeFormation = formation
        if (level!!.isClientSide) return
        style = SwordAttackCrystalStyle()
        style!!.crystalPos = worldPosition
        ParticleStyleManager.spawnStyle(level!!, worldPosition.center.add(0.0, -0.4, 0.0), style!!)
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
        val spawnLocation = Vec3(
            random.nextDouble(-range, range),
            random.nextDouble(0.0, range),
            random.nextDouble(-range, range),
        ).add(activeFormation!!.formationCore)
//        val spawnLocation = activeFormation!!.formationCore.add(0.0,2.0,0.0)
        val barrage =
            SwordAttackFormationBarrage(spawnLocation, level as ServerLevel, HitBox.of(2.0, 2.0, 2.0), 5.0, option)
        val owner = activeFormation!!.owner?.let(UsefulMagic.server.playerList::getPlayer)
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