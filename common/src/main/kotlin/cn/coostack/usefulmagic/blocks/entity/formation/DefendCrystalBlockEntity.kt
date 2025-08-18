package cn.coostack.usefulmagic.blocks.entity.formation

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.formation.api.BlockFormation
import cn.coostack.usefulmagic.formation.api.DefendCrystal
import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import cn.coostack.usefulmagic.formation.target.BarrageTargetOption
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.formation.target.MeteoriteEntityTargetOption
import cn.coostack.usefulmagic.formation.target.ProjectileEntityTargetOption
import cn.coostack.usefulmagic.particles.barrages.api.DamagedBarrage
import cn.coostack.usefulmagic.particles.emitters.CircleEmitters
import cn.coostack.usefulmagic.particles.style.formation.crystal.CrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.DefendCrystalStyle
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt

class DefendCrystalBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.DEFEND_CRYSTAL.get(), pos, state), DefendCrystal {
    override var activeFormation: BlockFormation? = null
    override var crystalPos: Vec3
        get() = worldPosition.center
        set(value) {
        }
    val preDamageTake = 15f
    var style: CrystalStyle? = null
    override fun onFormationActive(formation: BlockFormation) {
        this.activeFormation = formation
        if (formation.world!!.isClientSide) {
            return
        }

        style = DefendCrystalStyle()
        style!!.crystalPos = worldPosition
        ParticleStyleManager.spawnStyle(level!!, worldPosition.center.add(0.0, -0.4, 0.0), style!!)
    }


    override fun getUpdatePacket(): Packet<ClientGamePacketListener?> {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    override fun getWallWidth(): Int {
        return 4
    }

    override fun canDefendBarrage(target: BarrageTargetOption): Boolean {
        val barrage = target.target
        if (barrage !is DamagedBarrage) {
            return activeFormation!!.hasManaToTransform(30)
        }
        val need = (barrage.damage * preDamageTake).roundToInt()
        return activeFormation!!.hasManaToTransform(need)
    }

    override fun canDefendEntities(target: FormationTargetOption): Boolean {
        if (target is ProjectileEntityTargetOption) {
            return activeFormation!!.hasManaToTransform(20)
        }
        if (target is MeteoriteEntityTargetOption) {
            return activeFormation!!.hasManaToTransform((200 * preDamageTake).toInt())
        }
        return activeFormation!!.hasManaToTransform(1)
    }

    override fun defendEntities(
        target: FormationTargetOption,
        wallInner: Boolean
    ): Int {
        if (target is ProjectileEntityTargetOption) {
            displayDeterParticle(target.pos(), target.movementVec())
            target.target.kill()
            return 20
        }
        if (target is MeteoriteEntityTargetOption) {
            displayDeterParticle(target.pos(), target.movementVec())
            target.meteorite.hit(target.pos())
            return (200 * preDamageTake).toInt()
        }
        if (target is LivingEntityTargetOption && !target.touch) {
            // 这里的魔力值是被attack方法取代
            return 0
        }
        setVelocity(0.7, wallInner, target)
        return 1
    }

    private fun setVelocity(force: Double, wallInner: Boolean, target: FormationTargetOption) {
        if (wallInner) {
            // 向内设置向量
            val innerDirection = target.pos().relativize(activeFormation!!.formationCore).normalize().scale(force)
            target.setVelocity(innerDirection)
        } else {
            val outerDirection = target.pos().relativize(activeFormation!!.formationCore).normalize().scale(-force)
            target.setVelocity(outerDirection)
        }
    }

    override fun defendBarrage(target: BarrageTargetOption): Int {
        val barrage = target.target
        val d = target.pos().relativize(activeFormation!!.formationCore).normalize()
        displayDeterParticle(target.pos(), d)
        if (barrage !is DamagedBarrage) {
            barrage.hit(BarrageHitResult())
            barrage.direction = d
            return 30
        }
        barrage.direction = d
        barrage.hit(BarrageHitResult())
        return (barrage.damage * preDamageTake).roundToInt()
    }

    override fun displayDeterParticle(
        deterPos: Vec3,
        deterDirection: Vec3
    ) {
        if (level?.isClientSide == true) return

        level!!.playSound(
            null,
            crystalPos.x,
            crystalPos.y,
            crystalPos.z,
            UsefulMagicSoundEvents.DEFEND_SHIELD_HIT.get(),
            SoundSource.BLOCKS,
            10f,
            1f
        )

        val emitters =
            CircleEmitters(deterPos.add(deterDirection.normalize()), level)
        emitters.apply {
            maxTick = 1
            templateData.also {
                it.effect = ControlableEndRodEffect(uuid)
                it.size = 0.1f
                it.color = Math3DUtil.colorOf(147, 242, 255)
            }
            circleSpeed = 0.8
            precentDrag = 0.6
            circleDirection = deterDirection
        }
        ParticleEmittersManager.spawnEmitters(emitters)
    }
}

