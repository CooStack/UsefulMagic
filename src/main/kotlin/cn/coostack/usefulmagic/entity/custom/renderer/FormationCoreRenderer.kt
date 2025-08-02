package cn.coostack.usefulmagic.entity.custom.renderer

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.entity.custom.formation.FormationCoreEntity
import cn.coostack.usefulmagic.renderer.shader.TestShader
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL20


class FormationCoreRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<FormationCoreEntity>(ctx) {
    val shader = TestShader()
        .apply {
//            this.vertexShader = Identifier.of(UsefulMagic.MOD_ID, "shaders/test/test_vertex.vsh")
            this.fragmentShader = Identifier.of(UsefulMagic.MOD_ID, "shaders/test/test_frag.fsh")
        }

    init {
        shader.loadShader()
    }

    override fun render(
        entity: FormationCoreEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val renderer = MinecraftClient.getInstance().gameRenderer
        val camera = renderer.camera

        shader.bind()
        val timeLocation = shader.getUniformLocation("u_time")
        GL20.glUniform1f(timeLocation, System.currentTimeMillis() / 1000f)
        val pos = entity.pos.toVector3f()
        val entityPosLocation = shader.getUniformLocation("u_pos")
        GL20.glUniform3f(entityPosLocation, pos.x, pos.y, pos.z)
        val cameraLocation = shader.getUniformLocation("u_camera")
        val cameraPos = camera.pos.toVector3f()
        GL20.glUniform3f(cameraLocation, cameraPos.x, cameraPos.y, cameraPos.z)
        shader.unbind()
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    override fun getTexture(entity: FormationCoreEntity?): Identifier? {
        return null
    }
}