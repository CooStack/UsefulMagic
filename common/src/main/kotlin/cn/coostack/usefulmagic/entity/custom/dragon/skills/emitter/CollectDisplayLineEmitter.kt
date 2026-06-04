package cn.coostack.usefulmagic.entity.custom.dragon.skills.emitter

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.api.controler.Controlable
import cn.coostack.cooparticlesapi.api.controler.SerializableData
import cn.coostack.cooparticlesapi.network.particle.emitters.AutoEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.DisplayEntityEmittersData
import cn.coostack.cooparticlesapi.network.particle.emitters.SimpleRandomParticleData
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.NoiseMode
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.display.CylinderBillboardLineDisplay
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import cn.coostack.cooparticlesapi.extend.*

@CooAutoRegister
class CollectDisplayLineEmitter(pos: Vec3, world: Level?) : AutoEmitters(pos, world) {
    @CodecField
    var radius = 12.0

    @CodecField
    var simpleData = SimpleRandomParticleData()

    @CodecField
    var color = Vector3f(1f)

    @CodecField
    var fadeOutOnEmitterRemoved = false


    override fun doTick() {
    }

    override fun genControls(lerpProgress: Float): List<Pair<SerializableData, RelativeLocation>> {
        return PointsBuilder()
            .addBall(radius, simpleData.getRandomCount())
            .applyNoiseOffset(16.0, 16.0, 16.0, NoiseMode.AXIS_UNIFORM)
            .createWithoutClone()
            .map {
                DisplayEntityEmittersData(
                    CylinderBillboardLineDisplay().apply {
                        this.color = this@CollectDisplayLineEmitter.color
                        this.length = 0.8f
                        this.width = 0.1f
                        this.direction = it.toVector()
                    }
                ) to it
            }
    }

    override fun singleControlableAction(
        controler: Controlable<*>,
        data: SerializableData,
        spawnPos: RelativeLocation,
        spawnWorld: Level,
        particleLerpProgress: Float,
        posLerpProgress: Float
    ) {

        val display = controler.getControlObject() as? CylinderBillboardLineDisplay ?: return
        val velocity =
            (this@CollectDisplayLineEmitter.pos - spawnPos.toVector()).normalize() * simpleData.getRandomSpeed()
        val maxLength = (this@CollectDisplayLineEmitter.pos.distanceTo(spawnPos.toVector()))
        display.addPreTickActionPost {
            this as CylinderBillboardLineDisplay
            if (fadeOutOnEmitterRemoved && this@CollectDisplayLineEmitter.canceled) {
                setAlpha(alpha - 0.2f)
                if (alpha <= 0.01f) {
                    remove()
                }
                return@addPreTickActionPost
            }
            this@addPreTickActionPost.pos += velocity
            val progress = 1 - (this.pos.distanceTo(this@CollectDisplayLineEmitter.pos)) / maxLength
            val startPoint = 0.2
            val endPoint = 0.6

            GraphMathHelper.apply {
                // 这里限制了 0.0-0.2 (smooth) 0.2-0.8 (1) >0.8 (0)
                val startStep = smoothStep(0.0, startPoint, progress) * (1 - step(endPoint, progress))
                // 这里限制了 < 0.8 (0) >0.8 (smooth)
                val endStep = 1 - smoothStep(endPoint, .9, progress) * step(endPoint, progress)
                setAlpha((startStep + endStep).coerceIn(0.0, 1.0).toFloat())
            }
            if (progress - 0.9 >= 0) {
                this.remove()
            }
            if (this@addPreTickActionPost.age > 70) {
                remove()
            }
        }
    }
}
