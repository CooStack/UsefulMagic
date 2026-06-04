package cn.coostack.usefulmagic.entity.custom.dragon

import net.minecraft.core.Holder
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

fun playDragonSoundOnce(
    world: Level,
    pos: Vec3,
    sound: SoundEvent,
    source: SoundSource,
    volume: Float,
    pitch: Float,
    visibleRange: Double,
) {
    val serverLevel = world as? ServerLevel ?: return
    val packet = ClientboundSoundPacket(
        Holder.direct(sound),
        source,
        pos.x,
        pos.y,
        pos.z,
        volume,
        pitch,
        serverLevel.random.nextLong(),
    )
    val visibleRangeSqr = visibleRange * visibleRange
    serverLevel.players().forEach { player ->
        if (player.distanceToSqr(pos) <= visibleRangeSqr) {
            player.connection.send(packet)
        }
    }
}

fun playDragonSoundOnce(
    entity: Entity,
    sound: SoundEvent,
    source: SoundSource,
    volume: Float,
    pitch: Float,
    visibleRange: Double,
    pos: Vec3 = entity.position(),
) {
    playDragonSoundOnce(entity.level(), pos, sound, source, volume, pitch, visibleRange)
}
