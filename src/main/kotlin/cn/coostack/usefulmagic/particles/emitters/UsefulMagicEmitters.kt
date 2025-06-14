package cn.coostack.usefulmagic.particles.emitters

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionAnimateLaserMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionLineEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionWaveEmitters

object UsefulMagicEmitters {
    fun init() {
        ParticleEmittersManager.register(ExplodeMagicEmitters.ID, ExplodeMagicEmitters.CODEC)
        ParticleEmittersManager.register(CircleEmitters.ID, CircleEmitters.CODEC)
        ParticleEmittersManager.register(FlyingRuneCloudEmitters.ID, FlyingRuneCloudEmitters.CODEC)
        ParticleEmittersManager.register(LightningParticleEmitters.ID, LightningParticleEmitters.CODEC)
        ParticleEmittersManager.register(ExplosionLineEmitters.ID, ExplosionLineEmitters.CODEC)
        ParticleEmittersManager.register(ExplosionAnimateLaserMagicEmitters.ID, ExplosionAnimateLaserMagicEmitters.CODEC)
        ParticleEmittersManager.register(ExplosionWaveEmitters.ID, ExplosionWaveEmitters.CODEC)
        ParticleEmittersManager.register(ParticleWaveEmitters.ID, ParticleWaveEmitters.CODEC)
        ParticleEmittersManager.register(ShrinkParticleEmitters.ID, ShrinkParticleEmitters.CODEC)
        ParticleEmittersManager.register(DirectionShootEmitters.ID, DirectionShootEmitters.CODEC)
        ParticleEmittersManager.register(DiscreteCylinderEmitters.ID, DiscreteCylinderEmitters.CODEC)
        ParticleEmittersManager.register(LineEmitters.ID, LineEmitters.CODEC)
    }

}