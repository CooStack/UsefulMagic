package cn.coostack.usefulmagic.particles.particle

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.world.phys.Vec3

class WaveParticleProvider(private val spriteSet: SpriteSet) : ParticleProvider<WaveParticleEffect> {
    override fun createParticle(
        type: WaveParticleEffect,
        level: ClientLevel,
        x: Double,
        y: Double,
        z: Double,
        xSpeed: Double,
        ySpeed: Double,
        zSpeed: Double
    ): Particle {
        return WaveControlableParticle(
            level,
            Vec3(x, y, z),
            Vec3(xSpeed, ySpeed, zSpeed),
            type.controlUUID,
            spriteSet,
            type.faceToPlayer
        )
    }
}
