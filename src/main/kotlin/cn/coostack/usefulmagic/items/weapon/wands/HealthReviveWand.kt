package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.particles.style.EnchantLineStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.HealthReviveStyle
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.Random
import java.util.UUID
import kotlin.math.roundToInt

/**
 * 恢复自己 或者队友的生命值 (目标指向实体)
 *
 * 施法6秒 过后恢复满血 4分钟生命恢复IV 30秒瞬间治疗 4分钟 抗性提升II 4分钟力量I
 */
class HealthReviveWand(settings: Settings) : WandItem(settings, 500, 0.0) {
    val reviveAmount = 60
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(
            Text.translatable(
                "item.health_revive_wand.description"
            )
        )
        super.appendTooltip(stack, context, tooltip, type)
        tooltip[2] = Text.of(
            Text.translatable(
                "item.health_wand.amount"
            ).string.replace(
                "%amount%", "$reviveAmount"
            )
        )
    }

    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.BOW
    }


    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 120
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack? {
        val res = super.finishUsing(stack, world, user)
        if (user !is PlayerEntity) {
            return res
        }
        user.itemCooldownManager.set(UsefulMagicItems.HEALTH_REVIVE_WAND, 60)
        if (world.isClient) {
            return stack
        }
        world as ServerWorld
        user as ServerPlayerEntity
        cost(user)
        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.ITEM_TOTEM_USE,
            SoundCategory.PLAYERS,
            3f,
            1f
        )

        Math3DUtil.generateExplosionCurve(1.2, 8.0, 12.0, 0.5, 3 * options, 15 * options)
            .forEach {
                PointsBuilder()
                    .addLine(RelativeLocation(), it, (it.length() * 3).toInt())
                    .create().forEach { pos ->
                        ServerParticleUtil.spawnSingle(
                            ParticleTypes.CLOUD,
                            world as ServerWorld,
                            user.pos.add(pos.toVector()),
                            it.normalize().multiply(it.y / 3).toVector()
                        )
                    }
            }

        // 给予对象效果
        val direction = user.rotationVector.normalize()
        var currentPos = user.eyePos
        var find = user
        for (i in 1..30) {
            currentPos = currentPos.add(direction)
            val entities = world.getEntitiesByClass(
                LivingEntity::class.java,
                Box.of(currentPos, 1.0, 1.0, 1.0)
            ) {
                it.uuid != user.uuid
            }
            if (entities.isNotEmpty()) {
                find = entities.first()
                break
            }
        }
        // 给予实体效果
        find.health += reviveAmount.toFloat()
        find.addStatusEffect(
            StatusEffectInstance(StatusEffects.REGENERATION, 20 * 60 * 4, 2), user
        )
        find.addStatusEffect(
            StatusEffectInstance(StatusEffects.STRENGTH, 20 * 60 * 4, 0), user
        )
        find.addStatusEffect(
            StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 60 * 4, 2), user
        )
        find.addStatusEffect(
            StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 20 * 10, 1), user
        )
        if (find != user) {
            // 直线
            PointsBuilder().addLine(
                user.eyePos, currentPos, currentPos.distanceTo(user.eyePos).roundToInt() * 10
            ).create().forEach {
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.END_ROD, world as ServerWorld, it.toVector(), Vec3d.ZERO, 64.0
                    )
            }
        }

        PointsBuilder().addBall(1.0, 8).create().forEach {
            ServerParticleUtil
                .spawnSingle(
                    ParticleTypes.CLOUD, world as ServerWorld, find.eyePos, it.toVector(), 64.0
                )
        }

        val create = PointsBuilder().addRoundShape(
            16.0, 2.0, 10, 60
        ).pointsOnEach { it.y -= 1 }.create()
        CooParticleAPI.scheduler.runTaskTimerMaxTick(1, 30) {
            repeat(30) {
                val point = create.random()
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.CLOUD, world as ServerWorld, user.pos.add(point.toVector()), Vec3d(0.0, 3.0, 0.0)
                    )
            }
            repeat(10) {
                val point = create.random()
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.END_ROD,
                        world as ServerWorld,
                        user.pos.add(point.toVector()),
                        Vec3d(0.0, 5.0, 0.0)
                    )
            }
            repeat(5) {
                val point = create.random()
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.FLAME,
                        world as ServerWorld,
                        user.pos.add(point.toVector()),
                        Vec3d(0.0, 2.0, 0.0)
                    )
            }
        }
        return stack
    }

    val rangeBall = PointsBuilder().addBall(12.0, 6 * options).create()
    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val max = getMaxUseTime(stack, user)
        val tick = max - remainingUseTicks

        if (world.isClient) {
            return
        }
        val random = Random(System.currentTimeMillis())

        if (tick % 3 == 0) {
            repeat(if (tick < max / 2) 10 else 20) {
                val it = rangeBall.random()
                val pos = user.pos.add(it.toVector())
                val dir = it.normalize().multiply(-0.6)
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.CLOUD, world as ServerWorld, pos, dir.toVector()
                )

            }
        }
        if (tick % 4 == 0) {
            repeat(6) {
                val x = random.nextDouble(-10.0, 10.0)
                val y = random.nextDouble(-5.0, 10.0)
                val z = random.nextDouble(-10.0, 10.0)
                val pos = user.eyePos.add(x, y, z)
                val line = RelativeLocation(0.0, random.nextDouble(2.0, 4.0), 0.0)
                val count = (line.length() * 2).roundToInt()
                val style = EnchantLineStyle(line, count, random.nextInt(40, 60))
                style.apply {
                    particleRandomAgePreTick = true
                    fade = true
                    fadeInTick = 30
                    fadeOutTick = 30
                    this.colorOf(144, 252, 167)
                    speedDirection = RelativeLocation(0.0, random.nextDouble(-0.1, 0.1), 0.0)
                }
                ParticleStyleManager.spawnStyle(world, pos, style)
            }
        }
        if (tick % 5 == 0) {
            world.playSound(
                null,
                user.x,
                user.y,
                user.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
                3f,
                1.5f
            )
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack?> {
        val res = super.use(world, user, hand)
        if (res.result == ActionResult.FAIL) {
            return res
        }
        if (world.isClient) {
            return res
        }
        val style = HealthReviveStyle(user.uuid, UUID.randomUUID())
        ParticleStyleManager.spawnStyle(
            world, user.pos, style
        )
        return res
    }


    /**
     * 该物品只能使用魔力修复
     */
    override fun canRepair(stack: ItemStack, ingredient: ItemStack): Boolean {
        return false
    }
}