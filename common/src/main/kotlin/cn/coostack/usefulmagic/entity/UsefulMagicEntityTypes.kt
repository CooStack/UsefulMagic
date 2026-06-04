package cn.coostack.usefulmagic.entity

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredEntityType
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.custom.book.MagicBookEntity
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicHeartEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicSubEyeEntity
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

    val MAGIC_DRAGON_ENTITY_TYPE: CommonDeferredEntityType<MagicDragonEntity> = register("magic_dragon_entity") {
        EntityType.Builder.of(::MagicDragonEntity, MobCategory.CREATURE)
            .sized(MagicDragonEntity.COLLISION_WIDTH, MagicDragonEntity.COLLISION_HEIGHT)
            .clientTrackingRange(128)
            .build("magic_dragon_entity")
    }


    val MAGIC_EYE_ENTITY_TYPE: CommonDeferredEntityType<MagicEyeEntity> = register("magic_eye_entity") {
        EntityType.Builder.of(::MagicEyeEntity, MobCategory.CREATURE)
            .sized(1.8f, 1.8f)
            .clientTrackingRange(128)
            .build("magic_eye_entity")
    }

    val MAGIC_SUB_EYE_ENTITY_TYPE: CommonDeferredEntityType<MagicSubEyeEntity> = register("magic_sub_eye_entity") {
        EntityType.Builder.of(::MagicSubEyeEntity, MobCategory.CREATURE)
            .sized(0.81f, 0.81f)
            .clientTrackingRange(128)
            .build("magic_sub_eye_entity")
    }

    val MAGIC_HEART_ENTITY_TYPE: CommonDeferredEntityType<MagicHeartEntity> = register("magic_heart_entity") {
        EntityType.Builder.of(::MagicHeartEntity, MobCategory.CREATURE)
            .sized(2.43f, 2.43f)
            .clientTrackingRange(128)
            .build("magic_heart_entity")
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
