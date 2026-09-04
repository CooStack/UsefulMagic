package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
import cn.coostack.cooparticlesapi.annotations.CooAutoRegisterRenderer
import cn.coostack.cooparticlesapi.extend.minus
import cn.coostack.cooparticlesapi.renderer.client.ClientRenderPipelineManager
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
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL33
import kotlin.math.max

/** 负责公告板星芒实体的材质、几何体和 Pipeline 提交。 */
@CooAutoRegisterRenderer
class BillboardStarRenderEntityRenderer : RenderEntityRenderer<BillboardStarRenderEntity> {
    /** 星芒本体及其辉光使用的渲染 Pipeline。 */
    override val pipeline = UsefulMagicShaderPipelines.maskBloom<BillboardStarRenderEntity>(
        renderEntityId = BillboardStarRenderEntity.ID,
        blurSigma = 13.0F,
        blurRange = 9.5F,
        intensity = 4.6F,
        baseMaskIntensity = 0.36F,
        thresholdSoftness = 0.02F,
    )

    /** 提交当前帧可见的星芒几何体。 */
    override fun render(input: RenderInput<BillboardStarRenderEntity>) {
        initStatic()
        val entity = input.entity
        val bodyAlpha = entity.currentBodyAlpha(input.tickDelta)
        if (bodyAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val scale = entity.currentScale(input.tickDelta)
        val viewRotationMatrix = Matrix3f(input.viewMatrix)
        val inverseViewRotationMatrix = Matrix3f(viewRotationMatrix).invert()
        val directWeight = projectedDirectWeight(
            entity = entity,
            scale = scale,
            cameraWorldPos = currentCameraWorldPos(),
            screenSize = currentScreenSize(),
        )
        val directAlpha = bodyAlpha * BillboardStarRenderEntity.mix(0.18F, 1.0F, directWeight)
        if (directAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        renderPasses(
            entity = entity,
            modelMatrix = input.modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            inverseViewRotationMatrix = inverseViewRotationMatrix,
            scale = scale,
            bloomScale = scale * BillboardStarRenderEntity.mix(1.0F, 1.18F, 1F - directWeight),
            time = entity.currentTimeline(input.tickDelta),
            roll = entity.currentRoll(input.tickDelta),
            rayMorph = entity.currentRayMorph(input.tickDelta),
            whiteCore = entity.currentWhiteCore(input.tickDelta),
            twinkle = entity.currentTwinkle(input.tickDelta),
            collapse = entity.currentCollapse(input.tickDelta),
            passAlpha = directAlpha,
            bloomAlpha = entity.currentBloomAlpha(input.tickDelta) * (1F + (1F - directWeight) * 0.36F),
        )
    }

    /** 按外层星芒、中心亮部和白色核心顺序绘制材质层。 */
    private fun renderPasses(
        entity: BillboardStarRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        inverseViewRotationMatrix: Matrix3f,
        scale: Float,
        bloomScale: Float,
        time: Float,
        roll: Float,
        rayMorph: Float,
        whiteCore: Float,
        twinkle: Float,
        collapse: Float,
        passAlpha: Float,
        bloomAlpha: Float,
    ) {
        if (passAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
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
                inverseViewRotationMatrix = inverseViewRotationMatrix,
                scale = Vector2f(scale, scale),
                time = time,
                roll = roll,
                depthOffset = -0.002F,
                passColor = entity.color,
                alpha = passAlpha * 0.42F,
                brightness = 1.26F,
                haloStrength = 0.34F,
                rayStrength = rayMorph,
                raySharpness = 0.78F,
                coreRadius = 0.34F,
                whiteCore = whiteCore * 0.62F,
                twinkle = twinkle,
                collapse = collapse,
                renderTarget = 0,
            )
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
            drawPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                inverseViewRotationMatrix = inverseViewRotationMatrix,
                scale = Vector2f(scale * 0.58F, scale * 0.58F),
                time = time,
                roll = -roll * 1.34F,
                depthOffset = 0.0035F,
                passColor = mixColor(entity.color, Vector3f(1.0F, 0.98F, 0.90F), 0.44F),
                alpha = passAlpha * 0.30F,
                brightness = 2.7F,
                haloStrength = 0.12F,
                rayStrength = BillboardStarRenderEntity.mix(0.52F, 0.76F, rayMorph),
                raySharpness = 0.96F,
                coreRadius = 0.48F,
                whiteCore = whiteCore,
                twinkle = twinkle,
                collapse = collapse,
                renderTarget = 0,
            )
            drawPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                inverseViewRotationMatrix = inverseViewRotationMatrix,
                scale = Vector2f(bloomScale * 1.22F, bloomScale * 1.22F),
                time = time,
                roll = roll,
                depthOffset = -0.002F,
                passColor = mixColor(entity.color, Vector3f(1.0F, 0.96F, 0.84F), 0.26F),
                alpha = bloomAlpha * 0.34F,
                brightness = 2.9F,
                haloStrength = 0.78F,
                rayStrength = rayMorph,
                raySharpness = 0.62F,
                coreRadius = 0.30F,
                whiteCore = whiteCore * 0.88F,
                twinkle = twinkle,
                collapse = collapse,
                renderTarget = 1,
            )
            drawPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                inverseViewRotationMatrix = inverseViewRotationMatrix,
                scale = Vector2f(bloomScale * 1.74F, bloomScale * 1.74F),
                time = time,
                roll = -roll * 1.18F,
                depthOffset = 0.0015F,
                passColor = mixColor(entity.color, Vector3f(1.0F, 0.98F, 0.94F), 0.38F),
                alpha = bloomAlpha * 0.18F,
                brightness = 4.1F,
                haloStrength = 1.12F,
                rayStrength = BillboardStarRenderEntity.mix(0.66F, 1.0F, rayMorph),
                raySharpness = 0.42F,
                coreRadius = 0.22F,
                whiteCore = whiteCore * 0.56F,
                twinkle = twinkle,
                collapse = collapse,
                renderTarget = 1,
            )
        } finally {
            renderState.restore()
        }
    }

    /** 使用公告板 shader 绘制一个材质层。 */
    private fun drawPass(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        inverseViewRotationMatrix: Matrix3f,
        scale: Vector2f,
        time: Float,
        roll: Float,
        depthOffset: Float,
        passColor: Vector3f,
        alpha: Float,
        brightness: Float,
        haloStrength: Float,
        rayStrength: Float,
        raySharpness: Float,
        coreRadius: Float,
        whiteCore: Float,
        twinkle: Float,
        collapse: Float,
        renderTarget: Int,
    ) {
        if (alpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        starShader.useOnContext {
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setMatrix3f("inverseViewRotationMatrix", inverseViewRotationMatrix)
            setFloat2("scale", scale)
            setFloat("spin", roll)
            setFloat("depthOffset", depthOffset)
            setFloat3("color", passColor)
            setFloat("alpha", alpha)
            setFloat("brightness", brightness)
            setFloat("haloStrength", haloStrength)
            setFloat("rayStrength", rayStrength)
            setFloat("raySharpness", raySharpness)
            setFloat("coreRadius", coreRadius)
            setFloat("whiteCore", whiteCore)
            setFloat("twinkle", twinkle)
            setFloat("collapse", collapse)
            setFloat("time", time)
            setInt("renderTarget", renderTarget)
            starVertexBuffer.draw()
        }
    }

    /**
     * 按屏幕投影尺寸计算直接可见层的权重。
     *
     * @return 位于 `0F..1F` 的直接绘制权重
     */
    private fun projectedDirectWeight(
        entity: BillboardStarRenderEntity,
        scale: Float,
        cameraWorldPos: Vector3f,
        screenSize: Vector2f,
    ): Float {
        val centerPosition = Vector3f(entity.pos.x.toFloat(), entity.pos.y.toFloat(), entity.pos.z.toFloat())
        val distance = (centerPosition - cameraWorldPos).length().coerceAtLeast(0.125F)
        val screenHeight = screenSize.y.coerceAtLeast(1.0F)
        val glowWorldRadius = max(0.05F, scale * 0.84F)
        // 用世界半径除以视距，再按渲染目标高度换算近似像素覆盖范围。
        val projectedRadiusPx = glowWorldRadius / distance * screenHeight * 0.92F
        return BillboardStarRenderEntity.smoothstep(5.0F, 20.0F, projectedRadiusPx)
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
            ?: renderTarget?.width?.takeIf { it > 0 }
            ?: 0
        val height = ClientRenderPipelineManager.currentRenderHeight().takeIf { it > 0 }
            ?: renderTarget?.height?.takeIf { it > 0 }
            ?: 0
        return Vector2f(width.toFloat(), height.toFloat())
    }

    /**
     * 在线性空间混合两种颜色。
     *
     * @return 按限制后的权重混合得到的颜色
     */
    private fun mixColor(from: Vector3f, to: Vector3f, alpha: Float): Vector3f {
        val t = alpha.coerceIn(0F, 1F)
        return Vector3f(
            BillboardStarRenderEntity.mix(from.x, to.x, t),
            BillboardStarRenderEntity.mix(from.y, to.y, t),
            BillboardStarRenderEntity.mix(from.z, to.z, t),
        )
    }

    /** 管理星芒 renderer 共享的 GPU 资源。 */
    companion object {
        /** 多个绘制阶段共用的最小可见透明度。 */
        private const val MIN_VISIBLE_ALPHA = 0.001F

        /** 客户端重复使用的星芒顶点缓冲。 */
        private lateinit var starVertexBuffer: SimpleVertexBuffer
        /** 客户端重复使用的星芒 shader。 */
        private lateinit var starShader: CooShaderProgram
        /** 标记 renderer 的 GPU 资源是否已创建。 */
        private var initialized = false

        /** 在渲染线程首次使用时创建 GPU 资源。 */
        private fun initStatic() {
            if (initialized) {
                return
            }
            val billboardExtent = 2.4F
            // 两个三角形共用对角线，保持公告板顶点顺序与 shader 的局部坐标约定一致。
            val vertices = listOf(
                VertexData(Vector3f(-billboardExtent, -billboardExtent, 0F), Vector4f(), Vector2f()),
                VertexData(Vector3f(billboardExtent, -billboardExtent, 0F), Vector4f(), Vector2f()),
                VertexData(Vector3f(billboardExtent, billboardExtent, 0F), Vector4f(), Vector2f()),
                VertexData(Vector3f(-billboardExtent, -billboardExtent, 0F), Vector4f(), Vector2f()),
                VertexData(Vector3f(billboardExtent, billboardExtent, 0F), Vector4f(), Vector2f()),
                VertexData(Vector3f(-billboardExtent, billboardExtent, 0F), Vector4f(), Vector2f()),
            )
            UsefulMagicRenderState.preserve {
                starVertexBuffer = SimpleVertexBuffer().apply {
                    init()
                    setVertexes(vertices, CooVertexFormat.POINT_FORMAT)
                }
            }
            starShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID, "core/vsh/billboard_star.vsh"),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID, "core/fsh/billboard_star.fsh"),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            starShader.init()
            initialized = true
        }
    }
}
