package cn.coostack.usefulmagic.blocks.entitiy

import cn.coostack.usefulmagic.blocks.AltarBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
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
}