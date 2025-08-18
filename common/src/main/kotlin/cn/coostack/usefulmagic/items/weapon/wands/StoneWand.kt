package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.extend.isIn
import cn.coostack.usefulmagic.formation.CrystalFormation
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import kotlin.math.pow
import kotlin.math.roundToInt


/**
 * 发射一个射线
 * 能够击中3个实体
 */
class StoneWand(settings: Properties) : WandItem(settings, 10, 5.0) {
    val attenuation = 0.9
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.stone_wand.description"
            )
        )
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }


     override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }


    override fun isValidRepairItem(stack: ItemStack, ingredient: ItemStack): Boolean {
        return ingredient.isIn(ItemTags.PLANKS)
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
        world.playSound(
            null, user.x, user.y, user.z, SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 6f, 1.3f
        )
        // 释放直线魔法
        // 如果直线(AABB.ofSize(pos,1.0,1.0,1.0) 内存在实体 视为击中 攻击距离为30
        // 然后寻找附近 AABB.ofSize(pos,10.0,10.0,10.0)的实体 2次
        val direction = user.forward.normalize()
        var currentPos = user.eyePosition
        for (i in 1..30) {
            currentPos = currentPos.add(direction)
            val entities = world.getEntitiesOfClass(
                LivingEntity::class.java,
                AABB.ofSize(currentPos, 1.0, 1.0, 1.0)
            ) {
                it.uuid != user.uuid
            }
            if (entities.isNotEmpty()) {
                val damagedEntitySet = HashSet<LivingEntity>()
                val entity = entities.first()
                damagedEntitySet.add(entity)
                val source = entity.damageSources().playerAttack(user)
                entity.hurt(source, damage.toFloat())
                var prePos = currentPos
                var nextPos = prePos
                for (j in 1..2) {
                    val next = world.getEntitiesOfClass(LivingEntity::class.java, AABB.ofSize(prePos, 16.0, 10.0, 16.0)) {
                        val cant = ServerFormationManager.getFormationFromPos(currentPos, world)?.let { it ->
                            it is CrystalFormation && it.hasDefend
                        } ?: false
                        it.uuid != user.uuid && it !in damagedEntitySet && !cant
                    }.firstOrNull() ?: continue
                    prePos = nextPos
                    nextPos = next.eyePosition
                    next.hurt(source, (damage * attenuation.pow(j)).toFloat())
                    damagedEntitySet.add(next)
                    PointsBuilder().addLine(
                        prePos, nextPos, prePos.distanceTo(nextPos).roundToInt() * 10
                    ).create().forEach {
                        ServerParticleUtil
                            .spawnSingle(
                                ParticleTypes.ENCHANT, world, it.toVector(), Vec3.ZERO, 64.0
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
            user.eyePosition, currentPos, currentPos.distanceTo(user.eyePosition).roundToInt() * 10
        ).create().forEach {
            ServerParticleUtil
                .spawnSingle(
                    ParticleTypes.ENCHANT, world, it.toVector(), Vec3.ZERO, 64.0
                )
        }
        // 扣除魔法
        cost(user)
    }


    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 20 * 3600
    }

}