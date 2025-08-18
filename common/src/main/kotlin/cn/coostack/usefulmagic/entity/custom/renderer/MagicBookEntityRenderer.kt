package cn.coostack.usefulmagic.entity.custom.renderer

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.MagicBookEntityModel
import cn.coostack.usefulmagic.entity.UsefulMagicEntityLayers
import cn.coostack.usefulmagic.entity.UsefulMagicEntityTypes
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation

class MagicBookEntityRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<MagicBookEntity, MagicBookEntityModel>(
        context, MagicBookEntityModel(context.bakeLayer(UsefulMagicEntityLayers.MAGIC_BOOK_ENTITY_LAYER)), 2f
    ) {
    companion object {
        @JvmStatic
        private val TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/entity/magic_book_entity.png")
    }

    override fun getTextureLocation(entity: MagicBookEntity): ResourceLocation {
        return TEXTURE
    }

    override fun render(
        entity: MagicBookEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        poseStack.pushPose()
        poseStack.scale(4f, 4f, 4f)
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
        poseStack.popPose()
    }

}