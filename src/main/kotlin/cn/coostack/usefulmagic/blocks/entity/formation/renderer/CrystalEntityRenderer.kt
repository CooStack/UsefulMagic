package cn.coostack.usefulmagic.blocks.entity.formation.renderer

import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.formation.api.FormationCrystal
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import org.joml.Quaternionf
import kotlin.math.PI
import kotlin.math.sin

class CrystalEntityRenderer : BlockEntityRenderer<BlockEntity> {
    override fun render(
        entity: BlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {

        if (entity !is FormationCrystal) {
            return
        }

        val world = entity.world ?: return
        val time = world.time
        val angle = (PI / 1800 * ((time + tickDelta) * 50 % 3600)).toFloat()
        val state = entity.cachedState
        if (entity.activeFormation == null) {
            doNormal(matrices, vertexConsumers, state, light, overlay)
            return
        }
        if (!entity.activeFormation!!.isActiveFormation()) {
            doNormal(matrices, vertexConsumers, state, light, overlay)
            entity.activeFormation = null
            return
        }
        val core = world.getBlockEntity(BlockPos.ofFloored(entity.activeFormation!!.formationCore))

        if (core !is FormationCoreBlockEntity) {
            doNormal(matrices, vertexConsumers, state, light, overlay)
            entity.activeFormation = null
            return
        }

        matrices.translate(0.5, 0.0, 0.5)
        matrices.multiply(Quaternionf().rotateY(angle))
        matrices.translate(-0.5, 0.0, -0.5)
        matrices.translate(0.0, 0.125 * sin(angle), 0.0)

        MinecraftClient.getInstance().blockRenderManager.modelRenderer.render(
            matrices.peek(),
            vertexConsumers.getBuffer(RenderLayer.getTranslucent()),
            state,
            MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
    }


    fun doNormal(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        state: BlockState,
        light: Int,
        overlay: Int
    ) {
        MinecraftClient.getInstance().blockRenderManager.modelRenderer.render(
            matrices.peek(),
            vertexConsumers.getBuffer(RenderLayer.getTranslucent()),
            state,
            MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
    }
}