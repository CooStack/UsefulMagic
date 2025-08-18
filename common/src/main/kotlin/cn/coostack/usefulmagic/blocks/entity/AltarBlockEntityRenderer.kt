package cn.coostack.usefulmagic.blocks.entity

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.world.item.ItemDisplayContext
import org.joml.Quaternionf
import kotlin.math.PI
import kotlin.math.sin

class AltarBlockEntityRenderer : BlockEntityRenderer<AltarBlockEntity> {
    override fun render(
        entity: AltarBlockEntity,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        val itemRenderer = Minecraft.getInstance().itemRenderer
        val world = entity.level ?: return
        val time = world.gameTime
        val stack = entity.stack
        if (stack.isEmpty) {
            return
        }
        val angle = (PI / 1800 * ((time + tickDelta) * 50 % 3600)).toFloat()
        matrices.translate(0.5, 0.75 + 0.125 * sin(angle), 0.5)
        matrices.mulPose(Quaternionf().rotateY(angle))
        matrices.scale(0.75f, 0.75f, 0.75f)
//        val lightLevel = WorldRenderer.getLightmapCoordinates(world, entity.pos.up())
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
}