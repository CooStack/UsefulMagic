package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.annotations.CooAutoRegisterRenderer
import cn.coostack.cooparticlesapi.renderer.client.ClientRenderPipelineManager
import cn.coostack.cooparticlesapi.renderer.pipeline.CooPipelines
import cn.coostack.cooparticlesapi.renderer.pipeline.CooRenderPipeline
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
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL33
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/** 负责天坠光柱实体的客户端材质、屏幕覆盖估算、几何体与绘制状态。 */
@CooAutoRegisterRenderer
class SkyFallingRenderEntityRenderer : RenderEntityRenderer<SkyFallingRenderEntity> {
    /** 光柱本体及其辉光使用的渲染 Pipeline。 */
    override val pipeline: CooRenderPipeline<SkyFallingRenderEntity> = CooPipelines.MASK_BLOOM
        .bloomSoftKnee(0.02f)
        .bloomThreshold(0.2f)
        .intensity(18f)
//    override val pipeline = UsefulMagicShaderPipelines.maskBloom<SkyFallingRenderEntity>(
//        renderEntityId = SkyFallingRenderEntity.ID,
//        blurSigma = 15.5F,
//        blurRange = 11.0F,
//        intensity = 4.8F,
//        baseMaskIntensity = 0.38F,
//        thresholdSoftness = 0.02F,
//    )

    /** 根据当前阶段和屏幕覆盖范围提交光柱。 */
    override fun render(input: RenderInput<SkyFallingRenderEntity>) {
        initStatic()
        val entity = input.entity
        val timeline = entity.currentTimeline(input.tickDelta)
        val state = SkyFallingRenderEntity.buildState(timeline, entity.alpha.toFloat())
        if (state.opacity <= MIN_VISIBLE_ALPHA) {
            return
        }
        val projectedRadiusPx = projectedRadiusPx(
            entity = entity,
            state = state,
            cameraWorldPos = currentCameraWorldPos(),
            screenSize = currentScreenSize(),
        )
        val directScale = buildDirectScale(projectedRadiusPx)
        val bloomScale = buildBloomScale(projectedRadiusPx)
        renderPasses(
            entity = entity,
            state = state,
            directScale = directScale,
            bloomScale = bloomScale,
            flowTime = entity.currentFlowTime(timeline),
            modelMatrix = input.modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
        )
    }

    /**
     * 大范围覆盖屏幕时适当压低直接绘制层。
     *
     * @return 直接绘制层的尺寸倍率
     */
    private fun buildDirectScale(projectedRadiusPx: Float): Float {
        val coverage = smoothstep(260F, 1800F, projectedRadiusPx)
        return mix(1.0F, 0.88F, coverage)
    }

    /**
     * 补偿远距离光柱的 mask 强度，并在大范围覆盖屏幕时抑制过曝。
     *
     * @return 当前投影尺寸对应的 mask 强度倍率
     */
    private fun buildBloomScale(projectedRadiusPx: Float): Float {
        val coverage = smoothstep(260F, 1800F, projectedRadiusPx)
        val farWeight = 1F - smoothstep(4F, 30F, projectedRadiusPx)
        val farCompensation = 1F + farWeight * 3.2F * 0.18F
        return mix(1.36F, 0.92F, coverage) * farCompensation
    }

    /**
     * 估算光柱半径在当前渲染目标中的像素尺寸。
     *
     * @return 光柱外缘的近似投影像素半径
     */
    private fun projectedRadiusPx(
        entity: SkyFallingRenderEntity,
        state: SkyFallingCylinderState,
        cameraWorldPos: Vector3f,
        screenSize: Vector2f,
    ): Float {
        val centerPosition = Vector3f(
            entity.pos.x.toFloat(),
            entity.pos.y.toFloat() + state.height * 0.5F,
            entity.pos.z.toFloat(),
        )
        val distance = (centerPosition - cameraWorldPos).length().coerceAtLeast(0.125F)
        val screenHeight = screenSize.y.coerceAtLeast(1.0F)
        // 用光柱中点到相机的距离估算半径像素覆盖范围，驱动近景压制和远景补偿。
        return state.radius.coerceAtLeast(MIN_RENDER_RADIUS) / distance * screenHeight * 0.92F
    }

    /** 绘制光柱外壳和中心亮柱。 */
    private fun renderPasses(
        entity: SkyFallingRenderEntity,
        state: SkyFallingCylinderState,
        directScale: Float,
        bloomScale: Float,
        flowTime: Float,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
    ) {
        val shellRadius = state.radius.coerceAtLeast(MIN_RENDER_RADIUS)
        val coreRadius = max(shellRadius * 0.58F, 1.3F)
        val bloomRadius = shellRadius * 1.08F
        val auraRadius = shellRadius * 1.18F
        val baseColor = mixColor(entity.color, Vector3f(0.14F, 0.36F, 1.0F), 0.74F)
        val shellColor = mixColor(baseColor, Vector3f(0.34F, 0.20F, 0.96F), 0.22F)
        val coreColor = mixColor(baseColor, Vector3f(0.90F, 0.95F, 1.0F), 0.30F)
        val auraColor = mixColor(baseColor, Vector3f(0.58F, 0.30F, 1.0F), 0.42F)
        val noiseTexture = ofID(
            UsefulMagic.MOD_ID,
            "textures/effect/straight_laser_impact_noise.png",
        )

        val renderState = UsefulMagicRenderState.capture()
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA)
            drawPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                texture = noiseTexture,
                color = shellColor,
                scale = Vector3f(shellRadius, state.height, shellRadius),
                alpha = (state.opacity * 0.46F * directScale).coerceAtMost(0.82F),
                brightness = 1.45F,
                rimPower = 1.20F,
                coreBias = 0.28F,
                highlightStrength = 0.70F,
                impactStrength = 0.72F,
                textureScale = 1.0F,
                textureSpeed = 0.96F,
                phaseProgress = state.phaseProgress,
                collapse = state.collapse,
                time = flowTime,
                renderTarget = 0,
            )
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
            drawPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                texture = noiseTexture,
                color = coreColor,
                scale = Vector3f(coreRadius, state.height, coreRadius),
                alpha = (state.opacity * 0.24F * directScale).coerceAtMost(0.48F),
                brightness = 2.85F,
                rimPower = 0.92F,
                coreBias = 0.82F,
                highlightStrength = 0.92F,
                impactStrength = 0.84F,
                textureScale = 1.12F,
                textureSpeed = 1.14F,
                phaseProgress = state.phaseProgress,
                collapse = state.collapse,
                time = flowTime,
                renderTarget = 0,
            )
            drawPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                texture = noiseTexture,
                color = shellColor,
                scale = Vector3f(bloomRadius, state.height, bloomRadius),
                alpha = (state.bloomAlpha * 0.34F * bloomScale).coerceAtMost(0.78F),
                brightness = 2.65F,
                rimPower = 1.36F,
                coreBias = 0.34F,
                highlightStrength = 0.74F,
                impactStrength = 0.92F,
                textureScale = 1.0F,
                textureSpeed = 0.88F,
                phaseProgress = state.phaseProgress,
                collapse = state.collapse,
                time = flowTime,
                renderTarget = 1,
            )
            drawPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                texture = noiseTexture,
                color = coreColor,
                scale = Vector3f(max(shellRadius * 0.72F, 1.8F), state.height, max(shellRadius * 0.72F, 1.8F)),
                alpha = (state.bloomAlpha * 0.28F * bloomScale).coerceAtMost(0.74F),
                brightness = 4.35F,
                rimPower = 0.86F,
                coreBias = 0.90F,
                highlightStrength = 0.98F,
                impactStrength = 0.88F,
                textureScale = 1.18F,
                textureSpeed = 1.18F,
                phaseProgress = state.phaseProgress,
                collapse = state.collapse,
                time = flowTime,
                renderTarget = 1,
            )
            drawPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                texture = noiseTexture,
                color = auraColor,
                scale = Vector3f(auraRadius, state.height, auraRadius),
                alpha = (state.bloomAlpha * 0.18F * bloomScale).coerceAtMost(0.58F),
                brightness = 3.45F,
                rimPower = 1.62F,
                coreBias = 0.20F,
                highlightStrength = 0.78F,
                impactStrength = 1.0F,
                textureScale = 0.92F,
                textureSpeed = 0.76F,
                phaseProgress = state.phaseProgress,
                collapse = state.collapse,
                time = flowTime,
                renderTarget = 1,
            )
        } finally {
            renderState.restore()
        }
    }

    /** 使用光柱 shader 绘制一个圆柱材质层。 */
    private fun drawPass(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        texture: ResourceLocation,
        color: Vector3f,
        scale: Vector3f,
        alpha: Float,
        brightness: Float,
        rimPower: Float,
        coreBias: Float,
        highlightStrength: Float,
        impactStrength: Float,
        textureScale: Float,
        textureSpeed: Float,
        phaseProgress: Float,
        collapse: Float,
        time: Float,
        renderTarget: Int,
    ) {
        if (alpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        beamShader.useOnContext {
            RenderSystem.setShaderTexture(0, texture)
            setInt("impactNoise", 0)
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat3("scale", scale)
            setFloat("offsetY", 0F)
            setFloat3("color", color)
            setFloat("alpha", alpha)
            setFloat("brightness", brightness)
            setFloat("rimPower", rimPower)
            setFloat("coreBias", coreBias)
            setFloat("highlightStrength", highlightStrength)
            setFloat("impactStrength", impactStrength)
            setFloat("textureScale", textureScale)
            setFloat("textureSpeed", textureSpeed)
            setFloat("phaseProgress", phaseProgress)
            setFloat("collapse", collapse)
            setFloat("time", time)
            setInt("renderTarget", renderTarget)
            beamVertexBuffer.draw()
        }
    }

    /**
     * 获取当前主相机的世界坐标。
     *
     * @return 主相机世界坐标的单精度副本
     */
    private fun currentCameraWorldPos(): Vector3f {
        val cameraPos = Minecraft.getInstance().gameRenderer.mainCamera.position
        return Vector3f(cameraPos.x.toFloat(), cameraPos.y.toFloat(), cameraPos.z.toFloat())
    }

    /**
     * 获取 Pipeline 当前使用的渲染尺寸。
     *
     * @return 当前渲染目标的宽高
     */
    private fun currentScreenSize(): Vector2f {
        val renderTarget = Minecraft.getInstance().mainRenderTarget
        val width = ClientRenderPipelineManager.currentRenderWidth().takeIf { it > 0 }
            ?: renderTarget.width.takeIf { it > 0 }
            ?: 0
        val height = ClientRenderPipelineManager.currentRenderHeight().takeIf { it > 0 }
            ?: renderTarget.height.takeIf { it > 0 }
            ?: 0
        return Vector2f(width.toFloat(), height.toFloat())
    }

    /**
     * 在线性区间插值两个标量。
     *
     * @return 按限制后的插值权重计算的标量
     */
    private fun mix(from: Float, to: Float, alpha: Float): Float {
        return from + (to - from) * alpha.coerceIn(0F, 1F)
    }

    /**
     * 在线性空间混合两种颜色。
     *
     * @return 按限制后的权重混合得到的颜色
     */
    private fun mixColor(a: Vector3f, b: Vector3f, alpha: Float): Vector3f {
        val amount = alpha.coerceIn(0F, 1F)
        return Vector3f(
            mix(a.x, b.x, amount),
            mix(a.y, b.y, amount),
            mix(a.z, b.z, amount),
        )
    }

    /**
     * 返回区间内平滑过渡的插值值。
     *
     * @return 位于 `0F..1F` 的平滑插值进度
     */
    private fun smoothstep(edge0: Float, edge1: Float, value: Float): Float {
        if (edge0 == edge1) {
            return if (value >= edge1) 1F else 0F
        }
        val amount = ((value - edge0) / (edge1 - edge0)).coerceIn(0F, 1F)
        return amount * amount * (3F - 2F * amount)
    }

    /** 管理天降光柱 renderer 共享的 GPU 资源和网格。 */
    companion object {
        /** 屏幕投影和几何缩放共用的最小光柱半径。 */
        private const val MIN_RENDER_RADIUS = 0.75F

        /** 多个光柱绘制阶段共用的最小可见透明度。 */
        private const val MIN_VISIBLE_ALPHA = 0.001F

        /** 复用的光柱圆柱顶点缓冲。 */
        private lateinit var beamVertexBuffer: SimpleVertexBuffer

        /** 复用的光柱材质程序。 */
        private lateinit var beamShader: CooShaderProgram

        /** 标记客户端渲染资源是否已经完成初始化。 */
        private var initialized = false

        /** 在首次绘制时创建 Coo shader 与圆柱网格。 */
        private fun initStatic() {
            if (initialized) {
                return
            }
            UsefulMagicRenderState.preserve {
                beamVertexBuffer = SimpleVertexBuffer().apply {
                    init()
                    setVertexes(buildCylinderVertices(), CooVertexFormat.POINT_FORMAT)
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
                        ofID(UsefulMagic.MOD_ID, "core/fsh/sky_falling_beam.fsh"),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            beamShader.init()
            initialized = true
        }

        /**
         * 构建带顶盖和底盖的单位圆柱网格。
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
                val a = Vector3f(cos(angle0), 0F, sin(angle0))
                val b = Vector3f(cos(angle1), 0F, sin(angle1))
                val c = Vector3f(cos(angle1), 1F, sin(angle1))
                val d = Vector3f(cos(angle0), 1F, sin(angle0))

                appendQuad(vertices, a, b, c, d)
                appendTriangle(vertices, topCenter, d, c)
                appendTriangle(vertices, bottomCenter, b, a)
            }
            return vertices
        }

        /** 向顶点列表追加一个四边形。 */
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
