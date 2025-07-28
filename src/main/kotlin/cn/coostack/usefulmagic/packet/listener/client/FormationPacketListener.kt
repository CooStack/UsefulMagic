package cn.coostack.usefulmagic.packet.listener.client

import cn.coostack.usefulmagic.blocks.entity.formation.EnergyCrystalsBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.packet.s2c.PacketS2CEnergyCrystalChange
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationBreak
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationCreate
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

@Environment(EnvType.CLIENT)
object FormationPacketListener {

    fun handleCreate(payload: PacketS2CFormationCreate, context: ClientPlayNetworking.Context) {
        val pos = payload.pos
        val world = context.client().world ?: return
        val entity = world.getBlockEntity(pos) ?: return
        if (entity !is FormationCoreBlockEntity) return
        entity.formation.tryBuildFormation()
    }

    fun handleBreak(payload: PacketS2CFormationBreak, context: ClientPlayNetworking.Context) {
        val pos = payload.formationPos
        val world = context.client().world ?: return
        val entity = world.getBlockEntity(pos) ?: return
        if (entity !is FormationCoreBlockEntity) return
        entity.formation.breakFormation(payload.damage, null)
    }

    fun handleEnergyChange(payload: PacketS2CEnergyCrystalChange, context: ClientPlayNetworking.Context) {
        val crystal = payload.crystal
        val world = context.client().world ?: return
        val entity = world.getBlockEntity(crystal) ?: return
        if (entity !is EnergyCrystalsBlockEntity) return
        entity.currentMana = payload.mana
        entity.maxMana = payload.maxMana
        entity.markDirty()
    }

}