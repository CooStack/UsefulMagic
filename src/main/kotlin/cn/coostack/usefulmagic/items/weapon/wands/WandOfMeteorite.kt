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
import net.minecraft.entity.LivingEntity
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
import net.minecraft.world.World
import java.util.Random
import kotlin.math.roundToInt

/**
 * 最弱的毕业武器
 * 威力更大的陨石
 * 500魔力没问题吧
 */
class WandOfMeteorite(settings: Settings) : WandItem(settings, 1000, 200.0) {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(
            Text.translatable(
                "item.wand_of_meteorite.description"
            )
        )
        super.appendTooltip(stack, context, tooltip, type)
    }

    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.BOW
    }

    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 240
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun canRepair(stack: ItemStack, ingredient: ItemStack): Boolean {
        return false
    }

    val rangeBall = PointsBuilder().addBall(64.0, 8 * options).create()
    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val max = getMaxUseTime(stack, user)
        val tick = max - remainingUseTicks

        if (world.isClient) {
            return
        }
        val random = Random(System.currentTimeMillis())

        if (tick % 3 == 0) {
            repeat(if (tick < max / 2) 20 else 40) {
                val it = rangeBall.random()
                val pos = user.pos.add(0.0, 1.5, 0.0).add(it.toVector())
                val dir = it.normalize().multiply(-5.75)
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.END_ROD, world as ServerWorld, pos, dir.toVector()
                )
            }
        }
        if (tick % 3 == 0) {
            repeat(6) {
                val x = random.nextDouble(-15.0, 15.0)
                val y = random.nextDouble(-5.0, 10.0)
                val z = random.nextDouble(-15.0, 15.0)
                val pos = user.eyePos.add(x, y, z)
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
                SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
                3f,
                1.5f
            )
        }
    }

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack? {
        if (world.isClient) {
            return stack
        }
        if (user !is ServerPlayerEntity) {
            return stack
        }
        world as ServerWorld
//        stack.damage(1, world as ServerWorld, user as ServerPlayerEntity) {
//            world.playSound(
//                null,
//                BlockPos.ofFloored(user.pos),
//                SoundEvents.ENTITY_ITEM_BREAK,
//                SoundCategory.PLAYERS,
//                5.0f,
//                1.0f
//            )
//        }
        // 声音
        world.playSound(
            null,
            user.pos.x, user.pos.y, user.pos.z,
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.PLAYERS,
            10.0f,
            2.0f
        )
        // 召唤弹幕
        val barrage = WandMeteoriteBarrage(damage, user, user.eyePos, world)
        barrage.direction = user.rotationVector
        BarrageManager.spawn(barrage)
        // 扣除魔法
        cost(user)
        user.itemCooldownManager.set(this, 240)
        return super.finishUsing(stack, world, user)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack?> {
        val res = super.use(world, user, hand)
        if (res.result == ActionResult.FAIL) {
            return res
        }
        if (world.isClient) {
            return res
        }
        val style = WandMeteoriteSpellcasterStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.eyePos, style)
        // 生成小阵法
        return res
    }

}