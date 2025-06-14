package cn.coostack.usefulmagic.particles.emitters.explosion

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.utils.ParticleOption
import net.fabricmc.loader.impl.lib.sat4j.core.Vec
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.entity.LivingEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.Random
import kotlin.math.PI

class ExplosionLineEmitters(pos: Vec3d, world: World?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()
    var targetPoint = Vec3d.ZERO
    var emittersVelocity = Vec3d.ZERO

    companion object {
        const val ID = "explosion-line-emitters"

        @JvmStatic
        val CODEC = PacketCodec.ofStatic<PacketByteBuf, ParticleEmitters>(
            { buf, data ->
                data as ExplosionLineEmitters
                buf.writeVec3d(data.targetPoint)
                buf.writeVec3d(data.emittersVelocity)
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)
            }, {
                val target = it.readVec3d()
                val velocity = it.readVec3d()
                val instance = ExplosionLineEmitters(Vec3d.ZERO, null)
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
    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        return PointsBuilder()
            .addBall(
                0.5,
                ParticleOption.getParticleCounts()
            )
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.yAxis())
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .create().associateBy {
                templateData.clone()
            }
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3d,
        spawnWorld: World
    ) {
//        data.maxAge = 10
        data.color = Math3DUtil
            .colorOf(
                random.nextInt(100, 150),
                random.nextInt(100, 150),
                255,
            )
        data.alpha = random.nextDouble(0.4, 0.7).toFloat()
        data.textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
        val a = PhysicConstant.EARTH_GRAVITY / 10
        controler.addPreTickAction {
            data.speed = (data.speed - a).coerceAtLeast(0.5)
            data.velocity = data.velocity.add(
                pos.relativize(targetPoint).normalize().multiply(0.3)
            )
                .add(
                    Vec3d(
                        random.nextDouble(-0.5, 0.5),
                        random.nextDouble(-0.5, 0.5),
                        random.nextDouble(-0.5, 0.5)
                    ).normalize().multiply(0.05)
                ).normalize().multiply(data.speed)

            if (pos.distanceTo(targetPoint) <= 2.0) {
                markDead()
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

    override fun getCodec(): PacketCodec<PacketByteBuf, ParticleEmitters> {
        return CODEC
    }
}