package cn.coostack.usefulmagic.blocks.entity.formation

import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.formation.CrystalFormation
import cn.coostack.usefulmagic.formation.api.FormationScale
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.UUID

class FormationCoreBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.FORMATION_CORE.get(), pos, state) {
    override fun saveAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        nbt.putString("owner_uuid", formation.owner?.toString() ?: "null")
        nbt.putString("scale", formation.scale.name)
        val settings = formation.settings
        val settingsNBT = CompoundTag()
        settingsNBT.putBoolean("player_attack", settings.playerEntityAttack)
        settingsNBT.putBoolean("hostile_attack", settings.hostileEntityAttack)
        settingsNBT.putBoolean("animal_attack", settings.animalEntityAttack)
        settingsNBT.putBoolean("another_attack", settings.anotherEntityAttack)
        settingsNBT.putBoolean("display_particle_only_trigger", settings.displayParticleOnlyTrigger)
        settingsNBT.putDouble("trigger_range", settings.triggerRange)
        nbt.put("settings", settingsNBT)
        nbt.putUUID("formation_uuid", formation.uuid)
    }

    override fun loadAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        formation.world = level
        formation.scale = FormationScale.valueOf(nbt.getString("scale"))
        val settings = formation.settings
        val settingsNBT = nbt.getCompound("settings")
        settings.playerEntityAttack = settingsNBT.getBoolean("player_attack")
        settings.hostileEntityAttack = settingsNBT.getBoolean("hostile_attack")
        settings.animalEntityAttack = settingsNBT.getBoolean("animal_attack")
        settings.anotherEntityAttack = settingsNBT.getBoolean("another_attack")
        settings.displayParticleOnlyTrigger = settingsNBT.getBoolean("display_particle_only_trigger")
        settings.triggerRange = settingsNBT.getDouble("trigger_range")
        val formationUUID = nbt.getUUID("formation_uuid")
        formation.uuid = formationUUID
        val ownerUUID = nbt.getString("owner_uuid") ?: return
        if (ownerUUID != "null") {
            formation.owner = UUID.fromString(ownerUUID)
        }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener?>? {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    val formation = CrystalFormation(level, null, pos.center)
    fun tick(
        world: Level,
        pos: BlockPos,
        state: BlockState,
    ) {
        if (formation.world == null) formation.world = world
        /**
         * 确保了一定是被玩家激活过的
         * 同时也彻底隔绝了自然阵法 (owner == null) 的阵法
         */
        if (!formation.isActiveFormation() && formation.owner != null) {
            formation.tryBuildFormation()
        }
        formation.tick()
        setChanged()
    }
}