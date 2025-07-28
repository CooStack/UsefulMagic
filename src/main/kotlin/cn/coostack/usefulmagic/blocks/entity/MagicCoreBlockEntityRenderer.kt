package cn.coostack.usefulmagic.blocks.entity

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import org.joml.Quaternionf
import kotlin.math.PI

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
        val vertex = vertexConsumers.getBuffer(RenderLayer.getTranslucent())
        MinecraftClient.getInstance().blockRenderManager.modelRenderer.render(
            matrices.peek(),
            vertex,
            state,
            MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
    }
}