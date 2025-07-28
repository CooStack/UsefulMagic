package cn.coostack.usefulmagic.blocks.entity

import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.renderer.AlphasVertexConsumers
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import org.joml.Quaternionf
import kotlin.math.PI
import kotlin.math.sin

class AltarBlockCoreEntityRenderer : BlockEntityRenderer<AltarBlockCoreEntity> {
    val shouldPlaceLocations = listOf(
        RelativeLocation(3.0, 0.0, 0.0),
        RelativeLocation(-3.0, 0.0, 0.0),
        RelativeLocation(0.0, 0.0, 3.0),
        RelativeLocation(0.0, 0.0, -3.0),
        RelativeLocation(2.0, 0.0, 2.0),
        RelativeLocation(2.0, 0.0, -2.0),
        RelativeLocation(-2.0, 0.0, 2.0),
        RelativeLocation(-2.0, 0.0, -2.0),
    )
    val up = BlockPos(0, 3, 0)
    val upRelative = RelativeLocation(0.0, 3.0, 0.0)
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

        shouldPlaceLocations.forEach {
            val blockEntity =
                world.getBlockEntity(entity.pos.add(it.x.toInt(), it.y.toInt(), it.z.toInt()))

            if (blockEntity is AltarBlockEntity) {
                return@forEach
            }
            handleAltarBlockRender(it, matrices, vertexConsumers, light, overlay)
        }

        val e = world.getBlockEntity(entity.pos.add(up))
        if (e !is MagicCoreBlockEntity) {
            handleMagicCoreRender(upRelative, matrices, vertexConsumers, light, overlay)
        }

        if (stack.isEmpty) {
            return
        }
        matrices.translate(0.5, 1.0 + 0.125 * sin(angle), 0.5)
        matrices.multiply(Quaternionf().rotateY(angle))
        matrices.scale(0.75f, 0.75f, 0.75f)
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
        val buffer = AlphasVertexConsumers((255 * 0.5).toInt(), vertexConsumers.getBuffer(RenderLayer.getTranslucent()))
        MinecraftClient.getInstance().blockRenderManager.modelRenderer.render(
            matrices.peek(),
            buffer,
            state,
            MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
        matrices.translate(-relative.x, -relative.y, -relative.z)
    }

    fun handleMagicCoreRender(
        relative: RelativeLocation,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        matrices.translate(relative.x, relative.y, relative.z)
        val state = UsefulMagicBlocks.MAGIC_CORE.defaultState
        val buffer = AlphasVertexConsumers((255 * 0.5).toInt(), vertexConsumers.getBuffer(RenderLayer.getTranslucent()))
        MinecraftClient.getInstance().blockRenderManager.modelRenderer.render(
            matrices.peek(),
            buffer,
            state,
            MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
        matrices.translate(-relative.x, -relative.y, -relative.z)
    }

}