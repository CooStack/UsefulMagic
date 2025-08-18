package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.extend.isOf
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import cn.coostack.usefulmagic.particles.barrages.wand.SplitBarrage
import cn.coostack.usefulmagic.particles.group.server.EnchantBallParticleServer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class IronWand(settings: Properties) : WandItem(settings, 40, 5.0) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.iron_wand.description"
            )
        )
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }


     override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }


    override fun isValidRepairItem(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isOf(Items.IRON_INGOT)
    }

    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val canUse = if (!world.isClientSide) {
            UsefulMagic.state.getDataFromServer(user.uuid)
                .canCost(cost, false)
        } else {
            ClientManaManager.getSelfMana().canCost(cost, true)
        }
        if (!canUse) {
            user.stopUsingItem()
            return
        }
        if (world.isClientSide) {
            return
        }
        if (user !is ServerPlayer) {
            return
        }
        val tick = getUseDuration(stack, user) - remainingUseTicks
        if (tick % 10 != 0 || tick < 10) return
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

        val split = SplitBarrage(
            user.eyePosition,
            world,
            HitBox.of(2.0, 2.0, 2.0),
            EnchantBallParticleServer(Vec3(255.0, 255.0, 255.0), 0.2f, 0.8, 16),
            BarrageOption()
                .apply {
                    enableSpeed = true
                    speed = 1.0
                    noneHitBoxTick = 1
                },
            Vec3(255.0, 255.0, 255.0),
            user,
            damage = this.damage * 3,
            20 * 1,
        )
        world.playSound(
            null, user.x, user.y, user.z, SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 6f, 1.3f
        )
        split.shooter = user
        split.direction = user.forward
        user.cooldowns.addCooldown(this, 2)
        BarrageManager.spawn(split)
        // 扣除魔法
        cost(user)

    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 20 * 3600
    }

}