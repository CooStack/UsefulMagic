package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.barrages.CopperBarrage
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.UseAction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class CopperWand(settings: Settings) : WandItem(settings, 50, 20.0) {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(
            Text.translatable(
                "item.copper_wand.description"
            )
        )
        super.appendTooltip(stack, context, tooltip, type)
    }


    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.BOW
    }


    override fun canRepair(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isOf(Items.COPPER_INGOT)
    }

    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        if (world.isClient) {
            return
        }
        val usingTime = getMaxUseTime(stack, user) - remainingUseTicks
        val newPos = user.eyePos.add(user.rotationVector.multiply(5.0))
        if (usingTime % 2 == 0) {
            ServerParticleUtil.spawnSingle(
                ParticleTypes.CLOUD,
                world as ServerWorld, newPos,
                Vec3d(1.0, 1.0, 1.0),
                true,
                0.04,
                3,
                60.0
            )
        }
        world as ServerWorld
        if (usingTime % 10 == 0) {
            val times = usingTime / 10
            if (times > 0 && times != 6) {
                world.playSound(
                    null,
                    user.pos.x, user.pos.y, user.pos.z,
                    SoundEvents.ITEM_TRIDENT_HIT_GROUND,
                    SoundCategory.PLAYERS,
                    10.0f,
                    times.toFloat() * 0.6f
                )
                PointsBuilder().addCircle(
                    3.0, 300
                )
                    .rotateTo(user.rotationVector)
                    .create()
                    .forEach {
                        ServerParticleUtil.spawnSingle(
                            ParticleTypes.SMOKE,
                            world,
                            it.toVector().add(newPos),
                            it.normalize().multiply(-0.2).toVector(),
                            64.0
                        )
                    }
            }
        }

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
        // 声音
        world.playSound(
            null,
            user.pos.x, user.pos.y, user.pos.z,
            SoundEvents.ENTITY_GENERIC_EXPLODE,
            SoundCategory.PLAYERS,
            10.0f,
            3.0f
        )
        // 召唤弹幕
        val barrage = CopperBarrage(damage, user, user.eyePos, world as ServerWorld)
        barrage.direction = user.rotationVector
        BarrageManager.spawn(barrage)
        // 扣除魔法
        cost(user)
        user.itemCooldownManager.set(this, 60)
        return super.finishUsing(stack, world, user)
    }

    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 60
    }

}