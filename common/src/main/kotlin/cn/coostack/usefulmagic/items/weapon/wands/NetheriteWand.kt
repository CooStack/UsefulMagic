package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.extend.isOf
import cn.coostack.usefulmagic.particles.barrages.wand.NetheriteSwordBarrage
import cn.coostack.usefulmagic.particles.style.barrage.wand.NetheriteWandStyle
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
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

class NetheriteWand(settings: Properties) : WandItem(settings, 100, 5.0) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.netherite_wand.description"
            )
        )
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }


    override fun isValidRepairItem(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isOf(Items.NETHERITE_INGOT)
    }


    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        if (world.isClientSide) {
            return stack
        }
        if (user !is ServerPlayer) {
            return stack
        }
        world as ServerLevel

        user.cooldowns.addCooldown(this, 20)
        val rounds = Math3DUtil.getRoundScapeLocations(8.0, 0.5, 10, 100)
        Math3DUtil.rotatePointsToPoint(rounds, RelativeLocation.of(user.forward), RelativeLocation.yAxis())
        val direction = user.forward
        val pos = user.eyePosition
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(1, 60) {
            val barrage = NetheriteSwordBarrage(pos.add(rounds.random().toVector()), world, damage, user, 1.5)
            barrage.direction = direction
            barrage.shooter = user
            BarrageManager.spawn(barrage)
            val loc = barrage.loc
            world.playSound(
                null,
                loc.x,
                loc.y,
                loc.z,
                SoundEvents.PLAYER_ATTACK_CRIT,
                SoundSource.PLAYERS,
                3f,
                1.5f
            )
        }

        // 扣除魔法
        cost(user)
        return super.finishUsingItem(stack, world, user)
    }


    val rangeBall = PointsBuilder().addBall(12.0, 6 * options).create()
    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val tick = getUseDuration(stack, user) - remainingUseTicks
        val max = getUseDuration(stack, user)
        if (world.isClientSide) {
            return
        }
        if (tick % 3 == 0) {
            repeat(if (tick < max / 2) 10 else 20) {
                val it = rangeBall.random()
                val pos = user.position().add(it.toVector())
                val dir = it.normalize().multiply(-0.6)
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.CLOUD, world as ServerLevel, pos, dir.toVector()
                )

            }
        }

        if (tick % 5 == 0) {
            world.playSound(
                null,
                user.x,
                user.y,
                user.z,
                SoundEvents.WEATHER_RAIN,
                SoundSource.PLAYERS,
                3f,
                2f
            )
//            world.playSound(
//                null,
//                user.x, user.y, user.z,
//                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 3f, 1.3f
//            )
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
        val style = NetheriteWandStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.position(), style)
        // 生成小阵法
        return res
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 100
    }
}