package cn.coostack.usefulmagic.meteorite

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockEntity.Companion.LAST_UPDATE_TIME
import cn.coostack.usefulmagic.mixin.FallingBlockEntityAccessor
import net.minecraft.core.BlockPos
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class MeteoriteFallingBlockEntity(entityType: EntityType<out FallingBlockEntity>, world: Level) :
    FallingBlockEntity(entityType, world) {
    companion object {
        /**
         * 每次重新加载区块的时候 因为不会有任何东西引用（陨石已经没了 没人控制）
         *
         * 如果是由 Meteorite控制的实体 则会更新这个值
         */
        @JvmStatic
        private val LAST_UPDATE_TIME = SynchedEntityData.defineId(
            MeteoriteFallingBlockEntity::class.java, EntityDataSerializers.INT
        )

        fun create(world: Level, pos: BlockPos, block: Block): MeteoriteFallingBlockEntity {
            return create(world, pos, block.defaultBlockState())
        }

        fun create(world: Level, pos: BlockPos, state: BlockState): MeteoriteFallingBlockEntity {
            val entity = MeteoriteFallingBlockEntity(world, state, pos.bottomCenter)
            world.addFreshEntity(entity)
            return entity
        }
    }

    var current = 0
    var max = 120 * 10
    override fun getStartPos(): BlockPos = entityData.get(DATA_START_POS)

    constructor(world: Level, state: BlockState, pos: Vec3) : this(
        UsefulMagicEntityTypes.METEORITE_ENTITY.get(),
        world
    ) {
        (this as FallingBlockEntityAccessor).blockState = state
//        this.intersectionChecked = true
        disableDrop()
        setPos(pos.x, pos.y, pos.z)
        setDeltaMovement(Vec3.ZERO)
        xo = x
        yo = y
        zo = z

        entityData.set(DATA_START_POS, ofFloored(pos))
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(LAST_UPDATE_TIME, 0)
    }

    init {
        dropItem = false
//        setNoGravity(true)
    }

    override fun tick() {
        xo = x
        yo = y
        zo = z
        if (current++ > max) {
            discard()
        }
        if (current - entityData.get(LAST_UPDATE_TIME) > 20) discard()
    }

    fun updateAliveTick() {
        entityData.set(LAST_UPDATE_TIME, current)
    }

    fun tp(vec3d: Vec3) {
        setPos(vec3d)
    }

    fun checkEntityOrBlockInBox(): Boolean {
        return level()
            .getEntitiesOfClass(
                LivingEntity::class.java, boundingBox.inflate(0.2)
            ) {
                it != this && !it.noPhysics
            }.isNotEmpty() || isBlock()
    }

    private fun isBlock(): Boolean {
        val pos = ofFloored(position())
        val state = level().getBlockState(pos)
        val canAcross = !state.fluidState.isEmpty || state.isAir || state.getCollisionShape(level(), pos).isEmpty
        return !canAcross
    }


}