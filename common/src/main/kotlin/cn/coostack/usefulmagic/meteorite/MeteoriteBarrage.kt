package cn.coostack.usefulmagic.meteorite

import cn.coostack.cooparticlesapi.animation.timeline.ValueConstSpeedAnimator
import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.AbstractBarrage
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.absoluteValue

class MeteoriteBarrage(
    loc: Vec3,
    world: ServerLevel,
    val control: MeteoriteDisplay,
    options: BarrageOption
) : AbstractBarrage(
    loc,
    world,
    options
) {
    var tick = 0
    var mergeCount = 0
    var targetSize = ValueConstSpeedAnimator(0.1, 1)
    var freeze = false
    var mergeSign = false
    private val preTickOnServer = ArrayDeque<MeteoriteBarrage.() -> Unit>()
    private val hitOnServer = ArrayDeque<MeteoriteBarrage.(BarrageHitResult) -> Unit>()
    private var prevScale = 0.0

    private val ignoredEntityTypes = ArrayList<Class<out LivingEntity>>()
    fun ignore(type: Class<out LivingEntity>): MeteoriteBarrage {
        ignoredEntityTypes.add(type)
        return this
    }

    fun addHitOnServer(action: MeteoriteBarrage.(BarrageHitResult) -> Unit): MeteoriteBarrage {
        hitOnServer.add(action)
        return this
    }

    fun addPreTickAction(action: MeteoriteBarrage.() -> Unit): MeteoriteBarrage {
        preTickOnServer.add(action)
        return this
    }

    override fun tick() {
        val next = targetSize.next()
        if ((next - prevScale).absoluteValue > 1e-6) {
            prevScale = next
            control.scale = next.toFloat()
            hitBox.resetMemo()
        }
        if (freeze) {
            return
        }
        super.tick()

        tick++
        preTickOnServer.forEach { it() }
        control.rotateToPoint(direction.asRelative())
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity != shooter && !ignoredEntityTypes.contains(livingEntity::class.java)
    }

    override fun createHitBox(): HitBox {
        return HitBox.of(1.0 * control.scale, 1.0 * control.scale, 1.0 * control.scale)
    }

    override fun createControler(): ServerControler<*> {
        return control
    }

    override fun onHit(result: BarrageHitResult) {
        // 爆炸
        hitOnServer.forEach { it(result) }
    }
}