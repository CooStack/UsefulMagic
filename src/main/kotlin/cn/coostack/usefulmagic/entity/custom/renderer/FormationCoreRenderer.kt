package cn.coostack.usefulmagic.entity.custom.renderer

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.custom.formation.FormationCoreEntity
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.renderer.shader.TestShader
import com.mojang.blaze3d.platform.GlConst.GL_TRIANGLES
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL33


class FormationCoreRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<FormationCoreEntity>(ctx) {
    val shader = TestShader()
        .apply {
            this.vertexShader = Identifier.of(UsefulMagic.MOD_ID, "shaders/test/test_vertex.vsh")
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
        val transform = matrices.peek().positionMatrix
        val transLoc = shader.getUniformLocation("transform")
        val p = entity.pos.toVector3f()
        GL33.glUniformMatrix4fv(transLoc, false, transform.get(BufferUtils.createFloatBuffer(16)))
        val modelViewMat = Matrix4f().translate(-p.x, -p.y, -p.z).get(BufferUtils.createFloatBuffer(16))
        val viewMatLoc = shader.getUniformLocation("ModelViewMat")
        GL33.glUniformMatrix4fv(viewMatLoc, false, modelViewMat)
        val consumer =
            vertexConsumers.getBuffer(RenderLayers.getItemLayer(UsefulMagicItems.WOODEN_WAND.defaultStack, false))
        vertex(consumer, -0.5f, -0.5f, 0f, 0f, 0f)
        vertex(consumer, 0.5f, -0.5f, 0f, 1f, 0f)
        vertex(consumer, -0.5f, 0.5f, 0f, 0f, 1f)
        vertex(consumer, 0.5f, -0.5f, 0f, 1f, 0f)
        vertex(consumer, -0.5f, 0.5f, 0f, 0f, 1f)
        vertex(consumer, 0.5f, 0.5f, 0f, 1f, 1f)

        GL33.glEnable(GL33.GL_DEPTH_TEST)
        GL33.glDepthFunc(GL33.GL_LEQUAL)
        shader.unbind()
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    private fun vertex(consumer: VertexConsumer, x: Float, y: Float, z: Float, u: Float, v: Float) {
        consumer.vertex(
            x,
            y,
            z,
            0xFFFFFFFFU.toInt(),
            u,
            v,
            1,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            1f,
            1f,
            1f
        )
    }

    override fun getTexture(entity: FormationCoreEntity?): Identifier? {
        return null
    }
}