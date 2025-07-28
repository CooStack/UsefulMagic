package cn.coostack.usefulmagic.meteorite

import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.block.BlockState
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.UUID

/**
 * 陨石
 */
abstract class Meteorite {
    var origin: Vec3d = Vec3d.ZERO
    var world: ServerWorld? = null
    var direction: RelativeLocation = RelativeLocation.zero()
    var shooter: LivingEntity? = null
    var hit = false
        protected set
    var valid = true
        protected set
    var spawned = false
        protected set
    var speed = 1.0
    val entities = HashMap<UUID, MeteoriteFallingBlockEntity>()
    protected val entityLocations = HashMap<RelativeLocation, MeteoriteFallingBlockEntity>()
    var maxAge = 240
    var age = 0
    var uuid = UUID.randomUUID()

    /**
     * 陨石组成
     * 输入的x y z 必须为整数
     */
    abstract fun getBlocks(): Map<RelativeLocation, BlockState>

    /**
     * @param pos 击中的位置
     */
    abstract fun onHit(pos: Vec3d)


    /**
     * 生成对应的falling block
     */
    fun spawn(pos: Vec3d, world: ServerWorld) {
        if (spawned) {
            return
        }
        spawned = true
        this.origin = pos
        this.world = world
        val blocks = getBlocks()
        blocks.forEach {
            val entity = MeteoriteFallingBlockEntity.create(
                world, BlockPos.ofFloored(pos.add(it.key.toVector())), it.value
            )
            entities[entity.uuid] = entity
            entityLocations[it.key] = entity
        }
        MeteoriteManager.addTicks(this)
    }


    fun flushRelative() {
        entityLocations.forEach {
            val key = it.key
            val entity = it.value
            val n = origin.add(key.toVector())
            entity.tp(n)
        }
    }

    open fun tick() {
        if (!valid || hit) return
        // 判定碰撞箱
        val value = entities.asSequence().filter { it.value.checkEntityOrBlockInBox() }.firstOrNull()?.value
        if (value != null) {
            hit(value.pos)
            return
        }
        if (age++ > maxAge) {
            hit(origin)
        }
        // 更新位置
        origin = origin.add(direction.normalize().multiply(speed).toVector())
        flushRelative()
    }


    fun hit(pos: Vec3d) {
        hit = true
        // 解散
        clear()
        onHit(pos)
    }

    fun clear() {
        valid = false
        entities.onEach {
            it.value.discard()
        }.clear()
        entityLocations.clear()
    }


}