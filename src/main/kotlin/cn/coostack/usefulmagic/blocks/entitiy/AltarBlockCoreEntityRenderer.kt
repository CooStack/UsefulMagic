package cn.coostack.usefulmagic.blocks.entitiy

import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.blocks.AltarBlock
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.Items
import org.joml.Quaternionf
import kotlin.math.PI
import kotlin.math.sin

class AltarBlockCoreEntityRenderer : BlockEntityRenderer<AltarBlockCoreEntity> {
    override fun render(
        entity: AltarBlockCoreEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val itemRenderer = MinecraftClient.getInstance().itemRenderer
        val world = entity.world ?: return
        val time = world.time
        val angle = (PI / 1800 * ((time + tickDelta) * 50 % 3600)).toFloat()
        val stack = entity.stack
//        handleAltarBlockRender(RelativeLocation(2.0, 0.0, 0.0), matrices, vertexConsumers, light, overlay)
        if (stack.isEmpty) {
            return
        }
        matrices.translate(0.5, 1.0 + 0.125 * sin(angle), 0.5)
        matrices.multiply(Quaternionf().rotateY(angle))
        matrices.scale(0.75f, 0.75f, 0.75f)
//        val lightLevel = WorldRenderer.getLightmapCoordinates(world, entity.pos.up())
        itemRenderer.renderItem(
            stack,
            ModelTransformationMode.GROUND,
            LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE,
            overlay,
            matrices,
            vertexConsumers,
            entity.world,
            0
        )
    }

    fun handleAltarBlockRender(
        relative: RelativeLocation,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        matrices.translate(relative.x, relative.y, relative.z)
        val state = UsefulMagicBlocks.ALTAR_BLOCK.defaultState
//        val buffer = vertexConsumers.getBuffer(RenderLayers.getEntityBlockLayer(state, false))
        val buffer = vertexConsumers.getBuffer(RenderLayer.getTranslucent())
        MinecraftClient.getInstance().blockRenderManager.modelRenderer.render(
            matrices.peek(),
            vertexConsumers.getBuffer(RenderLayer.getTranslucent()),
            state,
            MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
        matrices.translate(-relative.x, -relative.y, -relative.z)
    }


}