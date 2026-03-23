package cn.coostack.usefulmagic.sounds

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredSoundEvent
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent

object UsefulMagicSoundEvents {
    val soundEvents = mutableListOf<CommonDeferredSoundEvent>()

    @JvmStatic
    val ELECTRIC_EFFECT = register("electric_effect")

    @JvmStatic
    val MAGIC_ACTIVATE = register("magic_activate")

    @JvmStatic
    val MAGIC_EXTEND = register("magic_extend")

    @JvmStatic
    val MAGIC_SWORD = register("magic_sword")

    @JvmStatic
    val SKY_FALLING_MAGIC_IDLE = register("sky_falling_magic_idle")

    @JvmStatic
    val SKY_FALLING_MAGIC_START = register("sky_falling_magic_start")

    @JvmStatic
    val DEFEND_SHIELD_HIT = register("defend_shield_hit")

    @JvmStatic
    val MAGIC_EXPLODE = register("magic_explode")
    @JvmStatic
    val STAR = register("star")

    @JvmStatic
    val METEOR_FALL_FAR = register("meteor_fall_far")

    @JvmStatic
    val METEOR_FALL_NEAR = register("meteor_fall_near")

    @JvmStatic
    val METEOR_IMPACT = register("meteor_impact")

    @JvmStatic
    val LASER_CHARGE_UP = register("laser_charge_up")

    @JvmStatic
    val LASER_START = register("laser_start")

    @JvmStatic
    val LASER_LOOP = register("laser_loop")

    @JvmStatic
    val LASER_OBLITERATION = register("laser_obliteration")

    @JvmStatic
    val EYE_TELEPORT = register("eye_teleport")

    @JvmStatic
    val EYE_ACTIVE = register("eye_active")

    @JvmStatic
    val EYE_SPAWN = register("eye_spawn")

    @JvmStatic
    val DRAGON_SPAWN_MAGIC_LASER = register("dragon_spawn_magic_laser")

    @JvmStatic
    val DRAGON_HUGE_LASER_CHARGE_UP = register("dragon_huge_laser_charge_up")

    @JvmStatic
    val DRAGON_HUGE_LASER_SHOOT = register("dragon_huge_laser_shoot")

    @JvmStatic
    val LIQUID_BALL = register("liquid_ball")

    @JvmStatic
    val ROCK_LOOP = register("rock_loop")

    @JvmStatic
    val SMALL_LASER_SHOOT = register("small_laser_shoot")

    @JvmStatic
    val THORN_HIT = register("thorn_hit")

    @JvmStatic
    val DRAGON_MAGIC_ACTIVE = register("dragon_magic_active")

    @JvmStatic
    val DRAGON_REWIND = register("dragon_rewind")

    @JvmStatic
    val DRAGON_MAGIC_APPEAR = register("dragon_magic_appear")

    @JvmStatic
    fun register(name: String): CommonDeferredSoundEvent {
        val id = ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, name)

        val common = CommonDeferredSoundEvent(id) {
            SoundEvent.createVariableRangeEvent(id)
        }
        soundEvents.add(common)
        return common
    }

    fun init() {}
}
