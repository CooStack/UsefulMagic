package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
import cn.coostack.cooparticlesapi.annotations.CooAutoRegisterRenderer
import cn.coostack.cooparticlesapi.renderer.pipeline.CooPipelines
import cn.coostack.cooparticlesapi.renderer.runtime.RenderEntityRenderer
import cn.coostack.cooparticlesapi.renderer.runtime.RenderInput
import cn.coostack.cooparticlesapi.renderer.shader.ShaderProgramBuilder
import cn.coostack.cooparticlesapi.renderer.shader.api.CooShaderProgram
import cn.coostack.cooparticlesapi.renderer.shader.api.glsl.GlShaderType
import cn.coostack.cooparticlesapi.renderer.shader.data.CooVertexFormat
import cn.coostack.cooparticlesapi.renderer.shader.data.VertexData
import cn.coostack.cooparticlesapi.renderer.shader.glsl.IdentifierShader
import cn.coostack.cooparticlesapi.renderer.shader.vertex.SimpleVertexBuffer
import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.blaze3d.systems.RenderSystem
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL33

/** 负责冲击波公告板实体的客户端材质、几何与默认 Pipeline 提交。 */
@CooAutoRegisterRenderer
class ShotWaveBillboardRenderEntityRenderer : RenderEntityRenderer<ShotWaveBillboardRenderEntity> {
    /** 冲击波只需直接世界绘制，不启用后处理。 */
    override val pipeline = CooPipelines.DEFAULT

    /** 根据当前缩放和透明度提交冲击波公告板。 */
    override fun render(input: RenderInput<ShotWaveBillboardRenderEntity>) {
        initStatic()
        val entity = input.entity
        val visibleAlpha = entity.currentAlpha(input.tickDelta)
        if (visibleAlpha <= 0.001F) {
            return
        }
        val scale = entity.currentScale(input.tickDelta)
        if (scale <= 0.001F) {
            return
        }
        renderBillboard(
            entity = entity,
            modelMatrix = input.modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            inverseViewRotationMatrix = Matrix3f(input.viewMatrix).invert(),
            scale = scale,
            visibleAlpha = visibleAlpha,
            time = entity.currentTimeline(input.tickDelta),
        )
    }

    /** 使用冲击波 shader 绘制面向相机的四边形。 */
    private fun renderBillboard(
        entity: ShotWaveBillboardRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        inverseViewRotationMatrix: Matrix3f,
        scale: Float,
        visibleAlpha: Float,
        time: Float,
    ) {
        val renderState = UsefulMagicRenderState.capture()
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
        RenderSystem.depthMask(false)
        try {
            shotWaveShader.useOnContext {
                val texturePath = if (entity.useWave2Texture) {
                    "textures/effect/shot_wave_2.png"
                } else {
                    "textures/effect/shot_wave.png"
                }
                RenderSystem.setShaderTexture(
                    0,
                    ofID(UsefulMagic.MOD_ID, texturePath),
                )
                setMatrix4("modelMatrix", modelMatrix)
                setMatrix4("viewMatrix", viewMatrix)
                setMatrix4("projMatrix", projMatrix)
                setMatrix3f("inverseViewRotationMatrix", inverseViewRotationMatrix)
                setFloat2("scale", Vector2f(scale, scale))
                setFloat("roll", entity.roll + time * entity.rollSpeed)
                setFloat("alpha", visibleAlpha)
                setFloat("time", time)
                setInt("shotWaveTexture", 0)
                shotWaveVertexBuffer.draw()
            }
        } finally {
            renderState.restore()
        }
    }

    /** 管理冲击波 renderer 共享的 GPU 资源和网格。 */
    companion object {
        /** 客户端重复使用的公告板顶点缓冲。 */
        private lateinit var shotWaveVertexBuffer: SimpleVertexBuffer
        /** 客户端重复使用的冲击波 shader。 */
        private lateinit var shotWaveShader: CooShaderProgram
        /** 标记 renderer 的 GPU 资源是否已创建。 */
        private var initialized = false

        /** 在渲染线程首次使用时创建 GPU 资源。 */
        private fun initStatic() {
            if (initialized) {
                return
            }
            UsefulMagicRenderState.preserve {
                shotWaveVertexBuffer = SimpleVertexBuffer().apply {
                    init()
                    setVertexes(buildQuadVertices(), CooVertexFormat.POINT_TEXTURE_UV_FORMAT)
                }
            }
            shotWaveShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID, "core/vsh/shot_wave_billboard.vsh"),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID, "core/fsh/shot_wave_billboard.fsh"),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            shotWaveShader.init()
            initialized = true
        }

        /**
         * 构建带完整 UV 的单位公告板。
         *
         * @return 可上传到共享顶点缓冲的公告板顶点
         */
        private fun buildQuadVertices(): List<VertexData> {
            val extent = 1F
            return listOf(
                VertexData(Vector3f(-extent, -extent, 0F), Vector4f(), Vector2f(0F, 0F)),
                VertexData(Vector3f(extent, -extent, 0F), Vector4f(), Vector2f(1F, 0F)),
                VertexData(Vector3f(extent, extent, 0F), Vector4f(), Vector2f(1F, 1F)),
                VertexData(Vector3f(-extent, -extent, 0F), Vector4f(), Vector2f(0F, 0F)),
                VertexData(Vector3f(extent, extent, 0F), Vector4f(), Vector2f(1F, 1F)),
                VertexData(Vector3f(-extent, extent, 0F), Vector4f(), Vector2f(0F, 1F)),
            )
        }
    }
}
