package cn.coostack.usefulmagic.entity.custom.formation

import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.MobEntity.createMobAttributes
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * 该生物的生命值和阵法生命值挂钩
 * 如果实体遭受破坏, 则阵法破坏 (核心破坏为掉落物)
 * 阵法遭受到的攻击 会作用在该实体上
 * 无视防御阵法
 */
class FormationCoreEntity(type: EntityType<*>, world: World) : Entity(type, world) {
    var core: BlockPos = BlockPos.ORIGIN

    constructor(world: World) : this(
        UsefulMagicEntityTypes.FORMATION_CORE_ENTITY, world
    )

    companion object {
        val HEALTH_DATA_TRACKER = DataTracker.registerData(
            FormationCoreEntity::class.java, TrackedDataHandlerRegistry.FLOAT
        )
    }

    init {
        setNoGravity(true)
    }

    var health: Float
        get() = dataTracker.get(HEALTH_DATA_TRACKER)
        set(value) = dataTracker.set(HEALTH_DATA_TRACKER, value)

    override fun initDataTracker(builder: DataTracker.Builder) {
        builder.add(HEALTH_DATA_TRACKER, 100f)
    }


    override fun damage(source: DamageSource, amount: Float): Boolean {
        if (health < 0f) return false
        health -= amount
        return true
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        val posX = nbt.getInt("core_pos_x")
        val posY = nbt.getInt("core_pos_y")
        val posZ = nbt.getInt("core_pos_z")
        core = BlockPos(posX, posY, posZ)
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        nbt.putInt("core_pos_x", core.x)
        nbt.putInt("core_pos_y", core.y)
        nbt.putInt("core_pos_z", core.z)
    }

    override fun tick() {
        assertFormationCoreEntity()
//        setPosition(core.up(3).toCenterPos())
        super.tick()
    }


    /**
     * 如果方块中心不是目标BlockEntity 则直接死亡
     */
    private fun assertFormationCoreEntity() {
//        val entity = world.getBlockEntity(core) ?: let {
//            kill()
//            return
//        }
//
//        if (entity !is FormationCoreBlockEntity) {
//            kill()
//            return
//        }
//
//        if (!entity.formation.isActiveFormation()) {
//            kill()
//            return
//        }
//        // 强制设置距离
//        // 如果距离大于16 则判定是不正常的实体生成
//        if (pos.distanceTo(entity.pos.toCenterPos()) > 16) {
//            kill()
//            return
//        }
    }
}