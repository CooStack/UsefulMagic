package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ClassParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.particles.control.ParticleControler
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.emitters.LinearResistanceHelper
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.network.FriendlyByteBuf

import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import java.util.Random
import java.util.UUID
import kotlin.math.PI

class FlyingRuneCloudEmitters(var player: UUID, pos: Vec3, world: Level?) : ClassParticleEmitters(pos, world) {
    var templateData = ControlableParticleData()

    init {
        gravity = PhysicConstant.EARTH_GRAVITY / 10
        maxTick = -1
        templateData.effect = ControlableCloudEffect(templateData.uuid)
    }

    companion object {
        const val ID = "flying-rune-cloud-emitters"

        @JvmStatic
        val CODEC = StreamCodec.of<FriendlyByteBuf, ParticleEmitters>(
            { buf, data ->
                data as FlyingRuneCloudEmitters
                buf.writeUUID(data.player)
                encodeBase(data, buf)
                ControlableParticleData.PACKET_CODEC.encode(buf, data.templateData)
            }, {
                val player = it.readUUID()
                val instance = FlyingRuneCloudEmitters(player, Vec3.ZERO, null)
                decodeBase(instance, it)
                instance.templateData = ControlableParticleData.PACKET_CODEC.decode(it)
                instance
            }
        )
    }

    override fun doTick() {
        val player = world?.getPlayerByUUID(player) ?: let {
            cancelled = true
            return
        }
        this.pos = player.position()
    }

    val random = Random(System.currentTimeMillis())
    override fun genParticles(): Map<ControlableParticleData, RelativeLocation> {
        val res = HashMap<ControlableParticleData, RelativeLocation>()
        val velocityList = PointsBuilder()
            .addBall(0.5, 5)
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.yAxis())
            .create()
        val count = random.nextInt(10, 20)
        repeat(count) {
            val velocity = velocityList.random()
            res[
                templateData.clone().apply {
                    this.velocity = velocity.normalize().multiply(-0.01).toVector()
                }
            ] = velocity.clone()
        }

        return res
    }

    override fun singleParticleAction(
        controler: ParticleControler,
        data: ControlableParticleData,
        spawnPos: Vec3,
        spawnWorld: Level
    ) {
        data.maxAge = 10
        data.color = Math3DUtil
            .colorOf(
                random.nextInt(200, 255),
                random.nextInt(200, 255),
                random.nextInt(200, 255),
            )
        data.alpha = random.nextDouble(0.35, 0.85).toFloat()
        data.setTextureSheet(ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT)
        controler.addPreTickAction {
            updatePhysics(this.loc, data)
            val r = (color.x * 255).toInt()
            val g = (color.y * 255).toInt()
            val b = (color.z * 255).toInt()
            colorOfRGB(
                (r + currentAge * 20).coerceIn(0, 255),
                (g + currentAge * 20).coerceIn(0, 255),
                (b + currentAge * 20).coerceIn(0, 255)
            )

        }
    }

    override fun getEmittersID(): String {
        return ID
    }

    override fun getCodec(): StreamCodec<FriendlyByteBuf, ParticleEmitters> {
        return CODEC
    }
}