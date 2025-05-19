package cn.coostack.usefulmagic.items.wands

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.usefulmagic.particles.barrages.SplitBarrage
import cn.coostack.usefulmagic.particles.group.server.EnchantBallParticleServer
import net.minecraft.entity.LivingEntity
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

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack? {
        if (world.isClient) {
            return stack
        }
        if (user !is ServerPlayerEntity) {
            return stack
        }
        stack.damage(1, world as ServerWorld, user as ServerPlayerEntity) {
            world.playSound(
                null,
                BlockPos.ofFloored(user.pos),
                SoundEvents.ENTITY_ITEM_BREAK,
                SoundCategory.PLAYERS,
                5.0f,
                1.0f
            )
        }

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
//        val ironStyle = IronWandStyle(user.uuid)
//        ParticleStyleManager.spawnStyle(world, user.eyePos, ironStyle)
        // 生成小阵法
        return res
    }

    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 8
    }

}