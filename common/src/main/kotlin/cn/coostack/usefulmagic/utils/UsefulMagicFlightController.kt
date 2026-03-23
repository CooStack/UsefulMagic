package cn.coostack.usefulmagic.utils

import net.minecraft.server.level.ServerPlayer
import java.util.EnumSet
import java.util.UUID

object UsefulMagicFlightController {
    enum class Source {
        FLYING_RUNE,
        SKY_FALLING,
    }

    private data class FlightState(
        val sources: MutableSet<Source> = EnumSet.noneOf(Source::class.java),
        var appliedMayfly: Boolean = false,
    )

    private val states = HashMap<UUID, FlightState>()

    fun setFlyingRuneGranted(player: ServerPlayer, granted: Boolean) {
        if (granted) {
            grant(player, Source.FLYING_RUNE)
        } else {
            revoke(player, Source.FLYING_RUNE)
        }
    }

    fun grant(player: ServerPlayer, source: Source) {
        if (player.isCreative || player.isSpectator) {
            return
        }
        val state = states.getOrPut(player.uuid) { FlightState() }
        state.sources.add(source)
        if (!player.abilities.mayfly) {
            player.abilities.mayfly = true
            state.appliedMayfly = true
            player.onUpdateAbilities()
        }
    }

    fun revoke(player: ServerPlayer, source: Source) {
        val state = states[player.uuid] ?: return
        if (!state.sources.remove(source)) {
            return
        }
        if (state.sources.isNotEmpty()) {
            return
        }
        if (player.isCreative || player.isSpectator) {
            states.remove(player.uuid)
            return
        }
        val shouldUpdate = state.appliedMayfly || player.abilities.flying
        if (state.appliedMayfly) {
            player.abilities.mayfly = false
        }
        if (player.abilities.flying) {
            player.abilities.flying = false
        }
        if (shouldUpdate) {
            player.onUpdateAbilities()
        }
        states.remove(player.uuid)
    }

    fun hasFlyingRuneGrant(player: ServerPlayer): Boolean {
        return states[player.uuid]?.sources?.contains(Source.FLYING_RUNE) == true
    }
}
