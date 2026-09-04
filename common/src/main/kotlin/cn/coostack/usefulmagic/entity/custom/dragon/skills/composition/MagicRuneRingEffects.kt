package cn.coostack.usefulmagic.entity.custom.dragon.skills.composition

import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.PI

object MagicRuneRingEffects {
    fun spawnRewindRings(position: Vec3, level: Level): ArrayList<MagicRuneRingComposition> {
        val compositions = arrayListOf(
            MagicRuneRingComposition(position, level).apply {
                radianThetaOffset = -PI * 0.73
                radianAlphaOffset = PI * 0.41
                alphaRotationSpeed = PI / 256
                thetaRotationSpeed = PI / 192
                radius = 8.0
            },
            MagicRuneRingComposition(position, level).apply {
                radianThetaOffset = PI * 0.18
                radianAlphaOffset = -PI * 0.62
                alphaRotationSpeed = PI / 128
                thetaRotationSpeed = PI / 96
                radius = 10.0
            },
            MagicRuneRingComposition(position, level).apply {
                radianThetaOffset = -PI * 0.31
                radianAlphaOffset = PI * 0.86
                alphaRotationSpeed = PI / 80
                thetaRotationSpeed = PI / 64
                radius = 12.0
            },
            MagicRuneRingComposition(position, level).apply {
                radianThetaOffset = PI * 0.57
                radianAlphaOffset = -PI * 0.14
                alphaRotationSpeed = PI / 32
                thetaRotationSpeed = PI / 48
                radius = 14.0
            },
            MagicRuneRingComposition(position, level).apply {
                radianThetaOffset = -PI * 0.92
                radianAlphaOffset = PI * 0.25
                alphaRotationSpeed = PI / 16
                thetaRotationSpeed = PI / 24
                radius = 16.0
            }
        )

        compositions.forEach {
            ParticleCompositionManager.spawn(it)
        }
        return compositions
    }
}
