package cn.coostack.usefulmagic.blocks.entity

import cn.coostack.cooparticlesapi.renderer.client.AlphasVertexConsumers
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemDisplayContext
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
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        val itemRenderer = Minecraft.getInstance().itemRenderer
        val world = entity.level ?: return
        val time = world.gameTime
        val angle = (PI / 1800 * ((time + tickDelta) * 50 % 3600)).toFloat()
        val stack = entity.stack

        shouldPlaceLocations.forEach {
            val blockEntity =
                world.getBlockEntity(entity.blockPos.offset(it.x.toInt(), it.y.toInt(), it.z.toInt()))

            if (blockEntity is AltarBlockEntity) {
                return@forEach
            }
            handleAltarBlockRender(it, matrices, vertexConsumers, light, overlay)
        }

        val e = world.getBlockEntity(entity.blockPos.offset(up))
        if (e !is MagicCoreBlockEntity) {
            handleMagicCoreRender(upRelative, matrices, vertexConsumers, light, overlay)
        }

        if (stack.isEmpty) {
            return
        }
        matrices.translate(0.5, 1.0 + 0.125 * sin(angle), 0.5)
        matrices.mulPose(Quaternionf().rotateY(angle))
        matrices.scale(0.75f, 0.75f, 0.75f)
        itemRenderer.renderStatic(
            stack,
            ItemDisplayContext.GROUND,
            LightTexture.FULL_BLOCK,
            overlay,
            matrices,
            vertexConsumers,
            entity.level,
            0
        )
    }

    fun handleAltarBlockRender(
        relative: RelativeLocation,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        matrices.translate(relative.x, relative.y, relative.z)
        val state = UsefulMagicBlocks.ALTAR_BLOCK.get().defaultBlockState()
        val buffer = AlphasVertexConsumers((255 * 0.5).toInt(), vertexConsumers.getBuffer(RenderType.translucent()))
        Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(
            matrices.last(),
            buffer,
            state,
            Minecraft.getInstance().blockRenderer.blockModelShaper.getBlockModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
        matrices.translate(-relative.x, -relative.y, -relative.z)
    }

    fun handleMagicCoreRender(
        relative: RelativeLocation,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        matrices.translate(relative.x, relative.y, relative.z)
        val state = UsefulMagicBlocks.MAGIC_CORE.get().defaultBlockState()
        val buffer = AlphasVertexConsumers((255 * 0.5).toInt(), vertexConsumers.getBuffer(RenderType.translucent()))
        Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(
            matrices.last(),
            buffer,
            state,
            Minecraft.getInstance().blockRenderer.blockModelShaper.getBlockModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
        matrices.translate(-relative.x, -relative.y, -relative.z)
    }

}