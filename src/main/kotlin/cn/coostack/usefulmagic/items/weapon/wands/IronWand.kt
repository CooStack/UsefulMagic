package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import cn.coostack.usefulmagic.particles.barrages.wand.SplitBarrage
import cn.coostack.usefulmagic.particles.group.server.EnchantBallParticleServer
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.UseAction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class IronWand(settings: Settings) : WandItem(settings, 40, 5.0) {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(
            Text.translatable(
                "item.iron_wand.description"
            )
        )
        super.appendTooltip(stack, context, tooltip, type)
    }


    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.BOW
    }


    override fun canRepair(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isOf(Items.IRON_INGOT)
    }

    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val canUse = if (!world.isClient) {
            UsefulMagic.state.getDataFromServer(user.uuid)
                .canCost(cost, false)
        } else {
            ClientManaManager.getSelfMana().canCost(cost, true)
        }
        if (!canUse) {
            user.clearActiveItem()
            return
        }
        if (world.isClient) {
            return
        }
        if (user !is ServerPlayerEntity) {
            return
        }
        val tick = getMaxUseTime(stack, user) - remainingUseTicks
        if (tick % 10 != 0 || tick < 10) return
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

        val split = SplitBarrage(
            user.eyePos,
            world,
            HitBox.of(2.0, 2.0, 2.0),
            EnchantBallParticleServer(Vec3d(255.0, 255.0, 255.0), 0.2f, 0.8, 16),
            BarrageOption()
                .apply {
                    enableSpeed = true
                    speed = 1.0
                    noneHitBoxTick = 1
                },
            Vec3d(255.0, 255.0, 255.0),
            user,
            damage = this.damage * 3,
            20 * 1,
        )
        world.playSound(
            null, user.x, user.y, user.z, SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 6f, 1.3f
        )
        split.shooter = user
        split.direction = user.rotationVector
        user.itemCooldownManager.set(this, 2)
        BarrageManager.spawn(split)
        // 扣除魔法
        cost(user)

    }

    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 20 * 3600
    }

}