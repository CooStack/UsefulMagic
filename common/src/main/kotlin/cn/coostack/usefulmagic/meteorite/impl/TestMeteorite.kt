package cn.coostack.usefulmagic.meteorite.impl

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.meteorite.Meteorite
import cn.coostack.usefulmagic.utils.MathUtil
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import kotlin.math.pow

class TestMeteorite : Meteorite() {
    var tick = 0
    override fun tick() {
        super.tick()
        tick++
        ServerParticleUtil.spawnSingle(
            ParticleTypes.CLOUD, world!!, origin, Vec3.ZERO, true, 0.3, 36
        )

        if (tick % 5 == 0) {

            PointsBuilder().addCircle(1.5, 180)
                .rotateTo(direction)
                .create().forEach {
                    ServerParticleUtil.spawnSingle(
                        ParticleTypes.FLAME, world!!, origin, it.toVector()
                    )
                }

        }

    }

    override fun getBlocks(): Map<RelativeLocation, BlockState> {
        return PointsBuilder().addPoints(MathUtil.getSolidBall(3))
            .create().map {
                ofFloored(it.toVector())
            }
            .map { RelativeLocation(it.x, it.y, it.z) }
            .associateWith {
                Blocks.NETHERRACK.defaultBlockState()
            }
    }

    override fun onHit(pos: Vec3) {
        ServerParticleUtil.spawnSingle(
            ParticleTypes.CLOUD, world as ServerLevel, pos, Vec3.ZERO, true, 0.5, 160
        )
        world!!.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.GENERIC_EXPLODE,
            SoundSource.BLOCKS,
            10f,
            3f
        )

        PointsBuilder().addBall(3.0, 16).addCircle(2.0, 360).create().forEach {
            ServerParticleUtil.spawnSingle(
                ParticleTypes.LARGE_SMOKE, world!!, pos, it.toVector()
            )
        }

        PointsBuilder().addRoundShape(8.0, 1.0, 10, 180)
            .create().forEach {
                val state = world!!.getBlockState(
                    ofFloored(
                        pos
                    )
                )
                val len = 16 / it.length()
                repeat(len.toInt()) { i ->
                    ServerParticleUtil.spawnSingle(
                        BlockParticleOption(ParticleTypes.BLOCK, state), world!!,
                        pos.add(it.toVector().add(0.0, i + 1.0, 0.0)),
                        Vec3.ZERO
                    )
                }
            }
        world?.explode(null, pos.x, pos.y, pos.z, 3f, Level.ExplosionInteraction.TNT)

    }
}