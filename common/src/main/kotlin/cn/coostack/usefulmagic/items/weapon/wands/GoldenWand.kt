package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.extend.isOf
import cn.coostack.usefulmagic.particles.barrages.wand.GoldenMagicBallBarrage
import cn.coostack.usefulmagic.particles.style.barrage.wand.GoldenWandStyle
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

class GoldenWand(settings: Properties) : WandItem(settings, 80, 4.0) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.golden_wand.description"
            )
        )
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }


    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }


    override fun isValidRepairItem(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isOf(Items.GOLD_INGOT)
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

        val barrage = GoldenMagicBallBarrage(
            user.eyePosition,
            world,
            damage
        )
        barrage.shooter = user
        barrage.direction = user.forward
        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.PLAYERS,
            3f,
            1.2f
        )
        user.cooldowns.addCooldown(this, 30)
        BarrageManager.spawn(barrage)
        // 扣除魔法
        cost(user)
        return super.finishUsingItem(stack, world, user)
    }


    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val tick = getUseDuration(stack, user) - remainingUseTicks
        if (world.isClientSide) {
            return
        }
        val newPos = user.eyePosition.add(user.forward.scale(5.0))
        if (tick % 10 == 0) {
            world.playSound(
                null,
                user.x,
                user.y,
                user.z,
                SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS,
                3f,
                0.8f
            )
        }
        if (tick % 2 == 0) {
            ServerParticleUtil.spawnSingle(
                ParticleTypes.ENCHANT,
                world as ServerLevel, newPos,
                Vec3(2.0, 2.0, 2.0),
                true,
                0.04,
                10,
                60.0
            )

            val points = PointsBuilder().addCircle(6.0, 32)
                .rotateTo(user.forward)
                .create()
            repeat(16) {
                val it = points.random()
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.CLOUD,
                    world, newPos.add(it.toVector()),
                    it.normalize().multiply(-0.3).toVector(),
                    60.0
                )
            }
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
        val style = GoldenWandStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.eyePosition, style)
        // 生成小阵法
        return res
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 60
    }

}