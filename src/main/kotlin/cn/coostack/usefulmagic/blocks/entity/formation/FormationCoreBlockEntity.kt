package cn.coostack.usefulmagic.blocks.entity.formation

import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.formation.CrystalFormation
import cn.coostack.usefulmagic.formation.api.FormationScale
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.UUID

class FormationCoreBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.FORMATION_CORE, pos, state) {
    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.writeNbt(nbt, registryLookup)
        nbt.putString("owner_uuid", formation.owner?.toString() ?: "null")
        nbt.putString("scale", formation.scale.name)
        val settings = formation.settings
        val settingsNBT = NbtCompound()
        settingsNBT.putBoolean("player_attack", settings.playerEntityAttack)
        settingsNBT.putBoolean("hostile_attack", settings.hostileEntityAttack)
        settingsNBT.putBoolean("animal_attack", settings.animalEntityAttack)
        settingsNBT.putBoolean("another_attack", settings.anotherEntityAttack)
        settingsNBT.putBoolean("display_particle_only_trigger", settings.displayParticleOnlyTrigger)
        settingsNBT.putDouble("trigger_range", settings.triggerRange)
        nbt.put("settings", settingsNBT)
        nbt.putUuid("formation_uuid", formation.uuid)
    }

    override fun readNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.readNbt(nbt, registryLookup)
        formation.world = world
        formation.scale = FormationScale.valueOf(nbt.getString("scale"))
        val settings = formation.settings
        val settingsNBT = nbt.getCompound("settings")
        settings.playerEntityAttack = settingsNBT.getBoolean("player_attack")
        settings.hostileEntityAttack = settingsNBT.getBoolean("hostile_attack")
        settings.animalEntityAttack = settingsNBT.getBoolean("animal_attack")
        settings.anotherEntityAttack = settingsNBT.getBoolean("another_attack")
        settings.displayParticleOnlyTrigger = settingsNBT.getBoolean("display_particle_only_trigger")
        settings.triggerRange = settingsNBT.getDouble("trigger_range")
        val formationUUID = nbt.getUuid("formation_uuid")
        formation.uuid = formationUUID
        val ownerUUID = nbt.getString("owner_uuid") ?: return
        if (ownerUUID != "null") {
            formation.owner = UUID.fromString(ownerUUID)
        }
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener?> {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound? {
        return createNbt(registryLookup)
    }

    val formation = CrystalFormation(world, null, pos.toCenterPos())
    fun tick(
        world: World,
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
        markDirty()
    }
}