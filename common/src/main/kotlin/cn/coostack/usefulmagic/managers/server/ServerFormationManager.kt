package cn.coostack.usefulmagic.managers.server

import cn.coostack.usefulmagic.formation.api.BlockFormation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import java.util.UUID

/**
 * 服务器使用
 */
object ServerFormationManager {
    val activeFormations = HashMap<UUID, BlockFormation>()
    fun onFormationActive(formation: BlockFormation) {
        activeFormations[formation.uuid] = formation
    }

    fun checkPosInFormationRange(pos: Vec3, world: ServerLevel): Boolean {
        return activeFormations.values.any {
            val distance = it.formationCore.distanceTo(pos)
            it.getFormationTriggerRange() >= distance && world == it.world
        }
    }

    fun getFormationFromPos(pos: Vec3, world: ServerLevel): BlockFormation? {
        return activeFormations.values.firstOrNull {
            val distance = it.formationCore.distanceTo(pos)
            it.getFormationTriggerRange() >= distance && world == it.world
        }
    }

    fun getActive(uuid: UUID): BlockFormation? = activeFormations[uuid]


    fun removeNotActiveFormations() {
        val iterator = activeFormations.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val formation = entry.value
            var removed = false
            if (!formation.isActiveFormation()) {
                iterator.remove()
                removed = true
            }
            if (formation.chunkLoaded() && !formation.canBeFormation() && !removed) {
                iterator.remove()
            }
        }
    }
}