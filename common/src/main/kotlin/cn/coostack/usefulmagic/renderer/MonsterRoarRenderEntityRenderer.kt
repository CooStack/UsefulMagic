package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
import cn.coostack.cooparticlesapi.annotations.CooAutoRegisterRenderer
import cn.coostack.cooparticlesapi.extend.minus
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
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL33
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** 负责怪物咆哮实体的客户端锥体材质、几何体与绘制状态。 */
@CooAutoRegisterRenderer
class MonsterRoarRenderEntityRenderer : RenderEntityRenderer<MonsterRoarRenderEntity> {
    /** 咆哮锥体及其辉光使用的渲染 Pipeline。 */
    override val pipeline = UsefulMagicShaderPipelines.maskBloom<MonsterRoarRenderEntity>(
        renderEntityId = MonsterRoarRenderEntity.ID,
        blurSigma = 6.2F,
        blurRange = 4.6F,
        intensity = 1.55F,
        threshold = 0.018F,
        thresholdSoftness = 0.03F,
    )

    /** 根据咆哮方向和生命周期提交当前帧锥体。 */
    override fun render(input: RenderInput<MonsterRoarRenderEntity>) {
        initStatic()
        val entity = input.entity
        entity.refreshClientState()
        val timeline = entity.currentTimeline(input.tickDelta)
        val visibleAlpha = entity.currentAlpha(timeline)
        if (
            visibleAlpha <= MIN_VISIBLE_ALPHA ||
            entity.maxDistance <= MonsterRoarRenderEntity.MIN_DISTANCE ||
            entity.maxRadius <= MonsterRoarRenderEntity.MIN_RADIUS
        ) {
            return
        }
        renderPasses(
            entity = entity,
            modelMatrix = orientedModelMatrix(input.modelMatrix, entity),
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            time = timeline,
            passAlpha = visibleAlpha,
        )
    }

    /** 绘制咆哮锥体的外层和中心亮层。 */
    private fun renderPasses(
        entity: MonsterRoarRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        time: Float,
        passAlpha: Float,
    ) {
        val baseColor = Vector3f(entity.color)
        val minimumBloomGain = 0.78F
        val maximumBloomGain = 1.18F
        val bloomEnergy = passAlpha.coerceIn(0.18F, 1.0F)
        val bloomGain = minimumBloomGain + (maximumBloomGain - minimumBloomGain) * bloomEnergy
        val renderState = UsefulMagicRenderState.capture()
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA)
            drawPass(
                entity = entity,
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                time = time,
                color = baseColor,
                alpha = (passAlpha * 0.38F).coerceAtMost(0.62F),
                brightness = 1.38F,
                edgeBoost = 0.72F,
                coreGlow = 0.28F,
                refractionStrength = 0.18F,
                noiseStrength = 0.72F,
                renderTarget = 0,
            )
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
            drawPass(
                entity = entity,
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                time = time,
                color = Vector3f(baseColor).lerp(Vector3f(1.0F, 0.96F, 0.84F), 0.34F),
                alpha = (passAlpha * 0.16F).coerceAtMost(0.34F),
                brightness = 2.5F,
                edgeBoost = 1.12F,
                coreGlow = 0.44F,
                refractionStrength = 0.10F,
                noiseStrength = 0.92F,
                renderTarget = 0,
            )
            drawPass(
                entity = entity,
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                time = time,
                color = Vector3f(baseColor).lerp(Vector3f(0.92F, 0.99F, 1.0F), 0.42F),
                alpha = (passAlpha * 0.72F * 0.42F * bloomGain).coerceAtMost(0.62F),
                brightness = 3.8F,
                edgeBoost = 1.26F,
                coreGlow = 0.58F,
                refractionStrength = 0.12F,
                noiseStrength = 0.86F,
                renderTarget = 1,
            )
        } finally {
            renderState.restore()
        }
    }

    /** 使用咆哮 shader 绘制一个锥体材质层。 */
    private fun drawPass(
        entity: MonsterRoarRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        time: Float,
        color: Vector3f,
        alpha: Float,
        brightness: Float,
        edgeBoost: Float,
        coreGlow: Float,
        refractionStrength: Float,
        noiseStrength: Float,
        renderTarget: Int,
    ) {
        if (alpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        roarShader.useOnContext {
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat("coneLength", entity.maxDistance.coerceAtLeast(MonsterRoarRenderEntity.MIN_DISTANCE))
            setFloat("coneRadius", entity.maxRadius.coerceAtLeast(MonsterRoarRenderEntity.MIN_RADIUS))
            setFloat3("color", color)
            setFloat("alpha", alpha)
            setFloat("brightness", brightness)
            setFloat("edgeBoost", edgeBoost)
            setFloat("coreGlow", coreGlow)
            setFloat("refractionStrength", refractionStrength)
            setFloat("noiseStrength", noiseStrength)
            setFloat("lifetime", entity.lifetime.coerceAtLeast(1).toFloat())
            setFloat("fadeTicks", entity.fadeTicks.coerceAtLeast(1).toFloat())
            setFloat("time", time)
            setInt("renderTarget", renderTarget)
            roarVertexBuffer.draw()
        }
    }

    /**
     * 构建从起点沿咆哮方向延伸的模型矩阵。
     *
     * @return 以锥体尖端为原点且局部 Z 轴沿咆哮方向的模型矩阵
     */
    private fun orientedModelMatrix(
        baseMatrix: Matrix4f,
        entity: MonsterRoarRenderEntity,
    ): Matrix4f {
        val normalized = MonsterRoarRenderEntity.normalizedDirection(entity.direction)
        val offset = entity.start - entity.pos
        return Matrix4f(baseMatrix).translate(
            offset.x.toFloat(),
            offset.y.toFloat(),
            offset.z.toFloat(),
        ).rotate(
            Quaternionf().rotationTo(
                0F,
                1F,
                0F,
                normalized.x.toFloat(),
                normalized.y.toFloat(),
                normalized.z.toFloat(),
            ),
        )
    }

    /** 管理怪物咆哮 renderer 共享的 GPU 资源和网格。 */
    companion object {
        /** 多个锥体绘制阶段共用的最小可见透明度。 */
        private const val MIN_VISIBLE_ALPHA = 0.001F

        /** 复用的咆哮锥体顶点缓冲。 */
        private lateinit var roarVertexBuffer: SimpleVertexBuffer

        /** 复用的咆哮材质程序。 */
        private lateinit var roarShader: CooShaderProgram

        /** 标记客户端渲染资源是否已经完成初始化。 */
        private var initialized = false

        /** 在首次绘制时创建 Coo shader 与锥体网格。 */
        private fun initStatic() {
            if (initialized) {
                return
            }
            UsefulMagicRenderState.preserve {
                roarVertexBuffer = SimpleVertexBuffer().apply {
                    init()
                    setVertexes(buildConeFieldVertices(), CooVertexFormat.POINT_FORMAT)
                }
            }
            roarShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID,
                            "core/vsh/monster_roar_beam.vsh",
                        ),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID,
                            "core/fsh/monster_roar_beam.fsh",
                        ),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            roarShader.init()
            initialized = true
        }

        /**
         * 构建单位长度的双面锥体扇形网格。
         *
         * @return 可上传到共享顶点缓冲的双面锥体顶点
         */
        private fun buildConeFieldVertices(): List<VertexData> {
            val segments = 42
            val vertices = ArrayList<VertexData>(segments * 9)
            val tip = Vector3f(0F, 0F, 0F)
            val center = Vector3f(0F, 1F, 0F)

            // 每个环向分段同时生成锥体侧面和底面，避免关闭面剔除时看到内部缺口。
            for (segment in 0 until segments) {
                val angle0 = (PI.toFloat() * 2F * segment) / segments.toFloat()
                val angle1 = (PI.toFloat() * 2F * (segment + 1)) / segments.toFloat()
                val a = Vector3f(cos(angle0), 1F, sin(angle0))
                val b = Vector3f(cos(angle1), 1F, sin(angle1))

                appendTriangle(vertices, tip, a, b)
                appendTriangle(vertices, center, b, a)
            }
            return vertices
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
