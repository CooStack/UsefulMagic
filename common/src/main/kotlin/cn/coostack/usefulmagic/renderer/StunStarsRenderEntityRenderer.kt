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
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL33
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/** 负责眩晕星星实体的客户端公告板、拖尾几何体与绘制状态。 */
@CooAutoRegisterRenderer
class StunStarsRenderEntityRenderer : RenderEntityRenderer<StunStarsRenderEntity> {
    /** 星形本体和拖尾辉光使用的渲染 Pipeline。 */
    override val pipeline = UsefulMagicShaderPipelines.maskBloom<StunStarsRenderEntity>(
        renderEntityId = StunStarsRenderEntity.ID,
        blurSigma = 7.4F,
        blurRange = 5.2F,
        intensity = 2.2F,
        baseMaskIntensity = 0.20F,
        threshold = 0.01F,
        thresholdSoftness = 0.025F,
    )

    /** 根据出现和消散状态提交当前帧星形与拖尾。 */
    override fun render(input: RenderInput<StunStarsRenderEntity>) {
        initStatic()
        val entity = input.entity
        val visibleAlpha = entity.currentAlpha(input.tickDelta)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val starTexture = ofID(UsefulMagic.MOD_ID, "textures/effect/star.png")
        val trailTexture = ofID(UsefulMagic.MOD_ID,
            "textures/effect/stun_star_trail.png",
        )
        renderStars(
            entity = entity,
            modelMatrix = input.modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            tickDelta = input.tickDelta,
            passAlpha = visibleAlpha,
            starTexture = starTexture,
            trailTexture = trailTexture,
        )
    }

    /** 绘制轨道拖尾后再绘制三个星形公告板。 */
    private fun renderStars(
        entity: StunStarsRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        tickDelta: Float,
        passAlpha: Float,
        starTexture: ResourceLocation,
        trailTexture: ResourceLocation,
    ) {
        val starCount = 3
        val fullTurn = PI.toFloat() * 2F
        val baseColor = normalizedColor(entity.color)
        val time = entity.timeline(tickDelta)
        val appearScale = entity.currentAppearScale(tickDelta)
        val inverseViewRotationMatrix = Matrix3f(viewMatrix).invert()
        val renderState = UsefulMagicRenderState.capture()
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.depthFunc(GL33.GL_LEQUAL)
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
            for (bloomPass in booleanArrayOf(false, true)) {
                val brightnessScale = entity.brightness.coerceIn(0F, StunStarsRenderEntity.MAX_BRIGHTNESS)
                val bloomPulse = 0.92F + (0.5F + 0.5F * sin(time * 0.22F)) * 0.18F
                val passBrightness = if (bloomPass) {
                    2.8F * bloomPulse * brightnessScale * brightnessScale
                } else {
                    1.65F * brightnessScale
                }
                val starAlpha = passAlpha * if (bloomPass) 0.58F else 0.88F
                val trailAlpha = passAlpha * if (bloomPass) 0.50F else 0.38F
                val renderTarget = if (bloomPass) 1 else 0
                repeat(starCount) { index ->
                    val phase = time * entity.orbitSpeed + fullTurn * index / starCount
                    renderOrbitTrail(
                        entity = entity,
                        modelMatrix = modelMatrix,
                        viewMatrix = viewMatrix,
                        projMatrix = projMatrix,
                        inverseViewRotationMatrix = inverseViewRotationMatrix,
                        texture = trailTexture,
                        baseColor = baseColor,
                        phase = phase,
                        time = time,
                        starIndex = index,
                        appearScale = appearScale,
                        trailAlpha = trailAlpha,
                        trailBrightness = passBrightness * 0.72F,
                        bloomPass = bloomPass,
                        renderTarget = renderTarget,
                    )
                }
                repeat(starCount) { index ->
                    val phase = time * entity.orbitSpeed + fullTurn * index / starCount
                    val starOffset = orbitOffset(entity, phase, time, index, appearScale)
                    val starScale = entity.starScale * appearScale * if (bloomPass) 1.34F else 1F
                    drawBillboard(
                        modelMatrix = modelMatrix,
                        viewMatrix = viewMatrix,
                        projMatrix = projMatrix,
                        inverseViewRotationMatrix = inverseViewRotationMatrix,
                        texture = starTexture,
                        centerOffset = starOffset,
                        scale = Vector2f(starScale, starScale),
                        spin = time * entity.starSpinSpeed + index * 0.73F,
                        color = baseColor,
                        alpha = starAlpha,
                        brightness = passBrightness,
                        trailFade = 1F,
                        time = time,
                        renderTarget = renderTarget,
                    )
                }
            }
        } finally {
            renderState.restore()
        }
    }

    /** 沿星形轨道的历史方向分段绘制拖尾。 */
    private fun renderOrbitTrail(
        entity: StunStarsRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        inverseViewRotationMatrix: Matrix3f,
        texture: ResourceLocation,
        baseColor: Vector3f,
        phase: Float,
        time: Float,
        starIndex: Int,
        appearScale: Float,
        trailAlpha: Float,
        trailBrightness: Float,
        bloomPass: Boolean,
        renderTarget: Int,
    ) {
        if (entity.trailLength <= StunStarsRenderEntity.MIN_SIZE || entity.trailWidth <= StunStarsRenderEntity.MIN_SIZE) {
            return
        }
        val maxTrailPhaseSpan = PI.toFloat() * 0.9F
        val trailSegmentPhaseStep = 0.075F
        val radius = entity.orbitRadius.coerceAtLeast(StunStarsRenderEntity.MIN_SIZE)
        val direction = if (entity.orbitSpeed >= 0F) 1F else -1F
        // 把世界拖尾长度换算为轨道相位跨度，并限制分段数以稳定近远距离开销。
        val phaseSpan = (entity.trailLength / radius).coerceIn(0.04F, maxTrailPhaseSpan)
        val segmentCount = (ceil(phaseSpan / trailSegmentPhaseStep).toInt() + 1).coerceIn(5, 18)
        val segmentPhaseStep = phaseSpan / segmentCount
        val widthScale = entity.trailWidth * appearScale * if (bloomPass) 1.18F else 0.82F
        var previousOffset = orbitOffset(entity, phase, time, starIndex, appearScale)

        // 沿历史相位反向采样轨道，使用二次衰减让拖尾末端自然消失。
        for (segment in 1..segmentCount) {
            val progress = segment.toFloat() / segmentCount.toFloat()
            val lag = segmentPhaseStep * segment * direction
            val laggedPhase = phase - lag
            val laggedTime = time - segmentPhaseStep * segment /
                entity.orbitSpeed.absoluteValue.coerceAtLeast(0.001F)
            val segmentOffset = orbitOffset(entity, laggedPhase, laggedTime, starIndex, appearScale)
            val inverseProgress = 1F - progress
            val segmentFade = inverseProgress * inverseProgress * trailVisibility(starIndex, laggedTime)
            if (segmentFade > MIN_VISIBLE_ALPHA) {
                drawTrailSegment(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    inverseViewRotationMatrix = inverseViewRotationMatrix,
                    texture = texture,
                    startOffset = segmentOffset,
                    endOffset = previousOffset,
                    width = widthScale * (0.38F + 0.62F * segmentFade),
                    color = baseColor,
                    alpha = trailAlpha * segmentFade * if (bloomPass) 0.92F else 0.68F,
                    brightness = trailBrightness,
                    trailFade = segmentFade,
                    time = laggedTime,
                    renderTarget = renderTarget,
                )
            }
            previousOffset = segmentOffset
        }
    }

    /** 使用公告板 shader 绘制一段线形拖尾。 */
    private fun drawTrailSegment(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        inverseViewRotationMatrix: Matrix3f,
        texture: ResourceLocation,
        startOffset: Vector3f,
        endOffset: Vector3f,
        width: Float,
        color: Vector3f,
        alpha: Float,
        brightness: Float,
        trailFade: Float,
        time: Float,
        renderTarget: Int,
    ) {
        val minSize = StunStarsRenderEntity.MIN_SIZE
        if (alpha <= MIN_VISIBLE_ALPHA || width <= minSize || startOffset.distanceSquared(endOffset) <= minSize * minSize) {
            return
        }
        billboardShader.useOnContext {
            RenderSystem.setShaderTexture(0, texture)
            setInt("spriteTexture", 0)
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setMatrix3f("inverseViewRotationMatrix", inverseViewRotationMatrix)
            setFloat3("centerOffset", Vector3f())
            setFloat2("scale", Vector2f(1F, 1F))
            setFloat("spin", 0F)
            setFloat("depthOffset", 0F)
            setFloat3("segmentStart", startOffset)
            setFloat3("segmentEnd", endOffset)
            setFloat("segmentWidth", width)
            setFloat3("color", color)
            setFloat("alpha", alpha.coerceIn(0F, 1.4F))
            setFloat("brightness", brightness.coerceIn(0F, 6F))
            setFloat("mode", 1F)
            setFloat("trailFade", trailFade.coerceIn(0F, 1F))
            setFloat("time", time)
            setInt("renderTarget", renderTarget)
            billboardVertexBuffer.draw()
        }
    }

    /** 使用公告板 shader 绘制一个星形精灵。 */
    private fun drawBillboard(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        inverseViewRotationMatrix: Matrix3f,
        texture: ResourceLocation,
        centerOffset: Vector3f,
        scale: Vector2f,
        spin: Float,
        color: Vector3f,
        alpha: Float,
        brightness: Float,
        trailFade: Float,
        time: Float,
        renderTarget: Int,
    ) {
        if (
            alpha <= MIN_VISIBLE_ALPHA ||
            scale.x <= StunStarsRenderEntity.MIN_SIZE ||
            scale.y <= StunStarsRenderEntity.MIN_SIZE
        ) {
            return
        }
        billboardShader.useOnContext {
            RenderSystem.setShaderTexture(0, texture)
            setInt("spriteTexture", 0)
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setMatrix3f("inverseViewRotationMatrix", inverseViewRotationMatrix)
            setFloat3("centerOffset", centerOffset)
            setFloat2("scale", scale)
            setFloat("spin", spin)
            setFloat("depthOffset", 0F)
            setFloat3("segmentStart", Vector3f())
            setFloat3("segmentEnd", Vector3f())
            setFloat("segmentWidth", 0F)
            setFloat3("color", color)
            setFloat("alpha", alpha.coerceIn(0F, 1.4F))
            setFloat("brightness", brightness.coerceIn(0F, 6F))
            setFloat("mode", 0F)
            setFloat("trailFade", trailFade.coerceIn(0F, 1F))
            setFloat("time", time)
            setInt("renderTarget", renderTarget)
            billboardVertexBuffer.draw()
        }
    }

    /**
     * 计算指定星形在轨道上的局部偏移。
     *
     * @return 相对 RenderEntity 锚点的三维偏移
     */
    private fun orbitOffset(
        entity: StunStarsRenderEntity,
        phase: Float,
        time: Float,
        index: Int,
        appearScale: Float,
    ): Vector3f {
        val bob = sin(time * 0.18F + index * 1.7F) * 0.055F
        return Vector3f(
            cos(phase) * entity.orbitRadius * appearScale,
            entity.verticalOffset * appearScale + bob * appearScale,
            sin(phase) * entity.orbitRadius * appearScale,
        )
    }

    /**
     * 计算拖尾随时间变化的可见倍率。
     *
     * @return 用于抑制静态拖尾的周期可见倍率
     */
    private fun trailVisibility(index: Int, time: Float): Float {
        return 0.82F + 0.18F * sin(time * 0.34F + index * 2.1F)
    }

    /**
     * 限制同步颜色范围，并为近黑颜色提供可见回退。
     *
     * @return 可直接传给 shader 的 RGB 颜色副本
     */
    private fun normalizedColor(color: Vector3f): Vector3f {
        val normalized = Vector3f(
            color.x.coerceIn(0F, 1F),
            color.y.coerceIn(0F, 1F),
            color.z.coerceIn(0F, 1F),
        )
        if (max(max(normalized.x, normalized.y), normalized.z) < 0.05F) {
            return Vector3f(1.0F, 0.92F, 0.42F)
        }
        return normalized
    }

    /** 管理眩晕星体 renderer 共享的 GPU 资源和网格。 */
    companion object {
        /** 星形和拖尾绘制共用的最小可见透明度。 */
        private const val MIN_VISIBLE_ALPHA = 0.001F

        /** 复用的公告板顶点缓冲。 */
        private lateinit var billboardVertexBuffer: SimpleVertexBuffer

        /** 复用的星形和拖尾材质程序。 */
        private lateinit var billboardShader: CooShaderProgram

        /** 标记客户端渲染资源是否已经完成初始化。 */
        private var initialized = false

        /** 在首次绘制时创建 Coo shader 与公告板网格。 */
        @Synchronized
        private fun initStatic() {
            if (initialized) {
                return
            }
            UsefulMagicRenderState.preserve {
                billboardVertexBuffer = SimpleVertexBuffer().apply {
                    init()
                    setVertexes(buildQuadVertices(), CooVertexFormat.POINT_FORMAT)
                }
            }
            billboardShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID,
                            "core/vsh/stun_stars_billboard.vsh",
                        ),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID,
                            "core/fsh/stun_stars_billboard.fsh",
                        ),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            billboardShader.init()
            initialized = true
        }

        /**
         * 构建单位公告板的两个三角形。
         *
         * @return 可上传到共享顶点缓冲的公告板顶点
         */
        private fun buildQuadVertices(): List<VertexData> {
            return listOf(
                VertexData(Vector3f(-1F, -1F, 0F), Vector4f(), Vector2f()),
                VertexData(Vector3f(1F, -1F, 0F), Vector4f(), Vector2f()),
                VertexData(Vector3f(1F, 1F, 0F), Vector4f(), Vector2f()),
                VertexData(Vector3f(-1F, -1F, 0F), Vector4f(), Vector2f()),
                VertexData(Vector3f(1F, 1F, 0F), Vector4f(), Vector2f()),
                VertexData(Vector3f(-1F, 1F, 0F), Vector4f(), Vector2f()),
            )
        }
    }
}
