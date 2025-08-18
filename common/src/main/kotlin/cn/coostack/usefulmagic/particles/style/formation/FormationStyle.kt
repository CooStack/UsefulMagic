package cn.coostack.usefulmagic.particles.style.formation

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import net.minecraft.core.BlockPos
import java.util.UUID

/**
 * @param formationPos 绑定的核心方块位置 对此方块必须是 BlockWithEntity 且 Entity必须是FormationCoreBlockEntity
 * 设置小, 中, 大 三种不同阵法的粒子效果
 */
abstract class FormationStyle(uuid: UUID) : ParticleGroupStyle(128.0, uuid) {
    var formationPos: BlockPos = BlockPos.ZERO

    enum class FormationStatus {
        /**
         * 阵法正在工作
         *
         * 判定阵法工作方法
         *  1. 特殊效果启用 (攻击阵法正在攻击生物 (判定到了会在20秒内是work状态))
         *  2. 防御阵法生效 (阵法范围内出现非友好生物, 则会出现攻击)
         *  3. TODO 供给水晶(目前还没做) 开始给符合要求的实体(玩家)提供魔力恢复
         */
        WORKING,

        /**
         * 阵法闲置时的状态
         */
        IDLE
    }

    @ControlableBuffer("change_need_respawn")
    var changeStatusNeedRespawn = false
    var status = FormationStatus.IDLE
    val statusHelper = HelperUtil.styleStatus(20).apply {
        loadControler(this@FormationStyle)
        initHelper()
    }


    @ControlableBuffer("time")
    var time = 0
    lateinit var formationEntity: FormationCoreBlockEntity
    override fun onDisplay() {
        if (world!!.isClientSide) {
            // 根据不同状态设置粒子转速
            addPreTickAction {
                displayParticleAnimate()
            }
            return
        }
        val entity = world!!.getBlockEntity(formationPos) ?: let {
            remove()
            return
        }
        if (entity !is FormationCoreBlockEntity) {
            remove()
            return
        }
        formationEntity = entity
        // 判定粒子中心是否失效
        addPreTickAction {
            if (world!!.isClientSide) {
                return@addPreTickAction
            }
            val world = world!!
            val get = world.getBlockEntity(formationPos)
            if (!formationEntity.formation.isActiveFormation()
                || (!formationEntity.formation.inTriggerRangeActive && formationEntity.formation.settings.displayParticleOnlyTrigger)
                || get !is FormationCoreBlockEntity
            ) {
                cancel()
            }
            time++
        }
        // 根据不同状态设置粒子转速
        addPreTickAction {
            displayParticleAnimate()
        }
    }

    /**
     * 播放粒子动画帧
     * @see time 提供的时间参数
     * 推荐将动画和时间参数结合, 在客户端-服务器同步中有更好的效果
     */
    abstract fun displayParticleAnimate()

    fun changeStatus(status: FormationStatus) {
        if (world!!.isClientSide) {
            return
        }
        change({
            this@FormationStyle.status = status
        }, mapOf("formation_status" to ParticleControlerDataBuffers.string(status.name)))
        if (changeStatusNeedRespawn) flush()
    }

    fun cancel() {
        if (statusHelper.displayStatus == StatusHelper.Status.DISABLE.id) {
            return
        }
        statusHelper.setStatus(StatusHelper.Status.DISABLE)
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap(ControlableBufferHelper.getPairs(this))
            .apply {
                putAll(statusHelper.toArgsPairs())
                this["formation_status"] = ParticleControlerDataBuffers.string(status.name)
                this["formation_pos"] =
                    ParticleControlerDataBuffers.intArray(intArrayOf(formationPos.x, formationPos.y, formationPos.z))
            }
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        statusHelper.readFromServer(args)
        args["formation_status"]?.let {
            val s = it.loadedValue as String
            val get = runCatching { FormationStatus.valueOf(s) }.getOrNull() ?: FormationStatus.IDLE
            this.status = get
        }
        args["formation_pos"]?.let {
            val pos = it.loadedValue as IntArray
            formationPos = BlockPos(pos[0], pos[1], pos[2])
        }
    }
}