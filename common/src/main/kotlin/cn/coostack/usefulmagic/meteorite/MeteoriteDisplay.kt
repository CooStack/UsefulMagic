package cn.coostack.usefulmagic.meteorite

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.display.handle.DisplayEntityHelper
import cn.coostack.cooparticlesapi.display.DisplayEntity
import cn.coostack.cooparticlesapi.extend.unaryMinus
import cn.coostack.cooparticlesapi.utils.MinecraftRendererUtil
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f

@CooAutoRegister
class MeteoriteDisplay(pos: Vec3, world: Level?) : DisplayEntity(pos, world) {
    init {
        manageRotation = false
    }

    @CodecField
    var state = Blocks.NETHERRACK.defaultBlockState()

    override fun render(
        view: Matrix4f,
        proj: Matrix4f,
        modelMatrixStack: PoseStack,
        buffer: MultiBufferSource,
        delta: Float,
        camera: Camera
    ) {
        val scale = scale(delta)
        MinecraftRendererUtil.applyAtPoint(
            renderCenterOffset(),
            modelMatrixStack
        ) {
            MinecraftRendererUtil.applyRotation(this, yaw(delta), pitch(delta), roll(delta))
            scale(scale, scale, scale)
        }
        val block = state.block
        val itemRenderer = Minecraft.getInstance().itemRenderer

        val stack = block.asItem().defaultInstance
        val model = itemRenderer.getModel(
            stack, world, null, 1
        )
        MinecraftRendererUtil.renderItemModel(
            itemRenderer, stack, modelMatrixStack, model, LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY, buffer.getBuffer(RenderType.cutout())
        )
    }

    override fun transformOffset(): Vec3 {
        return -renderCenterOffset()
    }

    override fun getCodec(): StreamCodec<FriendlyByteBuf, DisplayEntity> {
        return DisplayEntityHelper.generateCodec(this)
    }
}
