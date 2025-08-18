package cn.coostack.usefulmagic.items.prop

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.particles.emitters.FlyingRuneCloudEmitters
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Interaction
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class FlyingRuneItem : Item(Properties().stacksTo(1)) {
    companion object {
        const val MANA_COST = 20
        private val emittersMap = ConcurrentHashMap<UUID, ParticleEmitters>()

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
            emitters.cancelled = true
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

        init {
            CooParticlesAPI.scheduler.runTaskTimer(20) {
                val list = CooParticlesAPI.server.playerList.players
                list.filter {
                    !it.isCreative && !it.isSpectator
                }.forEach {
                    val flying = it.abilities.flying
                    // 魔力值是否足够
                    val data = UsefulMagic.state.getDataFromServer(it.uuid)
                    val canCost = data.mana >= MANA_COST
                    val canFly =
                        it.inventory.contains(enabledFlyingRuneItem)
                    it.abilities.mayfly = canFly
                    if (!canCost || !canFly) {
                        it.abilities.flying = false
                        it.onUpdateAbilities()
                        handleDisableFlying(it)
                        return@forEach
                    }
                    if (flying) {
                        // 扣除魔力值
                        data.mana -= MANA_COST
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


    override fun inventoryTick(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) {
        if (entity !is ServerPlayer) {
            return
        }
        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED.get()) ?: false
        entity.abilities.mayfly = if (!entity.isCreative && !entity.isSpectator) enabled else true
        entity.onUpdateAbilities()
        val data = UsefulMagic.state.getDataFromServer(entity.uuid)
        val canCost = data.mana >= MANA_COST
        if (entity.abilities.flying && enabled) {
            handleFlying(world as ServerLevel, entity)
        } else {
            handleDisableFlying(entity)
        }
        if (!canCost && enabled) {
            stack.set(UsefulMagicDataComponentTypes.ENABLED.get(), false)
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = user.getItemInHand(hand)
        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED.get()) ?: false
        stack.set(UsefulMagicDataComponentTypes.ENABLED.get(), !enabled)
        return super.use(world, user, hand)
    }

}