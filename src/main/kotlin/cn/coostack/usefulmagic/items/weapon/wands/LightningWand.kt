package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.formation.CrystalFormation
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.tag.ItemTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.text.Text
import net.minecraft.util.UseAction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.World
import kotlin.math.pow
import kotlin.random.Random

class LightningWand(settings: Settings) : WandItem(settings, 20, 8.0) {
    val attenuation = 0.9
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(
            Text.translatable(
                "item.lightning_wand.description"
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
        if (tick % 5 != 0 || tick < 5) return
        world as ServerWorld
        world.playSound(
            null, user.x, user.y, user.z,
            UsefulMagicSoundEvents.ELECTRIC_EFFECT,
            SoundCategory.PLAYERS,
            6f, 1f
        )
        // 释放直线魔法
        // 如果直线(Box.of(pos,1.0,1.0,1.0) 内存在实体 视为击中 攻击距离为50
        // 然后寻找附近 Box.of(pos,10.0,10.0,10.0)的实体 2次
        val direction = user.rotationVector.normalize()
        var currentPos = user.eyePos
        val random = Random(System.currentTimeMillis())
        for (i in 1..50) {
            currentPos = currentPos.add(direction)
            val entities = world.getEntitiesByClass(
                LivingEntity::class.java,
                Box.of(currentPos, 4.0, 4.0, 4.0)
            ) {
                it.uuid != user.uuid
            }
            if (entities.isNotEmpty()) {
                val damagedEntitySet = HashSet<LivingEntity>()
                val entity = entities.first()
                damagedEntitySet.add(entity)
                val source = entity.damageSources.playerAttack(user)
                entity.setOnFireForTicks(60)
                entity.damage(source, damage.toFloat())
                entity.timeUntilRegen = 0
                entity.hurtTime = 0
                var prePos = currentPos
                var nextPos = prePos
                val dir = prePos.relativize(nextPos).normalize().multiply(4.5)
                for (j in 1..5) {
                    val next = world.getEntitiesByClass(LivingEntity::class.java, Box.of(prePos, 48.0, 48.0, 48.0)) {
                        val cant = ServerFormationManager.getFormationFromPos(currentPos, world)?.let { it ->
                            it is CrystalFormation && it.hasDefend
                        } ?: false
                        it.uuid != user.uuid && it !in damagedEntitySet && !cant
                    }.minByOrNull {
                        prePos.distanceTo(it.pos)
                    } ?: continue
                    prePos = nextPos
                    nextPos = next.eyePos
                    next.setOnFireForTicks(60)
                    next.damage(source, (damage * attenuation.pow(j)).toFloat())
                    next.timeUntilRegen = 0
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
            val blockPos = BlockPos.ofFloored(currentPos)
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
        val dir = user.eyePos.relativize(currentPos).normalize().multiply(4.5)
        val lightning = LightningParticleEmitters(
            user.eyePos.add(
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
            ), world
        )
            .apply {
                endPos = RelativeLocation.of(user.eyePos.relativize(currentPos.add(dir)))
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


    override fun getMaxUseTime(stack: ItemStack?, user: LivingEntity?): Int {
        return 20 * 3600
    }

}