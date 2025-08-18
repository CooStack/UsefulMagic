package cn.coostack.usefulmagic.packet.listener.client

import cn.coostack.cooparticlesapi.platform.network.ClientContext
import cn.coostack.usefulmagic.blocks.entity.formation.EnergyCrystalsBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.packet.s2c.PacketS2CEnergyCrystalChange
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationBreak
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationCreate

object FormationPacketListener {

    fun handleCreate(payload: PacketS2CFormationCreate, context: ClientContext) {
        val pos = payload.pos
        val world = context.client().level ?: return
        val entity = world.getBlockEntity(pos) ?: return
        if (entity !is FormationCoreBlockEntity) return
        entity.formation.tryBuildFormation()
    }

    fun handleBreak(payload: PacketS2CFormationBreak, context: ClientContext) {
        val pos = payload.formationPos
        val world = context.client().level ?: return
        val entity = world.getBlockEntity(pos) ?: return
        if (entity !is FormationCoreBlockEntity) return
        entity.formation.breakFormation(payload.damage, null)
    }

    fun handleEnergyChange(payload: PacketS2CEnergyCrystalChange, context: ClientContext) {
        val crystal = payload.crystal
        val world = context.client().level ?: return
        val entity = world.getBlockEntity(crystal) ?: return
        if (entity !is EnergyCrystalsBlockEntity) return
        entity.currentMana = payload.mana
        entity.maxMana = payload.maxMana
        entity.setChanged()
    }

}