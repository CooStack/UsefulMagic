package cn.coostack.usefulmagic.damagetypes

import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

object UsefulMagicDamageSources {
    /**
     * 实体使用了魔法攻击
     *
     * @param world 发生攻击的世界
     * @param shooter 发射者 （释放魔法的人）
     * @param source 来源 （如果这个不是连锁魔法，那就是shooter）
     */
    @JvmStatic
    fun entityMagic(world: Level, shooter: LivingEntity, source: LivingEntity): DamageSource {
        return DamageSource(
            getOrThrow(world, UsefulMagicDamageTypes.MAGIC),
            shooter,
            source
        )
    }

    @JvmStatic
    fun entityDamage(world: Level, shooter: LivingEntity, source: LivingEntity): DamageSource {
        return entityMagic(world, shooter, source)
    }

    @JvmStatic
    fun getOrThrow(world: Level, key: ResourceKey<DamageType>): Holder<DamageType> {
        return world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key)
    }
}
