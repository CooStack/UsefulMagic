package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.barrages.wand.AntiEntityWandBarrage
import cn.coostack.usefulmagic.particles.style.barrage.wand.AntiEntityWandSpellcasterStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.AntiEntityWandStyle
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
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import java.util.Random

class AntiEntityWand(settings: Properties) : WandItem(settings, 1000, 5.0) {

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.anti_entity_wand.description"
            )
        )
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 200
    }


    val options: Int
        get() = ParticleOption.getParticleCounts()


    override fun isValidRepairItem(stack: ItemStack, ingredient: ItemStack): Boolean {
        return false
    }

    val rangeBall = PointsBuilder().addBall(64.0, 6 * options).create()
    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val max = getUseDuration(stack, user)
        val tick = max - remainingUseTicks
        if (world.isClientSide) {
            return
        }
        if (tick % 5 == 0) {
            world.playSound(
                null,
                user.position().x, user.position().y, user.position().z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                10.0f,
                2.0f
            )
        }
        if (tick % 3 == 0) {
            repeat(if (tick < max / 2) 20 else 40) {
                val it = rangeBall.random()
                val pos = user.position().add(0.0, 1.5, 0.0).add(it.toVector())
                val dir = it.normalize().multiply(-5.75)
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.FIREWORK, world as ServerLevel, pos, dir.toVector()
                )
            }
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
        // 声音
        world.playSound(
            null,
            user.position().x, user.position().y, user.position().z,
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundSource.PLAYERS,
            10.0f,
            2.0f
        )
        world.playSound(
            null,
            ofFloored(user.position()),
            SoundEvents.ITEM_BREAK,
            SoundSource.PLAYERS,
            5.0f,
            2.0f
        )
        val random = Random(System.currentTimeMillis())
        val dir = Vec3(
            random.nextDouble(-16.0, 16.0),
            40.0,
            random.nextDouble(-16.0, 16.0)
        )
        val randomSpawn = user.position().add(dir)
        val style = AntiEntityWandStyle(RelativeLocation.of(dir).normalize(), user.uuid)
        val maxAge = style.maxAge
        ParticleStyleManager.spawnStyle(world, randomSpawn, style)
        // 召唤弹幕
        val shape = PointsBuilder()
            .addRoundShape(20.0, 0.5, 18, 180)
        var currentAxis = style.axis
        var tick = 0
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(3, maxAge) {
            tick += 3
            if (tick < 60) return@runTaskTimerMaxTick
            currentAxis = style.axis
            shape.rotateTo(currentAxis)
            val create = shape.create()
            repeat(4) {
                val barragePos = randomSpawn.add(create.random().toVector())
                val barrage = AntiEntityWandBarrage(damage, user, barragePos, world, HitBox.of(1.0, 1.0, 1.0))
                barrage.direction = style.axis.toVector()
                barrage.shooter = user
                BarrageManager.spawn(barrage)
            }
        }
        // 扣除魔法
        cost(user)
        user.cooldowns.addCooldown(this, 60 * 20)
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
        val style = AntiEntityWandSpellcasterStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.position(), style)
        // 生成小阵法
        return res
    }
}