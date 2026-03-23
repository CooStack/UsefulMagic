package cn.coostack.usefulmagic.particles.emitters.explosion

import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.extend.multiply
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.network.RegistryFriendlyByteBuf

import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import java.util.Random
import kotlin.math.PI

@CooAutoRegister
class ExplosionLineEmitters(pos: Vec3, world: Level?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()
    var targetPoint = Vec3.ZERO
    var emittersVelocity = Vec3.ZERO

    companion object {
        const val ID = "explosion-line-emitters"

        @JvmStatic
        val CODEC = StreamCodec.of<RegistryFriendlyByteBuf, ParticleEmitters>(
            { buf, data ->
                data as ExplosionLineEmitters
                buf.writeVec3(data.targetPoint)
                buf.writeVec3(data.emittersVelocity)
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)
            }, {
                val target = it.readVec3()
                val velocity = it.readVec3()
                val instance = ExplosionLineEmitters(Vec3.ZERO, null)
                decodeBase(instance, it)
                instance.targetPoint = target
                instance.emittersVelocity = velocity
                instance.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                instance
            }
        )
    }

    override fun doTick() {
        pos = pos.add(emittersVelocity)
    }

    val random = Random(System.currentTimeMillis())
    override fun genParticles(lerpProgress: Float): List<Pair<ControlableParticleData, RelativeLocation>> {
        return PointsBuilder()
            .addBall(0.5, ParticleOption.getParticleCounts())
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.yAxis())
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .create().map {
                templateData.clone().apply {
                    setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                } to it
            }
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: RelativeLocation,
        spawnWorld: Level,
        particleLerpProgress: Float,
        posLerpProgress: Float
    ) {
        data.color = Math3DUtil
            .colorOf(
                random.nextInt(100, 200),
                random.nextInt(120, 200),
                255,
            )
        data.alpha = random.nextDouble(0.4, 1.0).toFloat()
        val a = PhysicConstant.EARTH_GRAVITY / 10
        controler.addPreTickAction {
            data.speed = (data.speed - a).coerceAtLeast(0.5)
            data.velocity = data.velocity.add(
                loc.relativize(targetPoint).normalize().multiply(0.3)
            ).add(
                Vec3(
                    random.nextDouble(-0.5, 0.5),
                    random.nextDouble(-0.5, 0.5),
                    random.nextDouble(-0.5, 0.5)
                ).normalize().multiply(0.05)
            ).normalize().multiply(data.speed)

            if (loc.distanceTo(targetPoint) <= 2.0) {
                remove()
            }
        }
    }

    override fun getEmittersID(): String {
        return ID
    }

    override fun update(emitters: ParticleEmitters) {
        super.update(emitters)
        if (emitters !is ExplosionLineEmitters) {
            return
        }
        this.emittersVelocity = emitters.emittersVelocity
        this.templateData = emitters.templateData
        this.targetPoint = emitters.targetPoint
    }

    override fun getCodec(): StreamCodec<RegistryFriendlyByteBuf, ParticleEmitters> {
        return CODEC
    }
}
