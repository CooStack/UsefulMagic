package cn.coostack.usefulmagic.items.prop

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.particles.emitters.FlyingRuneCloudEmitters
import net.minecraft.command.argument.EntityArgumentType.entity
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class FlyingRuneItem : Item(Settings().maxCount(1)) {
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

        private fun handleDisableFlying(entity: ServerPlayerEntity) {
            val emitters = getEmittersFromUUID(entity.uuid) ?: let { return }
            emitters.cancelled = true
            resetEmitters(entity.uuid)
        }

        private fun handleFlying(world: ServerWorld, entity: ServerPlayerEntity) {
            getEmittersFromUUID(entity.uuid)?.let { return }
            // 创建一个云的粒子
            val cloud = FlyingRuneCloudEmitters(
                entity.uuid, entity.pos, world
            )
            ParticleEmittersManager
                .spawnEmitters(
                    loadEmitters(entity.uuid, cloud)
                )
        }

        val enabledFlyingRuneItem: ItemStack by lazy {
            UsefulMagicItems.FLYING_RUNE.defaultStack
                .also {
                    it.set(UsefulMagicDataComponentTypes.ENABLED, true)
                }
        }

        init {
            CooParticleAPI.scheduler.runTaskTimer(20) {
                val list = CooParticleAPI.server.playerManager.playerList
                list.filter {
                    !it.abilities.creativeMode && !it.isSpectator
                }.forEach {
                    val flying = it.abilities.flying
                    // 魔力值是否足够
                    val data = UsefulMagic.state.getDataFromServer(it.uuid)
                    val canCost = data.mana >= MANA_COST
                    val canFly =
                        it.inventory.contains(enabledFlyingRuneItem)
                    it.abilities.allowFlying = canFly
                    if (!canCost || !canFly) {
                        it.abilities.flying = false
                        it.sendAbilitiesUpdate()
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
                    "item.flying_rune_enabled"
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
        if (entity !is ServerPlayerEntity) {
            return
        }
        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED) ?: false
        entity.abilities.allowFlying = if (!entity.abilities.creativeMode && !entity.isSpectator) enabled else true
        entity.sendAbilitiesUpdate()
        val data = UsefulMagic.state.getDataFromServer(entity.uuid)
        val canCost = data.mana >= MANA_COST
        if (entity.abilities.flying && enabled) {
            handleFlying(world as ServerWorld, entity)
        } else {
            handleDisableFlying(entity)
        }
        if (!canCost && enabled) {
            stack.set(UsefulMagicDataComponentTypes.ENABLED, false)
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        val enabled = stack.get(UsefulMagicDataComponentTypes.ENABLED) ?: false
        stack.set(UsefulMagicDataComponentTypes.ENABLED, !enabled)
        return super.use(world, user, hand)
    }

}