package cn.coostack.usefulmagic.items.weapon

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.usefulmagic.items.UsefulMagicToolMaterials
import cn.coostack.usefulmagic.particles.barrages.wand.NetheriteSwordBarrage
import cn.coostack.usefulmagic.particles.emitters.DirectionShootEmitters
import cn.coostack.usefulmagic.particles.emitters.LineEmitters
import cn.coostack.usefulmagic.skill.api.EntitySkillManager
import cn.coostack.usefulmagic.skill.player.ComboCondition
import cn.coostack.usefulmagic.skill.player.HeavyHitSkill
import cn.coostack.usefulmagic.skill.player.PlayerSwordLightSkill
import cn.coostack.usefulmagic.skill.player.PlayerSwordSlashSkill
import cn.coostack.usefulmagic.utils.ComboUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.passive.TameableEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.AxeItem
import net.minecraft.item.ItemStack
import net.minecraft.item.MaceItem
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.WorldEvents
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.math.pow

/**
 * 特性介绍
 *
 * 消耗自身魔力值 恢复耐久度
 * 右键使用技能(不对着方块)
 *
 * 连击会加强技能
 *
 * 设定技能
 * 按照连击次数
 * 技能释放优先级是 高连击次数 -> 低连击次数
 * 拥有最低连击次数可以获得一些粒子效果
 */
class MagicAxe(settings: Settings) : AxeItem(UsefulMagicToolMaterials.MAGIC, settings) {
    companion object {
        val playerSkills = HashMap<UUID, EntitySkillManager>()
        fun getSkillManager(player: ServerPlayerEntity): EntitySkillManager {
            return playerSkills.getOrPut(player.uuid) {
                EntitySkillManager(player).apply {
                    // 添加技能
                    addSkill(HeavyHitSkill(7f))
                    addSkill(PlayerSwordSlashSkill(4f))
                    addSkill(PlayerSwordLightSkill())
                }
            }.also {
                // 由于player死亡会导致会生成一个新的PlayerEntity
                // 于是必须做这个设置
                it.owner = player
            }
        }

        fun postPlayerAxeSkillTick() {
            playerSkills.forEach {
                it.value.tick()
            }
        }
    }

    val random = Random(System.currentTimeMillis())

    /**
     * 造成了伤害
     */
    override fun postDamageEntity(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        if (attacker !is ServerPlayerEntity) {
            super.postDamageEntity(stack, target, attacker)
            return
        }
        val state = ComboUtil.getComboState(attacker.uuid)
        state.increase()
        super.postDamageEntity(stack, target, attacker)
        if (state.count > 6 && random.nextDouble() > 0.8) {
            val direction = attacker.rotationVector
            CooParticleAPI.scheduler.runTaskTimerMaxTick(2, 6) {
                val spawnPos = attacker.eyePos
                    .add(
                        random.nextDouble(-3.0, 3.0),
                        random.nextDouble(-0.5, 2.0),
                        random.nextDouble(-3.0, 3.0),
                    )
                val barrage = NetheriteSwordBarrage(spawnPos, attacker.world as ServerWorld, 5.0, attacker, 1.5)
                barrage.direction = direction
                BarrageManager.spawn(barrage)
                val loc = barrage.loc
                attacker.world.playSound(
                    null,
                    loc.x,
                    loc.y,
                    loc.z,
                    SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                    SoundCategory.PLAYERS,
                    3f,
                    1.5f
                )
            }
        }
        if (state.count < 3) return
        val targetPos = target.eyePos.add(0.0, -0.5, 0.0)
        val spawnPos = targetPos.add(
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
        )
        val dir = spawnPos.relativize(targetPos)
        // 连击技能
        CooParticleAPI.scheduler.runTask(10) {
            val emitter = LineEmitters(spawnPos, attacker.world).apply {
                templateData.apply {
                    effect = ControlableCloudEffect(uuid)
                    maxAge = 15
                    size = 0.1f
                }
                maxTick = 1
                this.endPos = dir.multiply(2.0)
                this.count = (dir.length() * 3).toInt()
            }
            val world = attacker.world
            world.playSound(
                null, attacker.x, attacker.y, attacker.z,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 10f, 2f
            )
            ParticleEmittersManager.spawnEmitters(emitter)
            world.getEntitiesByClass(LivingEntity::class.java, Box.of(targetPos, 4.0, 4.0, 4.0)) {
                it.uuid != attacker.uuid
            }.forEach {
                val source = it.damageSources.playerAttack(attacker)
                it.timeUntilRegen = 0
                it.hurtTime = 0
                it.damage(source, 5f)
            }
        }
    }

    /**
     * 攻击
     * 返回值为是否能造成伤害
     */
    override fun postHit(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        maceHit(attacker, target)
        return super.postHit(stack, target, attacker)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack?> {
        if (world.isClient || user !is ServerPlayerEntity) {
            return super.use(world, user, hand)
        }
        val stack = user.getStackInHand(hand)
        val skillManager = getSkillManager(user)
        if (skillManager.hasActiveSkill()) {
            return super.use(world, user, hand)
        }
        val choice = skillManager.getSkills {
            it is ComboCondition && it.canTrigger(user)
        }.maxByOrNull { (it.value as ComboCondition).triggerComboMin }?.value ?: return super.use(world, user, hand)
        skillManager.setActiveSkill(choice, false)
        user.setCurrentHand(hand)
        return TypedActionResult.consume(stack)
    }

    override fun onStoppedUsing(stack: ItemStack, world: World, user: LivingEntity, remainingUseTicks: Int) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks)
        if (world.isClient) return
        if (user !is ServerPlayerEntity) return
        val skillManager = getSkillManager(user)
        if (skillManager.hasActiveSkill()) {
            skillManager.resetActiveSkill(false)
        }
    }

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack? {
        if (world.isClient) return super.finishUsing(stack, world, user)
        if (user !is ServerPlayerEntity) return super.finishUsing(stack, world, user)
        val skillManager = getSkillManager(user)
        if (skillManager.hasActiveSkill()) {
            skillManager.resetActiveSkill(false)
        }
        return super.finishUsing(stack, world, user)
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        if (!selected) {
            return
        }

        if (entity !is ServerPlayerEntity) {
            return
        }

        val state = ComboUtil.getComboState(entity.uuid)
        if (state.count < 3) {
            return
        }

        val emitter = DirectionShootEmitters(entity.pos, world)
            .apply {
                maxTick = 1
                shootDirection = Vec3d(0.0, 1.0, 0.0)
                randomX = 0.2
                randomZ = 0.2
                randomY = 0.1
                speedDrag = 0.98
                count = 3
                shootType = EmittersShootTypes.box(
                    HitBox.of(1.0, 0.5, 1.0)
                )
                gravity = PhysicConstant.EARTH_GRAVITY
                templateData.apply {
                    this.speed = 0.3
                    this.maxAge = 20
                    this.effect = ControlableCloudEffect(uuid)
                }
            }
        ParticleEmittersManager.spawnEmitters(emitter)
    }

    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        if (user !is ServerPlayerEntity) {
            return super.getMaxUseTime(stack, user)
        }
        val manager = getSkillManager(user)
        if (!manager.hasActiveSkill()) {
            return super.getMaxUseTime(stack, user)
        }
        return manager.active!!.getMaxHoldingTick(user)
    }

    private fun maceHit(attacker: LivingEntity, target: LivingEntity) {
        if (attacker is ServerPlayerEntity && MaceItem.shouldDealAdditionalDamage(attacker)) {
            val serverWorld = attacker.world as ServerWorld
            if (attacker.shouldIgnoreFallDamageFromCurrentExplosion() && attacker.currentExplosionImpactPos != null) {
                if (attacker.currentExplosionImpactPos!!.y > attacker.pos.y) {
                    attacker.currentExplosionImpactPos = attacker.pos
                }
            } else {
                attacker.currentExplosionImpactPos = attacker.pos
            }

            attacker.setIgnoreFallDamageFromCurrentExplosion(true)
            attacker.velocity = attacker.velocity.withAxis(Direction.Axis.Y, 0.01)
            attacker.networkHandler.sendPacket(EntityVelocityUpdateS2CPacket(attacker))
            if (target.isOnGround) {
                attacker.setSpawnExtraParticlesOnFall(true)
                val soundEvent =
                    if (attacker.fallDistance > 5.0f) SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY else SoundEvents.ITEM_MACE_SMASH_GROUND
                serverWorld.playSound(
                    null,
                    attacker.x,
                    attacker.y,
                    attacker.z,
                    soundEvent,
                    attacker.soundCategory,
                    1.0f,
                    1.0f
                )
            } else {
                serverWorld.playSound(
                    null,
                    attacker.x,
                    attacker.y,
                    attacker.z,
                    SoundEvents.ITEM_MACE_SMASH_AIR,
                    attacker.soundCategory,
                    1.0f,
                    1.0f
                )
            }
            knockbackNearbyEntities(serverWorld, attacker, target)
        }
    }

    private fun knockbackNearbyEntities(world: World, player: PlayerEntity, attacked: Entity) {
        world.syncWorldEvent(WorldEvents.SMASH_ATTACK, attacked.steppingPos, 750)
        world.getEntitiesByClass<LivingEntity?>(
            LivingEntity::class.java,
            attacked.boundingBox.expand(3.5),
            getKnockbackPredicate(player, attacked)
        ).forEach(
            Consumer { entity: LivingEntity? ->
                val vec3d = entity!!.getPos().subtract(attacked.pos)
                val d = getKnockback(player, entity, vec3d)
                val vec3d2 = vec3d.normalize().multiply(d)
                if (d > 0.0) {
                    entity.addVelocity(vec3d2.x, 0.7, vec3d2.z)
                    if (entity is ServerPlayerEntity) {
                        entity.networkHandler.sendPacket(EntityVelocityUpdateS2CPacket(entity))
                    }
                }
            })
    }

    private fun getKnockbackPredicate(player: PlayerEntity, attacked: Entity): Predicate<LivingEntity?> {
        return Predicate { entity: LivingEntity? ->
            val bl = !entity!!.isSpectator
            val bl2 = entity !== player && entity !== attacked
            val bl3 = !player.isTeammate(entity)
            val bl4 = !(entity is TameableEntity && entity.isTamed && player.getUuid() == entity.ownerUuid)
            val bl5 = !(entity is ArmorStandEntity && entity.isMarker)
            val bl6 = attacked.squaredDistanceTo(entity) <= 3.5.pow(2.0)
            bl && bl2 && bl3 && bl4 && bl5 && bl6
        }
    }

    fun getKnockback(player: PlayerEntity, attacked: LivingEntity, distance: Vec3d): Double {
        return ((3.5 - distance.length())
                * 0.7f
                * (if (player.fallDistance > 5.0f) 2 else 1)
                * (1.0 - attacked.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)))
    }
}