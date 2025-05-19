package cn.coostack.usefulmagic.blocks.entitiy

import cn.coostack.usefulmagic.blocks.AltarBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.Items
import org.joml.Quaternionf
import kotlin.math.PI
import kotlin.math.sin

class MagicCoreBlockEntityRenderer : BlockEntityRenderer<MagicCoreBlockEntity> {
    override fun render(
        entity: MagicCoreBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val world = entity.world ?: return
        val time = world.time
        var angle = (PI / 3600 * ((time + tickDelta) * 50)).toFloat()
        val state = entity.cachedState
        if (entity.crafting) {
            angle *= 2.5f
        }
        if (entity.currentMana != 0 || entity.maxMana != 0 || entity.currentReviveSpeed != 0) {
            matrices.translate(0.5, 0.5, 0.5)
            matrices.multiply(Quaternionf().rotateXYZ(angle, angle, angle))
            matrices.translate(-0.5, -0.5, -0.5)
        }
        val light = if (entity.crafting) {
            LightmapTextureManager.pack(15, 15)
        } else {
            WorldRenderer.getLightmapCoordinates(entity.world!!, entity.pos.up()) // 动态光照
        }
        MinecraftClient.getInstance().blockRenderManager.modelRenderer.render(
            matrices.peek(),
            vertexConsumers.getBuffer(RenderLayers.getEntityBlockLayer(state, true)),
            state,
            MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
    }
}