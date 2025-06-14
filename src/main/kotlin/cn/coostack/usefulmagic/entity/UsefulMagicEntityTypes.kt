package cn.coostack.usefulmagic.entity

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object UsefulMagicEntityTypes {

    val MAGIC_BOOK_ENTITY_TYPE = Registry.register(
        Registries.ENTITY_TYPE, Identifier.of(UsefulMagic.MOD_ID, "magic_book_entity"),
        EntityType.Builder.create(::MagicBookEntity, SpawnGroup.CREATURE)
            .dimensions(4f, 2f).build()
    )

    fun init(){

    }

}