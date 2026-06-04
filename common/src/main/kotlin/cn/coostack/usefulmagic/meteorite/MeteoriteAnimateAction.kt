package cn.coostack.usefulmagic.meteorite

import cn.coostack.cooparticlesapi.animation.AnimateAction
import cn.coostack.cooparticlesapi.extend.asRelative
import cn.coostack.cooparticlesapi.extend.minus
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.usefulmagic.particles.composition.magic.MeteoriteMagicComposition
import cn.coostack.usefulmagic.particles.emitters.meteorite.MeteoriteTailEmitter
import cn.coostack.usefulmagic.renderer.MeteoriteAtmosphereFireRenderEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import java.util.ArrayDeque

class MeteoriteAnimateAction(
    var mainSpawnPos: Vec3,
    var fallingTarget: Vec3,
    val world: ServerLevel,
    var targetSize: Float,
    var spellDamage: Float,
    val shooter: LivingEntity
) : AnimateAction() {
    var barrageState: MeteoriteStateAction = MeteoriteState.START
    var mainBarrage: MeteoriteBarrage? = null
    var barrageMagicComposition = MeteoriteMagicComposition(mainSpawnPos, world).apply {
        direction = (fallingTarget - mainSpawnPos).asRelative()
    }
    var mergingTick = 0
    var mergingTotalTick = 0
    var beforeFallingTick = 0
    var fallingFinished = false
    var farFlySoundStarted = false
    var nearFlySoundPlayed = false
    var impactTriggered = false
    val pendingExplosionBlocks = ArrayDeque<BlockPos>()
    var explosionBlocksPerTick = 96
    var tailEmitter = MeteoriteTailEmitter(mainSpawnPos, world)
        .apply {
            this.radius = targetSize.toDouble()
        }
    var mainMeteoriteRenderer: MeteoriteAtmosphereFireRenderEntity? = null

    // 时间会有大概半秒的浮动
    var mergeSpeed = targetSize / 40

    // 生成的副陨石个数范围, 他的时间大概就是 targetSize / mergeSpeed
    // 那么生成陨石的时间会等于这个值 （转int） 单位是tick
    // 然后要平均分摊
    var summingCount = 7 minRangeTo 9
    var fallingSpeed = 3.0

    var explosionPower = 2f

    fun hasPendingExplosion(): Boolean {
        return pendingExplosionBlocks.isNotEmpty()
    }

    override fun checkDone(): Boolean {
        return barrageState == MeteoriteState.FALLING && barrageState.canNext(this)
    }

    override fun tick() {
        if (!barrageMagicComposition.displayed) {
            ParticleCompositionManager.spawn(barrageMagicComposition)
        }
        barrageState.tick(this)
        if (barrageState.canNext(this)) {
            barrageState.next(this)
            if (!done) {
                barrageState.start(this)
            }
        }
    }

    override fun onStart() {
        mergingTick = 0
        mergingTotalTick = 0
        fallingFinished = false
        farFlySoundStarted = false
        nearFlySoundPlayed = false
        impactTriggered = false
        mainMeteoriteRenderer = null
        pendingExplosionBlocks.clear()
        barrageState.start(this)
    }

    override fun onDone() {
        mainMeteoriteRenderer?.discard(10)
        mainMeteoriteRenderer = null
    }
}
