package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
import cn.coostack.cooparticlesapi.annotations.CooAutoRegisterRenderer
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
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL33
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** 爆炸光柱材质层使用的混合方式。 */
private enum class ExplosionBeamBlendMode {
    ALPHA,
    ADDITIVE,
}

/**
 * 描述爆炸光柱的一层材质参数。
 *
 * @property blendMode 绘制该层时使用的混合方式。
 * @property color shader 接收的颜色。
 * @property alpha 该层透明度。
 * @property brightness 该层亮度倍率。
 * @property rimPower 边缘光幂指数。
 * @property coreBias 中心亮部偏移。
 * @property highlightStrength 高光强度。
 * @property impactStrength 冲击纹理强度。
 * @property textureScale 纹理缩放。
 * @property textureSpeed 纹理流动速度。
 */
private data class ExplosionBeamPass(
    val blendMode: ExplosionBeamBlendMode,
    val color: Vector3f,
    val alpha: Float,
    val brightness: Float,
    val rimPower: Float,
    val coreBias: Float,
    val highlightStrength: Float,
    val impactStrength: Float,
    val textureScale: Float,
    val textureSpeed: Float,
)

/** 负责爆炸光柱的材质、圆柱几何体和 Pipeline 提交。 */
@CooAutoRegisterRenderer
class ExplosionBeamRenderEntityRenderer : RenderEntityRenderer<ExplosionBeamRenderEntity> {
    /** 光柱本体及其辉光使用的渲染 Pipeline。 */
    override val pipeline = UsefulMagicShaderPipelines.maskBloom<ExplosionBeamRenderEntity>(
        renderEntityId = ExplosionBeamRenderEntity.ID,
        blurSigma = 7.2F,
        blurRange = 5.8F,
        intensity = 2.2F,
        threshold = 0.015F,
        thresholdSoftness = 0.02F,
    )

    /** 根据实体阶段状态提交当前帧光柱。 */
    override fun render(input: RenderInput<ExplosionBeamRenderEntity>) {
        initStatic()
        val entity = input.entity
        val timeline = entity.currentTimeline(input.tickDelta)
        val state = entity.buildState(timeline)
        if (state.opacity <= MIN_VISIBLE_ALPHA) {
            return
        }
        renderPasses(
            entity = entity,
            modelMatrix = input.modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            state = state,
            time = timeline,
        )
    }

    /** 构建并绘制外层光柱与中心亮柱。 */
    private fun renderPasses(
        entity: ExplosionBeamRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        state: ExplosionBeamState,
        time: Float,
    ) {
        val outerPass = ExplosionBeamPass(
            blendMode = ExplosionBeamBlendMode.ALPHA,
            color = entity.color,
            alpha = (state.opacity * 0.42F).coerceAtMost(0.82F),
            brightness = state.brightness,
            rimPower = 1.12F,
            coreBias = 0.32F,
            highlightStrength = 0.72F,
            impactStrength = 0.84F,
            textureScale = 1.0F,
            textureSpeed = 1.08F,
        )
        val corePass = ExplosionBeamPass(
            blendMode = ExplosionBeamBlendMode.ADDITIVE,
            color = Vector3f(1.0F, 0.72F, 0.36F),
            alpha = (state.opacity * 0.24F).coerceAtMost(0.56F),
            brightness = state.brightness * 1.35F,
            rimPower = 0.86F,
            coreBias = 0.88F,
            highlightStrength = 0.96F,
            impactStrength = 0.92F,
            textureScale = 1.14F,
            textureSpeed = 1.24F,
        )
        val renderState = UsefulMagicRenderState.capture()
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            listOf(outerPass, corePass).forEach { pass ->
                if (pass.alpha <= MIN_VISIBLE_ALPHA) {
                    return@forEach
                }
                when (pass.blendMode) {
                    ExplosionBeamBlendMode.ALPHA -> RenderSystem.blendFunc(
                        GL33.GL_SRC_ALPHA,
                        GL33.GL_ONE_MINUS_SRC_ALPHA,
                    )
                    ExplosionBeamBlendMode.ADDITIVE -> RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
                }
                drawPass(modelMatrix, viewMatrix, projMatrix, state, time, pass)
            }
        } finally {
            renderState.restore()
        }
    }

    /** 使用光柱 shader 绘制一个圆柱材质层。 */
    private fun drawPass(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        state: ExplosionBeamState,
        time: Float,
        pass: ExplosionBeamPass,
    ) {
        val radius = state.radius.coerceAtLeast(ExplosionBeamRenderEntity.MIN_RADIUS)
        beamShader.useOnContext {
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat3(
                "scale",
                Vector3f(radius, state.height.coerceAtLeast(ExplosionBeamRenderEntity.MIN_HEIGHT), radius),
            )
            setFloat("offsetY", state.offsetY)
            setFloat3("color", pass.color)
            setFloat("alpha", pass.alpha)
            setFloat("brightness", pass.brightness)
            setFloat("rimPower", pass.rimPower)
            setFloat("coreBias", pass.coreBias)
            setFloat("highlightStrength", pass.highlightStrength)
            setFloat("impactStrength", pass.impactStrength)
            setFloat("textureScale", pass.textureScale)
            setFloat("textureSpeed", pass.textureSpeed)
            setFloat("phaseProgress", state.phaseProgress)
            setFloat("collapse", state.collapse)
            setFloat("time", time)
            setFloat("maskIntensityScale", state.opacity.coerceIn(0.25F, 1.0F))
            beamVertexBuffer.draw()
        }
    }

    /** 管理爆炸光柱 renderer 共享的 GPU 资源和网格。 */
    companion object {
        /** 多个光柱绘制阶段共用的最小可见透明度。 */
        private const val MIN_VISIBLE_ALPHA = 0.001F

        /** 客户端重复使用的圆柱顶点缓冲。 */
        private lateinit var beamVertexBuffer: SimpleVertexBuffer
        /** 客户端重复使用的光柱 shader。 */
        private lateinit var beamShader: CooShaderProgram
        /** 标记 renderer 的 GPU 资源是否已创建。 */
        private var initialized = false

        /** 在渲染线程首次使用时创建 GPU 资源。 */
        private fun initStatic() {
            if (initialized) {
                return
            }
            val vertices = buildCylinderVertices()
            UsefulMagicRenderState.preserve {
                beamVertexBuffer = SimpleVertexBuffer().apply {
                    init()
                    setVertexes(vertices, CooVertexFormat.POINT_FORMAT)
                }
            }
            beamShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID, "core/vsh/sky_falling_beam.vsh"),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID, "core/fsh/explosion_beam.fsh"),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            beamShader.init()
            initialized = true
        }

        /**
         * 构建单位圆柱的侧面和端面三角形。
         *
         * @return 可上传到共享顶点缓冲的圆柱顶点
         */
        private fun buildCylinderVertices(): List<VertexData> {
            val segments = 48
            val vertices = ArrayList<VertexData>(segments * 12)
            val bottomCenter = Vector3f(0F, 0F, 0F)
            val topCenter = Vector3f(0F, 1F, 0F)

            // 每个环向分段生成侧面、底盖和顶盖，统一保持朝外的顶点顺序。
            for (segment in 0 until segments) {
                val angle0 = (PI.toFloat() * 2F * segment) / segments.toFloat()
                val angle1 = (PI.toFloat() * 2F * (segment + 1)) / segments.toFloat()
                val x0 = cos(angle0)
                val z0 = sin(angle0)
                val x1 = cos(angle1)
                val z1 = sin(angle1)

                val a = Vector3f(x0, 0F, z0)
                val b = Vector3f(x1, 0F, z1)
                val c = Vector3f(x1, 1F, z1)
                val d = Vector3f(x0, 1F, z0)

                appendQuad(vertices, a, b, c, d)
                appendTriangle(vertices, topCenter, d, c)
                appendTriangle(vertices, bottomCenter, b, a)
            }
            return vertices
        }

        /** 向顶点列表追加由两个三角形组成的四边形。 */
        private fun appendQuad(
            output: MutableList<VertexData>,
            a: Vector3f,
            b: Vector3f,
            c: Vector3f,
            d: Vector3f,
        ) {
            appendTriangle(output, a, b, c)
            appendTriangle(output, a, c, d)
        }

        /** 向顶点列表追加一个三角形。 */
        private fun appendTriangle(
            output: MutableList<VertexData>,
            a: Vector3f,
            b: Vector3f,
            c: Vector3f,
        ) {
            output += VertexData(a, Vector4f(), Vector2f())
            output += VertexData(b, Vector4f(), Vector2f())
            output += VertexData(c, Vector4f(), Vector2f())
        }
    }
}
