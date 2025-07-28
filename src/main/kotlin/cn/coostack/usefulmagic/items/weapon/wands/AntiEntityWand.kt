package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.particles.barrages.wand.AntiEntityWandBarrage
import cn.coostack.usefulmagic.particles.style.barrage.wand.AntiEntityWandSpellcasterStyle
import cn.coostack.usefulmagic.particles.style.barrage.wand.AntiEntityWandStyle
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
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
import java.util.Random

class AntiEntityWand(settings: Settings) : WandItem(settings, 1000, 5.0) {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(
            Text.translatable(
                "item.anti_entity_wand.description"
            )
        )
        super.appendTooltip(stack, context, tooltip, type)
    }

    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.BOW
    }

    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 200
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()

    override fun canRepair(stack: ItemStack, ingredient: ItemStack): Boolean {
        return false
    }

    val rangeBall = PointsBuilder().addBall(64.0, 6 * options).create()
    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val max = getMaxUseTime(stack, user)
        val tick = max - remainingUseTicks
        if (world.isClient) {
            return
        }
        if (tick % 5 == 0) {
            world.playSound(
                null,
                user.pos.x, user.pos.y, user.pos.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
                10.0f,
                2.0f
            )
        }
        if (tick % 3 == 0) {
            repeat(if (tick < max / 2) 20 else 40) {
                val it = rangeBall.random()
                val pos = user.pos.add(0.0, 1.5, 0.0).add(it.toVector())
                val dir = it.normalize().multiply(-5.75)
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.FIREWORK, world as ServerWorld, pos, dir.toVector()
                )
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
        world as ServerWorld
        // 声音
        world.playSound(
            null,
            user.pos.x, user.pos.y, user.pos.z,
            SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
            SoundCategory.PLAYERS,
            10.0f,
            2.0f
        )
        world.playSound(
            null,
            BlockPos.ofFloored(user.pos),
            SoundEvents.ENTITY_ITEM_BREAK,
            SoundCategory.PLAYERS,
            5.0f,
            2.0f
        )
        val random = Random(System.currentTimeMillis())
        val dir = Vec3d(
            random.nextDouble(-16.0, 16.0),
            40.0,
            random.nextDouble(-16.0, 16.0)
        )
        val randomSpawn = user.pos.add(dir)
        val style = AntiEntityWandStyle(RelativeLocation.of(dir).normalize(), user.uuid)
        val maxAge = style.maxAge
        ParticleStyleManager.spawnStyle(world, randomSpawn, style)
        // 召唤弹幕
        val shape = PointsBuilder()
            .addRoundShape(20.0, 0.5, 18, 180)
        var currentAxis = style.axis
        var tick = 0
        CooParticleAPI.scheduler.runTaskTimerMaxTick(3, maxAge) {
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
        user.itemCooldownManager.set(this, 60 * 20)
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
        val style = AntiEntityWandSpellcasterStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.pos, style)
        // 生成小阵法
        return res
    }
}