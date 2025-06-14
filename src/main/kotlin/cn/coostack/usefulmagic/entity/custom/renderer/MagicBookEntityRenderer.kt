package cn.coostack.usefulmagic.entity.custom.renderer

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.MagicBookEntityModel
import cn.coostack.usefulmagic.entity.UsefulMagicEntityLayers
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

class MagicBookEntityRenderer(context: EntityRendererFactory.Context) :
    MobEntityRenderer<MagicBookEntity, MagicBookEntityModel>(
        context, MagicBookEntityModel(context.getPart(UsefulMagicEntityLayers.MAGIC_BOOK_ENTITY_LAYER)), 2f
    ) {
    companion object {
        @JvmStatic
        private val TEXTURE = Identifier.of(UsefulMagic.MOD_ID, "textures/entity/magic_book_entity.png")
    }

    override fun getTexture(entity: MagicBookEntity): Identifier {
        return TEXTURE
    }

}