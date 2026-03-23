package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.renderer.AutoRenderEntity
import cn.coostack.cooparticlesapi.renderer.RenderEntity
import cn.coostack.cooparticlesapi.renderer.client.ClientRenderPipelineManager
import cn.coostack.cooparticlesapi.renderer.client.RenderUtil
import cn.coostack.cooparticlesapi.renderer.effects.builtin.BuiltinRenderEffectDescriptors
import cn.coostack.cooparticlesapi.renderer.effects.builtin.MaskBloomConfig
import cn.coostack.cooparticlesapi.renderer.runtime.FramePostRenderEntityRenderer
import cn.coostack.cooparticlesapi.renderer.runtime.LocalRenderInput
import cn.coostack.cooparticlesapi.renderer.runtime.RenderContributionCollector
import cn.coostack.cooparticlesapi.renderer.runtime.RenderContributionInput
import cn.coostack.cooparticlesapi.renderer.runtime.RenderEntityInstance
import cn.coostack.cooparticlesapi.renderer.runtime.RenderEntityReleaseHook
import cn.coostack.cooparticlesapi.renderer.runtime.WorldPassRenderEntityRenderer
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
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Matrix4fStack
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.max
import kotlin.math.sin

private data class BillboardProjectedBlend(
    val projectedRadiusPx: Float,
    val directWeight: Float,
)

@CooAutoRegister
class BillboardStarRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos),
    WorldPassRenderEntityRenderer<BillboardStarRenderEntity>,
    FramePostRenderEntityRenderer<BillboardStarRenderEntity>,
    RenderEntityReleaseHook<BillboardStarRenderEntity> {
    @CodecField
    var color: Vector3f = Vector3f(1.0f, 0.94f, 0.72f)

    @CodecField
    var alpha: Double = 1.0

    @CodecField
    var maxScale: Float = DEFAULT_MAX_SCALE

    @CodecField
    var expandTicks: Int = DEFAULT_EXPAND_TICKS

    @CodecField
    var holdTicks: Int = DEFAULT_HOLD_TICKS

    @CodecField
    var shrinkTicks: Int = DEFAULT_SHRINK_TICKS

    @CodecField
    var spinSpeed: Float = DEFAULT_SPIN_SPEED

    init {
        renderRange = 96.0
    }

    override fun initialize(instance: RenderEntityInstance<BillboardStarRenderEntity>) {
        initStatic()
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun release(instance: RenderEntityInstance<BillboardStarRenderEntity>) {
    }

    override fun clientTick() {
        updateLifecycle()
    }

    override fun serverTick() {
        updateLifecycle()
    }

    override fun renderLocal(input: LocalRenderInput<BillboardStarRenderEntity>) {
        initStatic()
        val bodyAlpha = currentBodyAlpha(input.tickDelta)
        if (bodyAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val scale = currentScale(input.tickDelta)
        val viewRotationMatrix = Matrix3f(input.viewMatrix)
        val inverseViewRotationMatrix = Matrix3f(viewRotationMatrix).invert()
        val blend = projectedBlend(
            scale = scale,
            cameraWorldPos = currentCameraWorldPos(),
            viewRotationMatrix = viewRotationMatrix,
            inverseViewRotationMatrix = inverseViewRotationMatrix,
            projMatrix = input.projMatrix,
            screenSize = currentScreenSize(),
        )
        val directAlpha = bodyAlpha * mix(0.18f, 1.0f, blend.directWeight)
        if (directAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        renderPasses(
            modelMatrix = input.modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            inverseViewRotationMatrix = inverseViewRotationMatrix,
            scale = scale,
            time = currentTimeline(input.tickDelta),
            roll = currentRoll(input.tickDelta),
            rayMorph = currentRayMorph(input.tickDelta),
            whiteCore = currentWhiteCore(input.tickDelta),
            twinkle = currentTwinkle(input.tickDelta),
            collapse = currentCollapse(input.tickDelta),
            passAlpha = directAlpha,
            bloomPass = false,
        )
    }

    override fun collectRenderContributions(
        input: RenderContributionInput<BillboardStarRenderEntity>,
        collector: RenderContributionCollector,
    ) {
        collector.submit(
            BuiltinRenderEffectDescriptors.maskBloom(
                effectId = STAR_BLOOM_EFFECT_ID,
                sourceInstanceId = uuid.toString(),
                frameContext = input.frameContext,
                sourceEntity = this,
                config = STAR_BLOOM_CONFIG,
                priority = STAR_BLOOM_PRIORITY,
                requiredCapabilities = UsefulMagicShaderPipelines.DEPTH_AWARE_MASK_BLOOM_CAPABILITIES,
            ) {
                renderBloomMask(
                    tickDelta = input.frameContext.tickDelta,
                    viewMatrix = input.frameContext.viewMatrix,
                    projMatrix = input.frameContext.projMatrix,
                )
            },
        )
    }

    private fun renderBloomMask(
        tickDelta: Float,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
    ) {
        initStatic()
        val bloomAlpha = currentBloomAlpha(tickDelta)
        if (bloomAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val scale = currentScale(tickDelta)
        val viewRotationMatrix = Matrix3f(viewMatrix)
        val inverseViewRotationMatrix = Matrix3f(viewRotationMatrix).invert()
        val blend = projectedBlend(
            scale = scale,
            cameraWorldPos = currentCameraWorldPos(),
            viewRotationMatrix = viewRotationMatrix,
            inverseViewRotationMatrix = inverseViewRotationMatrix,
            projMatrix = projMatrix,
            screenSize = currentScreenSize(),
        )
        val bloomScale = scale * mix(1.0f, 1.18f, 1.0f - blend.directWeight)
        val maskAlpha = bloomAlpha * max(0.42f, farBloomIntensityScale(blend.projectedRadiusPx, 18.0f, 2.0f))
        val modelMatrix = Matrix4fStack(16)
        RenderUtil.setRenderStackWithEntity(modelMatrix, this, tickDelta)
        renderPasses(
            modelMatrix = modelMatrix,
            viewMatrix = viewMatrix,
            projMatrix = projMatrix,
            inverseViewRotationMatrix = inverseViewRotationMatrix,
            scale = bloomScale,
            time = currentTimeline(tickDelta),
            roll = currentRoll(tickDelta),
            rayMorph = currentRayMorph(tickDelta),
            whiteCore = currentWhiteCore(tickDelta),
            twinkle = currentTwinkle(tickDelta),
            collapse = currentCollapse(tickDelta),
            passAlpha = maskAlpha,
            bloomPass = true,
        )
    }

    private fun renderPasses(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        inverseViewRotationMatrix: Matrix3f,
        scale: Float,
        time: Float,
        roll: Float,
        rayMorph: Float,
        whiteCore: Float,
        twinkle: Float,
        collapse: Float,
        passAlpha: Float,
        bloomPass: Boolean,
    ) {
        if (passAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            if (bloomPass) {
                RenderSystem.blendFunc(770, 1)
                drawPass(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    inverseViewRotationMatrix = inverseViewRotationMatrix,
                    scale = Vector2f(scale * 1.22f, scale * 1.22f),
                    time = time,
                    roll = roll,
                    depthOffset = -0.002f,
                    passColor = mixColor(color, Vector3f(1.0f, 0.96f, 0.84f), 0.26f),
                    alpha = passAlpha * 0.34f,
                    brightness = 2.9f,
                    haloStrength = 0.78f,
                    rayStrength = rayMorph,
                    raySharpness = 0.62f,
                    coreRadius = 0.30f,
                    whiteCore = whiteCore * 0.88f,
                    twinkle = twinkle,
                    collapse = collapse,
                )
                drawPass(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    inverseViewRotationMatrix = inverseViewRotationMatrix,
                    scale = Vector2f(scale * 1.74f, scale * 1.74f),
                    time = time,
                    roll = -roll * 1.18f,
                    depthOffset = 0.0015f,
                    passColor = mixColor(color, Vector3f(1.0f, 0.98f, 0.94f), 0.38f),
                    alpha = passAlpha * 0.18f,
                    brightness = 4.1f,
                    haloStrength = 1.12f,
                    rayStrength = mix(0.66f, 1.0f, rayMorph),
                    raySharpness = 0.42f,
                    coreRadius = 0.22f,
                    whiteCore = whiteCore * 0.56f,
                    twinkle = twinkle,
                    collapse = collapse,
                )
            } else {
                RenderSystem.blendFunc(770, 771)
                drawPass(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    inverseViewRotationMatrix = inverseViewRotationMatrix,
                    scale = Vector2f(scale, scale),
                    time = time,
                    roll = roll,
                    depthOffset = -0.002f,
                    passColor = color,
                    alpha = passAlpha * 0.42f,
                    brightness = 1.26f,
                    haloStrength = 0.34f,
                    rayStrength = rayMorph,
                    raySharpness = 0.78f,
                    coreRadius = 0.34f,
                    whiteCore = whiteCore * 0.62f,
                    twinkle = twinkle,
                    collapse = collapse,
                )
                RenderSystem.blendFunc(770, 1)
                drawPass(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    inverseViewRotationMatrix = inverseViewRotationMatrix,
                    scale = Vector2f(scale * 0.58f, scale * 0.58f),
                    time = time,
                    roll = -roll * 1.34f,
                    depthOffset = 0.0035f,
                    passColor = mixColor(color, Vector3f(1.0f, 0.98f, 0.90f), 0.44f),
                    alpha = passAlpha * 0.30f,
                    brightness = 2.7f,
                    haloStrength = 0.12f,
                    rayStrength = mix(0.52f, 0.76f, rayMorph),
                    raySharpness = 0.96f,
                    coreRadius = 0.48f,
                    whiteCore = whiteCore,
                    twinkle = twinkle,
                    collapse = collapse,
                )
            }
        } finally {
            RenderSystem.depthMask(true)
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableBlend()
            RenderSystem.enableCull()
        }
    }

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
            starVertexBuffer.draw()
        }
    }

    private fun updateLifecycle() {
        if (age > totalDurationTicks() + 1) {
            canceled = true
        }
    }

    private fun totalDurationTicks(): Int {
        return expandTicks.coerceAtLeast(1) + holdTicks.coerceAtLeast(0) + shrinkTicks.coerceAtLeast(1)
    }

    private fun currentTimeline(tickDelta: Float): Float {
        return (age - 1f + tickDelta).coerceAtLeast(0f)
    }

    private fun currentScale(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        val pulse = currentTwinkle(tickDelta)
        return when {
            time < expandDuration -> maxScale.coerceAtLeast(MIN_WORLD_SCALE) *
                    mix(0.16f, 1.0f, easeOutQuint(smoothstep(0f, expandDuration, time)))

            time < holdEnd -> maxScale.coerceAtLeast(MIN_WORLD_SCALE) * mix(0.96f, 1.08f, pulse)
            else -> {
                val collapse = smoothstep(0f, shrinkDuration, time - holdEnd)
                maxScale.coerceAtLeast(MIN_WORLD_SCALE) * mix(1.0f, 0.12f, collapse)
            }
        }
    }

    private fun currentBodyAlpha(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        val baseAlpha = alpha.toFloat().coerceIn(0f, 1f)
        return when {
            time < expandDuration -> mix(0.18f, 1.0f, easeOutCubic(smoothstep(0f, expandDuration, time))) * baseAlpha
            time < holdEnd -> mix(0.84f, 1.0f, currentTwinkle(tickDelta)) * baseAlpha
            else -> {
                val collapse = smoothstep(0f, shrinkDuration, time - holdEnd)
                val fade = 1f - collapse
                fade * fade * baseAlpha
            }
        }
    }

    private fun currentBloomAlpha(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        val baseAlpha = alpha.toFloat().coerceIn(0f, 1f)
        return when {
            time < expandDuration -> mix(0.28f, 1.0f, easeOutQuint(smoothstep(0f, expandDuration, time))) * baseAlpha
            time < holdEnd -> mix(0.92f, 1.12f, currentTwinkle(tickDelta)) * baseAlpha
            else -> {
                val collapse = smoothstep(0f, shrinkDuration, time - holdEnd)
                val fade = 1f - collapse
                fade * fade * fade * baseAlpha
            }
        }
    }

    private fun currentGlowAlpha(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        val baseAlpha = alpha.toFloat().coerceIn(0f, 1f)
        return when {
            time < expandDuration -> mix(0.22f, 0.94f, easeOutCubic(smoothstep(0f, expandDuration, time))) * baseAlpha
            time < holdEnd -> mix(0.86f, 1.08f, currentTwinkle(tickDelta)) * baseAlpha
            else -> {
                val collapse = smoothstep(0f, shrinkDuration, time - holdEnd)
                val fade = 1f - collapse
                fade * fade * baseAlpha
            }
        }
    }

    private fun currentRayMorph(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        return when {
            time < expandDuration -> mix(0.0f, 1.0f, easeOutCubic(smoothstep(0f, expandDuration, time)))
            time < holdEnd -> mix(0.84f, 1.0f, currentTwinkle(tickDelta))
            else -> mix(1.0f, 0.24f, smoothstep(0f, shrinkDuration, time - holdEnd))
        }
    }

    private fun currentWhiteCore(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        return when {
            time < expandDuration -> mix(0.22f, 1.0f, easeOutCubic(smoothstep(0f, expandDuration, time)))
            time < holdEnd -> mix(0.76f, 1.0f, currentTwinkle(tickDelta))
            else -> mix(1.0f, 0.42f, smoothstep(0f, shrinkDuration, time - holdEnd))
        }
    }

    private fun currentTwinkle(tickDelta: Float): Float {
        return 0.5f + 0.5f * sin(currentTimeline(tickDelta) * 0.72f + 0.8f)
    }

    private fun currentCollapse(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        return if (time < holdEnd) {
            0f
        } else {
            smoothstep(0f, shrinkDuration, time - holdEnd)
        }
    }

    private fun currentRoll(tickDelta: Float): Float {
        return currentTimeline(tickDelta) * spinSpeed
    }

    private fun centerPosition(): Vector3f {
        return Vector3f(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
    }

    private fun glowWorldRadius(scale: Float): Float {
        return max(MIN_WORLD_SCALE, scale * 0.84f)
    }

    private fun projectedBlend(
        scale: Float,
        cameraWorldPos: Vector3f,
        viewRotationMatrix: Matrix3f,
        inverseViewRotationMatrix: Matrix3f,
        projMatrix: Matrix4f,
        screenSize: Vector2f,
    ): BillboardProjectedBlend {
        val distance = Vector3f(centerPosition()).sub(cameraWorldPos).length().coerceAtLeast(0.125f)
        val screenHeight = screenSize.y.coerceAtLeast(1.0f)
        val projectedRadiusPx = glowWorldRadius(scale) / distance * screenHeight * 0.92f
        return BillboardProjectedBlend(
            projectedRadiusPx = projectedRadiusPx,
            directWeight = smoothstep(5.0f, 20.0f, projectedRadiusPx),
        )
    }

    private fun farBloomIntensityScale(
        projectedRadiusPx: Float,
        compensationFadeStartPx: Float,
        maxCompensationPx: Float,
    ): Float {
        val farWeight = 1f - smoothstep(4.0f, compensationFadeStartPx, projectedRadiusPx)
        return 1f + farWeight * maxCompensationPx * 0.18f
    }

    private fun currentCameraWorldPos(): Vector3f {
        val cameraPos = Minecraft.getInstance().gameRenderer.mainCamera.position
        return Vector3f(cameraPos.x.toFloat(), cameraPos.y.toFloat(), cameraPos.z.toFloat())
    }

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

    companion object {
        private const val DEFAULT_MAX_SCALE = 3.5f
        private const val DEFAULT_EXPAND_TICKS = 4
        private const val DEFAULT_HOLD_TICKS = 8
        private const val DEFAULT_SHRINK_TICKS = 6
        private const val DEFAULT_SPIN_SPEED = 0.24f
        private const val BILLBOARD_EXTENT = 2.4f
        private const val MIN_VISIBLE_ALPHA = 0.001f
        private const val MIN_WORLD_SCALE = 0.05f
        private const val STAR_BLOOM_EFFECT_ID = "usefulmagic:billboard_star_bloom"
        private const val STAR_BLOOM_PRIORITY = 240

        @JvmField
        val CODEC: StreamCodec<FriendlyByteBuf, RenderEntity> = RenderEntity.createCodec(::BillboardStarRenderEntity)

        @JvmField
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "billboard_star_render_entity")

        @JvmField
        var initialized: Boolean = false

        private val STAR_BLOOM_CONFIG = MaskBloomConfig(
            blurSigma = 13.0f,
            blurRange = 9.5f,
            intensity = 4.6f,
            baseMaskIntensity = 0.36f,
            threshold = 0.0f,
            thresholdSoftness = 0.02f,
            tint = Vector3f(1.0f, 0.94f, 0.78f),
        )

        private lateinit var starVertexBuffer: SimpleVertexBuffer
        private lateinit var starShader: CooShaderProgram

        @JvmStatic
        fun initStatic() {
            if (initialized) {
                return
            }
            starVertexBuffer = SimpleVertexBuffer().apply {
                init()
                setVertexes(buildQuadVertices(), CooVertexFormat.POINT_FORMAT)
            }
            starShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/vsh/billboard_star.vsh"),
                        GlShaderType.VERTEX,
                    )
                )
                .fragment(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/fsh/billboard_star.fsh"),
                        GlShaderType.FRAGMENT,
                    )
                )
                .build()
            starShader.init()
            initialized = true
        }

        private fun buildQuadVertices(): List<VertexData> {
            return listOf(
                VertexData(Vector3f(-BILLBOARD_EXTENT, -BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f()),
                VertexData(Vector3f(BILLBOARD_EXTENT, -BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f()),
                VertexData(Vector3f(BILLBOARD_EXTENT, BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f()),
                VertexData(Vector3f(-BILLBOARD_EXTENT, -BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f()),
                VertexData(Vector3f(BILLBOARD_EXTENT, BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f()),
                VertexData(Vector3f(-BILLBOARD_EXTENT, BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f()),
            )
        }

        private fun smoothstep(edge0: Float, edge1: Float, value: Float): Float {
            if (edge0 == edge1) {
                return if (value >= edge1) 1f else 0f
            }
            val x = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
            return x * x * (3f - 2f * x)
        }

        private fun easeOutCubic(value: Float): Float {
            val x = value.coerceIn(0f, 1f)
            val inverse = 1f - x
            return 1f - inverse * inverse * inverse
        }

        private fun easeOutQuint(value: Float): Float {
            val x = value.coerceIn(0f, 1f)
            val inverse = 1f - x
            return 1f - inverse * inverse * inverse * inverse * inverse
        }

        private fun mix(from: Float, to: Float, alpha: Float): Float {
            return from + (to - from) * alpha.coerceIn(0f, 1f)
        }

        private fun mixColor(a: Vector3f, b: Vector3f, alpha: Float): Vector3f {
            val t = alpha.coerceIn(0f, 1f)
            return Vector3f(
                mix(a.x, b.x, t),
                mix(a.y, b.y, t),
                mix(a.z, b.z, t),
            )
        }
    }
}
