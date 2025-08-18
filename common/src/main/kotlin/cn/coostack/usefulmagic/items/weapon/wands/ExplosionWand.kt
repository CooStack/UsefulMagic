package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.PresetLaserEmitters
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.extend.isOf
import cn.coostack.usefulmagic.formation.CrystalFormation
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.particles.animation.EmitterAnimate
import cn.coostack.usefulmagic.particles.animation.EmittersAnimate
import cn.coostack.usefulmagic.particles.animation.ParticleAnimation
import cn.coostack.usefulmagic.particles.animation.StyleAnimate
import cn.coostack.usefulmagic.particles.animation.StylesAnimate
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionAnimateLaserMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionLineEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionWaveEmitters
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicBallStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionStarStyle
import cn.coostack.usefulmagic.utils.ExplosionUtil
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
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
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import java.util.Random
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class ExplosionWand(settings: Properties) : WandItem(settings, 3500, 1024.0) {
    companion object {
        private val playerAnimations = HashMap<UUID, ParticleAnimation>()
        private val targetBuffer = HashMap<UUID, Vec3>()
        fun getOrCreateAnimate(uuid: UUID, initMethod: ParticleAnimation.() -> ParticleAnimation): ParticleAnimation {
            return playerAnimations.getOrPut(uuid) { initMethod(ParticleAnimation()) }
        }
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.explosion_wand.description"
            )
        )
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }


    override fun isValidRepairItem(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isOf(Items.DIAMOND)
    }


    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        if (world.isClientSide) {
            return stack
        }
        if (user !is ServerPlayer) {
            return stack
        }
        world as ServerLevel
        user.cooldowns.addCooldown(this, 20)
        val animate = getOrCreateAnimate(user.uuid) { this }
        // 最后一步操作
        val target = targetBuffer[user.uuid] ?: return super.finishUsingItem(stack, world, user)
        animate.cancel()
        // 对范围内的实体进行高额伤害
        handleExplodeParticle(world, user, target)
        handleExplode(world, user, target)
        playerAnimations.remove(user.uuid)
        // 扣除魔法
        cost(user)
        return super.finishUsingItem(stack, world, user)
    }

    private fun handleExplode(
        world: ServerLevel,
        user: ServerPlayer,
        target: Vec3
    ) {
        val damageSource = world.damageSources().playerAttack(user)
        val entities = world.getEntitiesOfClass(
            LivingEntity::class.java,
            AABB.ofSize(target, 128.0, 64.0, 128.0)
        ) {
            it.uuid != user.uuid
        }.map { it to it.position().distanceTo(target) }
        ExplosionUtil.createRoundExplosion(world, target, 15f, 10.0, user, 15, Level.ExplosionInteraction.TNT)
        ExplosionUtil.createRoundExplosion(
            world,
            target.add(0.0, -10.0, 0.0),
            18f,
            5.0,
            user,
            8,
            Level.ExplosionInteraction.TNT
        )
        ExplosionUtil.createRoundExplosion(
            world,
            target.add(0.0, -15.0, 0.0),
            10f,
            2.0,
            user,
            4,
            Level.ExplosionInteraction.TNT
        )

        ServerFormationManager.getFormationFromPos(target, world)?.let {
            if (it is CrystalFormation && it.hasDefend) {
                it.attack(damage.toFloat() / 4f, null, target)
            }
        }
        entities.forEach { entry ->
            val it = entry.first
            val len = entry.second / 2
            it.hurt(damageSource, ((damage / len.pow(0.5)).coerceAtMost(damage)).toFloat())
        }
    }


    private fun handleExplodeParticle(world: ServerLevel, user: LivingEntity, target: Vec3) {
        // 爆炸粒子
        ServerCameraUtil.sendShake(world, target, 64.0, 2.0, 40)
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
        val animation = ParticleAnimation()

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
                        it.maxAge = 60
                    }
                }
        }

        // 冲击波云
        animation.addAnimate(EmitterAnimate(genWave(20.0, 4.0, 0.8, 1.0), 100))
        animation.addAnimate(EmitterAnimate(genWave(40.0, 8.0, 0.8, 1.0), 100))
        animation.addAnimate(EmitterAnimate(genWave(60.0, 12.5, 0.8, 1.0), 100))
        animation.addAnimate(EmitterAnimate(genWave(80.0, 16.0, 0.8, 1.0), 100))
        animation.addAnimate(EmitterAnimate(genWave(90.0, 7.0, 0.8, 1.0), 100))
        animation.addAnimate(EmitterAnimate(genWave(110.0, 13.0, 0.8, 1.0), 100))
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(3, 18) {
            animation.spawnSingle()
        }

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

    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val tick = getUseDuration(stack, user) - remainingUseTicks
        val max = getUseDuration(stack, user)
        if (world.isClientSide) {
            return
        }
        val canUse = UsefulMagic.state.getDataFromServer(user.uuid)
            .canCost(cost, false)
        if (!canUse) {
            return
        }

        val animation = getOrCreateAnimate(user.uuid) {
            this
        }
        animation.doTick()
        // 对应 玻璃球 魔力条 星星 星星2 魔法阵 魔力柱
        val canNext =
            tick == 1 || tick == 2 || tick == 3 || tick == 11 * 20 || tick == 10 * 20 || tick == 13 * 20 || tick == 19 * 20

        if (canNext) {
            animation.spawnSingle()
        }

        if (tick % 30 == 0) {
            world.playSound(
                null,
                user.x,
                user.y,
                user.z,
                SoundEvents.BEACON_AMBIENT,
                SoundSource.PLAYERS,
                3f,
                (tick * 2 / max).toFloat()
            )
//            handleExplodeParticle(world as ServerLevel, user, targetBuffer[user.uuid] ?: user.pos)
        }
        val target = targetBuffer[user.uuid] ?: return
        world.getEntitiesOfClass(
            LivingEntity::class.java,
            AABB.ofSize(target, 128.0, 64.0, 128.0)
        ) {
            it.uuid != user.uuid
        }.forEach {
            it.addEffect(
                MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 20, 10
                )
            )
            it.deltaMovement = Vec3.ZERO
        }
    }


    override fun releaseUsing(stack: ItemStack, world: Level, user: LivingEntity, remainingUseTicks: Int) {
        super.releaseUsing(stack, world, user, remainingUseTicks)
        val max = getUseDuration(stack, user)
        val use = max - remainingUseTicks
        if (user is Player) {
            user.cooldowns.addCooldown(this, 10)
            // 有减速 所以相当于在扣款
            if (!user.isCreative && user is ServerPlayer) {
                val step = (use.toDouble() / max).coerceAtLeast(0.01)
                val data = UsefulMagic.state.getDataFromServer(user.uuid)
                data.mana -= (cost * step).toInt()
            }
        }
        user.stopUsingItem()
        playerAnimations.remove(user.uuid)?.cancel()
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        val res = super.use(world, user, hand)
        if (!res.result.consumesAction()) {
            return res
        }
        if (world.isClientSide) {
            return res
        }
        val maxUseTime = getUseDuration(user.getItemInHand(hand), user)
        // 初始化视角指向的实体 or 方块
        // 50个方块内 如果没有符合条件的方块 那就在第50个方块处
        val direction = user.forward.normalize()
        var currentPos = user.eyePosition
        var count = 0
        while (count < 100) {
            // 判断是否存在符合要求的点
            // 实体判断
            val box = AABB.ofSize(currentPos, 6.0, 6.0, 6.0)
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
                break
            }
            val block = world.getBlockState(blockPos)

            if (!block.isAir && !block.getCollisionShape(world, blockPos).isEmpty) {
                break
            }
            val cant = ServerFormationManager.getFormationFromPos(currentPos, world as ServerLevel)?.let {
                val option = LivingEntityTargetOption(user, false)
                it is CrystalFormation && it.hasDefend && !it.isFriendly(option)
            } ?: false
            if (cant) break
            currentPos = currentPos.add(direction)
            count++
        }
        // 对currentPos进行一些操作
        targetBuffer[user.uuid] = currentPos
        val animate = getOrCreateAnimate(user.uuid) {
            val random = Random(System.currentTimeMillis())
            val r = 45.0
            addAnimate(
                StyleAnimate(
                    ExplosionMagicBallStyle(user.uuid), world as ServerLevel, user.position(), -1
                )
            ).addAnimate(
                EmittersAnimate({
                    val startRadius = 5.0
                    val endRandomRange = 40.0

                    val sin = sin(random.nextDouble(-PI, PI)) * startRadius
                    val cos = cos(random.nextDouble(-PI, PI)) * startRadius

                    val randomPos = Vec3(
                        cos,
                        random.nextDouble(-startRadius, startRadius),
                        sin,
                    )
                    val spawnPos = user.eyePosition.add(randomPos)
                    LightningParticleEmitters(
                        spawnPos, world
                    ).apply {
                        endPos = RelativeLocation(
                            random.nextDouble(-endRandomRange, endRandomRange),
                            random.nextDouble(-endRandomRange, endRandomRange),
                            random.nextDouble(-endRandomRange, endRandomRange),
                        )
                        maxTick = random.nextInt(1, 5)
                        templateData.also {
                            it.maxAge = 5
                            it.color = Math3DUtil.colorOf(
                                121, 211, 249
                            )
                        }
                    }
                }, user.position(), 2, -1) {
                    it as LightningParticleEmitters
                    CooParticlesAPI.scheduler.runTaskTimerMaxTick(it.maxTick) {
                        val endRandomRange = 40.0
                        it.endPos = RelativeLocation(
                            random.nextDouble(-endRandomRange, endRandomRange),
                            random.nextDouble(-endRandomRange, endRandomRange),
                            random.nextDouble(-endRandomRange, endRandomRange),
                        )
                    }
                }
            ).addAnimate(
                EmittersAnimate(
                    {
                        val p = PointsBuilder()
                            .addBall(r, 1)
                            .rotateAsAxis(random.nextDouble(-PI, PI))
                            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
                            .create().random()
                        val spawnPos = user.eyePosition.add(p.toVector())
                        ExplosionLineEmitters(spawnPos, user.level() as ServerLevel)
                            .apply {
                                this.maxTick = 80
                                val targetPoint = user.eyePosition.add(user.forward.normalize().scale(5.0))
                                this.targetPoint = targetPoint
                                this.templateData.maxAge = 120
                                this.templateData.speed = 1.2
                                this.templateData.velocity = Vec3(
                                    random.nextDouble(-5.0, 5.0),
                                    random.nextDouble(-5.0, 5.0),
                                    random.nextDouble(-5.0, 5.0),
                                )
                            }
                    }, user.position(), 5, maxUseTime - 40
                ) {
                    it as ExplosionLineEmitters
                    CooParticlesAPI.scheduler.runTaskTimerMaxTick(1, maxUseTime - 40) {
                        it.targetPoint = user.eyePosition.add(user.forward.normalize().scale(3.0))
                    }
                }
            ).addAnimate(
                StyleAnimate(
                    ExplosionMagicStyle().apply {
                        rotateDirection = RelativeLocation.yAxis()
                    },
                    user.level() as ServerLevel, currentPos.add(0.0, 18.0, 0.0), maxUseTime,
                )
            ).addAnimate(
                StylesAnimate({
                    val style = ExplosionStarStyle(user.uuid)
                    val r = random.nextDouble(3.0, 10.0)
                    val p = PointsBuilder()
                        .addBall(r, 1)
                        .rotateAsAxis(random.nextDouble(-PI, PI))
                        .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
                        .create().random()
                    style to user.eyePosition.add(p.toVector())
                }, user.position(), user.level() as ServerLevel, 1, maxUseTime / 2) {}
            ).addAnimate(
                StylesAnimate({
                    val style = ExplosionStarStyle(user.uuid)
                    val r = random.nextDouble(3.0, 10.0)
                    val p = PointsBuilder()
                        .addBall(r, 1)
                        .rotateAsAxis(random.nextDouble(-PI, PI))
                        .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
                        .create().random()
                    style to user.eyePosition.add(p.toVector())
                }, user.position(), user.level() as ServerLevel, 1, maxUseTime / 2) {}
            ).addAnimate(
                EmitterAnimate(
                    PresetLaserEmitters(currentPos, world).apply {
                        targetPoint = Vec3(0.0, 100.0, 0.0)
                        lineStartScale = 1f
                        lineScaleMin = 0.01f
                        lineScaleMax = 5f
                        particleCountPreBlock = 1
                        lineStartIncreaseTick = 1
                        lineStartDecreaseTick = 15
                        increaseAcceleration = 0.01f
                        defaultIncreaseSpeed = 0.1f
                        defaultDecreaseSpeed = 0.2f
                        decreaseAcceleration = 0.3f
                        maxDecreaseSpeed = 3f
                        lineMaxTick = 100
                        markDeadWhenArriveMinScale = true
                        particleAge = lineMaxTick / 6 + 1
                        templateData.color = Math3DUtil.colorOf(255, 100, 100)
                    }, 100
                )
            )
            this
        }

        var canceled = false
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(maxUseTime) {
            if (canceled) return@runTaskTimerMaxTick
            if (!user.useItem.isOf(this@ExplosionWand) || hand != user.usedItemHand) {
                canceled = true
                user.cooldowns.addCooldown(this, 10)
                user.stopUsingItem()
                playerAnimations.remove(user.uuid)
                animate.cancel()
            }
        }
        return res
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 20 * 20
    }

}