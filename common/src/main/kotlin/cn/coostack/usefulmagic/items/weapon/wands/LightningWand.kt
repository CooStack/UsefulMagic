package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.extend.isIn
import cn.coostack.usefulmagic.formation.CrystalFormation
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.network.chat.Component
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import kotlin.math.pow
import kotlin.random.Random

class LightningWand(settings: Properties) : WandItem(settings, 20, 8.0) {
    val attenuation = 0.9
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable(
                "item.lightning_wand.description"
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
        if (tick % 5 != 0 || tick < 5) return
        world as ServerLevel
        world.playSound(
            null, user.x, user.y, user.z,
            UsefulMagicSoundEvents.ELECTRIC_EFFECT.get(),
            SoundSource.PLAYERS,
            6f, 1f
        )
        // 释放直线魔法
        // 如果直线(AABB.ofSize(pos,1.0,1.0,1.0) 内存在实体 视为击中 攻击距离为50
        // 然后寻找附近 AABB.ofSize(pos,10.0,10.0,10.0)的实体 2次
        val direction = user.forward.normalize()
        var currentPos = user.eyePosition
        val random = Random(System.currentTimeMillis())
        val data = UsefulMagic.state.getDataFromServer(user.uuid)
        for (i in 1..50) {
            currentPos = currentPos.add(direction)
            val entities = world.getEntitiesOfClass(
                LivingEntity::class.java,
                AABB.ofSize(currentPos, 4.0, 4.0, 4.0)
            ) {
                it.uuid != user.uuid && !data.isFriend(it.uuid)
            }
            if (entities.isNotEmpty()) {
                val damagedEntitySet = HashSet<LivingEntity>()
                val entity = entities.first()
                damagedEntitySet.add(entity)
                val source = entity.damageSources().playerAttack(user)
                entity.remainingFireTicks = 60
                entity.hurt(source, damage.toFloat())
                entity.hurtTime = 0
                var prePos = currentPos
                var nextPos = prePos
                val dir = prePos.relativize(nextPos).normalize().scale(4.5)
                for (j in 1..5) {
                    val next =
                        world.getEntitiesOfClass(LivingEntity::class.java, AABB.ofSize(prePos, 48.0, 48.0, 48.0)) {
                            val cant = ServerFormationManager.getFormationFromPos(currentPos, world)?.let { it ->
                                it is CrystalFormation && it.hasDefend
                            } ?: false
                            it.uuid != user.uuid && it !in damagedEntitySet && !cant && !data.isFriend(it.uuid)
                        }.minByOrNull {
                            prePos.distanceTo(it.position())
                        } ?: continue
                    prePos = nextPos
                    nextPos = next.eyePosition
                    next.remainingFireTicks = 60
                    next.hurt(source, (damage * attenuation.pow(j)).toFloat())
                    next.hurtTime = 0
                    damagedEntitySet.add(next)
                    // 生成闪电
                    val lightning = LightningParticleEmitters(prePos, world)
                        .apply {
                            endPos = RelativeLocation.of(prePos.relativize(nextPos.add(dir)))
                            maxTick = random.nextInt(1, 3)
                            templateData.also {
                                it.color = Math3DUtil.colorOf(
                                    121, 211, 249
                                )
                                it.maxAge = 5
                            }
                        }
                    ParticleEmittersManager.spawnEmitters(lightning)
                }
                break
            }
            val blockPos = ofFloored(currentPos)
            val state = world.getBlockState(blockPos)
            if (!state.isAir && !state.getCollisionShape(world, blockPos).isEmpty) {
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
        val dir = user.eyePosition.relativize(currentPos).normalize().scale(4.5)
        val lightning = LightningParticleEmitters(
            user.eyePosition.add(
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
            ), world
        )
            .apply {
                endPos = RelativeLocation.of(user.eyePosition.relativize(currentPos.add(dir)))
                maxTick = random.nextInt(1, 6)
                templateData.also {
                    it.color = Math3DUtil.colorOf(
                        121, 211, 249
                    )
                    it.maxAge = 5
                }
            }
        ParticleEmittersManager.spawnEmitters(lightning)
        // 扣除魔法
        cost(user)
    }


    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 20 * 3600
    }

}