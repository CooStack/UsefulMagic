package cn.coostack.usefulmagic.entity.util.phases

import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.*
import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.sub.EyeFollowOwnerPhase
import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.sub.EyeFollowTargetPhase
import cn.coostack.usefulmagic.entity.custom.dragon.eye.phases.sub.EyeSuicidePhase
import cn.coostack.usefulmagic.entity.custom.dragon.phases.*
import net.minecraft.world.entity.LivingEntity
import java.util.function.Supplier

/**
 * 负责反序列化成Manager的内容
 */
object PhaseRegistries {

    @JvmStatic
    private val registryMap = HashMap<String, PhaseHolder<out LivingEntity>>()

    @JvmStatic
    fun <T : LivingEntity> register(id: String, supplier: Supplier<PhaseDefinition<T>>): PhaseHolder<T> {
        if (hasRegistered(id)) {
            throw IllegalArgumentException("$id is already registered!")
        }
        val res = PhaseHolder(id, supplier)
        registryMap[id] = res
        return res
    }


    @JvmStatic
    fun hasRegistered(id: String): Boolean {
        return registryMap.containsKey(id)
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : LivingEntity> build(id: String): PhaseDefinition<T>? {
        return registryMap[id]?.get() as? PhaseDefinition<T>
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : LivingEntity> buildOrThrow(id: String): PhaseDefinition<T> {
        return registryMap[id]?.get() as? PhaseDefinition<T> ?: throw IllegalArgumentException("未注册的ID$id")
    }


    @JvmStatic
    fun init() {
        arrayOf<PhaseHolder<out LivingEntity>>(
            DragonCrossFlightPhase.HOLDER,
            DragonHoverFlightPhase.HOLDER,
            DragonImpactSkillPhase.HOLDER,
            DragonLookAndKeepDistancePhase.HOLDER,
            DragonNonePhase.HOLDER,
            DragonOrbitFlightPhase.HOLDER,
            DragonOrbitCloserFlightPhase.HOLDER,
            DragonSimpleFlightPhase.HOLDER,
            DragonWalkPhase.HOLDER,
            EyeCombatFlightPhase.HOLDER,
            EyeHoverPhase.HOLDER,
            EyeKeepDistancePhase.HOLDER,
            EyeOrbitCloserPhase.HOLDER,
            TeleportRandomlyPhase.HOLDER,
            EyeFollowOwnerPhase.HOLDER,
            EyeSuicidePhase.HOLDER,
            EyeFollowTargetPhase.HOLDER,
        )
    }
}
