package cn.coostack.usefulmagic.entity.custom.dragon.eye

import cn.coostack.cooparticlesapi.animation.Animate
import cn.coostack.cooparticlesapi.animation.AnimateManager
import cn.coostack.cooparticlesapi.animation.AnimateNode
import cn.coostack.cooparticlesapi.particles.impl.ControlableEndRodEffect
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.animate.TickableAction
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.dragon.emitters.TrackingTailEmitter
import cn.coostack.usefulmagic.extend.boxCenterPosition
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import kotlin.random.Random

class MagicHeartEntity(
    entityType: EntityType<MagicHeartEntity>,
    level: Level
) : PathfinderMob(entityType, level) {
    var owner: LivingEntity? = null
        set(value) {
            field = value
            ownerUUID = value?.uuid
        }

    private var ownerUUID: UUID? = null
    private var missingOwnerTicks = 0

    // 机制

    private var animate: Animate? = null

    companion object {
        private const val OWNER_UUID_KEY = "owner_uuid"
        private const val NO_OWNER_DEATH_TICKS = 10

        @JvmStatic
        fun createDefaultMobAttributes(): AttributeSupplier.Builder {
            return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0) // 100 太他妈难打了
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FLYING_SPEED, 0.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
        }
    }

    constructor(level: Level) : this(UsefulMagicEntityTypes.MAGIC_HEART_ENTITY_TYPE.get(), level)

    init {
        isNoGravity = true
        xpReward = 0
    }

    fun positionOnHeart() = position().add(0.0, bbHeight.toDouble() / 2, 0.0)

    override fun remove(reason: RemovalReason) {
        super.remove(reason)
        animate?.cancel()
        animate = null
    }

    override fun tick() {
        clearFire()
        if (!level().isClientSide) {
            resolveOwner()
            tickOwnerLifetime()


            animate ?: let {
                animate = Animate()
                    .addNode(
                        AnimateNode()
                            .addAction(
                                TickableAction {
                                    false // 始终
                                }
                                    .addPreTickAction {
                                        if (tickCount % 15 != 0) {
                                            return@addPreTickAction
                                        }
                                        TrackingTailEmitter(
                                            boxCenterPosition().offsetRandomly(
                                                Random.nextDouble(
                                                    16.0,
                                                    32.0
                                                )
                                            ), level()
                                        ).apply {
                                            offsetNoise = 0.2
                                            maxTick = -1
                                            simpleConfig.apply {
                                                minCount = 2
                                                maxCount = 6
                                                minAge = 5
                                                maxAge = 10
                                            }
                                            emittersInterpolator.setRefiner(1.5)
                                            strength = 1.8
                                            arriveRadius = 0.5
                                            particleConfig.apply {
                                                color = Math3DUtil.colorOf(255, 150, 200)
                                                setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT_NOT_HDR)
                                                effect = ControlableEndRodEffect(uuid)
                                            }
                                            velocity = Vec3.ZERO.random()
                                            this.trackingTarget = this@MagicHeartEntity.positionOnHeart()
                                            spawn(world!!, pos)
                                        }
                                    }
                            )
                    )
                AnimateManager.displayAnimateServer(animate!!)
            }

        }
        super.tick()
        clearFire()
        deltaMovement = Vec3.ZERO
        isNoGravity = true
        fallDistance = 0f
        yBodyRot = yRot
        yHeadRot = yRot
    }

    internal fun resolveOwner() {
        val currentOwner = owner
        if (currentOwner != null && !currentOwner.isRemoved && currentOwner.uuid == ownerUUID) {
            return
        }
        val uuid = ownerUUID ?: run {
            owner = null
            return
        }
        owner = null
        ownerUUID = uuid
        val serverLevel = level() as? ServerLevel ?: return
        owner = serverLevel.getEntity(uuid) as? MagicEyeEntity
    }

    fun isOwnedBy(owner: MagicEyeEntity): Boolean {
        return this.owner?.uuid == owner.uuid || ownerUUID == owner.uuid
    }

    private fun tickOwnerLifetime() {
        val currentOwner = owner
        if (currentOwner != null && currentOwner.isAlive && !currentOwner.isRemoved) {
            missingOwnerTicks = 0
            return
        }
        missingOwnerTicks++
        if (missingOwnerTicks >= NO_OWNER_DEATH_TICKS) {
            kill()
        }
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        return super.hurt(source, amount * 0.5f)
    }

    override fun causeFallDamage(
        fallDistance: Float,
        multiplier: Float,
        damageSource: DamageSource
    ): Boolean {
        return false
    }

    override fun removeWhenFarAway(distanceToClosestPlayer: Double): Boolean {
        return false
    }

    override fun isPushable(): Boolean {
        return false
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        ownerUUID?.let { nbt.putUUID(OWNER_UUID_KEY, it) }
        super.addAdditionalSaveData(nbt)
    }


    override fun readAdditionalSaveData(nbt: CompoundTag) {
        ownerUUID = if (nbt.hasUUID(OWNER_UUID_KEY)) nbt.getUUID(OWNER_UUID_KEY) else null
        resolveOwner()
        super.readAdditionalSaveData(nbt)
    }

    override fun isPersistenceRequired(): Boolean {
        return true
    }

    override fun getAmbientSound(): SoundEvent {
        return SoundEvents.CONDUIT_AMBIENT
    }

    override fun getHurtSound(source: DamageSource): SoundEvent {
        return SoundEvents.IRON_GOLEM_REPAIR
    }

    override fun getDeathSound(): SoundEvent {
        return SoundEvents.CONDUIT_DEACTIVATE
    }
}
