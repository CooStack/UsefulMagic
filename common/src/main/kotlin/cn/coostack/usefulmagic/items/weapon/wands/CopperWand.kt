package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.extend.isOf
import cn.coostack.usefulmagic.particles.barrages.wand.CopperBarrage
import net.minecraft.core.particles.ParticleTypes
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

class CopperWand(settings: Properties) : WandItem(settings, 50, 20.0) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.copper_wand.description"
            )
        )
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }


    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }


    override fun isValidRepairItem(stack: ItemStack, repairCandidate: ItemStack): Boolean {
        return repairCandidate.isOf(Items.COPPER_INGOT)
    }

    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        if (world.isClientSide) {
            return
        }
        val usingTime = getUseDuration(stack, user) - remainingUseTicks
        val newPos = user.eyePosition.add(user.forward.scale(5.0))
        if (usingTime % 2 == 0) {
            ServerParticleUtil.spawnSingle(
                ParticleTypes.CLOUD,
                world as ServerLevel, newPos,
                Vec3(1.0, 1.0, 1.0),
                true,
                0.04,
                3,
                60.0
            )
        }
        world as ServerLevel
        if (usingTime % 10 == 0) {
            val times = usingTime / 10
            if (times > 0 && times != 6) {
                world.playSound(
                    null,
                    user.position().x, user.position().y, user.position().z,
                    SoundEvents.TRIDENT_HIT_GROUND,
                    SoundSource.PLAYERS,
                    10.0f,
                    times.toFloat() * 0.6f
                )
                PointsBuilder().addCircle(
                    3.0, 300
                )
                    .rotateTo(user.forward)
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

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        if (world.isClientSide) {
            return stack
        }
        if (user !is ServerPlayer) {
            return stack
        }
        stack.hurtAndBreak(1, world as ServerLevel, user as ServerPlayer) {
            world.playSound(
                null,
                ofFloored(user.position()),
                SoundEvents.ITEM_BREAK,
                SoundSource.PLAYERS,
                5.0f,
                1.0f
            )
        }
        // 声音
        world.playSound(
            null,
            user.position().x, user.position().y, user.position().z,
            SoundEvents.GENERIC_EXPLODE,
            SoundSource.PLAYERS,
            10.0f,
            3.0f
        )
        // 召唤弹幕
        val barrage = CopperBarrage(damage, user, user.eyePosition, world as ServerLevel)
        barrage.direction = user.forward
        BarrageManager.spawn(barrage)
        // 扣除魔法
        cost(user)
        user.cooldowns.addCooldown(this, 60)
        return super.finishUsingItem(stack, world, user)
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 60
    }

}