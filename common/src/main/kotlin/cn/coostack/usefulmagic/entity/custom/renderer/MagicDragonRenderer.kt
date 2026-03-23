package cn.coostack.usefulmagic.entity.custom.renderer

import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.model.MagicDragonModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import software.bernie.geckolib.renderer.GeoEntityRenderer

class MagicDragonRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<MagicDragonEntity>(context, MagicDragonModel()) {
    init {
        withScale(2f)
        shadowRadius = 2.4f
    }

    override fun getRenderType(
        animatable: MagicDragonEntity,
        texture: ResourceLocation,
        bufferSource: MultiBufferSource?,
        partialTick: Float
    ): RenderType? {
        return RenderType.entityTranslucent(texture)
    }

    override fun render(
        entity: MagicDragonEntity,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, LightTexture.FULL_BRIGHT)
    }
}
