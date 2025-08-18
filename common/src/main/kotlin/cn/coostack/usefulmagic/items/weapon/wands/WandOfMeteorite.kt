package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.barrages.wand.WandMeteoriteBarrage
import cn.coostack.usefulmagic.particles.style.EnchantLineStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.WandMeteoriteSpellcasterStyle
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import java.util.Random
import kotlin.math.roundToInt

/**
 * 最弱的毕业武器
 * 威力更大的陨石
 * 500魔力没问题吧
 */
class WandOfMeteorite(settings: Properties) : WandItem(settings, 1000, 200.0) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.wand_of_meteorite.description"
            )
        )
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }

     override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 240
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun isValidRepairItem(stack: ItemStack, ingredient: ItemStack): Boolean {
        return false
    }

    val rangeBall = PointsBuilder().addBall(64.0, 8 * options).create()
    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val max = getUseDuration(stack, user)
        val tick = max - remainingUseTicks

        if (world.isClientSide) {
            return
        }
        val random = Random(System.currentTimeMillis())

        if (tick % 3 == 0) {
            repeat(if (tick < max / 2) 20 else 40) {
                val it = rangeBall.random()
                val pos = user.position().add(0.0, 1.5, 0.0).add(it.toVector())
                val dir = it.normalize().multiply(-5.75)
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.END_ROD, world as ServerLevel, pos, dir.toVector()
                )
            }
        }
        if (tick % 3 == 0) {
            repeat(6) {
                val x = random.nextDouble(-15.0, 15.0)
                val y = random.nextDouble(-5.0, 10.0)
                val z = random.nextDouble(-15.0, 15.0)
                val pos = user.eyePosition.add(x, y, z)
                val line = RelativeLocation(0.0, random.nextDouble(2.0, 4.0), 0.0)
                val count = (line.length() * 2).roundToInt()
                val style = EnchantLineStyle(line, count, random.nextInt(40, 60))
                style.apply {
                    particleRandomAgePreTick = true
                    fade = true
                    fadeInTick = 30
                    fadeOutTick = 30
                    this.colorOf(255, 117, 148)
                    speedDirection = RelativeLocation(0.0, random.nextDouble(-0.1, 0.1), 0.0)
                }
                ParticleStyleManager.spawnStyle(world, pos, style)
            }
        }
        if (tick % 3 == 0) {
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

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        if (world.isClientSide) {
            return stack
        }
        if (user !is ServerPlayer) {
            return stack
        }
        world as ServerLevel
//        stack.damage(1, world as ServerLevel, user as ServerPlayer) {
//            world.playSound(
//                null,
//                ofFloored(user.pos),
//                SoundEvents.ITEM_BREAK,
//                SoundSource.PLAYERS,
//                5.0f,
//                1.0f
//            )
//        }
        // 声音
        world.playSound(
            null,
            user.position().x, user.position().y, user.position().z,
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.PLAYERS,
            10.0f,
            2.0f
        )
        // 召唤弹幕
        val barrage = WandMeteoriteBarrage(damage, user, user.eyePosition, world)
        barrage.direction = user.forward
        BarrageManager.spawn(barrage)
        // 扣除魔法
        cost(user)
        user.cooldowns.addCooldown(this, 240)
        return super.finishUsingItem(stack, world, user)
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        val res = super.use(world, user, hand)
        if (!res.result.consumesAction()) {
            return res
        }
        if (world.isClientSide) {
            return res
        }
        val style = WandMeteoriteSpellcasterStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.eyePosition, style)
        // 生成小阵法
        return res
    }

}