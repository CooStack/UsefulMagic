package cn.coostack.usefulmagic.explode

import cn.coostack.usefulmagic.utils.MathUtil
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 无爆炸伤害
 */
class MagicExplode(
    val world: World,
    val center: Vec3d,
    val power: Double,
) {

    companion object {
        private const val attenuation = 0.9
    }

    val effectBlocks = HashSet<BlockPos>()
    val random = Random(System.currentTimeMillis())
    fun clearEffectBlocks(dropChance: Double) {
        effectBlocks.forEach {
            val canDrop = random.nextDouble() <= dropChance
            world.breakBlock(it, canDrop)
        }
    }

    fun calculateExplodeBlocks() {
        // 计算最大作用范围
        val scopeRadius = power.pow(1.0).roundToInt().coerceAtLeast(1)
        var currentRadius = 1
        var currentPower = power
        while (currentRadius <= scopeRadius) {
            val hollowSphere = MathUtil.getHollowSphere(currentRadius - 1)
            hollowSphere.filter {
                val blockPos = BlockPos.ofFloored(it.toVector())
                val state = world.getBlockState(blockPos)
                val blockStrength = state.getHardness(world, blockPos)
                val liquid = state.isLiquid
                val air = state.isAir
                val canBreak = blockStrength > 0 && blockStrength <= currentPower
                canBreak && !air && !liquid
            }
            currentPower *= attenuation
            currentRadius++
        }
    }


}