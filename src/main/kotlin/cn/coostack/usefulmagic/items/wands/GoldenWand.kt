package cn.coostack.usefulmagic.items.wands

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.barrages.GoldenMagicBallBarrage
import cn.coostack.usefulmagic.particles.style.GoldenWandStyle
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
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
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.UUID

class GoldenWand(settings: Settings) : WandItem(settings, 80, 4.0) {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(
            Text.translatable(
                "item.golden_wand.description"
            )
        )
        super.appendTooltip(stack, context, tooltip, type)
    }


    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.BOW
    }


    override fun canRepair(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isOf(Items.GOLD_INGOT)
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

        val barrage = GoldenMagicBallBarrage(
            user.eyePos,
            world,
            damage
        )
        barrage.shooter = user
        barrage.direction = user.rotationVector
        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.PLAYERS,
            3f,
            1.2f
        )
        user.itemCooldownManager.set(this, 30)
        BarrageManager.spawn(barrage)
        // 扣除魔法
        cost(user)
        return super.finishUsing(stack, world, user)
    }


    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val tick = getMaxUseTime(stack, user) - remainingUseTicks
        if (world.isClient) {
            return
        }
        val newPos = user.eyePos.add(user.rotationVector.multiply(5.0))
        if (tick % 10 == 0){
            world.playSound(
                null,
                user.x,
                user.y,
                user.z,
                SoundEvents.ITEM_FIRECHARGE_USE,
                SoundCategory.PLAYERS,
                3f,
                0.8f
            )
        }
        if (tick % 2 == 0) {
            ServerParticleUtil.spawnSingle(
                ParticleTypes.ENCHANT,
                world as ServerWorld, newPos,
                Vec3d(2.0, 2.0, 2.0),
                true,
                0.04,
                10,
                60.0
            )

            val points = PointsBuilder().addCircle(6.0, 32)
                .rotateTo(user.rotationVector)
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

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack?> {
        val res = super.use(world, user, hand)
        if (res.result == ActionResult.FAIL) {
            return res
        }
        if (world.isClient) {
            return res
        }
        val style = GoldenWandStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.eyePos, style)
        // 生成小阵法
        return res
    }

    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 60
    }

}