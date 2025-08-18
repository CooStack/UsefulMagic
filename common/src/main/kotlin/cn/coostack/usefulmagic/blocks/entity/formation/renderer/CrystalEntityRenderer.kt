package cn.coostack.usefulmagic.blocks.entity.formation.renderer

import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.formation.api.FormationCrystal
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.joml.Quaternionf
import kotlin.math.PI
import kotlin.math.sin

class CrystalEntityRenderer : BlockEntityRenderer<BlockEntity> {
    override fun render(
        entity: BlockEntity,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        if (entity !is FormationCrystal) {
            return
        }

        val world = entity.level ?: return
        val time = world.gameTime
        val angle = (PI / 1800 * ((time + tickDelta) * 50 % 3600)).toFloat()
        val state = entity.blockState
        if (entity.activeFormation == null) {
            doNormal(matrices, vertexConsumers, state, light, overlay)
            return
        }
        if (!entity.activeFormation!!.isActiveFormation()) {
            doNormal(matrices, vertexConsumers, state, light, overlay)
            entity.activeFormation = null
            return
        }
        val core = world.getBlockEntity(ofFloored(entity.activeFormation!!.formationCore))
        if (core !is FormationCoreBlockEntity) {
            doNormal(matrices, vertexConsumers, state, light, overlay)
            entity.activeFormation = null
            return
        }

        matrices.translate(0.5, 0.0, 0.5)
        matrices.mulPose(Quaternionf().rotateY(angle))
        matrices.translate(-0.5, 0.0, -0.5)
        matrices.translate(0.0, 0.125 * sin(angle), 0.0)

        Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(
            matrices.last(),
            vertexConsumers.getBuffer(RenderType.translucent()),
            state,
            Minecraft.getInstance().modelManager.blockModelShaper.getBlockModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
    }


    fun doNormal(
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        state: BlockState,
        light: Int,
        overlay: Int
    ) {
        Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(
            matrices.last(),
            vertexConsumers.getBuffer(RenderType.translucent()),
            state,
            Minecraft.getInstance().modelManager.blockModelShaper.getBlockModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
    }

}