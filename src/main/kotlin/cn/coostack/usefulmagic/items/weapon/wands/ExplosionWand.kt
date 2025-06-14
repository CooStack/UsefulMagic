package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.PresetLaserEmitters
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.UsefulMagic
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
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.Random
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class ExplosionWand(settings: Settings) : WandItem(settings, 3500, 1024.0) {
    companion object {
        private val playerAnimations = HashMap<UUID, ParticleAnimation>()
        private val targetBuffer = HashMap<UUID, Vec3d>()
        fun getOrCreateAnimate(uuid: UUID, initMethod: ParticleAnimation.() -> ParticleAnimation): ParticleAnimation {
            return playerAnimations.getOrPut(uuid) { initMethod(ParticleAnimation()) }
        }
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(
            Text.translatable(
                "item.explosion_wand.description"
            )
        )
        super.appendTooltip(stack, context, tooltip, type)
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.BOW
    }


    override fun canRepair(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isOf(Items.DIAMOND)
    }


    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack? {
        if (world.isClient) {
            return stack
        }
        if (user !is ServerPlayerEntity) {
            return stack
        }
        world as ServerWorld
        user.itemCooldownManager.set(this, 20)
        val animate = getOrCreateAnimate(user.uuid) { this }
        // 最后一步操作
        val target = targetBuffer[user.uuid] ?: return super.finishUsing(stack, world, user)
        animate.cancel()
        // 对范围内的实体进行高额伤害
        handleExplodeParticle(world, user, target)
        handleExplode(world, user, target)
        playerAnimations.remove(user.uuid)
        // 扣除魔法
        cost(user)
        return super.finishUsing(stack, world, user)
    }

    private fun handleExplode(
        world: ServerWorld,
        user: ServerPlayerEntity,
        target: Vec3d
    ) {
        val damageSource = world.damageSources.playerAttack(user)
        val entities = world.getEntitiesByClass(
            LivingEntity::class.java,
            Box.of(target, 128.0, 64.0, 128.0)
        ) {
            it.uuid != user.uuid
        }.map { it to it.pos.distanceTo(target) }
        ExplosionUtil.createRoundExplosion(world, target, 15f, 10.0, user, 15, World.ExplosionSourceType.TNT)
        ExplosionUtil.createRoundExplosion(
            world,
            target.add(0.0, -10.0, 0.0),
            18f,
            5.0,
            user,
            8,
            World.ExplosionSourceType.TNT
        )
        ExplosionUtil.createRoundExplosion(
            world,
            target.add(0.0, -15.0, 0.0),
            10f,
            2.0,
            user,
            4,
            World.ExplosionSourceType.TNT
        )

        entities.forEach { entry ->
            val it = entry.first
            val len = entry.second / 2
            it.damage(damageSource, ((damage / len.pow(0.5)).coerceAtMost(damage)).toFloat())
        }
    }


    private fun handleExplodeParticle(world: ServerWorld, user: LivingEntity, target: Vec3d) {
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
            wind.direction = Vec3d(0.0, 0.8, 0.0)
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
        CooParticleAPI.scheduler.runTaskTimerMaxTick(3, 18) {
            animation.spawnSingle()
        }

        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.BLOCK_BEACON_ACTIVATE,
            SoundCategory.PLAYERS,
            10f,
            2f
        )
        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.ENTITY_GENERIC_EXPLODE,
            SoundCategory.PLAYERS,
            10f,
            1.5f
        )
    }

    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val tick = getMaxUseTime(stack, user) - remainingUseTicks
        val max = getMaxUseTime(stack, user)
        if (world.isClient) {
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
                SoundEvents.BLOCK_BEACON_AMBIENT,
                SoundCategory.PLAYERS,
                3f,
                (tick * 2 / max).toFloat()
            )
//            handleExplodeParticle(world as ServerWorld, user, targetBuffer[user.uuid] ?: user.pos)
        }
        val target = targetBuffer[user.uuid] ?: return
        world.getEntitiesByClass(
            LivingEntity::class.java,
            Box.of(target, 128.0, 64.0, 128.0)
        ) {
            it.uuid != user.uuid
        }.forEach {
            it.addStatusEffect(
                StatusEffectInstance(
                    StatusEffects.SLOWNESS, 20, 10
                )
            )
            it.velocity = Vec3d.ZERO
            it.velocityModified = true
            it.movementSpeed = 0f
        }
    }

    override fun onStoppedUsing(stack: ItemStack, world: World, user: LivingEntity, remainingUseTicks: Int) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks)
        val max = getMaxUseTime(stack, user)
        val use = max - remainingUseTicks
        if (user is PlayerEntity) {
            user.itemCooldownManager.set(this, 10)
            // 有减速 所以相当于在扣款
            if (!user.isInCreativeMode) {
                val step = (use.toDouble() / max).coerceAtLeast(0.01)
                val data = UsefulMagic.state.getDataFromServer(user.uuid)
                data.mana -= (cost * step).toInt()
            }
        }
        user.clearActiveItem()
        playerAnimations.remove(user.uuid)?.cancel()
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack?> {
        val res = super.use(world, user, hand)
        if (res.result == ActionResult.FAIL) {
            return res
        }
        if (world.isClient) {
            return res
        }
        val maxUseTime = getMaxUseTime(null, user)
        // 初始化视角指向的实体 or 方块
        // 50个方块内 如果没有符合条件的方块 那就在第50个方块处
        val direction = user.rotationVector.normalize()
        var currentPos = user.eyePos
        var count = 0
        while (count < 100) {
            // 判断是否存在符合要求的点
            // 实体判断
            val box = Box.of(currentPos, 6.0, 6.0, 6.0)
            val entities = world.getEntitiesByClass(LivingEntity::class.java, box) {
                it.uuid != user.uuid
            }
            val entity = entities.minByOrNull { it.distanceTo(user) }

            if (entity != null) {
                currentPos = entity.pos
                break
            }
            val blockPos = BlockPos.ofFloored(currentPos)
            if (!world.shouldTickBlockPos(blockPos)) {
                break
            }
            val block = world.getBlockState(blockPos)

            if (!block.isAir && !block.getCollisionShape(world, blockPos).isEmpty) {
                break
            }
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
                    ExplosionMagicBallStyle(user.uuid), world as ServerWorld, user.pos, -1
                )
            ).addAnimate(
                EmittersAnimate({
                    val startRadius = 5.0
                    val endRandomRange = 40.0

                    val sin = sin(random.nextDouble(-PI, PI)) * startRadius
                    val cos = cos(random.nextDouble(-PI, PI)) * startRadius

                    val randomPos = Vec3d(
                        cos,
                        random.nextDouble(-startRadius, startRadius),
                        sin,
                    )
                    val spawnPos = user.eyePos.add(randomPos)
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
                            it.color = Math3DUtil.colorOf(
                                121, 211, 249
                            )
                        }
                    }
                }, user.pos, 2, -1) {
                    it as LightningParticleEmitters
                    CooParticleAPI.scheduler.runTaskTimerMaxTick(it.maxTick) {
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
                        val spawnPos = user.eyePos.add(p.toVector())
                        ExplosionLineEmitters(spawnPos, user.world as ServerWorld)
                            .apply {
                                this.maxTick = 80
                                val targetPoint = user.eyePos.add(user.rotationVector.normalize().multiply(5.0))
                                this.targetPoint = targetPoint
                                this.templateData.maxAge = 120
                                this.templateData.speed = 1.2
                                this.templateData.velocity = Vec3d(
                                    random.nextDouble(-5.0, 5.0),
                                    random.nextDouble(-5.0, 5.0),
                                    random.nextDouble(-5.0, 5.0),
                                )
                            }
                    }, user.pos, 5, maxUseTime - 40
                ) {
                    it as ExplosionLineEmitters
                    CooParticleAPI.scheduler.runTaskTimerMaxTick(1, maxUseTime - 40) {
                        it.targetPoint = user.eyePos.add(user.rotationVector.normalize().multiply(3.0))
                    }
                }
            ).addAnimate(
                StyleAnimate(
                    ExplosionMagicStyle().apply {
                        rotateDirection = RelativeLocation.yAxis()
                    },
                    user.world as ServerWorld, currentPos.add(0.0, 18.0, 0.0), maxUseTime,
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
                    style to user.eyePos.add(p.toVector())
                }, user.pos, user.world as ServerWorld, 1, maxUseTime / 2) {}
            ).addAnimate(
                StylesAnimate({
                    val style = ExplosionStarStyle(user.uuid)
                    val r = random.nextDouble(3.0, 10.0)
                    val p = PointsBuilder()
                        .addBall(r, 1)
                        .rotateAsAxis(random.nextDouble(-PI, PI))
                        .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
                        .create().random()
                    style to user.eyePos.add(p.toVector())
                }, user.pos, user.world as ServerWorld, 1, maxUseTime / 2) {}
            ).addAnimate(
                EmitterAnimate(
                    PresetLaserEmitters(currentPos, world).apply {
                        targetPoint = Vec3d(0.0, 100.0, 0.0)
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
        CooParticleAPI.scheduler.runTaskTimerMaxTick(maxUseTime) {
            if (canceled) return@runTaskTimerMaxTick
            if (!user.activeItem.isOf(this@ExplosionWand) || hand != user.activeHand) {
                canceled = true
                user.itemCooldownManager.set(this, 10)
                user.clearActiveItem()
                playerAnimations.remove(user.uuid)
                animate.cancel()
            }
        }
        return res
    }

    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 20 * 20
    }

}