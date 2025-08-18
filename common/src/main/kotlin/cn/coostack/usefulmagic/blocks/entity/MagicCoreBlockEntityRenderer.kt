package cn.coostack.usefulmagic.blocks.entity

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer

import org.joml.Quaternionf
import kotlin.math.PI

class MagicCoreBlockEntityRenderer : BlockEntityRenderer<MagicCoreBlockEntity> {
    override fun render(
        entity: MagicCoreBlockEntity,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        val world = entity.level ?: return
        val time = world.gameTime
        var angle = (PI / 3600 * ((time + tickDelta) * 50)).toFloat()
        val state = entity.blockState
        if (entity.crafting) {
            angle *= 2.5f
        }
        if (entity.currentMana != 0 || entity.maxMana != 0 || entity.currentReviveSpeed != 0) {
            matrices.translate(0.5, 0.5, 0.5)
            matrices.mulPose(Quaternionf().rotateXYZ(angle, angle, angle))
            matrices.translate(-0.5, -0.5, -0.5)
        }
        val light = if (entity.crafting) {
            LightTexture.pack(15, 15)
        } else {
            LevelRenderer.getLightColor(entity.level!!, entity.blockPos.below()) // 动态光照
        }
        val vertex = vertexConsumers.getBuffer(RenderType.translucent())
        Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(
            matrices.last(),
            vertex,
            state,
            Minecraft.getInstance().modelManager.blockModelShaper.getBlockModel(state),
            1f, 1f, 1f,
            light,
            overlay
        )
    }
}