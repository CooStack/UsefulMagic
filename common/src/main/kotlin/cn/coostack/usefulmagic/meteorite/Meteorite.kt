package cn.coostack.usefulmagic.meteorite

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import java.util.UUID

/**
 * 陨石
 */
abstract class Meteorite {
    var origin: Vec3 = Vec3.ZERO
    var world: ServerLevel? = null
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
    var uuid: UUID = UUID.randomUUID()

    /**
     * 陨石组成
     * 输入的x y z 必须为整数
     */
    abstract fun getBlocks(): Map<RelativeLocation, BlockState>

    /**
     * @param pos 击中的位置
     */
    abstract fun onHit(pos: Vec3)


    /**
     * 生成对应的falling block
     */
    fun spawn(pos: Vec3, world: ServerLevel) {
        if (spawned) {
            return
        }
        spawned = true
        this.origin = pos
        this.world = world
        val blocks = getBlocks()
        blocks.forEach {
            val entity = MeteoriteFallingBlockEntity.create(
                world, ofFloored(pos.add(it.key.toVector())), it.value
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
            hit(value.position())
            return
        }
        if (age++ > maxAge) {
            hit(origin)
        }
        // 更新位置
        origin = origin.add(direction.normalize().multiply(speed).toVector())
        flushRelative()
    }


    fun hit(pos: Vec3) {
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