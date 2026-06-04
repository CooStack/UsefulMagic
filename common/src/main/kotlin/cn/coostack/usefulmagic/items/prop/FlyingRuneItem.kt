package cn.coostack.usefulmagic.items.prop

import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.extend.mana
import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.particles.emitters.FlyingRuneCloudEmitters
import cn.coostack.usefulmagic.utils.UsefulMagicFlightController
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class FlyingRuneItem : Item(Properties().stacksTo(1)) {
    companion object {
        const val MANA_COST = 20
        private val emittersMap = ConcurrentHashMap<UUID, ParticleEmitters>()
        private val lastDrainTickMap = ConcurrentHashMap<UUID, Long>()

        fun loadEmitters(uuid: UUID, emitters: ParticleEmitters): ParticleEmitters {
            emittersMap[uuid] = emitters
            return emitters
        }

        fun resetEmitters(uuid: UUID) {
            emittersMap.remove(uuid)
        }

        fun getEmittersFromUUID(uuid: UUID): ParticleEmitters? {
            return emittersMap[uuid]
        }

        private fun handleDisableFlying(entity: ServerPlayer) {
            val emitters = getEmittersFromUUID(entity.uuid) ?: let { return }
            emitters.canceled = true
            resetEmitters(entity.uuid)
        }

        private fun handleFlying(world: ServerLevel, entity: ServerPlayer) {
            getEmittersFromUUID(entity.uuid)?.let { return }
            // 创建一个云的粒子
            val cloud = FlyingRuneCloudEmitters(
                entity.uuid, entity.position(), world
            )
            ParticleEmittersManager
                .spawnEmitters(
                    loadEmitters(entity.uuid, cloud)
                )
        }

        val enabledFlyingRuneItem: ItemStack by lazy {
            UsefulMagicItems.FLYING_RUNE.getItem().defaultInstance
                .also {
                    it.set(UsefulMagicDataComponentTypes.ENABLED.get(), true)
                }
        }

        private fun syncFlightAccess(player: ServerPlayer) {
            val canGrant = player.inventory.contains(enabledFlyingRuneItem)
                && !UsefulMagicEffects.isMagicSealed(player)
                && player.mana >= MANA_COST
            UsefulMagicFlightController.setFlyingRuneGranted(player, canGrant)
            if (!canGrant) {
                handleDisableFlying(player)
                lastDrainTickMap.remove(player.uuid)
            }
        }

        fun tickServer() {
            val server = UsefulMagic.server
            val gameTime = server.overworld().gameTime
            server.playerList.players.forEach { player ->
                if (player.isCreative || player.isSpectator) {
                    UsefulMagicFlightController.setFlyingRuneGranted(player, false)
                    handleDisableFlying(player)
                    lastDrainTickMap.remove(player.uuid)
                    return@forEach
                }
                val hasEnabledRune = player.inventory.contains(enabledFlyingRuneItem)
                val blocked = UsefulMagicEffects.isMagicSealed(player)
                val canGrant = hasEnabledRune && !blocked && player.mana >= MANA_COST
                UsefulMagicFlightController.setFlyingRuneGranted(player, canGrant)
                if (!canGrant) {
                    handleDisableFlying(player)
                    lastDrainTickMap.remove(player.uuid)
                    return@forEach
                }
                if (!player.abilities.flying) {
                    handleDisableFlying(player)
                    lastDrainTickMap.remove(player.uuid)
                    return@forEach
                }
                handleFlying(player.level() as ServerLevel, player)
                val lastDrainTick = lastDrainTickMap[player.uuid]
                if (lastDrainTick == null || gameTime - lastDrainTick >= 20L) {
                    player.mana -= MANA_COST
                    lastDrainTickMap[player.uuid] = gameTime
                    if (player.mana < MANA_COST) {
                        syncFlightAccess(player)
                    }
                }
            }
        }
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED.get()) ?: let {
            stack.set(UsefulMagicDataComponentTypes.ENABLED.get(), false)
            false
        }
        tooltip.add(
            Component.literal(
                Component.translatable(
                    "item.flying_rune_enabled"
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

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = user.getItemInHand(hand)
        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED.get()) ?: false
        if (UsefulMagicEffects.isMagicSealed(user) && !enabled) {
            return InteractionResultHolder.fail(stack)
        }
        stack.set(UsefulMagicDataComponentTypes.ENABLED.get(), !enabled)
        if (user is ServerPlayer) {
            syncFlightAccess(user)
        }
        return super.use(world, user, hand)
    }

}
