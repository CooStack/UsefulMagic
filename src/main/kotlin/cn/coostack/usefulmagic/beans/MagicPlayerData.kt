package cn.coostack.usefulmagic.beans

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import java.util.UUID

class MagicPlayerData(var owner: UUID) {
    var maxMana: Int = 100
        set(value) {
            field = value.coerceAtLeast(0)
        }
    var mana: Int = 0
        set(value) {
            field = value.coerceIn(0, maxMana)
        }

    // 自然恢复 每秒
    var manaRegeneration = 1

    /**
     * 自动跟踪, 阵法触发绕过名单
     * 只在服务器储存
     */
    internal val friends = ArrayList<UUID>()

    fun addFriend(uuid: UUID) {
        if (isFriend(uuid)) {
            return
        }
        friends.add(uuid)
    }

    fun isFriend(uuid: UUID): Boolean {
        return friends.contains(uuid) || uuid == owner
    }

    fun isFriend(player: PlayerEntity): Boolean = isFriend(player.uuid)

    fun removeFriend(uuid: UUID) {
        friends.remove(uuid)
    }

    fun canCost(mana: Int, client: Boolean): Boolean {
        val playerInstance = if (client) {
            MinecraftClient.getInstance().player
        } else {
            UsefulMagic.server.playerManager.getPlayer(owner)
        }
        return this.mana >= mana || playerInstance?.isInCreativeMode ?: false
    }

    fun isFull(): Boolean = mana >= maxMana

    fun setFull() {
        mana = maxMana
    }

    private var tick = 0
    fun tick() {
        // 确保玩家在线
        UsefulMagic.server.playerManager.getPlayer(owner) ?: return
        if (tick++ % 20 != 0) {
            return
        }
        tick = 1
        mana += manaRegeneration
        if (mana > maxMana) {
            mana = maxMana
        }
    }

}