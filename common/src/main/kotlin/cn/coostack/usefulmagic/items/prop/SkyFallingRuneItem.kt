package cn.coostack.usefulmagic.items.prop

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.PresetLaserEmitters
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.scheduler.CooScheduler
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.formation.api.DefendCrystal
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionAnimateLaserMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionWaveEmitters
import cn.coostack.usefulmagic.particles.fall.style.GuildCircleStyle
import cn.coostack.usefulmagic.particles.fall.style.SkyFallingStyle
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.EntityUtil
import cn.coostack.usefulmagic.utils.ExplosionUtil
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.Rarity
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.phys.AABB
import java.util.*
import kotlin.math.exp

/**
 * 天空坠落魔法
 *
 * 消耗品
 * 使用时会在视线前方(最多)100 方块进行爆破
 * 展开时会周围(r <= 10) 的实体进行缓速
 */
class SkyFallingRuneItem : Item(Properties().stacksTo(16).rarity(Rarity.EPIC)) {
    companion object {
        private val playerGuildStyles = HashMap<UUID, GuildCircleStyle>()
        const val knockbackHitDamage = 100f
        const val hitDamage = 200f
        val playerTasks = HashMap<UUID, MutableList<CooScheduler.TickRunnable>>()
        val playerMagicStyles = HashMap<UUID, SkyFallingStyle>()

        @JvmStatic
        fun getTargetLocation(user: Player): Vec3 {
            val vec = user.forward.normalize()
            val eyePos = user.eyePosition
            val range = 100
            val world = user.level()
            var currentPos = eyePos
            var count = 0
            while (count < range) {
                // 判断是否存在符合要求的点
                // 实体判断
                val box = AABB.ofSize(currentPos, 3.0, 3.0, 3.0)
                val entities = world.getEntitiesOfClass(LivingEntity::class.java, box) {
                    it.uuid != user.uuid
                }
                val entity = entities.minByOrNull { it.distanceTo(user) }

                if (entity != null) {
                    currentPos = entity.position()
                    break
                }
                val blockPos = ofFloored(currentPos)
                if (!world.shouldTickBlocksAt(blockPos)) {
                    currentPos = currentPos.add(0.0, 0.5, 0.0)
                    break
                }
                val block = world.getBlockState(blockPos)

                if (!block.isAir && !block.getCollisionShape(world, blockPos).isEmpty) {
                    currentPos = currentPos.add(0.0, 0.5, 0.0)
                    break
                }
                val cant = ServerFormationManager.getFormationFromPos(currentPos, world as ServerLevel)?.let {
                    val option = LivingEntityTargetOption(user, false)
                    it.hasCrystalType(DefendCrystal::class.java) && !it.isFriendly(option)
                } ?: false
                if (cant) break
                currentPos = currentPos.add(vec)
                count++
            }
            return currentPos
        }
    }


    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(Component.literal("§7花费巨大的代价,迅速击溃敌人"))
        tooltip.add(Component.literal("§7超位魔法-天空坠落"))
        tooltip.add(Component.literal("§f右键使用"))
        tooltip.add(Component.literal("§7消耗品"))
        tooltip.add(Component.literal("§7不消耗魔力值"))
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }

    override fun inventoryTick(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) {
        if (!selected) return
        if (world.isClientSide) return
        if (entity !is Player) return
        val world = world as ServerLevel
        val entity = entity as ServerPlayer

        var style = playerGuildStyles.getOrPut(entity.uuid) {
            GuildCircleStyle()
        }
        style.bindPlayer = entity.uuid
        if (!style.valid) {
            style = GuildCircleStyle()
            playerGuildStyles[entity.uuid] = style
            ParticleStyleManager.spawnStyle(world, getTargetLocation(entity), style)
            return
        }
        if (!style.displayed) {
            ParticleStyleManager.spawnStyle(world, getTargetLocation(entity), style)
            return
        }
    }


    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }


    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 5
    }

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        if (user !is Player) return stack

        val hand = user.usedItemHand
        val stack = user.getItemInHand(hand)
        if (!user.isCreative) {
            stack.count -= 1
            user.cooldowns.addCooldown(this, 20 * 60 * 20)
        } else {
            user.cooldowns.addCooldown(this, 20 * 20)
        }
        if (world.isClientSide) return stack
        world.playSound(
            null, user.x, user.y, user.z,
            UsefulMagicSoundEvents.SKY_FALLING_MAGIC_START.get(),
            SoundSource.PLAYERS,
            10f, 1f
        )
        val world = world as ServerLevel
        if (!playerMagicStyles.containsKey(user.uuid) || !(playerMagicStyles[user.uuid]?.valid ?: false)) {
            val style = SkyFallingStyle()
            style.bindPlayer = user.uuid
            ParticleStyleManager.spawnStyle(world, user.position(), style)
            playerMagicStyles[user.uuid] = style
        }
        // 设置target
        val target = getTargetLocation(user)
        val tasks = playerTasks[user.uuid] ?: ArrayList()
        if (tasks.isNotEmpty() && tasks.any { !it.canceled }) {
            return stack
        }
        val explodeTask = CooParticlesAPI.scheduler.runTask(12 * 20) {
            handleExplodeParticle(world, user as ServerPlayer, target)
            handleExplode(world, user, target)
        }
        tasks.add(explodeTask)
        val data = UsefulMagic.state.getDataFromServer(user.uuid)
        val attackTask = CooParticlesAPI.scheduler.runTaskTimerMaxTick(1, 12 * 20) {
            val r = 12.0
            val box = AABB.ofSize(target, r * 2, r * 2, r * 2)
            val entities = world.getEntitiesOfClass(LivingEntity::class.java, box) {
                !data.isFriend(it.uuid) && it.uuid != user.uuid
            }
            entities.forEach { entity ->
                entity.addEffect(
                    MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 20, 10
                    )
                )
                EntityUtil.resetMovement(entity)
            }

            EntityUtil.resetMovement(user)
            user.abilities.mayfly = true
            user.onUpdateAbilities()
        }.setFinishCallback {
            user.abilities.mayfly = false
            user.abilities.flying = false
        }
        tasks.add(attackTask)
        playerTasks[user.uuid] = tasks
        return super.finishUsingItem(stack, world, user)
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        // 防止重复执行 (然后第二个无效)
        val styleAlive = playerMagicStyles.containsKey(user.uuid) && playerMagicStyles[user.uuid]?.valid ?: false
        val taskAlive = (playerTasks[user.uuid] ?: ArrayList()).all { it.canceled }
        if (styleAlive && taskAlive) {
            return super.use(world, user, hand)
        }
        user.startUsingItem(hand)
        return super.use(world, user, hand)
    }


    private fun handleExplode(world: ServerLevel, user: ServerPlayer, target: Vec3) {
        // 有效攻击范围  r <= 24
        // 攻击性击退范围 r <= 36
        // 击退范围 r <= 48
        val r = 48.0
        val data = UsefulMagic.state.getDataFromServer(user.uuid)
        val entities = world.getEntitiesOfClass(LivingEntity::class.java, AABB.ofSize(target, r * 2, r * 2, r * 2)) {
            !data.isFriend(it.uuid) && it.uuid != user.uuid
        }

        val knockbackEntity = entities.filter {
            val d = it.position().distanceTo(target)
            d > 36.0 && d <= r
        }

        val knockbackAndHitEntity = entities.filter {
            val d = it.position().distanceTo(target)
            d <= 36.0 && d > 24.0
        }

        val hitEntity = entities.filter {
            val d = it.position().distanceTo(target)
            d <= 24.0
        }
        // 第一轮的伤害
        knockbackEntity.forEach {
            val dir = target.relativize(it.position()).normalize().scale(3.0)
            // 3.0
            it.deltaMovement = dir
            it.hurtMarked = true
        }

        knockbackAndHitEntity.forEach {
            val dir = target.relativize(it.position()).normalize().scale(2.0)
            // 4.0
            it.deltaMovement = dir
            it.hurtMarked = true
            val source = world.damageSources().playerAttack(user)
            it.hurt(source, knockbackHitDamage)
        }
        hitEntity.forEach {
            val dir = target.relativize(it.position()).normalize().scale(0.5)
            // 5.0
            it.deltaMovement = dir
            it.hurtMarked = true
            val source = world.damageSources().playerAttack(user)
            it.hurt(source, 2048f)
        }

        val formation = ServerFormationManager.getFormationFromPos(target, world)
        formation?.attack(128f, LivingEntityTargetOption(user), target)
        var currentRadius = 1
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(5, 6 * 20) {
            ExplosionUtil.createHollowSphereExplosion(
                currentRadius++,
                world,
                target,
                user
            )
            ExplosionUtil.createHollowSphereExplosion(
                currentRadius++,
                world,
                target,
                user
            )
            formation?.attack(64f, LivingEntityTargetOption(user), target)
            world.getEntitiesOfClass(LivingEntity::class.java, AABB.ofSize(target, 24.0, 24.0, 24.0)) {
                !data.isFriend(it.uuid) && it.uuid != user.uuid
            }.forEach {
                val playerAttack = it.damageSources().playerAttack(user)
                it.hurt(playerAttack, hitDamage / 2)
//                it.timeUntilRegen = 0
                it.hurtTime = 0
            }
        }
    }

    private fun handleExplodeParticle(world: ServerLevel, user: ServerPlayer, target: Vec3) {
        val line = PresetLaserEmitters(target, world).apply {
            targetPoint = Vec3(0.0, 200.0, 0.0)
            lineStartScale = 1f
            lineScaleMin = 0.01f
            lineScaleMax = 50f
            particleCountPreBlock = 1
            lineStartIncreaseTick = 1
            lineStartDecreaseTick = 140
            increaseAcceleration = 0.5f
            defaultIncreaseSpeed = 1f
            defaultDecreaseSpeed = 0.2f
            decreaseAcceleration = 0.5f
            maxDecreaseSpeed = 3f
            lineMaxTick = 180
            markDeadWhenArriveMinScale = true
            particleAge = lineMaxTick / 6 + 1
            templateData.color = Math3DUtil.colorOf(120, 200, 200)
        }
        ParticleEmittersManager.spawnEmitters(line)

        // 爆炸粒子
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(
            10, 180
        ) {
            ServerCameraUtil.sendShake(world, target, 128.0, 1.0, 40)
        }
        val explosion = ExplodeMagicEmitters(target.add(0.0, 5.0, 0.0), world).apply {
            this.templateData.also {
                it.size = 0.4f
            }
            randomParticleAgeMin = 60
            randomParticleAgeMax = 120
            precentDrag = 0.95
            maxTick = 4
            ballCountPow = 3 * 15
            minSpeed = 10.0
            maxSpeed = 40.0
            randomCountMin = 120 * 3
            randomCountMax = 400 * 3
            wind.direction = Vec3(0.0, 0.8, 0.0)
        }
        ParticleEmittersManager.spawnEmitters(explosion)

        val explosionAnimate = ExplosionAnimateLaserMagicEmitters(target, world)
            .apply {
                maxTick = 2
                heightStep = 5.0
                minDiscrete = 5.0
                maxDiscrete = 15.0
                radiusStep = 2.0
                maxRadius = 20.0
                minCount = 20
                maxCount = 60
                templateData.maxAge = 80
                templateData.size = 0.3f
                templateData.effect = ControlableCloudEffect(templateData.uuid)
            }
        ParticleEmittersManager.spawnEmitters(explosionAnimate)

        fun genWave(yOffset: Double, speed: Double, drag: Double, size: Double): ExplosionWaveEmitters {
            return ExplosionWaveEmitters(target.add(0.0, yOffset, 0.0), world)
                .apply {
                    maxTick = 1
                    waveSpeed = speed
                    waveSize = size
                    waveCircleCountMax = 660
                    waveCircleCountMin = 120
                    speedDrag = drag
                    this.templateData.also {
                        it.effect = ControlableCloudEffect(it.uuid)
                        it.size = 1f
                        it.maxAge = 140
                        it.velocity
                    }
                    discrete = 0.1
                    randomVector = true
                    randomSpeed = 0.02
                }
        }

        // 冲击波云
        ParticleEmittersManager.spawnEmitters(genWave(20.0, 6.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(40.0, 10.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(60.0, 15.5, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(80.0, 19.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(90.0, 10.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(110.0, 18.0, 0.85, 1.0))


        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.BEACON_ACTIVATE,
            SoundSource.PLAYERS,
            10f,
            2f
        )
        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.GENERIC_EXPLODE,
            SoundSource.PLAYERS,
            10f,
            1.5f
        )
    }

}