package cn.coostack.usefulmagic.entity

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.util.Identifier

object UsefulMagicEntityLayers {

    @JvmField
    val MAGIC_BOOK_ENTITY_LAYER = EntityModelLayer(
        Identifier.of(UsefulMagic.MOD_ID, "magic_book_entity"), "main"
    )

}