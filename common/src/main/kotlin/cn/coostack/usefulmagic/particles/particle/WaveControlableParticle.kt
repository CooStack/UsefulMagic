package cn.coostack.usefulmagic.particles.particle

import cn.coostack.cooparticlesapi.particles.ControlableParticle
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.SpriteSet
import net.minecraft.world.phys.Vec3
import java.util.*

class WaveControlableParticle(
    world: ClientLevel,
    pos: Vec3,
    velocity: Vec3,
    controlUUID: UUID,
    spriteSet: SpriteSet,
    faceToCamera: Boolean = true
) : ControlableParticle(world, pos, velocity, controlUUID, faceToCamera) {
    init {
        pickSprite(spriteSet)

    }
}
