package cn.coostack.usefulmagic.entity.custom.dragon.eye.phases

import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.util.FlightMovementUtil
import cn.coostack.usefulmagic.entity.util.phases.PhaseDefinition
import cn.coostack.usefulmagic.entity.util.phases.PhaseRegistries
import cn.coostack.usefulmagic.entity.util.phases.PhaseResult
import cn.coostack.usefulmagic.entity.util.phases.PhaseRuntime

class EyeOrbitCloserPhase : PhaseDefinition<MagicEyeEntity> {
    companion object {
        const val ID = "eye-orbit-closer-phase"
        val HOLDER = PhaseRegistries.register(ID) { EyeOrbitCloserPhase() }
    }

    private var ticking = 0
    override fun canBegin(instance: MagicEyeEntity, runtime: PhaseRuntime): Boolean {
        return instance.target != null
    }

    override fun begin(instance: MagicEyeEntity, runtime: PhaseRuntime) {
        // TODO: 初始化环绕接近参数
        ticking = 0
    }

    override fun step(instance: MagicEyeEntity, runtime: PhaseRuntime): PhaseResult {
        val target = runtime.peekTargetOrNull() ?: return PhaseResult.Reset
        // 判断是否到达， 到达时直接转换为hover （继承target）
        ticking++
        if (ticking > 20) {
            instance.deltaMovement = FlightMovementUtil.orbitApproach(
                instance.positionOnEye(),
                instance.deltaMovement,
                target,
                0.5,
                4.0,
                0.8,
                0.95,
                1.0
            )
            if (Math3DUtil.isPointCrossBySphere(
                    instance.positionOnEye(),
                    instance.deltaMovement,
                    1,
                    target
                )
            ) {
                return PhaseResult.Next(EyeHoverPhase.HOLDER.get(), true)
            }
        } else {
            instance.deltaMovement = FlightMovementUtil.orbitAround(
                instance.positionOnEye(),
                instance.deltaMovement,
                target,
                8.0,
                0.0,
                0.8,
                0.95
            )
        }

        return PhaseResult.Continue
    }

    override fun end(instance: MagicEyeEntity, runtime: PhaseRuntime) {
    }

    override fun id(): String {
        return ID
    }
}