package cn.coostack.usefulmagic.utils

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.formation.api.DefendCrystal
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ExplosionDamageCalculator
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import kotlin.math.max

object ExplosionUtil {
    @JvmStatic
    val DEFAULT = ExplosionDamageCalculator()
    fun createRoundExplosion(
        world: ServerLevel,
        center: Vec3,
        power: Float,
        radius: Double,
        entity: LivingEntity,
        count: Int,
        sourceType: Level.ExplosionInteraction
    ) {
        Math3DUtil.getPolygonInCircleVertices(count, radius)
            .forEach {
                it.add(RelativeLocation.of(center))
                val pos = it.toVector()
                val formation = ServerFormationManager.getFormationFromPos(pos, world)
                if (formation != null && formation.hasCrystalType(DefendCrystal::class.java)) {
                    formation.attack(8f * power, LivingEntityTargetOption(entity, false), pos)
                }
                if (world.hasChunk(it.x.toInt() shr 4, it.z.toInt() shr 4)) {
                    world.explode(entity, it.x, it.y, it.z, power, sourceType)
                }
            }
    }


    fun createSolidBallExplosion(
        maxRadius: Int,
        world: ServerLevel,
        center: Vec3,
        entity: LivingEntity,
        drop: Boolean = false,
        displayBrokenParticles: Boolean = false
    ) {
        val solidBall = MathUtil.getSolidBall(maxRadius).map {
            ofFloored((it + RelativeLocation.of(center)).toVector())
        }.toSet()
        solidBall.forEach {
            if (!world.hasChunkAt(it)) {
                return@forEach
            }
            val formation = ServerFormationManager.getFormationFromPos(it.center, world)
            if (formation != null && formation.hasCrystalType(DefendCrystal::class.java)) {
                formation.attack(0.5f, LivingEntityTargetOption(entity, false), it.center)
                return@forEach
            }
            val state = world.getBlockState(it)
            val fluid = world.getFluidState(it)
            val resistance = max(state.block.getExplosionResistance(), fluid.getExplosionResistance())

            val canBreak = resistance >= 0f && resistance < 1000f && fluid.isEmpty && !state.isAir

            if (!canBreak) return@forEach
            if (!displayBrokenParticles && !drop) {
                world.setBlock(it, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)
                return@forEach
            }
            if (displayBrokenParticles && !drop) {
                world.destroyBlock(it, false, entity)
            }

            if (drop && !displayBrokenParticles) {
                val blockEntity = world.getBlockEntity(it)
                Block.dropResources(state, world, it, blockEntity, entity, ItemStack.EMPTY)
                world.setBlock(it, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)
            }
        }
    }

    fun createHollowSphereExplosion(
        currentRadius: Int,
        world: ServerLevel,
        center: Vec3,
        entity: LivingEntity,
        drop: Boolean = false,
        displayBrokenParticles: Boolean = false
    ) {
        val hollowSphere = MathUtil.getSolidBall(currentRadius).map {
            ofFloored((it + RelativeLocation.of(center)).toVector())
        }.toSet()
        hollowSphere.forEach {
            if (!world.hasChunkAt(it)) {
                return@forEach
            }
            val formation = ServerFormationManager.getFormationFromPos(it.center, world)
            if (formation != null && formation.hasCrystalType(DefendCrystal::class.java)) {
                formation.attack(0.5f, LivingEntityTargetOption(entity, false), it.center)
                return@forEach
            }
            val state = world.getBlockState(it)
            val fluid = world.getFluidState(it)
            val resistance = max(state.block.explosionResistance, fluid.explosionResistance)
            val canBreak = resistance >= 0f && resistance < 1000f && fluid.isEmpty && !state.isAir
            if (!canBreak) return@forEach
            if (!displayBrokenParticles && !drop) {
                world.setBlock(it, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)
                return@forEach
            }
            if (displayBrokenParticles && !drop) {
                world.destroyBlock(it, false, entity)
            }
            if (drop && !displayBrokenParticles) {
                val blockEntity = world.getBlockEntity(it)
                Block.dropResources(state, world, it, blockEntity, entity, ItemStack.EMPTY)
                world.setBlock(it, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)
            }
        }
    }
}