package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.formation.CrystalFormation
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.tag.ItemTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.UseAction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.pow
import kotlin.math.roundToInt


/**
 * 发射一个射线
 * 能够击中3个实体
 */
class StoneWand(settings: Settings) : WandItem(settings, 10, 5.0) {
    val attenuation = 0.9
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(
            Text.translatable(
                "item.stone_wand.description"
            )
        )
        super.appendTooltip(stack, context, tooltip, type)
    }


    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.BOW
    }


    override fun canRepair(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isIn(ItemTags.PLANKS)
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
        world.playSound(
            null, user.x, user.y, user.z, SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 6f, 1.3f
        )
        // 释放直线魔法
        // 如果直线(Box.of(pos,1.0,1.0,1.0) 内存在实体 视为击中 攻击距离为30
        // 然后寻找附近 Box.of(pos,10.0,10.0,10.0)的实体 2次
        val direction = user.rotationVector.normalize()
        var currentPos = user.eyePos
        for (i in 1..30) {
            currentPos = currentPos.add(direction)
            val entities = world.getEntitiesByClass(
                LivingEntity::class.java,
                Box.of(currentPos, 1.0, 1.0, 1.0)
            ) {
                it.uuid != user.uuid
            }
            if (entities.isNotEmpty()) {
                val damagedEntitySet = HashSet<LivingEntity>()
                val entity = entities.first()
                damagedEntitySet.add(entity)
                val source = entity.damageSources.playerAttack(user)
                entity.damage(source, damage.toFloat())
                var prePos = currentPos
                var nextPos = prePos
                for (j in 1..2) {
                    val next = world.getEntitiesByClass(LivingEntity::class.java, Box.of(prePos, 16.0, 10.0, 16.0)) {
                        val cant = ServerFormationManager.getFormationFromPos(currentPos, world)?.let { it ->
                            it is CrystalFormation && it.hasDefend
                        } ?: false
                        it.uuid != user.uuid && it !in damagedEntitySet && !cant
                    }.firstOrNull() ?: continue
                    prePos = nextPos
                    nextPos = next.eyePos
                    next.damage(source, (damage * attenuation.pow(j)).toFloat())
                    damagedEntitySet.add(next)
                    PointsBuilder().addLine(
                        prePos, nextPos, prePos.distanceTo(nextPos).roundToInt() * 10
                    ).create().forEach {
                        ServerParticleUtil
                            .spawnSingle(
                                ParticleTypes.ENCHANT, world, it.toVector(), Vec3d.ZERO, 64.0
                            )
                    }
                }
                break
            }
            val cant = ServerFormationManager.getFormationFromPos(currentPos, world)?.let {
                val option = LivingEntityTargetOption(user, false)
                if (it is CrystalFormation && it.hasDefend && !it.isFriendly(option)) {
                    it.attack(5f, option, currentPos)
                    true
                } else false
            } ?: false
            if (cant) break
        }
        // 生成直线
        PointsBuilder().addLine(
            user.eyePos, currentPos, currentPos.distanceTo(user.eyePos).roundToInt() * 10
        ).create().forEach {
            ServerParticleUtil
                .spawnSingle(
                    ParticleTypes.ENCHANT, world, it.toVector(), Vec3d.ZERO, 64.0
                )
        }
        // 扣除魔法
        cost(user)
    }


    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 20 * 3600
    }

}