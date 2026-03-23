package cn.coostack.usefulmagic.gui.friend

import cn.coostack.usefulmagic.gui.friend.widget.FriendItemWidget
import cn.coostack.usefulmagic.managers.client.ClientRequestManager
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendListRequest
import cn.coostack.usefulmagic.packet.c2s.PacketC2SFriendSettingsChangeRequest
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendChangeResponse
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFriendListResponse
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.PlayerSkin
import net.minecraft.network.chat.Component
import java.util.UUID
import kotlin.math.max

class FriendManagerScreen : Screen(Component.translatable("screen.title.friend_manager_title")) {

    private enum class MenuTab {
        FRIEND_LIST,
        FRIENDLY_SETTINGS
    }

    companion object {
        const val PANEL_MARGIN = 20
        const val PANEL_MAX_WIDTH = 430
        const val PANEL_MIN_WIDTH = 300
        const val PANEL_PADDING = 10
        const val PANEL_MIN_HEIGHT = 220
        const val HEADER_HEIGHT = 30
        const val FOOTER_HEIGHT = 28
        const val ITEM_CARD_HEIGHT = 34
        const val ITEM_CARD_GAP = 8
        const val NAV_BUTTON_WIDTH = 56
        const val NAV_BUTTON_HEIGHT = 20
        const val TAB_BUTTON_WIDTH = 90
        const val TAB_BUTTON_HEIGHT = 20
        const val SETTING_ROW_HEIGHT = 24
        const val SETTING_ROW_GAP = 10
        const val SETTING_COLUMN_GAP = 8
        const val SETTING_COLUMN_COUNT = 2
    }

    val client: Minecraft
        get() = Minecraft.getInstance()

    var currentPage = 1
    var maxPage = 1

    private var panelX = 0
    private var panelY = 0
    private var panelWidth = 0
    private var panelHeight = 0

    private var listX = 0
    private var listY = 0
    private var listWidth = 0
    private var listHeight = 0

    private var currentTab = MenuTab.FRIEND_LIST
    private var currentProfiles = mutableListOf<PacketS2CFriendListResponse.PlayerProfile>()
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button

    private var treatHostileAsFriend = false
    private var treatNeutralAsFriend = false
    private var treatNonFriendPlayerAsFriend = false
    private var treatFriendPlayerAsFriend = true
    private var treatAnimalAsFriend = false
    private var treatFriendlyMobAsFriend = false

    fun getOrRequestFriends(recall: (PacketS2CFriendListResponse) -> Unit) {
        val player = client.player ?: return
        ClientRequestManager.sendRequest(
            PacketC2SFriendListRequest(player.uuid, currentPage),
            PacketS2CFriendListResponse.payloadID
        ).recall {
            val response = it as PacketS2CFriendListResponse
            recall(response)
        }
    }

    fun tryFriendsSkinTexture(uuid: UUID): Pair<PlayerSkin?, PlayerSkin.Model?> {
        val networkHandler = client.connection
        val entry = networkHandler?.getPlayerInfo(uuid)
        return entry?.skin to entry?.skin?.model()
    }

    override fun init() {
        flushWidget()
        super.init()
    }

    internal fun flushWidget(requestData: Boolean = true) {
        clearWidgets()
        calculateLayout()
        initTabButtons()
        if (currentTab == MenuTab.FRIEND_LIST) {
            initNavigationButtons()
        }

        if (!requestData) {
            renderCurrentTabWidgets()
            return
        }

        currentProfiles.clear()
        getOrRequestFriends { response ->
            if (client.screen !== this@FriendManagerScreen) {
                return@getOrRequestFriends
            }
            applyResponse(response)
            renderCurrentTabWidgets()
        }
    }

    private fun applyResponse(response: PacketS2CFriendListResponse) {
        maxPage = response.maxPage
        currentProfiles = response.friends.toMutableList()
        treatHostileAsFriend = response.treatHostileAsFriend
        treatNeutralAsFriend = response.treatNeutralAsFriend
        treatNonFriendPlayerAsFriend = response.treatNonFriendPlayerAsFriend
        treatFriendPlayerAsFriend = response.treatFriendPlayerAsFriend
        treatAnimalAsFriend = response.treatAnimalAsFriend
        treatFriendlyMobAsFriend = response.treatFriendlyMobAsFriend
    }

    private fun calculateLayout() {
        panelWidth = (width - PANEL_MARGIN * 2).coerceIn(PANEL_MIN_WIDTH, PANEL_MAX_WIDTH)
        panelHeight = (height - PANEL_MARGIN * 2).coerceAtLeast(PANEL_MIN_HEIGHT)
        panelX = (width - panelWidth) / 2
        panelY = (height - panelHeight) / 2
        listX = panelX + PANEL_PADDING
        listY = panelY + HEADER_HEIGHT + PANEL_PADDING
        listWidth = panelWidth - PANEL_PADDING * 2
        listHeight = panelHeight - HEADER_HEIGHT - FOOTER_HEIGHT - PANEL_PADDING * 2
    }

    private fun initTabButtons() {
        val tabY = panelY + (HEADER_HEIGHT - TAB_BUTTON_HEIGHT) / 2
        val tabsStartX = panelX + panelWidth - PANEL_PADDING - TAB_BUTTON_WIDTH * 2 - 6

        addRenderableWidget(
            Button.builder(Component.translatable("screen.friend_manager.tab.friend_list")) {
                if (currentTab != MenuTab.FRIEND_LIST) {
                    currentTab = MenuTab.FRIEND_LIST
                    flushWidget()
                }
            }.bounds(
                tabsStartX,
                tabY,
                TAB_BUTTON_WIDTH,
                TAB_BUTTON_HEIGHT
            ).build().also {
                it.active = currentTab != MenuTab.FRIEND_LIST
            }
        )

        addRenderableWidget(
            Button.builder(Component.translatable("screen.friend_manager.tab.friendly_settings")) {
                if (currentTab != MenuTab.FRIENDLY_SETTINGS) {
                    currentTab = MenuTab.FRIENDLY_SETTINGS
                    flushWidget()
                }
            }.bounds(
                tabsStartX + TAB_BUTTON_WIDTH + 6,
                tabY,
                TAB_BUTTON_WIDTH,
                TAB_BUTTON_HEIGHT
            ).build().also {
                it.active = currentTab != MenuTab.FRIENDLY_SETTINGS
            }
        )
    }

    private fun initNavigationButtons() {
        val buttonY = panelY + panelHeight - FOOTER_HEIGHT + (FOOTER_HEIGHT - NAV_BUTTON_HEIGHT) / 2
        prevButton = addRenderableWidget(
            Button.builder(Component.translatable("screen.friend_manager.page_prev")) {
                if (currentPage > 1) {
                    currentPage--
                    flushWidget()
                }
            }.bounds(
                panelX + PANEL_PADDING,
                buttonY,
                NAV_BUTTON_WIDTH,
                NAV_BUTTON_HEIGHT
            ).build()
        )

        nextButton = addRenderableWidget(
            Button.builder(Component.translatable("screen.friend_manager.page_next")) {
                if (currentPage < maxPage) {
                    currentPage++
                    flushWidget()
                }
            }.bounds(
                panelX + panelWidth - PANEL_PADDING - NAV_BUTTON_WIDTH,
                buttonY,
                NAV_BUTTON_WIDTH,
                NAV_BUTTON_HEIGHT
            ).build()
        )
        updatePageButtons()
    }

    private fun updatePageButtons() {
        if (!::prevButton.isInitialized || !::nextButton.isInitialized) {
            return
        }
        prevButton.active = currentPage > 1
        nextButton.active = currentPage < maxPage
    }

    private fun renderCurrentTabWidgets() {
        if (currentTab == MenuTab.FRIEND_LIST) {
            setCurrentPageIcons(currentProfiles)
            updatePageButtons()
            return
        }
        initFriendlySettingButtons()
    }

    private fun initFriendlySettingButtons() {
        addFriendlySettingToggle(
            index = 0,
            titleKey = "screen.friend_manager.setting.hostile",
            getter = { treatHostileAsFriend },
            setter = { treatHostileAsFriend = it }
        )
        addFriendlySettingToggle(
            index = 1,
            titleKey = "screen.friend_manager.setting.neutral",
            getter = { treatNeutralAsFriend },
            setter = { treatNeutralAsFriend = it }
        )
        addFriendlySettingToggle(
            index = 2,
            titleKey = "screen.friend_manager.setting.non_friend_player",
            getter = { treatNonFriendPlayerAsFriend },
            setter = { treatNonFriendPlayerAsFriend = it }
        )
        addFriendlySettingToggle(
            index = 3,
            titleKey = "screen.friend_manager.setting.friend_player",
            getter = { treatFriendPlayerAsFriend },
            setter = { treatFriendPlayerAsFriend = it }
        )
        addFriendlySettingToggle(
            index = 4,
            titleKey = "screen.friend_manager.setting.animal",
            getter = { treatAnimalAsFriend },
            setter = { treatAnimalAsFriend = it }
        )
        addFriendlySettingToggle(
            index = 5,
            titleKey = "screen.friend_manager.setting.friendly_mob",
            getter = { treatFriendlyMobAsFriend },
            setter = { treatFriendlyMobAsFriend = it }
        )
    }

    private fun addFriendlySettingToggle(
        index: Int,
        titleKey: String,
        getter: () -> Boolean,
        setter: (Boolean) -> Unit
    ) {
        val columnWidth = (listWidth - 16 - SETTING_COLUMN_GAP) / SETTING_COLUMN_COUNT
        val columnIndex = index % SETTING_COLUMN_COUNT
        val rowIndex = index / SETTING_COLUMN_COUNT
        val x = listX + 8 + columnIndex * (columnWidth + SETTING_COLUMN_GAP)
        val y = listY + 8 + rowIndex * (SETTING_ROW_HEIGHT + SETTING_ROW_GAP)
        val enabled = getter()
        addRenderableWidget(
            Button.builder(friendlySettingText(Component.translatable(titleKey), enabled)) {
                setter(!enabled)
                submitFriendlySettings()
                flushWidget(false)
            }.bounds(
                x,
                y,
                columnWidth,
                SETTING_ROW_HEIGHT
            ).tooltip(
                Tooltip.create(friendlySettingTooltip(titleKey, enabled))
            ).build()
        )
    }

    private fun friendlySettingText(title: Component, enabled: Boolean): Component {
        return Component.literal(if (enabled) "[x] " else "[ ] ").append(title)
    }

    private fun friendlySettingTooltip(titleKey: String, enabled: Boolean): Component {
        return Component.translatable(
            if (enabled) "screen.friend_manager.setting.tooltip.on" else "screen.friend_manager.setting.tooltip.off",
            Component.translatable(titleKey)
        )
    }

    private fun submitFriendlySettings() {
        val player = client.player ?: return
        ClientRequestManager.sendRequest(
            PacketC2SFriendSettingsChangeRequest(
                owner = player.uuid,
                treatHostileAsFriend = treatHostileAsFriend,
                treatNeutralAsFriend = treatNeutralAsFriend,
                treatNonFriendPlayerAsFriend = treatNonFriendPlayerAsFriend,
                treatFriendPlayerAsFriend = treatFriendPlayerAsFriend,
                treatAnimalAsFriend = treatAnimalAsFriend,
                treatFriendlyMobAsFriend = treatFriendlyMobAsFriend
            ),
            PacketS2CFriendChangeResponse.payloadID
        ).recall {
            val response = it as PacketS2CFriendChangeResponse
            if (!response.status) {
                client.execute { flushWidget() }
            }
        }
    }

    private fun setCurrentPageIcons(currents: MutableList<PacketS2CFriendListResponse.PlayerProfile>) {
        if (currents.isEmpty()) {
            return
        }
        val maxVisible = max(1, (listHeight + ITEM_CARD_GAP) / (ITEM_CARD_HEIGHT + ITEM_CARD_GAP))
        val count = minOf(currents.size, maxVisible)
        repeat(count) {
            val profile = currents[it]
            addRenderableWidget(
                FriendItemWidget(
                    profile,
                    tryFriendsSkinTexture(profile.uuid).first,
                    listX,
                    listY + (ITEM_CARD_HEIGHT + ITEM_CARD_GAP) * it,
                    listWidth,
                    ITEM_CARD_HEIGHT,
                    this
                )
            )
        }
    }

    override fun renderBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xD8131D20u.toInt())
        graphics.fill(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + HEADER_HEIGHT, 0xCC1E2D32u.toInt())
        graphics.fill(
            panelX + PANEL_PADDING,
            listY,
            panelX + panelWidth - PANEL_PADDING,
            listY + listHeight,
            0xB20B1417u.toInt()
        )
        graphics.fill(
            panelX + 1,
            panelY + panelHeight - FOOTER_HEIGHT,
            panelX + panelWidth - 1,
            panelY + panelHeight - 1,
            0xCC1A272Cu.toInt()
        )
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF4A7A7Fu.toInt())
        graphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, 0xFF4A7A7Fu.toInt())
        graphics.fill(panelX, panelY, panelX + 1, panelY + panelHeight, 0xFF4A7A7Fu.toInt())
        graphics.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF4A7A7Fu.toInt())
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(graphics, mouseX, mouseY, delta)
        super.render(graphics, mouseX, mouseY, delta)

        val titleText = Component.translatable("screen.title.friend_manager_title")
        graphics.drawCenteredString(
            font,
            titleText,
            panelX + panelWidth / 2,
            panelY + (HEADER_HEIGHT - font.lineHeight) / 2,
            0xFFEAF7F4u.toInt()
        )

        if (currentTab == MenuTab.FRIEND_LIST) {
            val pageText = Component.translatable("screen.friend_manager.page_info", currentPage, maxPage)
            graphics.drawCenteredString(
                font,
                pageText,
                panelX + panelWidth / 2,
                panelY + panelHeight - FOOTER_HEIGHT + (FOOTER_HEIGHT - font.lineHeight) / 2,
                0xFFC8DED8u.toInt()
            )

            if (currentProfiles.isEmpty()) {
                graphics.drawCenteredString(
                    font,
                    Component.translatable("screen.friend_manager.empty"),
                    panelX + panelWidth / 2,
                    listY + listHeight / 2 - font.lineHeight / 2,
                    0xFF9FB8B1u.toInt()
                )
            }
            return
        }

        graphics.drawCenteredString(
            font,
            Component.translatable("screen.friend_manager.friendly_settings_hint"),
            panelX + panelWidth / 2,
            panelY + panelHeight - FOOTER_HEIGHT + (FOOTER_HEIGHT - font.lineHeight) / 2,
            0xFFC8DED8u.toInt()
        )
    }
}
