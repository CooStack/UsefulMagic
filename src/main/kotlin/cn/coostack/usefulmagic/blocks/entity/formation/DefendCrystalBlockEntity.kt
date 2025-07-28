package cn.coostack.usefulmagic.blocks.entity.formation

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.particles.impl.TestEndRodEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.formation.api.BlockFormation
import cn.coostack.usefulmagic.formation.api.DefendCrystal
import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import cn.coostack.usefulmagic.formation.target.BarrageTargetOption
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.formation.target.MeteoriteEntityTargetOption
import cn.coostack.usefulmagic.formation.target.ProjectileEntityTargetOption
import cn.coostack.usefulmagic.meteorite.impl.OptionMeteorite
import cn.coostack.usefulmagic.particles.barrages.api.DamagedBarrage
import cn.coostack.usefulmagic.particles.emitters.CircleEmitters
import cn.coostack.usefulmagic.particles.style.formation.crystal.CrystalStyle
import cn.coostack.usefulmagic.particles.style.formation.crystal.DefendCrystalStyle
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.roundToInt

class DefendCrystalBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.DEFEND_CRYSTAL, pos, state), DefendCrystal {
    override var activeFormation: BlockFormation? = null
    override var crystalPos: Vec3d
        get() = pos.toCenterPos()
        set(value) {
        }
    val preDamageTake = 15f
    var style: CrystalStyle? = null
    override fun onFormationActive(formation: BlockFormation) {
        this.activeFormation = formation
        if (formation.world!!.isClient) {
            return
        }
        style = DefendCrystalStyle()
        style!!.crystalPos = pos
        ParticleStyleManager.spawnStyle(world!!, pos.toCenterPos().add(0.0, -0.4, 0.0), style!!)
    }

    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.writeNbt(nbt, registryLookup)
    }

    override fun readNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.readNbt(nbt, registryLookup)
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener?> {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound? {
        return createNbt(registryLookup)
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
            val innerDirection = target.pos().relativize(activeFormation!!.formationCore).normalize().multiply(force)
            target.setVelocity(innerDirection)
        } else {
            val outerDirection = target.pos().relativize(activeFormation!!.formationCore).normalize().multiply(-force)
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
        deterPos: Vec3d,
        deterDirection: Vec3d
    ) {
        if (world?.isClient == true) return

        world!!.playSound(
            null,
            crystalPos.x,
            crystalPos.y,
            crystalPos.z,
            UsefulMagicSoundEvents.DEFEND_SHIELD_HIT,
            SoundCategory.BLOCKS,
            10f,
            1f
        )

        val emitters =
            CircleEmitters(deterPos.add(deterDirection.normalize()), world)
        emitters.apply {
            maxTick = 1
            templateData.also {
                it.effect = TestEndRodEffect(uuid)
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