package cn.coostack.usefulmagic.meteorite

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.formation.api.DefendCrystal
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.utils.MathUtil
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import java.util.ArrayDeque
import kotlin.math.max
import kotlin.math.roundToInt

object MeteoriteImpactHelper {
    fun queueImpactExplosion(
        pendingBlocks: ArrayDeque<BlockPos>,
        radius: Int,
        center: Vec3,
        impactDir: Vec3
    ): Int {
        pendingBlocks.clear()
        collectImpactBlocks(radius = radius, center = center, impactDir = impactDir).forEach {
            pendingBlocks.addLast(it)
        }
        return ((pendingBlocks.size + 9) / 10).coerceAtLeast(1)
    }

    fun processPendingExplosion(
        world: ServerLevel,
        pendingBlocks: ArrayDeque<BlockPos>,
        explosionBlocksPerTick: Int,
        source: LivingEntity
    ) {
        repeat(explosionBlocksPerTick) {
            val pos = pendingBlocks.pollFirst() ?: return@repeat
            breakImpactBlock(world, pos, source)
        }
    }

    private fun collectImpactBlocks(
        radius: Int,
        center: Vec3,
        impactDir: Vec3
    ): List<BlockPos> {
        val baseRadius = radius.coerceIn(1, 64)
        val dir = if (impactDir.lengthSqr() <= 1e-6) Vec3(0.0, -1.0, 0.0) else impactDir.normalize()
        val surfaceRadius = (baseRadius * 0.9).roundToInt().coerceAtLeast(1)
        val coreRadius = (baseRadius * 0.62).roundToInt().coerceAtLeast(1)
        val deepRadius = (baseRadius * 0.45).roundToInt().coerceAtLeast(1)
        val horizontalRadius = (baseRadius * 1.25).roundToInt().coerceAtLeast(surfaceRadius)
        val horizontalThickness = (baseRadius * 0.22).roundToInt().coerceIn(1, 4)
        val horizontalOffset = baseRadius * 0.18
        val deepCenter = center.add(dir.scale(baseRadius * 0.35))
        val coreCenter = center.add(dir.scale(baseRadius * 0.7))
        val surfaceBlastCenter = center.add(0.0, horizontalOffset, 0.0)
        val blocks = LinkedHashSet<BlockPos>()
        appendSolidBallBlocks(blocks, surfaceRadius, center)
        appendSolidBallBlocks(blocks, coreRadius, deepCenter)
        appendSolidBallBlocks(blocks, deepRadius, coreCenter)
        appendHorizontalBlastBlocks(blocks, surfaceBlastCenter, horizontalRadius, horizontalThickness)
        appendHorizontalBurstLobes(blocks, surfaceBlastCenter, horizontalRadius, horizontalThickness)
        return blocks.toList()
    }

    private fun appendSolidBallBlocks(target: LinkedHashSet<BlockPos>, radius: Int, center: Vec3) {
        MathUtil.getSolidBall(radius).forEach { offset ->
            val pos = ofFloored((offset + RelativeLocation.of(center)).toVector())
            target.add(pos)
        }
    }

    private fun appendHorizontalBlastBlocks(
        target: LinkedHashSet<BlockPos>,
        center: Vec3,
        radius: Int,
        halfHeight: Int
    ) {
        val radiusSq = radius * radius
        val verticalScale = (halfHeight.toDouble() + 0.75).coerceAtLeast(1.0)
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val horizontalSq = x * x + z * z
                if (horizontalSq > radiusSq) continue
                val horizontalRate = horizontalSq.toDouble() / radiusSq.toDouble().coerceAtLeast(1.0)
                val maxY = ((1.0 - horizontalRate) * verticalScale).roundToInt().coerceAtLeast(0)
                for (y in -maxY..maxY) {
                    target.add(BlockPos.containing(center.add(x.toDouble(), y.toDouble(), z.toDouble())))
                }
            }
        }
    }

    private fun appendHorizontalBurstLobes(
        target: LinkedHashSet<BlockPos>,
        center: Vec3,
        radius: Int,
        halfHeight: Int
    ) {
        val lobeOffset = radius * 0.45
        val lobeRadius = (radius * 0.42).roundToInt().coerceAtLeast(1)
        val offsets = listOf(
            Vec3(lobeOffset, 0.0, 0.0),
            Vec3(-lobeOffset, 0.0, 0.0),
            Vec3(0.0, 0.0, lobeOffset),
            Vec3(0.0, 0.0, -lobeOffset)
        )
        offsets.forEach { offset ->
            appendHorizontalBlastBlocks(
                target = target,
                center = center.add(offset),
                radius = lobeRadius,
                halfHeight = halfHeight.coerceAtLeast(1)
            )
        }
    }

    private fun breakImpactBlock(world: ServerLevel, pos: BlockPos, source: LivingEntity) {
        if (!world.hasChunk(pos.x shr 4, pos.z shr 4)) return
        val center = Vec3.atCenterOf(pos)
        val formation = ServerFormationManager.getFormationFromPos(center, world)
        if (formation != null && formation.hasCrystalType(DefendCrystal::class.java) && formation.isActiveFormation()) {
            formation.attack(0.5f, LivingEntityTargetOption(source, false), center)
            return
        }
        val state = world.getBlockState(pos)
        val fluid = world.getFluidState(pos)
        val resistance = max(state.block.getExplosionResistance(), fluid.getExplosionResistance())
        val canBreak = resistance >= 0f && resistance < 1000f && fluid.isEmpty && !state.isAir
        if (!canBreak) return
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)
    }
}
