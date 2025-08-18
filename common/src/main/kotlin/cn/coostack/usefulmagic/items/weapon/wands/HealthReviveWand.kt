package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.particles.style.EnchantLineStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.HealthReviveStyle
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.core.particles.ParticleTypes
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
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import java.util.Random
import java.util.UUID
import kotlin.math.roundToInt

/**
 * 恢复自己 或者队友的生命值 (目标指向实体)
 *
 * 施法6秒 过后恢复满血 4分钟生命恢复IV 30秒瞬间治疗 4分钟 抗性提升II 4分钟力量I
 */
class HealthReviveWand(settings: Properties) : WandItem(settings, 500, 0.0) {
    val reviveAmount = 60
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.health_revive_wand.description"
            )
        )
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
        tooltip[2] = Component.literal(
            Component.translatable(
                "item.health_wand.amount"
            ).string.replace(
                "%amount%", "$reviveAmount"
            )
        )
    }

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }


    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 120
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        val res = super.finishUsingItem(stack, world, user)
        if (user !is Player) {
            return res
        }
        user.cooldowns.addCooldown(UsefulMagicItems.HEALTH_REVIVE_WAND.getItem(), 60)
        if (world.isClientSide) {
            return stack
        }
        world as ServerLevel
        user as ServerPlayer
        cost(user)
        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.TOTEM_USE,
            SoundSource.PLAYERS,
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
                            world as ServerLevel,
                            user.position().add(pos.toVector()),
                            it.normalize().multiply(it.y / 3).toVector()
                        )
                    }
            }

        // 给予对象效果
        val direction = user.forward.normalize()
        var currentPos = user.eyePosition
        var find = user
        for (i in 1..30) {
            currentPos = currentPos.add(direction)
            val entities = world.getEntitiesOfClass(
                LivingEntity::class.java,
                AABB.ofSize(currentPos, 1.0, 1.0, 1.0)
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
        find.addEffect(
            MobEffectInstance(MobEffects.REGENERATION, 20 * 60 * 4, 2), user
        )
        find.addEffect(
            MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60 * 4, 0), user
        )
        find.addEffect(
            MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 60 * 4, 2), user
        )
        find.addEffect(
            MobEffectInstance(MobEffects.HEAL, 20 * 10, 1), user
        )
        if (find != user) {
            // 直线
            PointsBuilder().addLine(
                user.eyePosition, currentPos, currentPos.distanceTo(user.eyePosition).roundToInt() * 10
            ).create().forEach {
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.END_ROD, world as ServerLevel, it.toVector(), Vec3.ZERO, 64.0
                    )
            }
        }

        PointsBuilder().addBall(1.0, 8).create().forEach {
            ServerParticleUtil
                .spawnSingle(
                    ParticleTypes.CLOUD, world as ServerLevel, find.eyePosition, it.toVector(), 64.0
                )
        }

        val create = PointsBuilder().addRoundShape(
            16.0, 2.0, 10, 60
        ).pointsOnEach { it.y -= 1 }.create()
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(1, 30) {
            repeat(30) {
                val point = create.random()
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.CLOUD,
                        world as ServerLevel,
                        user.position().add(point.toVector()),
                        Vec3(0.0, 3.0, 0.0)
                    )
            }
            repeat(10) {
                val point = create.random()
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.END_ROD,
                        world as ServerLevel,
                        user.position().add(point.toVector()),
                        Vec3(0.0, 5.0, 0.0)
                    )
            }
            repeat(5) {
                val point = create.random()
                ServerParticleUtil
                    .spawnSingle(
                        ParticleTypes.FLAME,
                        world as ServerLevel,
                        user.position().add(point.toVector()),
                        Vec3(0.0, 2.0, 0.0)
                    )
            }
        }
        return stack
    }

    val rangeBall = PointsBuilder().addBall(12.0, 6 * options).create()
    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val max = getUseDuration(stack, user)
        val tick = max - remainingUseTicks

        if (world.isClientSide) {
            return
        }
        val random = Random(System.currentTimeMillis())

        if (tick % 3 == 0) {
            repeat(if (tick < max / 2) 10 else 20) {
                val it = rangeBall.random()
                val pos = user.position().add(it.toVector())
                val dir = it.normalize().multiply(-0.6)
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.CLOUD, world as ServerLevel, pos, dir.toVector()
                )

            }
        }
        if (tick % 4 == 0) {
            repeat(6) {
                val x = random.nextDouble(-10.0, 10.0)
                val y = random.nextDouble(-5.0, 10.0)
                val z = random.nextDouble(-10.0, 10.0)
                val pos = user.eyePosition.add(x, y, z)
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
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                3f,
                1.5f
            )
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        val res = super.use(world, user, hand)
        if (!res.result.consumesAction()) {
            return res
        }
        if (world.isClientSide) {
            return res
        }
        val style = HealthReviveStyle(user.uuid, UUID.randomUUID())
        ParticleStyleManager.spawnStyle(
            world, user.position(), style
        )
        return res
    }


    /**
     * 该物品只能使用魔力修复
     */
    override fun isValidRepairItem(stack: ItemStack, ingredient: ItemStack): Boolean {
        return false
    }
}