package cn.coostack.usefulmagic.meteorite

import net.minecraft.block.BlockRenderType
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.BlockRenderManager
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random

class MeteoriteFallingBlockRenderer(ctx: EntityRendererFactory.Context) :
    EntityRenderer<MeteoriteFallingBlockEntity>(ctx) {


    private val blockRenderManager: BlockRenderManager = ctx.blockRenderManager

    init {
        shadowRadius = 0.5f
    }

    override fun render(
        entity: MeteoriteFallingBlockEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val blockState = entity.blockState
        if (blockState.renderType != BlockRenderType.MODEL) return

        val world = entity.world
        matrices.push()

        // 应用精确位置偏移（包含小数部分）
        matrices.translate(
            -0.5,
            0.0,
            -0.5
        )

        // 应用自定义旋转
        matrices.translate(0.5, 0.5, 0.5) // 移动到方块中心

        // 获取近似 BlockPos（用于光照计算）
        val pos = BlockPos.ofFloored(entity.x, entity.y, entity.z)

        blockRenderManager.modelRenderer.render(
            world,
            blockRenderManager.getModel(blockState),
            blockState,
            pos,
            matrices,
            vertexConsumers.getBuffer(RenderLayers.getMovingBlockLayer(blockState)),
            false,
            Random.create(),
            blockState.getRenderingSeed(entity.fallingBlockPos),
            OverlayTexture.DEFAULT_UV

        )

        matrices.pop()
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }


    override fun getTexture(entity: MeteoriteFallingBlockEntity): Identifier {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}