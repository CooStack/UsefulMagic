package cn.coostack.usefulmagic.items.prop

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.listener.DefendMagicListener
import cn.coostack.usefulmagic.particles.barrages.api.EntityDamagedBarrage
import cn.coostack.usefulmagic.particles.barrages.api.PlayerDamagedBarrage
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Rarity
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.Box
import net.minecraft.world.World

class DefendCoreItem : Item(
    Settings()
        .maxCount(1)
        .rarity(Rarity.EPIC)
) {

    companion object {
        /**
         * 判断玩家背包内是否存在启动的core
         */
        fun checkEnabled(player: PlayerEntity): Boolean {
            return player.inventory.contains(UsefulMagicItems.DEFEND_CORE.defaultStack.also {
                it.set(UsefulMagicDataComponentTypes.ENABLED, true)
            })
        }
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        // 状态添加
        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED) ?: let {
            stack.set(UsefulMagicDataComponentTypes.ENABLED, false)
            false
        }
        tooltip.add(
            Text.of {
                Text.translatable(
                    "item.defend_core_enabled"
                ).string.replace(
                    "%enabled%",
                    if (enabled) Text.translatable("item.usefulmagic_enabled").string else {
                        Text.translatable("item.usefulmagic_disabled").string
                    }
                )
            }
        )

        super.appendTooltip(stack, context, tooltip, type)
    }


    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (entity !is PlayerEntity) return
        if (world.isClient) return
        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED) ?: false
        if (!enabled) {
            return
        }
        val barrages = BarrageManager.collectClipBarrages(world as ServerWorld, Box.of(entity.eyePos, 4.0, 4.0, 4.0))
        barrages.filter { it.shooter?.uuid != entity.uuid && it is EntityDamagedBarrage }.onEach {
            it as EntityDamagedBarrage
            it.hit(
                BarrageHitResult()
            )
            // tryDefend
            val source =
                if (it is PlayerDamagedBarrage) world.damageSources.playerAttack(it.shooter as PlayerEntity) else {
                    world.damageSources.mobAttack(it.shooter)
                }
            DefendMagicListener.tryDefend(
                entity as ServerPlayerEntity,
                it.shooter,
                it.damage.toFloat(),
                it.loc,
                source,
                true
            )
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED) ?: false
        stack.set(UsefulMagicDataComponentTypes.ENABLED, !enabled)
        return super.use(world, user, hand)
    }
}