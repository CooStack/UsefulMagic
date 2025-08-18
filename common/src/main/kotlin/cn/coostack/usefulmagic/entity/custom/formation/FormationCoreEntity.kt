package cn.coostack.usefulmagic.entity.custom.formation

import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

/**
 * 该生物的生命值和阵法生命值挂钩
 * 如果实体遭受破坏, 则阵法破坏 (核心破坏为掉落物)
 * 阵法遭受到的攻击 会作用在该实体上
 * 无视防御阵法
 */
class FormationCoreEntity(type: EntityType<*>, world: Level) : Entity(type, world) {
    var core: BlockPos = BlockPos.ZERO

    constructor(world: Level) : this(
        UsefulMagicEntityTypes.FORMATION_CORE_ENTITY.get(), world
    )

    companion object {
        val HEALTH_DATA_TRACKER = SynchedEntityData.defineId(
            FormationCoreEntity::class.java, EntityDataSerializers.FLOAT
        )
    }

    init {
        setNoGravity(true)
    }

    var health: Float
        get() = entityData.get(HEALTH_DATA_TRACKER)
        set(value) = entityData.set(HEALTH_DATA_TRACKER, value)

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        builder.define(HEALTH_DATA_TRACKER, 100f)
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (health < 0f) return false
        health -= amount
        return true
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        nbt.putInt("core_pos_x", core.x)
        nbt.putInt("core_pos_y", core.y)
        nbt.putInt("core_pos_z", core.z)
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        val posX = nbt.getInt("core_pos_x")
        val posY = nbt.getInt("core_pos_y")
        val posZ = nbt.getInt("core_pos_z")
        core = BlockPos(posX, posY, posZ)
    }

    override fun tick() {
        assertFormationCoreEntity()
//        setPosition(core.up(3).center)
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
//        if (pos.distanceTo(entity.pos.center) > 16) {
//            kill()
//            return
//        }
    }
}