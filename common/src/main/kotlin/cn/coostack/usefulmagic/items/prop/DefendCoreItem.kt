package cn.coostack.usefulmagic.items.prop

import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.listener.DefendMagicListener
import cn.coostack.usefulmagic.barrages.api.EntityMagicDamagedBarrage
import cn.coostack.usefulmagic.barrages.api.PlayerMagicDamagedBarrage
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

class DefendCoreItem : Item(
    Properties()
        .stacksTo(1)
        .rarity(Rarity.EPIC)
) {

    companion object {
        /**
         * 判断玩家背包内是否存在启动的core
         */
        fun checkEnabled(player: Player): Boolean {
            if (UsefulMagicEffects.isMagicSealed(player)) {
                return false
            }
            return player.inventory.contains(UsefulMagicItems.DEFEND_CORE.getItem().defaultInstance.also {
                it.set(UsefulMagicDataComponentTypes.ENABLED.get(), true)
            })
        }
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {

        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED.get()) ?: let {
            stack.set(UsefulMagicDataComponentTypes.ENABLED.get(), false)
            false
        }
        tooltip.add(
            Component.literal(
                Component.translatable(
                    "item.defend_core_enabled"
                ).string.replace(
                    "%enabled%",
                    if (enabled) Component.translatable("item.usefulmagic_enabled").string else {
                        Component.translatable("item.usefulmagic_disabled").string
                    }
                )
            )
        )

        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }


    override fun inventoryTick(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) {
        if (entity !is Player) return
        if (world.isClientSide) return
        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED.get()) ?: false
        if (!enabled || UsefulMagicEffects.isMagicSealed(entity)) {
            return
        }
        val barrages =
            BarrageManager.collectClipBarrages(world as ServerLevel, AABB.ofSize(entity.eyePosition, 4.0, 4.0, 4.0))
        barrages.filter { it.shooter?.uuid != entity.uuid && it is EntityMagicDamagedBarrage }.onEach {
            it as EntityMagicDamagedBarrage
            it.hit(
                BarrageHitResult()
            )
            // tryDefend
            val source =
                if (it is PlayerMagicDamagedBarrage) world.damageSources().playerAttack(it.shooter as Player) else {
                    world.damageSources().mobAttack(it.shooter)
                }
            DefendMagicListener.tryDefend(
                entity as ServerPlayer,
                it.shooter,
                it.damage.toFloat(),
                it.loc,
                source,
                true
            )
        }
    }


    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = user.getItemInHand(hand)
        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED.get()) ?: false
        if (UsefulMagicEffects.isMagicSealed(user) && !enabled) {
            return InteractionResultHolder.fail(stack)
        }
        stack.set(UsefulMagicDataComponentTypes.ENABLED.get(), !enabled)
        return super.use(world, user, hand)
    }
}
