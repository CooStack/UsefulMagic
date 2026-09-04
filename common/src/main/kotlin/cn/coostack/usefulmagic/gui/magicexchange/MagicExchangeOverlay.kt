package cn.coostack.usefulmagic.gui.magicexchange

import cn.coostack.cooparticlesapi.key.CooKeyBindingManager
import cn.coostack.cooparticlesapi.platform.CooParticlesServices
import cn.coostack.usefulmagic.UsefulMagicKeys
import cn.coostack.usefulmagic.items.prop.SpellBagItem
import cn.coostack.usefulmagic.items.prop.SpellBagSelection
import cn.coostack.usefulmagic.items.prop.SpellBagSelector
import cn.coostack.usefulmagic.items.weapon.wands.MagicWand
import cn.coostack.usefulmagic.packet.c2s.PacketC2SWandMagicExchangeRequest
import cn.coostack.usefulmagic.utils.ChargeStateAccess
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/**
 * 管理长按换法术键时显示的客户端轮盘，并提交玩家最后看到的高亮选项。
 */
object MagicExchangeOverlay {
    private const val PAGE_SIZE = 8
    private const val SLOT_SIZE = 24

    private data class Entry(
        val magicIndex: Int,
        val stack: ItemStack,
        val name: Component
    )

    private var active = false
    private var page = 0
    private var openTicks = 0
    private var blockedUntilRelease = false

    /** 轮盘关闭后需要额外屏蔽一次的鼠标处理帧。 */
    private var suppressNextCameraTurn = false
    private var bagSlot = -1
    private var selectedHotbarSlot = -1
    private var wandInOffhand = false
    private var wandSnapshot = ItemStack.EMPTY
    private var bagSnapshot = ItemStack.EMPTY
    private var entries = emptyList<Entry>()
    private var selectedEntry: Entry? = null

    /** 当前渲染帧实际显示的轮盘指针横坐标。 */
    private var renderMouseX = 0.0

    /** 当前渲染帧实际显示的轮盘指针纵坐标。 */
    private var renderMouseY = 0.0
    private var targetMouseX = 0.0
    private var targetMouseY = 0.0
    private var lastRawMouseX = 0.0
    private var lastRawMouseY = 0.0

    /**
     * 判断本帧是否应禁止玩家视角转动。
     *
     * 轮盘关闭后的抑制标记只消费一次，用于覆盖客户端 tick 与鼠标帧之间的释放边界。
     *
     * @return 当前鼠标处理帧需要禁止转向时返回 `true`
     */
    @JvmStatic
    fun shouldSuppressCameraTurn(): Boolean {
        if (active) {
            return true
        }
        if (!suppressNextCameraTurn) {
            return false
        }
        suppressNextCameraTurn = false
        return true
    }

    fun requestOpen() {
        if (!active) {
            tryOpen(Minecraft.getInstance())
        }
    }

    fun tickClient() {
        val client = Minecraft.getInstance()
        val mapping = CooKeyBindingManager.getMapping(UsefulMagicKeys.EXCHANGE_MAGIC)
        val requested = MagicExchangeOpenSignal.consume()
        val keyDown = mapping?.isDown == true
        if (!keyDown) {
            blockedUntilRelease = false
            if (active) {
                commitAndClose(client)
            }
            return
        }
        if (blockedUntilRelease) {
            return
        }
        if (!active) {
            if (requested) {
                tryOpen(client)
            }
            return
        }
        tickOpen(client)
    }

    fun onHudRender(context: GuiGraphics, partialTick: Float) {
        if (!active || entries.isEmpty()) {
            return
        }
        val client = Minecraft.getInstance()
        updateVirtualMouse(client)
        updateRenderMouse(client)
        updateSelected(contextWidth = context.guiWidth(), contextHeight = context.guiHeight())
        val font = client.font
        val centerX = context.guiWidth() / 2
        val centerY = context.guiHeight() / 2
        val visible = visibleEntries()
        val slotPositions = slotPositions(centerX, centerY, visible.size)
        val scale = min(1.0F, (openTicks + partialTick) / 4.0F)

        val pose = context.pose()
        pose.pushPose()
        pose.translate(centerX.toFloat(), centerY.toFloat(), 0.0F)
        pose.scale(scale, scale, 1.0F)
        pose.translate(-centerX.toFloat(), -centerY.toFloat(), 0.0F)

        visible.forEachIndexed { index, entry ->
            val (slotX, slotY) = slotPositions[index]
            val selected = entry == selectedEntry
            val background = if (selected) 0xCCF2E8C9u.toInt() else 0xAA101018u.toInt()
            val border = if (selected) 0xFFEFD37Au.toInt() else 0xAAFFFFFFu.toInt()
            context.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, background)
            context.renderOutline(slotX, slotY, SLOT_SIZE, SLOT_SIZE, border)
            if (entry.stack.isEmpty) {
                context.drawCenteredString(font, "-", slotX + SLOT_SIZE / 2, slotY + 8, 0xFFE6E6E6u.toInt())
            } else {
                context.renderItem(entry.stack, slotX + 4, slotY + 4)
                context.renderItemDecorations(font, entry.stack, slotX + 4, slotY + 4)
            }
        }

        pose.popPose()

        selectedEntry?.let { selected ->
            context.drawCenteredString(font, selected.name, centerX, centerY + 72, 0xFFFFFFFFu.toInt())
        }
        if (pageCount() > 1) {
            context.drawCenteredString(font, "${page + 1}/${pageCount()}", centerX, centerY + 14, 0xFFE6E6E6u.toInt())
        }
        context.fill(
            renderMouseX.toInt() - 1,
            renderMouseY.toInt() - 1,
            renderMouseX.toInt() + 2,
            renderMouseY.toInt() + 2,
            0xFFFFFFFFu.toInt()
        )

        selectedEntry?.let { selected ->
            val mouseX = renderMouseX.toInt()
            val mouseY = renderMouseY.toInt()
            if (selected.stack.isEmpty) {
                context.renderTooltip(font, selected.name, mouseX, mouseY)
            } else {
                context.renderTooltip(font, selected.stack, mouseX, mouseY)
            }
        }
    }

    @JvmStatic
    fun onMouseScroll(deltaY: Double): Boolean {
        if (!active) {
            return false
        }
        val pages = pageCount()
        if (pages > 1) {
            page = (page + if (deltaY < 0.0) 1 else -1).floorMod(pages)
            selectedEntry = null
        }
        return true
    }

    private fun tryOpen(client: Minecraft) {
        val player = client.player ?: return
        if (client.level == null || client.screen != null || ChargeStateAccess.isCharging(player) || player.isUsingItem) {
            return
        }
        val mainWand = player.mainHandItem.item is MagicWand
        val offhandWand = player.offhandItem.item is MagicWand
        if (!mainWand && !offhandWand) {
            return
        }
        val selection = SpellBagSelector.findFirstUsable(player) ?: return
        val handStack = if (mainWand) player.mainHandItem else player.offhandItem
        active = true
        page = 0
        openTicks = 0
        bagSlot = selection.inventorySlot
        selectedHotbarSlot = player.inventory.selected
        wandInOffhand = !mainWand && offhandWand
        wandSnapshot = handStack.copy()
        bagSnapshot = selection.stack.copy()
        entries = createEntries(selection)
        selectedEntry = if (entries.size == 1) entries.first() else null
        val centerX = client.window.guiScaledWidth / 2.0
        val centerY = client.window.guiScaledHeight / 2.0
        renderMouseX = centerX
        renderMouseY = centerY
        targetMouseX = centerX
        targetMouseY = centerY
        lastRawMouseX = client.mouseHandler.xpos()
        lastRawMouseY = client.mouseHandler.ypos()
    }

    private fun tickOpen(client: Minecraft) {
        val player = client.player
        if (player == null ||
            client.level == null ||
            client.screen != null ||
            ChargeStateAccess.isCharging(player) ||
            player.isUsingItem
        ) {
            cancelUntilRelease(client)
            return
        }
        val currentWand = if (wandInOffhand) player.offhandItem else player.mainHandItem
        if (player.inventory.selected != selectedHotbarSlot ||
            currentWand.item !is MagicWand ||
            !ItemStack.isSameItemSameComponents(currentWand, wandSnapshot)
        ) {
            cancelUntilRelease(client)
            return
        }
        val selection = SpellBagSelector.findFirstUsable(player)
        if (selection == null ||
            selection.inventorySlot != bagSlot ||
            !ItemStack.isSameItemSameComponents(selection.stack, bagSnapshot)
        ) {
            cancelUntilRelease(client)
            return
        }
        openTicks++
    }

    private fun commitAndClose(client: Minecraft) {
        val player = client.player
        if (player == null || client.level == null || ChargeStateAccess.isCharging(player) || player.isUsingItem) {
            close(client.screen == null)
            return
        }
        val selection = selectedEntry ?: if (entries.size == 1) entries.first() else null
        if (selection == null) {
            close(client.screen == null)
            return
        }
        val currentWand = if (wandInOffhand) player.offhandItem else player.mainHandItem
        val currentSelection = SpellBagSelector.findFirstUsable(player)
        if (currentWand.item is MagicWand &&
            ItemStack.isSameItemSameComponents(currentWand, wandSnapshot) &&
            currentSelection != null &&
            currentSelection.inventorySlot == bagSlot &&
            ItemStack.isSameItemSameComponents(currentSelection.stack, bagSnapshot) &&
            player.inventory.selected == selectedHotbarSlot
        ) {
            CooParticlesServices.CLIENT_NETWORK.send(
                PacketC2SWandMagicExchangeRequest(bagSlot, selection.magicIndex, wandInOffhand, selectedHotbarSlot)
            )
        }
        close(client.screen == null)
    }

    private fun cancelUntilRelease(client: Minecraft) {
        close(client.screen == null)
        blockedUntilRelease = true
    }

    private fun close(suppressCameraTurn: Boolean) {
        suppressNextCameraTurn = active && suppressCameraTurn
        active = false
        page = 0
        openTicks = 0
        bagSlot = -1
        selectedHotbarSlot = -1
        wandInOffhand = false
        wandSnapshot = ItemStack.EMPTY
        bagSnapshot = ItemStack.EMPTY
        entries = emptyList()
        selectedEntry = null
    }

    private fun createEntries(selection: SpellBagSelection): List<Entry> {
        if (selection.contents.isEmpty()) {
            return listOf(
                Entry(
                    SpellBagItem.EMPTY_MAGIC_INDEX,
                    ItemStack.EMPTY,
                    Component.translatable("item.usefulmagic.spell_bag.empty")
                )
            )
        }
        return selection.contents.mapIndexed { index, stack ->
            Entry(index, stack.copy(), stack.hoverName)
        }
    }

    private fun updateVirtualMouse(client: Minecraft) {
        val window = client.window
        val rawX = client.mouseHandler.xpos()
        val rawY = client.mouseHandler.ypos()
        val scaleX = window.guiScaledWidth.toDouble() / window.screenWidth.toDouble().coerceAtLeast(1.0)
        val scaleY = window.guiScaledHeight.toDouble() / window.screenHeight.toDouble().coerceAtLeast(1.0)
        targetMouseX =
            (targetMouseX + (rawX - lastRawMouseX) * scaleX).coerceIn(0.0, window.guiScaledWidth.toDouble())
        targetMouseY =
            (targetMouseY + (rawY - lastRawMouseY) * scaleY).coerceIn(0.0, window.guiScaledHeight.toDouble())
        lastRawMouseX = rawX
        lastRawMouseY = rawY
    }

    /** 让显示指针按真实帧间隔平滑追随每帧采样到的目标位置。 */
    private fun updateRenderMouse(client: Minecraft) {
        // 指数时间步让相同时间内的追随幅度不受帧率影响。
        val interpolation = 1.0 - exp(-2.0 * client.timer.realtimeDeltaTicks.toDouble().coerceAtLeast(0.0))
        renderMouseX += (targetMouseX - renderMouseX) * interpolation
        renderMouseY += (targetMouseY - renderMouseY) * interpolation
    }

    private fun updateSelected(contextWidth: Int, contextHeight: Int) {
        val visible = visibleEntries()
        if (visible.size == 1) {
            selectedEntry = visible.first()
            return
        }
        val centerX = contextWidth / 2
        val centerY = contextHeight / 2
        selectedEntry = entryBySector(visible, centerX, centerY)
    }

    private fun entryBySector(visible: List<Entry>, centerX: Int, centerY: Int): Entry? {
        val deltaX = renderMouseX - centerX
        val deltaY = renderMouseY - centerY
        if (hypot(deltaX, deltaY) < SLOT_SIZE / 2.0) {
            return null
        }
        // 屏幕正上方为第一个扇区，增加半个扇区宽度后再归一化到正角度。
        val sectorSize = PI * 2.0 / visible.size
        val normalized = (atan2(deltaY, deltaX) + PI / 2.0 + sectorSize / 2.0).floorMod(PI * 2.0)
        val sector = (normalized / sectorSize).toInt().coerceIn(0, visible.lastIndex)
        return visible[sector]
    }

    private fun visibleEntries(): List<Entry> {
        val start = page * PAGE_SIZE
        return entries.drop(start).take(PAGE_SIZE)
    }

    private fun pageCount(): Int {
        return ((entries.size + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)
    }

    private fun slotPositions(centerX: Int, centerY: Int, count: Int): List<Pair<Int, Int>> {
        val itemSize = 16
        val radius = 54.0
        if (count <= 1) {
            return listOf(centerX - itemSize / 2 - 4 to centerY - itemSize / 2 - 4)
        }
        return List(count) { index ->
            val angle = -PI / 2.0 + index.toDouble() / count.toDouble() * PI * 2.0
            val x = (centerX + cos(angle) * radius - SLOT_SIZE / 2).toInt()
            val y = (centerY + sin(angle) * radius - SLOT_SIZE / 2).toInt()
            x to y
        }
    }

    private fun Int.floorMod(modulus: Int): Int {
        return ((this % modulus) + modulus) % modulus
    }

    private fun Double.floorMod(modulus: Double): Double {
        return ((this % modulus) + modulus) % modulus
    }

}
