package cn.coostack.usefulmagic.meteorite

import cn.coostack.cooparticlesapi.extend.ofFloored
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.phys.Vec3


class MeteoriteFallingBlockRenderer(ctx: EntityRendererProvider.Context) :
    EntityRenderer<MeteoriteFallingBlockEntity>(ctx) {


    private val blockRenderManager: BlockRenderDispatcher = ctx.blockRenderDispatcher

    init {
        shadowRadius = 0.5f
    }

    override fun render(
        entity: MeteoriteFallingBlockEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int
    ) {
        val blockState = entity.blockState
        if (blockState.renderShape != RenderShape.MODEL) return

        val world = entity.level()
        matrices.pushPose()

        // 应用精确位置偏移（包含小数部分）
        matrices.translate(
            -0.5,
            0.0,
            -0.5
        )

        // 应用自定义旋转
        matrices.translate(0.5, 0.5, 0.5) // 移动到方块中心

        // 获取近似 BlockPos（用于光照计算）
        val pos = ofFloored(Vec3(entity.x, entity.y, entity.z))

        blockRenderManager.modelRenderer.tesselateBlock(
            world,
            blockRenderManager.getBlockModel(blockState),
            blockState,
            pos,
            matrices,
            vertexConsumers.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockState)),
            false,
            RandomSource.create(),
            blockState.getSeed(entity.startPos),
            OverlayTexture.NO_OVERLAY

        )
        matrices.popPose()
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    override fun getTextureLocation(p0: MeteoriteFallingBlockEntity): ResourceLocation {
        return TextureAtlas.LOCATION_BLOCKS
    }

}