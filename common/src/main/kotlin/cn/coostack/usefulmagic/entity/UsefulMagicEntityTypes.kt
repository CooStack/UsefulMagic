package cn.coostack.usefulmagic.entity

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredEntityType
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.entity.custom.formation.FormationCoreEntity
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockEntity
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import java.util.function.Supplier

object UsefulMagicEntityTypes {

    val entityTypes = mutableListOf<CommonDeferredEntityType<*>>()

    val MAGIC_BOOK_ENTITY_TYPE: CommonDeferredEntityType<MagicBookEntity> = register("magic_book_entity") {
        EntityType.Builder.of(::MagicBookEntity, MobCategory.MONSTER)
            .sized(4f, 2f).build("magic_book_entity")
    }

    val FORMATION_CORE_ENTITY: CommonDeferredEntityType<FormationCoreEntity> = register("formation_core_entity") {
        EntityType.Builder.of(::FormationCoreEntity, MobCategory.CREATURE)
            .sized(4f, 2f).build("formation_core_entity")
    }

    val METEORITE_ENTITY: CommonDeferredEntityType<MeteoriteFallingBlockEntity> = register(
        "meteorite_falling_block"
    ) {
        EntityType.Builder.of<MeteoriteFallingBlockEntity>(::MeteoriteFallingBlockEntity, MobCategory.MISC)
            .sized(0.98f, 0.98f)
            .clientTrackingRange(10)
            .updateInterval(1)
            .build("meteorite_falling_block")
    }

    fun <T : Entity> register(id: String, type: Supplier<EntityType<T>>): CommonDeferredEntityType<T> {
        val location = ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id)
        val common = CommonDeferredEntityType(location, type)
        entityTypes.add(common)
        return common
    }

    fun init() {

    }

}