package cn.coostack.usefulmagic.items.prop

import cn.coostack.usefulmagic.items.weapon.magic.MagicItem
import cn.coostack.usefulmagic.items.weapon.wands.MagicWand
import net.minecraft.ChatFormatting
import net.minecraft.core.NonNullList
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.SlotAccess
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickAction
import net.minecraft.world.inventory.Slot
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.ItemContainerContents
import net.minecraft.world.level.Level
import java.util.Optional

class SpellBagItem(properties: Properties) : Item(properties) {
    override fun overrideStackedOnOther(stack: ItemStack, slot: Slot, action: ClickAction, player: Player): Boolean {
        if (stack.count != 1 || action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false
        }
        val slotItem = slot.item
        return when {
            slotItem.isEmpty -> removeFirstMagicToSlot(stack, slot, player)
            slotItem.item is MagicWand -> exchangeWithWand(stack, slotItem, player)
            isMagicStack(slotItem) -> insertMagicFromSlot(stack, slot, player)
            else -> false
        }
    }

    override fun overrideOtherStackedOnMe(
        stack: ItemStack,
        other: ItemStack,
        slot: Slot,
        action: ClickAction,
        player: Player,
        access: SlotAccess,
    ): Boolean {
        if (stack.count != 1 || action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false
        }
        return when {
            other.isEmpty -> removeFirstMagicToAccess(stack, access, player)
            other.item is MagicWand -> exchangeWithWand(stack, other, player)
            isMagicStack(other) -> insertMagicFromCarried(stack, other, player)
            else -> false
        }
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)
        val otherHand = if (usedHand == InteractionHand.MAIN_HAND) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND
        val otherStack = player.getItemInHand(otherHand)
        if (otherStack.item !is MagicWand) {
            return InteractionResultHolder.pass(stack)
        }
        val changed = exchangeWithWand(stack, otherStack, player)
        return if (changed) {
            InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
        } else {
            InteractionResultHolder.fail(stack)
        }
    }

    override fun getTooltipImage(stack: ItemStack): Optional<TooltipComponent> {
        if (stack.has(DataComponents.HIDE_TOOLTIP) || stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
            return Optional.empty()
        }
        val contents = getMagicContents(stack)
        if (contents.isEmpty()) {
            return Optional.empty()
        }
        return Optional.of(SpellBagTooltip(contents.map { it.copy() }, MAX_MAGIC_COUNT))
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        val contents = getMagicContents(stack)
        tooltipComponents.add(
            Component.translatable(
                "item.usefulmagic.spell_bag.fullness",
                Component.literal("${contents.size}"),
                Component.literal("$MAX_MAGIC_COUNT"),
            ).withStyle(ChatFormatting.GRAY)
        )
        if (contents.isEmpty()) {
            tooltipComponents.add(Component.translatable("item.usefulmagic.spell_bag.empty").withStyle(ChatFormatting.DARK_GRAY))
            return
        }
    }

    override fun isBarVisible(stack: ItemStack): Boolean {
        return getMagicContents(stack).isNotEmpty()
    }

    override fun getBarWidth(stack: ItemStack): Int {
        val size = getMagicContents(stack).size
        return if (size <= 0) 0 else (1 + size * 12 / MAX_MAGIC_COUNT).coerceAtMost(13)
    }

    override fun getBarColor(stack: ItemStack): Int = BAR_COLOR

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onDestroyed(itemEntity: ItemEntity) {
        val contents = getMagicContents(itemEntity.item)
        if (contents.isEmpty()) {
            return
        }
        setMagicContents(itemEntity.item, emptyList())
        ItemUtils.onContainerDestroyed(itemEntity, contents)
    }

    private fun removeFirstMagicToSlot(stack: ItemStack, slot: Slot, player: Player): Boolean {
        val contents = getMagicContents(stack)
        val first = contents.firstOrNull() ?: return false
        val remain = slot.safeInsert(first.copy())
        if (!remain.isEmpty) {
            return false
        }
        setMagicContents(stack, contents.drop(1))
        playRemoveOneSound(player)
        return true
    }

    private fun removeFirstMagicToAccess(stack: ItemStack, access: SlotAccess, player: Player): Boolean {
        val contents = getMagicContents(stack)
        val first = contents.firstOrNull() ?: return false
        if (!access.set(first.copy())) {
            return false
        }
        setMagicContents(stack, contents.drop(1))
        playRemoveOneSound(player)
        return true
    }

    private fun insertMagicFromSlot(stack: ItemStack, slot: Slot, player: Player): Boolean {
        val taken = slot.safeTake(1, 1, player)
        if (taken.isEmpty) {
            return false
        }
        if (!insertMagic(stack, taken)) {
            slot.safeInsert(taken)
            return false
        }
        playInsertSound(player)
        return true
    }

    private fun insertMagicFromCarried(stack: ItemStack, other: ItemStack, player: Player): Boolean {
        val magic = other.split(1)
        if (magic.isEmpty) {
            return false
        }
        if (!insertMagic(stack, magic)) {
            other.grow(1)
            return false
        }
        playInsertSound(player)
        return true
    }

    fun exchangeWithWandAt(bagStack: ItemStack, wandStack: ItemStack, magicIndex: Int, player: Player): Boolean {
        val wand = wandStack.item as? MagicWand ?: return false
        val contents = getMagicContents(bagStack)
        val bagMagic = when {
            magicIndex == EMPTY_MAGIC_INDEX -> null
            magicIndex in contents.indices -> contents[magicIndex]
            else -> return false
        }
        val loadedMagic = wand.getLoadedMagic(wandStack)
        if (bagMagic == null && loadedMagic.isEmpty) {
            return false
        }
        if (bagMagic != null && !wand.canLoadMagic(wandStack, bagMagic)) {
            return false
        }

        val newContents = contents.toMutableList()
        if (bagMagic != null) {
            newContents.removeAt(magicIndex)
        }
        if (!loadedMagic.isEmpty) {
            if (newContents.size >= MAX_MAGIC_COUNT) {
                return false
            }
            val insertIndex = if (magicIndex == EMPTY_MAGIC_INDEX) 0 else magicIndex.coerceAtMost(newContents.size)
            newContents.add(insertIndex, sanitizeMagic(loadedMagic))
        }

        wand.cancelChargeIfNeeded(player, wandStack)
        wand.setLoadedMagic(wandStack, bagMagic?.copy() ?: ItemStack.EMPTY)
        setMagicContents(bagStack, newContents)
        if (bagMagic == null) {
            playRemoveOneSound(player)
        } else if (loadedMagic.isEmpty) {
            playInsertSound(player)
        } else {
            playInsertSound(player)
            playRemoveOneSound(player)
        }
        return true
    }

    private fun exchangeWithWand(bagStack: ItemStack, wandStack: ItemStack, player: Player): Boolean {
        val index = if (getMagicContents(bagStack).isEmpty()) EMPTY_MAGIC_INDEX else 0
        return exchangeWithWandAt(bagStack, wandStack, index, player)
    }

    private fun insertMagic(stack: ItemStack, magic: ItemStack): Boolean {
        if (!isMagicStack(magic)) {
            return false
        }
        val contents = getMagicContents(stack).toMutableList()
        if (contents.size >= MAX_MAGIC_COUNT) {
            return false
        }
        contents.add(0, sanitizeMagic(magic))
        setMagicContents(stack, contents)
        return true
    }

    private fun getMagicContents(stack: ItemStack): List<ItemStack> {
        return getMagicContentsForDisplay(stack)
    }

    private fun setMagicContents(stack: ItemStack, contents: List<ItemStack>) {
        val list = NonNullList.withSize(MAX_MAGIC_COUNT, ItemStack.EMPTY)
        contents.asSequence()
            .filter(::isMagicStack)
            .take(MAX_MAGIC_COUNT)
            .map(::sanitizeMagic)
            .forEachIndexed { index, magic -> list[index] = magic }
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list))
    }

    private fun playRemoveOneSound(entity: Entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().random.nextFloat() * 0.4F)
    }

    private fun playInsertSound(entity: Entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().random.nextFloat() * 0.4F)
    }

    companion object {
        const val MAX_MAGIC_COUNT = 16
        const val EMPTY_MAGIC_INDEX = -1
        private const val BAR_COLOR = 0x8D4DFF

        @JvmStatic
        fun hasMagicContents(stack: ItemStack): Boolean {
            return getMagicContentsFromContainer(stack)
                .filter(SpellBagItem::isMagicStack)
                .findAny()
                .isPresent
        }

        @JvmStatic
        fun getMagicContentsForDisplay(stack: ItemStack): List<ItemStack> {
            return getMagicContentsFromContainer(stack)
                .filter(SpellBagItem::isMagicStack)
                .limit(MAX_MAGIC_COUNT.toLong())
                .map(SpellBagItem::sanitizeMagic)
                .toList()
        }

        private fun getMagicContentsFromContainer(stack: ItemStack) =
            stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyStream()

        private fun isMagicStack(stack: ItemStack): Boolean {
            return !stack.isEmpty && stack.item is MagicItem
        }

        private fun sanitizeMagic(stack: ItemStack): ItemStack {
            return stack.copyWithCount(1)
        }
    }
}
