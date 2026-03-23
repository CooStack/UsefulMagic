package cn.coostack.usefulmagic.particles.particle

import cn.coostack.cooparticlesapi.particles.ControlableParticleEffect
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.Unpooled
import net.minecraft.core.particles.ParticleType
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import java.util.UUID

class WaveParticleEffect(controlUUID: UUID, faceToPlayer: Boolean = true) : ControlableParticleEffect(
    controlUUID,
    faceToPlayer
) {
    companion object {
        @JvmStatic
        val codec: MapCodec<WaveParticleEffect> = RecordCodecBuilder.mapCodec {
            return@mapCodec it.group(
                Codec.BYTE_BUFFER.fieldOf("uuid").forGetter { effect ->
                    val buffer = Unpooled.buffer()
                    buffer.writeBytes(effect.controlUUID.toString().toByteArray())
                    buffer.nioBuffer()
                },
                Codec.BOOL.fieldOf("face_to_player").forGetter { effect ->
                    effect.faceToPlayer
                }
            ).apply(it) { buf, faceToPlayer ->
                WaveParticleEffect(
                    UUID.fromString(String(buf.array())),
                    faceToPlayer
                )
            }
        }

        @JvmStatic
        val packetCode: StreamCodec<FriendlyByteBuf, WaveParticleEffect> = StreamCodec.of(
            { buf, effect ->
                buf.writeUUID(effect.controlUUID)
                buf.writeBoolean(effect.faceToPlayer)
            },
            {
                WaveParticleEffect(it.readUUID(), it.readBoolean())
            }
        )
    }

    override fun getType(): ParticleType<*> {
        return UsefulMagicParticleTypes.WAVE_PARTICLE.get()
    }

    override fun getPacketCodec(): StreamCodec<FriendlyByteBuf, out ControlableParticleEffect> {
        return packetCode
    }

    override fun clone(): ControlableParticleEffect {
        return WaveParticleEffect(controlUUID, faceToPlayer)
    }
}
