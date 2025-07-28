package cn.coostack.usefulmagic.utils

import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.formation.api.DefendCrystal
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.explosion.ExplosionBehavior
import kotlin.jvm.optionals.getOrNull
import kotlin.math.max

object ExplosionUtil {
    @JvmStatic
    val DEFAULT = ExplosionBehavior()
    fun createRoundExplosion(
        world: ServerWorld,
        center: Vec3d,
        power: Float,
        radius: Double,
        entity: LivingEntity,
        count: Int,
        sourceType: World.ExplosionSourceType
    ) {
        Math3DUtil.getPolygonInCircleVertices(count, radius)
            .forEach {
                it.add(RelativeLocation.of(center))
                val pos = it.toVector()
                val formation = ServerFormationManager.getFormationFromPos(pos, world)
                if (formation != null && formation.hasCrystalType(DefendCrystal::class.java)) {
                    formation.attack(8f * power, LivingEntityTargetOption(entity, false), pos)
                }
                if (world.isChunkLoaded(it.x.toInt() shr 4, it.z.toInt() shr 4)) {
                    world.createExplosion(entity, it.x, it.y, it.z, power, sourceType)
                }

            }
    }


    fun createSolidBallExplosion(
        maxRadius: Int,
        world: ServerWorld,
        center: Vec3d,
        entity: LivingEntity,
        drop: Boolean = false,
        displayBrokenParticles: Boolean = false
    ) {
        val solidBall = MathUtil.getSolidBall(maxRadius).map {
            BlockPos.ofFloored((it + RelativeLocation.of(center)).toVector())
        }.toSet()
        solidBall.forEach {
            if (!world.isChunkLoaded(it)) {
                return@forEach
            }
            val formation = ServerFormationManager.getFormationFromPos(it.toCenterPos(), world)
            if (formation != null && formation.hasCrystalType(DefendCrystal::class.java)) {
                formation.attack(0.5f, LivingEntityTargetOption(entity, false), it.toCenterPos())
                return@forEach
            }
            val state = world.getBlockState(it)
            val fluid = world.getFluidState(it)
            val resistance = max(state.block.blastResistance, fluid.blastResistance)

            val canBreak = resistance >= 0f && resistance < 1000f && fluid.isEmpty && !state.isAir

            if (!canBreak) return@forEach
            if (!displayBrokenParticles && !drop) {
                world.setBlockState(it, Blocks.AIR.defaultState)
                return@forEach
            }
            if (displayBrokenParticles && !drop) {
                world.breakBlock(it, false, entity)
            }
            if (drop && !displayBrokenParticles) {
                val blockEntity = world.getBlockEntity(it)
                Block.dropStacks(state, world, it, blockEntity, entity, ItemStack.EMPTY)
                world.setBlockState(it, Blocks.AIR.defaultState)
            }
        }
    }

    fun createHollowSphereExplosion(
        currentRadius: Int,
        world: ServerWorld,
        center: Vec3d,
        entity: LivingEntity,
        drop: Boolean = false,
        displayBrokenParticles: Boolean = false
    ) {
        val hollowSphere = MathUtil.getSolidBall(currentRadius).map {
            BlockPos.ofFloored((it + RelativeLocation.of(center)).toVector())
        }.toSet()
        hollowSphere.forEach {
            if (!world.isChunkLoaded(it)) {
                return@forEach
            }
            val formation = ServerFormationManager.getFormationFromPos(it.toCenterPos(), world)
            if (formation != null && formation.hasCrystalType(DefendCrystal::class.java)) {
                formation.attack(0.5f, LivingEntityTargetOption(entity, false), it.toCenterPos())
                return@forEach
            }
            val state = world.getBlockState(it)
            val fluid = world.getFluidState(it)
            val resistance = max(state.block.blastResistance, fluid.blastResistance)
            val canBreak = resistance >= 0f && resistance < 1000f && fluid.isEmpty && !state.isAir
            if (!canBreak) return@forEach
            if (!displayBrokenParticles && !drop) {
                world.setBlockState(it, Blocks.AIR.defaultState)
                return@forEach
            }
            if (displayBrokenParticles && !drop) {
                world.breakBlock(it, false, entity)
            }
            if (drop && !displayBrokenParticles) {
                val blockEntity = world.getBlockEntity(it)
                Block.dropStacks(state, world, it, blockEntity, entity, ItemStack.EMPTY)
                world.setBlockState(it, Blocks.AIR.defaultState)
            }
        }
    }
}