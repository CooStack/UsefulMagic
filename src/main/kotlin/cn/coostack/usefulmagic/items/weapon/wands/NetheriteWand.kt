package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.barrages.wand.NetheriteSwordBarrage
import cn.coostack.usefulmagic.particles.style.barrage.wand.NetheriteWandStyle
import cn.coostack.usefulmagic.utils.ParticleOption
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
import net.minecraft.world.World

class NetheriteWand(settings: Settings) : WandItem(settings, 100, 5.0) {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(
            Text.translatable(
                "item.netherite_wand.description"
            )
        )
        super.appendTooltip(stack, context, tooltip, type)
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.BOW
    }


    override fun canRepair(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isOf(Items.NETHERITE_INGOT)
    }


    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack? {
        if (world.isClient) {
            return stack
        }
        if (user !is ServerPlayerEntity) {
            return stack
        }
        world as ServerWorld

        user.itemCooldownManager.set(this, 20)
        val rounds = Math3DUtil.getRoundScapeLocations(8.0, 0.5, 10, 100)
        Math3DUtil.rotatePointsToPoint(rounds, RelativeLocation.of(user.rotationVector), RelativeLocation.yAxis())
        val direction = user.rotationVector
        val pos = user.eyePos
        CooParticleAPI.scheduler.runTaskTimerMaxTick(1, 60) {
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
                SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                SoundCategory.PLAYERS,
                3f,
                1.5f
            )
        }

        // 扣除魔法
        cost(user)
        return super.finishUsing(stack, world, user)
    }


    val rangeBall = PointsBuilder().addBall(12.0, 6 * options).create()
    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val tick = getMaxUseTime(stack, user) - remainingUseTicks
        val max = getMaxUseTime(stack, user)
        if (world.isClient) {
            return
        }
        if (tick % 3 == 0) {
            repeat(if (tick < max / 2) 10 else 20) {
                val it = rangeBall.random()
                val pos = user.pos.add(it.toVector())
                val dir = it.normalize().multiply(-0.6)
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.CLOUD, world as ServerWorld, pos, dir.toVector()
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
                SoundCategory.PLAYERS,
                3f,
                2f
            )
//            world.playSound(
//                null,
//                user.x, user.y, user.z,
//                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 3f, 1.3f
//            )
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
        val style = NetheriteWandStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.pos, style)
        // 生成小阵法
        return res
    }

    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 100
    }
}