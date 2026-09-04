package cn.coostack.usefulmagic.utils

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.properties.IntegerProperty
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

object BloomUtil {

    private val bloomFlowers = arrayOf(
        Blocks.DANDELION,
        Blocks.POPPY,
        Blocks.BLUE_ORCHID,
        Blocks.ALLIUM,
        Blocks.AZURE_BLUET,
        Blocks.RED_TULIP,
        Blocks.ORANGE_TULIP,
        Blocks.WHITE_TULIP,
        Blocks.PINK_TULIP,
        Blocks.OXEYE_DAISY,
        Blocks.CORNFLOWER,
        Blocks.LILY_OF_THE_VALLEY,
    )

    fun bloomAround(
        level: ServerLevel,
        center: BlockPos,
        radius: Int,
        verticalRadius: Int,
        maxChanges: Int
    ): Int {
        val radiusSqr = radius * radius
        var changed = 0

        for (rawPos in BlockPos.betweenClosed(
            center.offset(-radius, -verticalRadius, -radius),
            center.offset(radius, verticalRadius, radius)
        )) {
            val dx = rawPos.x - center.x
            val dz = rawPos.z - center.z
            if (dx * dx + dz * dz > radiusSqr) continue

            val pos = rawPos.immutable()
            if (tryBloomOne(level, pos)) {
                changed++
                level.levelEvent(1505, pos, 8) // 骨粉粒子
                if (changed >= maxChanges) break
            }
        }

        return changed
    }

    fun bloomRing(
        level: ServerLevel,
        center: BlockPos,
        radius: Double,
        verticalRadius: Int,
        maxChanges: Int,
        thickness: Double = 1.0
    ): Int {
        if (radius < 0.0 || thickness <= 0.0) return 0

        val halfThickness = thickness / 2.0
        val innerRadius = max(0.0, radius - halfThickness)
        val outerRadius = radius + halfThickness
        val innerRadiusSqr = innerRadius * innerRadius
        val outerRadiusSqr = outerRadius * outerRadius
        val blockRadius = ceil(outerRadius).toInt()
        val yRadius = max(0, verticalRadius)
        var changed = 0

        for (rawPos in BlockPos.betweenClosed(
            center.offset(-blockRadius, -yRadius, -blockRadius),
            center.offset(blockRadius, yRadius, blockRadius)
        )) {
            val dx = rawPos.x - center.x
            val dz = rawPos.z - center.z
            val distanceSqr = (dx * dx + dz * dz).toDouble()
            if (distanceSqr !in innerRadiusSqr..outerRadiusSqr) continue

            val pos = rawPos.immutable()
            if (tryBloomOne(level, pos)) {
                changed++
//                level.levelEvent(1505, pos, 8)
                if (maxChanges in 1..changed) break
            }
        }

        return changed
    }

    fun bloomGradientStep(
        level: ServerLevel,
        center: BlockPos,
        tick: Int,
        maxRadius: Double,
        radiusPerTick: Double,
        verticalRadius: Int,
        maxChanges: Int,
        thickness: Double = 1.0
    ): Int {
        if (tick < 0 || maxRadius < 0.0 || radiusPerTick <= 0.0) return 0

        val radius = min(maxRadius, tick * radiusPerTick)
        return bloomRing(level, center, radius, verticalRadius, maxChanges, thickness)
    }

    private fun tryPlaceFlower(level: ServerLevel, flowerPos: BlockPos): Boolean {
        if (!level.isEmptyBlock(flowerPos)) return false

        val flower = bloomFlowers[level.random.nextInt(bloomFlowers.size)].defaultBlockState()
        if (!flower.canSurvive(level, flowerPos)) return false

        level.setBlock(flowerPos, flower, Block.UPDATE_ALL)
        return true
    }

    private fun growVerticalPlant(
        level: ServerLevel,
        pos: BlockPos,
        block: Block,
        ageProperty: IntegerProperty
    ): Boolean {
        val state = level.getBlockState(pos)
        val above = pos.above()

        if (level.getBlockState(above).`is`(block)) return false // 不是顶端
        if (!level.isEmptyBlock(above)) return false

        var height = 1
        while (height < 3 && level.getBlockState(pos.below(height)).`is`(block)) {
            height++
        }
        if (height >= 3) return false

        val age = state.getValue(ageProperty)
        val nextAge = age + 4

        if (nextAge >= 15) {
            level.setBlockAndUpdate(above, block.defaultBlockState())
            level.setBlock(pos, state.setValue(ageProperty, 0), Block.UPDATE_CLIENTS)
        } else {
            level.setBlock(pos, state.setValue(ageProperty, nextAge), Block.UPDATE_CLIENTS)
        }

        return true
    }

    private fun tryBloomOne(level: ServerLevel, pos: BlockPos): Boolean {
        val state = level.getBlockState(pos)

        if (state.`is`(Blocks.FARMLAND)) {
            return tryBloomOne(level, pos.above())
        }

        if (state.`is`(Blocks.GRASS_BLOCK)) {
            return tryPlaceFlower(level, pos.above())
        }

        if (state.`is`(Blocks.SUGAR_CANE)) {
            return growVerticalPlant(level, pos, Blocks.SUGAR_CANE, SugarCaneBlock.AGE)
        }

        if (state.`is`(Blocks.CACTUS)) {
            return growVerticalPlant(level, pos, Blocks.CACTUS, CactusBlock.AGE)
        }

        if (!state.`is`(BlockTags.CROPS)) return false

        val block = state.block
        if (block !is BonemealableBlock) return false
        if (!block.isValidBonemealTarget(level, pos, state)) return false
        if (!block.isBonemealSuccess(level, level.random, pos, state)) return false

        block.performBonemeal(level, level.random, pos, state)
        return true
    }


}
