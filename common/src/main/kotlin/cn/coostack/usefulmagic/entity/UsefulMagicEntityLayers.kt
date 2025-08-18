package cn.coostack.usefulmagic.entity

import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.resources.ResourceLocation

object UsefulMagicEntityLayers {

    @JvmField
    val MAGIC_BOOK_ENTITY_LAYER = ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "magic_book_entity"),
        "main"
    )

}