package cn.coostack.usefulmagic.meteorite.impl

import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.meteorite.Meteorite
import cn.coostack.usefulmagic.utils.MathUtil
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.particle.BlockStateParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.pow

class TestMeteorite : Meteorite() {
    var tick = 0
    override fun tick() {
        super.tick()
        tick++
        ServerParticleUtil.spawnSingle(
            ParticleTypes.CLOUD, world!!, origin, Vec3d.ZERO, true, 0.3, 36
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
                BlockPos.ofFloored(it.toVector())
            }
            .map { RelativeLocation(it.x, it.y, it.z) }
            .associateWith {
                Blocks.NETHERRACK.defaultState
            }
    }

    override fun onHit(pos: Vec3d) {
        ServerParticleUtil.spawnSingle(
            ParticleTypes.CLOUD, world as ServerWorld, pos, Vec3d.ZERO, true, 0.5, 160
        )
        world!!.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_GENERIC_EXPLODE,
            SoundCategory.BLOCKS,
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
                    BlockPos.ofFloored(
                        pos
                    )
                )
                val len = 16 / it.length()
                repeat(len.toInt()) { i ->
                    ServerParticleUtil.spawnSingle(
                        BlockStateParticleEffect(ParticleTypes.BLOCK, state), world!!,
                        pos.add(it.toVector().add(0.0, i + 1.0, 0.0)),
                        Vec3d.ZERO
                    )
                }
            }
        world?.createExplosion(null, pos.x, pos.y, pos.z, 3f, World.ExplosionSourceType.TNT)

    }
}