package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.renderer.AutoRenderEntity
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
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Matrix4fStack
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private enum class ExplosionBeamStage {
    EXPAND,
    HOLD,
    SHRINK,
}

private data class ExplosionBeamState(
    val phase: ExplosionBeamStage,
    val phaseProgress: Float,
    val radius: Float,
    val height: Float,
    val offsetY: Float,
    val opacity: Float,
    val brightness: Float,
    val collapse: Float,
)

private enum class ExplosionBeamBlendMode {
    ALPHA,
    ADDITIVE,
}

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

@CooAutoRegister
class ExplosionBeamRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos),
    WorldPassRenderEntityRenderer<ExplosionBeamRenderEntity>,
    FramePostRenderEntityRenderer<ExplosionBeamRenderEntity>,
    RenderEntityReleaseHook<ExplosionBeamRenderEntity> {
    @CodecField
    var color: Vector3f = Vector3f(1.0f, 0.20f, 0.06f)

    @CodecField
    var alpha: Double = 1.0

    @CodecField
    var beamHeight: Float = DEFAULT_BEAM_HEIGHT

    @CodecField
    var maxRadius: Float = DEFAULT_MAX_RADIUS

    @CodecField
    var expandTicks: Int = DEFAULT_EXPAND_TICKS

    @CodecField
    var holdTicks: Int = DEFAULT_HOLD_TICKS

    @CodecField
    var shrinkTicks: Int = DEFAULT_SHRINK_TICKS

    override fun initialize(instance: RenderEntityInstance<ExplosionBeamRenderEntity>) {
        initStatic()
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun release(instance: RenderEntityInstance<ExplosionBeamRenderEntity>) {
    }

    override fun clientTick() {
        updateLifecycle()
    }

    override fun serverTick() {
        updateLifecycle()
    }

    override fun renderLocal(input: LocalRenderInput<ExplosionBeamRenderEntity>) {
        initStatic()
        val timeline = currentTimeline(input.tickDelta)
        val state = buildState(timeline)
        if (state.opacity <= MIN_VISIBLE_ALPHA) {
            return
        }
        val modelMatrix = input.modelMatrix
        renderPasses(
            modelMatrix = modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            state = state,
            time = timeline,
        )
    }

    override fun collectRenderContributions(
        input: RenderContributionInput<ExplosionBeamRenderEntity>,
        collector: RenderContributionCollector,
    ) {
        val timeline = currentTimeline(input.frameContext.tickDelta)
        val state = buildState(timeline)
        if (state.opacity <= MIN_VISIBLE_ALPHA) {
            return
        }
        collector.submit(
            BuiltinRenderEffectDescriptors.maskBloom(
                effectId = EXPLOSION_BEAM_BLOOM_EFFECT_ID,
                sourceInstanceId = uuid.toString(),
                frameContext = input.frameContext,
                sourceEntity = this,
                config = buildBloomConfig(state),
                priority = EXPLOSION_BEAM_BLOOM_PRIORITY,
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
        val timeline = currentTimeline(tickDelta)
        val state = buildState(timeline)
        if (state.opacity <= MIN_VISIBLE_ALPHA) {
            return
        }
        val modelMatrix = Matrix4fStack(16)
        RenderUtil.setRenderStackWithEntity(modelMatrix, this, tickDelta)
        renderPasses(
            modelMatrix = modelMatrix,
            viewMatrix = viewMatrix,
            projMatrix = projMatrix,
            state = state,
            time = timeline,
        )
    }

    private fun buildBloomConfig(state: ExplosionBeamState): MaskBloomConfig {
        return EXPLOSION_BEAM_BLOOM_CONFIG.copy(
            intensity = EXPLOSION_BEAM_BLOOM_CONFIG.intensity * state.opacity.coerceIn(0.25f, 1.0f),
            tint = Vector3f(color).lerp(Vector3f(1.0f, 0.62f, 0.28f), 0.34f),
        )
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

    private fun buildState(time: Float): ExplosionBeamState {
        val expandDuration = expandTicks.coerceAtLeast(1).toFloat()
        val holdDuration = holdTicks.coerceAtLeast(0).toFloat()
        val shrinkDuration = shrinkTicks.coerceAtLeast(1).toFloat()
        val holdEnd = expandDuration + holdDuration
        val totalDuration = holdEnd + shrinkDuration
        val clampedTime = time.coerceIn(0f, totalDuration)
        val clampedAlpha = alpha.toFloat().coerceIn(0f, 1f)
        return when {
            clampedTime < expandDuration -> {
                val phase = smoothstep(0f, expandDuration, clampedTime)
                val eased = easeOutCubic(phase)
                val currentHeight = mix(MIN_HEIGHT, beamHeight, eased)
                ExplosionBeamState(
                    phase = ExplosionBeamStage.EXPAND,
                    phaseProgress = phase,
                    radius = maxRadius.coerceAtLeast(MIN_RADIUS),
                    height = currentHeight,
                    offsetY = (beamHeight - currentHeight).coerceAtLeast(0f),
                    opacity = mix(0.18f, 1.0f, eased) * clampedAlpha,
                    brightness = mix(1.6f, 2.9f, eased),
                    collapse = 0f,
                )
            }

            clampedTime < holdEnd -> ExplosionBeamState(
                phase = ExplosionBeamStage.HOLD,
                phaseProgress = 1f,
                radius = maxRadius.coerceAtLeast(MIN_RADIUS),
                height = beamHeight,
                offsetY = 0f,
                opacity = clampedAlpha,
                brightness = 2.9f,
                collapse = 0f,
            )

            else -> {
                val phase = ((clampedTime - holdEnd) / shrinkDuration).coerceIn(0f, 1f)
                val collapse = smoothstep(0f, 1f, phase)
                val fade = 1f - collapse
                ExplosionBeamState(
                    phase = ExplosionBeamStage.SHRINK,
                    phaseProgress = phase,
                    radius = mix(maxRadius.coerceAtLeast(MIN_RADIUS), MIN_RADIUS, collapse),
                    height = mix(beamHeight, beamHeight * 0.82f, collapse),
                    offsetY = mix(0f, beamHeight * 0.09f, collapse),
                    opacity = fade * fade * clampedAlpha,
                    brightness = mix(2.9f, 1.0f, collapse),
                    collapse = collapse,
                )
            }
        }
    }

    private fun renderPasses(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        state: ExplosionBeamState,
        time: Float,
    ) {
        val outerPass = ExplosionBeamPass(
            blendMode = ExplosionBeamBlendMode.ALPHA,
            color = color,
            alpha = (state.opacity * 0.42f).coerceAtMost(0.82f),
            brightness = state.brightness,
            rimPower = 1.12f,
            coreBias = 0.32f,
            highlightStrength = 0.72f,
            impactStrength = 0.84f,
            textureScale = 1.0f,
            textureSpeed = 1.08f,
        )
        val corePass = ExplosionBeamPass(
            blendMode = ExplosionBeamBlendMode.ADDITIVE,
            color = Vector3f(1.0f, 0.72f, 0.36f),
            alpha = (state.opacity * 0.24f).coerceAtMost(0.56f),
            brightness = state.brightness * 1.35f,
            rimPower = 0.86f,
            coreBias = 0.88f,
            highlightStrength = 0.96f,
            impactStrength = 0.92f,
            textureScale = 1.14f,
            textureSpeed = 1.24f,
        )
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
                    ExplosionBeamBlendMode.ALPHA -> RenderSystem.blendFunc(770, 771)
                    ExplosionBeamBlendMode.ADDITIVE -> RenderSystem.blendFunc(770, 1)
                }
                drawPass(modelMatrix, viewMatrix, projMatrix, state, time, pass)
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
        state: ExplosionBeamState,
        time: Float,
        pass: ExplosionBeamPass,
    ) {
        val radius = state.radius.coerceAtLeast(MIN_RADIUS)
        beamShader.useOnContext {
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat3("scale", Vector3f(radius, state.height.coerceAtLeast(MIN_HEIGHT), radius))
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
            beamVertexBuffer.draw()
        }
    }

    companion object {
        private const val DEFAULT_BEAM_HEIGHT = 150f
        private const val DEFAULT_MAX_RADIUS = 2f
        private const val DEFAULT_EXPAND_TICKS = 5
        private const val DEFAULT_HOLD_TICKS = 10
        private const val DEFAULT_SHRINK_TICKS = 5
        private const val MIN_RADIUS = 0.02f
        private const val MIN_HEIGHT = 0.05f
        private const val MIN_VISIBLE_ALPHA = 0.001f
        private const val EXPLOSION_BEAM_BLOOM_EFFECT_ID = "usefulmagic:explosion_beam_bloom"
        private const val EXPLOSION_BEAM_BLOOM_PRIORITY = 250

        @JvmField
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "explosion_beam_render_entity")

        @JvmField
        var initialized: Boolean = false

        private val EXPLOSION_BEAM_BLOOM_CONFIG = MaskBloomConfig(
            blurSigma = 7.2f,
            blurRange = 5.8f,
            intensity = 2.2f,
            baseMaskIntensity = 0.0f,
            threshold = 0.015f,
            thresholdSoftness = 0.02f,
            tint = Vector3f(1.0f, 0.46f, 0.20f),
        )

        private lateinit var beamVertexBuffer: SimpleVertexBuffer
        private lateinit var beamShader: CooShaderProgram

        @JvmStatic
        fun initStatic() {
            if (initialized) {
                return
            }
            beamVertexBuffer = SimpleVertexBuffer().apply {
                init()
                setVertexes(buildCylinderVertices(), CooVertexFormat.POINT_FORMAT)
            }
            beamShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/vsh/sky_falling_beam.vsh"),
                        GlShaderType.VERTEX,
                    )
                )
                .fragment(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/fsh/explosion_beam.fsh"),
                        GlShaderType.FRAGMENT,
                    )
                )
                .build()
            beamShader.init()
            initialized = true
        }

        private fun buildCylinderVertices(): List<VertexData> {
            val segments = 48
            val vertices = ArrayList<VertexData>(segments * 12)
            val bottomCenter = Vector3f(0f, 0f, 0f)
            val topCenter = Vector3f(0f, 1f, 0f)

            for (segment in 0 until segments) {
                val angle0 = (Math.PI.toFloat() * 2f * segment) / segments.toFloat()
                val angle1 = (Math.PI.toFloat() * 2f * (segment + 1)) / segments.toFloat()
                val x0 = cos(angle0)
                val z0 = sin(angle0)
                val x1 = cos(angle1)
                val z1 = sin(angle1)

                val a = Vector3f(x0, 0f, z0)
                val b = Vector3f(x1, 0f, z1)
                val c = Vector3f(x1, 1f, z1)
                val d = Vector3f(x0, 1f, z0)

                appendQuad(vertices, a, b, c, d)
                appendTriangle(vertices, topCenter, d, c)
                appendTriangle(vertices, bottomCenter, b, a)
            }
            return vertices
        }

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

        private fun mix(from: Float, to: Float, alpha: Float): Float {
            return from + (to - from) * alpha.coerceIn(0f, 1f)
        }
    }
}
