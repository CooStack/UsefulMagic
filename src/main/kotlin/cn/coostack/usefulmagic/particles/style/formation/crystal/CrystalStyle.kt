package cn.coostack.usefulmagic.particles.style.formation.crystal

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.formation.api.FormationCrystal
import cn.coostack.usefulmagic.particles.style.formation.FormationStyle.FormationStatus
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.UUID
import kotlin.collections.set

abstract class CrystalStyle(uuid: UUID = UUID.randomUUID()) :
    ParticleGroupStyle(128.0, uuid) {
    var crystalPos: BlockPos = BlockPos.ORIGIN
    lateinit var crystal: FormationCrystal
    val helper = HelperUtil.styleStatus(10)
        .apply {
            loadControler(this@CrystalStyle)
            initHelper()
        }

    @ControlableBuffer("time")
    var time = 0
    override fun onDisplay() {
        if (client) {
            addPreTickAction {
                displayParticleAnimate()
            }
            return
        }
        val entity = world!!.getBlockEntity(crystalPos) ?: let {
            remove()
            return
        }
        if (entity !is FormationCrystal) {
            remove()
            return
        }
        if (entity.activeFormation == null) {
            return
        }
        val core = world!!.getBlockEntity(BlockPos.ofFloored(entity.activeFormation!!.formationCore))
        crystal = entity
        val coreCheck = core !is FormationCoreBlockEntity
        addPreTickAction {
            if (world!!.isClient) return@addPreTickAction
            if ((!(crystal.activeFormation?.isActiveFormation() ?: false)) || coreCheck) {
                cancel()
            }
            time++
        }
        addPreTickAction {
            displayParticleAnimate()
        }
    }

    abstract fun displayParticleAnimate()

    fun cancel() {
        if (helper.displayStatus == StatusHelper.Status.DISABLE.id) {
            return
        }
        helper.setStatus(StatusHelper.Status.DISABLE)
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(ControlableBufferHelper.getPairs(this))
            .apply {
                putAll(helper.toArgsPairs())
                this["crystal_pos"] = ParticleControlerDataBuffers.vec3d(crystalPos.toCenterPos())
            }
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        helper.readFromServer(args)
        args["crystal_pos"]?.let {
            this.crystalPos = BlockPos.ofFloored(it.loadedValue as Vec3d)
        }
    }

}