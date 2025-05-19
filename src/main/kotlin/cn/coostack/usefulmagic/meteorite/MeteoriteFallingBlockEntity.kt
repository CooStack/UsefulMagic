package cn.coostack.usefulmagic.meteorite

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.mixin.FallingBlockEntityMixin
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.entity.EntityChangeListener

class MeteoriteFallingBlockEntity(entityType: EntityType<out FallingBlockEntity>, world: World) :
    FallingBlockEntity(entityType, world) {
    companion object {
        val ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(UsefulMagic.MOD_ID, "meteorite_falling_block"),
            EntityType.Builder.create<MeteoriteFallingBlockEntity>(::MeteoriteFallingBlockEntity, SpawnGroup.MISC)
                .dimensions(0.98f, 0.98f)
                .maxTrackingRange(10)
                .trackingTickInterval(1)
                .build()
        )

        fun init() {}

        fun create(world: World, pos: BlockPos, block: Block): MeteoriteFallingBlockEntity {
            return create(world, pos, block.defaultState)
        }

        fun create(world: World, pos: BlockPos, state: BlockState): MeteoriteFallingBlockEntity {
            val entity = MeteoriteFallingBlockEntity(world, state, Vec3d(pos.x + 0.5, pos.y + 0.0, pos.z + 0.5))
//                .apply {
//                    setNoGravity(true)
//                }
            world.spawnEntity(entity)
            return entity
        }
    }

    var current = 0
    var max = 120 * 10

    constructor(world: World, state: BlockState, pos: Vec3d) : this(ENTITY_TYPE, world) {
        (this as FallingBlockEntityMixin).blockState = state
//        this.intersectionChecked = true
        setPosition(pos.x, pos.y, pos.z)
        setVelocity(Vec3d.ZERO)
        prevX = x
        prevY = y
        prevZ = z
        fallingBlockPos = blockPos
    }

    init {
        dropItem = false
//        setNoGravity(true)
    }

    override fun tick() {
        prevX = x
        prevY = y
        prevZ = z
        if (current++ > max) {
            discard()
        }
    }

    fun tp(vec3d: Vec3d) {
        updatePosition(vec3d.x, vec3d.y, vec3d.z)
    }

    fun checkEntityOrBlockInBox(): Boolean {
        return world
            .getEntitiesByClass(
                LivingEntity::class.java, boundingBox.expand(0.2)
            ) {
                it != this && !it.noClip
            }.isNotEmpty() || isBlock()
    }

    private fun isBlock(): Boolean {
        val pos = blockPos
        val state = world.getBlockState(pos)
        val canAcross = state.isLiquid || state.isAir || state.getCollisionShape(world, pos).isEmpty
        return !canAcross
    }


}