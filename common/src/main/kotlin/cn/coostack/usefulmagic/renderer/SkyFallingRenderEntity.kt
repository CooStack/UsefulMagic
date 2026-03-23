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
import cn.coostack.usefulmagic.items.prop.SkyFallingRuneItem
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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

enum class SkyFallingStage {
    EXPAND,
    HOLD,
    SHRINK,
}

data class SkyFallingCylinderState(
    val phase: SkyFallingStage,
    val phaseProgress: Float,
    val radius: Float,
    val height: Float,
    val opacity: Float,
    val bloomAlpha: Float,
    val glowAlpha: Float,
    val collapse: Float,
)

@CooAutoRegister
class SkyFallingRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos),
    WorldPassRenderEntityRenderer<SkyFallingRenderEntity>,
    FramePostRenderEntityRenderer<SkyFallingRenderEntity>,
    RenderEntityReleaseHook<SkyFallingRenderEntity> {
    @CodecField
    var color: Vector3f = Vector3f(56f / 255f, 104f / 255f, 1.0f)

    @CodecField
    var r: Vector3f = Vector3f(1f, CYLINDER_HEIGHT, 1f)

    @CodecField
    var alpha: Double = 1.0

    var prevR: Vector3f = Vector3f(r)

    @CodecField
    var speed: Float = 0f

    override fun initialize(instance: RenderEntityInstance<SkyFallingRenderEntity>) {
        initStatic()
        val state = buildState(0f, alpha.toFloat())
        r.set(state.radius, state.height, state.radius)
        prevR.set(r)
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun release(instance: RenderEntityInstance<SkyFallingRenderEntity>) {
    }

    override fun clientTick() {
        updateCylinderState()
    }

    override fun serverTick() {
        updateCylinderState()
    }

    override fun renderLocal(input: LocalRenderInput<SkyFallingRenderEntity>) {
        initStatic()
        val timeline = currentTimeline(input.tickDelta)
        val state = buildState(timeline, alpha.toFloat())
        if (state.opacity <= MIN_VISIBLE_ALPHA) {
            return
        }
        val compensation = buildGlowCompensation(
            projectedRadiusPx = projectedRadiusPx(
                state = state,
                cameraWorldPos = currentCameraWorldPos(),
                viewRotationMatrix = Matrix3f(input.viewMatrix),
                inverseViewRotationMatrix = Matrix3f(input.viewMatrix).invert(),
                projMatrix = input.projMatrix,
                screenSize = currentScreenSize(),
            ),
        )
        val frame = buildFrame(
            state = state,
            compensation = compensation,
            flowTime = currentFlowTime(timeline),
        )
        renderPasses(
            modelMatrix = input.modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            passes = frame.directPasses,
        )
    }

    override fun collectRenderContributions(
        input: RenderContributionInput<SkyFallingRenderEntity>,
        collector: RenderContributionCollector,
    ) {
        collector.submit(
            BuiltinRenderEffectDescriptors.maskBloom(
                effectId = SKY_FALLING_BLOOM_EFFECT_ID,
                sourceInstanceId = uuid.toString(),
                frameContext = input.frameContext,
                sourceEntity = this,
                config = SKY_FALLING_BLOOM_CONFIG,
                priority = SKY_FALLING_BLOOM_PRIORITY,
                requiredCapabilities = UsefulMagicShaderPipelines.DEPTH_AWARE_MASK_BLOOM_CAPABILITIES,
            ) {
                renderBloomMask(
                    tickDelta = input.frameContext.tickDelta,
                    viewMatrix = input.frameContext.viewMatrix,
                    projMatrix = input.frameContext.projMatrix,
                )
            }
        )
    }

    private fun buildFrame(
        state: SkyFallingCylinderState,
        compensation: SkyFallingGlowCompensation,
        flowTime: Float,
    ): SkyFallingFrame {
        val palette = buildPalette(color)
        val shellRadius = state.radius.coerceAtLeast(MIN_RADIUS)
        val coreRadius = max(shellRadius * 0.58f, 1.3f)
        val bloomRadius = shellRadius * 1.08f
        val auraRadius = shellRadius * 1.18f

        val directPasses = listOf(
            SkyFallingRenderPass(
                blendMode = SkyFallingBlendMode.ALPHA,
                color = palette.shell,
                scale = Vector3f(shellRadius, state.height, shellRadius),
                alpha = (state.opacity * 0.46f * compensation.directScale).coerceAtMost(0.82f),
                brightness = 1.45f,
                rimPower = 1.20f,
                coreBias = 0.28f,
                highlightStrength = 0.70f,
                impactStrength = 0.72f,
                textureScale = 1.0f,
                textureSpeed = 0.96f,
                phaseProgress = state.phaseProgress,
                collapse = state.collapse,
                time = flowTime,
            ),
            SkyFallingRenderPass(
                blendMode = SkyFallingBlendMode.ADDITIVE,
                color = palette.core,
                scale = Vector3f(coreRadius, state.height, coreRadius),
                alpha = (state.opacity * 0.24f * compensation.directScale).coerceAtMost(0.48f),
                brightness = 2.85f,
                rimPower = 0.92f,
                coreBias = 0.82f,
                highlightStrength = 0.92f,
                impactStrength = 0.84f,
                textureScale = 1.12f,
                textureSpeed = 1.14f,
                phaseProgress = state.phaseProgress,
                collapse = state.collapse,
                time = flowTime,
            ),
        ).filter { it.alpha > MIN_VISIBLE_ALPHA }

        val bloomPasses = listOf(
            SkyFallingRenderPass(
                blendMode = SkyFallingBlendMode.ADDITIVE,
                color = palette.shell,
                scale = Vector3f(bloomRadius, state.height, bloomRadius),
                alpha = (state.bloomAlpha * 0.34f * compensation.bloomScale).coerceAtMost(0.78f),
                brightness = 2.65f,
                rimPower = 1.36f,
                coreBias = 0.34f,
                highlightStrength = 0.74f,
                impactStrength = 0.92f,
                textureScale = 1.0f,
                textureSpeed = 0.88f,
                phaseProgress = state.phaseProgress,
                collapse = state.collapse,
                time = flowTime,
            ),
            SkyFallingRenderPass(
                blendMode = SkyFallingBlendMode.ADDITIVE,
                color = palette.core,
                scale = Vector3f(max(shellRadius * 0.72f, 1.8f), state.height, max(shellRadius * 0.72f, 1.8f)),
                alpha = (state.bloomAlpha * 0.28f * compensation.bloomScale).coerceAtMost(0.74f),
                brightness = 4.35f,
                rimPower = 0.86f,
                coreBias = 0.90f,
                highlightStrength = 0.98f,
                impactStrength = 0.88f,
                textureScale = 1.18f,
                textureSpeed = 1.18f,
                phaseProgress = state.phaseProgress,
                collapse = state.collapse,
                time = flowTime,
            ),
            SkyFallingRenderPass(
                blendMode = SkyFallingBlendMode.ADDITIVE,
                color = palette.aura,
                scale = Vector3f(auraRadius, state.height, auraRadius),
                alpha = (state.bloomAlpha * 0.18f * compensation.bloomScale).coerceAtMost(0.58f),
                brightness = 3.45f,
                rimPower = 1.62f,
                coreBias = 0.20f,
                highlightStrength = 0.78f,
                impactStrength = 1.0f,
                textureScale = 0.92f,
                textureSpeed = 0.76f,
                phaseProgress = state.phaseProgress,
                collapse = state.collapse,
                time = flowTime,
            ),
        ).filter { it.alpha > MIN_VISIBLE_ALPHA }

        return SkyFallingFrame(
            directPasses = directPasses,
            bloomPasses = bloomPasses,
        )
    }

    private fun buildGlowCompensation(projectedRadiusPx: Float): SkyFallingGlowCompensation {
        val farIntensityScale = farBloomIntensityScale(projectedRadiusPx, 30f, 3.2f)
        val coverage = smoothstep(260f, 1800f, projectedRadiusPx)
        return SkyFallingGlowCompensation(
            directScale = mix(1.0f, 0.88f, coverage),
            bloomScale = mix(1.36f, 0.92f, coverage) * farIntensityScale,
        )
    }

    private fun projectedRadiusPx(
        state: SkyFallingCylinderState,
        cameraWorldPos: Vector3f,
        viewRotationMatrix: Matrix3f,
        inverseViewRotationMatrix: Matrix3f,
        projMatrix: Matrix4f,
        screenSize: Vector2f,
    ): Float {
        val distance = Vector3f(centerPosition(state)).sub(cameraWorldPos).length().coerceAtLeast(0.125f)
        val screenHeight = screenSize.y.coerceAtLeast(1.0f)
        return state.radius.coerceAtLeast(MIN_RADIUS) / distance * screenHeight * 0.92f
    }

    private fun farBloomIntensityScale(
        projectedRadiusPx: Float,
        compensationFadeStartPx: Float,
        maxCompensationPx: Float,
    ): Float {
        val farWeight = 1f - smoothstep(4.0f, compensationFadeStartPx, projectedRadiusPx)
        return 1f + farWeight * maxCompensationPx * 0.18f
    }

    private fun renderBloomMask(
        tickDelta: Float,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
    ) {
        initStatic()
        val timeline = currentTimeline(tickDelta)
        val state = buildState(timeline, alpha.toFloat())
        if (state.bloomAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val compensation = buildGlowCompensation(
            projectedRadiusPx = projectedRadiusPx(
                state = state,
                cameraWorldPos = currentCameraWorldPos(),
                viewRotationMatrix = Matrix3f(viewMatrix),
                inverseViewRotationMatrix = Matrix3f(viewMatrix).invert(),
                projMatrix = projMatrix,
                screenSize = currentScreenSize(),
            ),
        )
        val frame = buildFrame(
            state = state,
            compensation = compensation,
            flowTime = currentFlowTime(timeline),
        )
        if (frame.bloomPasses.isEmpty()) {
            return
        }
        val modelMatrix = Matrix4fStack(16)
        RenderUtil.setRenderStackWithEntity(modelMatrix, this, tickDelta)
        renderPasses(
            modelMatrix = modelMatrix,
            viewMatrix = viewMatrix,
            projMatrix = projMatrix,
            passes = frame.bloomPasses,
        )
    }

    private fun renderPasses(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        passes: List<SkyFallingRenderPass>,
    ) {
        if (passes.isEmpty()) {
            return
        }
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            passes.forEach { pass ->
                when (pass.blendMode) {
                    SkyFallingBlendMode.ALPHA -> RenderSystem.blendFunc(770, 771)
                    SkyFallingBlendMode.ADDITIVE -> RenderSystem.blendFunc(770, 1)
                }
                drawPass(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    pass = pass,
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
        pass: SkyFallingRenderPass,
    ) {
        if (pass.alpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        beamShader.useOnContext {
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat3("scale", pass.scale)
            setFloat("offsetY", 0f)
            setFloat3("color", pass.color)
            setFloat("alpha", pass.alpha)
            setFloat("brightness", pass.brightness)
            setFloat("rimPower", pass.rimPower)
            setFloat("coreBias", pass.coreBias)
            setFloat("highlightStrength", pass.highlightStrength)
            setFloat("impactStrength", pass.impactStrength)
            setFloat("textureScale", pass.textureScale)
            setFloat("textureSpeed", pass.textureSpeed)
            setFloat("phaseProgress", pass.phaseProgress)
            setFloat("collapse", pass.collapse)
            setFloat("time", pass.time)
            beamVertexBuffer.draw()
        }
    }

    private fun updateCylinderState() {
        prevR.set(r)
        val state = buildState(age.toFloat(), alpha.toFloat())
        r.set(state.radius, state.height, state.radius)
        if (age > MAX_AGE) {
            canceled = true
        }
    }

    private fun basePosition(): Vector3f {
        return Vector3f(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
    }

    private fun centerPosition(state: SkyFallingCylinderState): Vector3f {
        return Vector3f(pos.x.toFloat(), pos.y.toFloat() + state.height * 0.5f, pos.z.toFloat())
    }

    private fun currentTimeline(tickDelta: Float): Float {
        return (age - 1f + tickDelta).coerceAtLeast(0f)
    }

    private fun currentFlowTime(timeline: Float): Float {
        val speedScale = (1.0f + speed * 0.08f).coerceAtLeast(0.12f)
        return timeline * speedScale
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
        const val MAX_AGE = 168
        const val CYLINDER_HEIGHT = 200f
        const val EXPAND_END = 24f
        const val HOLD_END = 132f
        val MAX_RADIUS = SkyFallingRuneItem.EXPLOSION_MAX_RADIUS.toFloat()
        private const val MIN_RADIUS = 0.75f
        private const val MIN_VISIBLE_ALPHA = 0.001f

        @JvmField
        val CODEC: StreamCodec<FriendlyByteBuf, RenderEntity> = RenderEntity.createCodec(::SkyFallingRenderEntity)

        @JvmField
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "sky_falling_render_entity")

        @JvmField
        var initialized: Boolean = false

        private val SKY_FALLING_BLOOM_CONFIG = MaskBloomConfig(
            blurSigma = 15.5f,
            blurRange = 11.0f,
            intensity = 4.8f,
            baseMaskIntensity = 0.38f,
            threshold = 0.0f,
            thresholdSoftness = 0.02f,
            tint = Vector3f(0.72f, 0.84f, 1.0f),
        )
        private const val SKY_FALLING_BLOOM_EFFECT_ID = "usefulmagic:sky_falling_bloom"
        private const val SKY_FALLING_BLOOM_PRIORITY = 260

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
                        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/fsh/sky_falling_beam.fsh"),
                        GlShaderType.FRAGMENT,
                    )
                )
                .build()
            beamShader.init()
            initialized = true
        }

        @JvmStatic
        fun buildState(time: Float, alpha: Float): SkyFallingCylinderState {
            val clampedTime = time.coerceIn(0f, MAX_AGE.toFloat())
            val clampedAlpha = alpha.coerceIn(0f, 1f)
            return when {
                clampedTime < EXPAND_END -> buildExpandState(clampedTime, clampedAlpha)
                clampedTime < HOLD_END -> buildHoldState(clampedAlpha)
                else -> buildShrinkState(clampedTime, clampedAlpha)
            }
        }

        private fun buildExpandState(time: Float, alpha: Float): SkyFallingCylinderState {
            val phase = smoothstep(0f, EXPAND_END, time)
            val eased = easeOutQuint(phase)
            return SkyFallingCylinderState(
                phase = SkyFallingStage.EXPAND,
                phaseProgress = phase,
                radius = mix(MIN_RADIUS, MAX_RADIUS, eased),
                height = CYLINDER_HEIGHT,
                opacity = mix(0.16f, 1.0f, easeOutCubic(phase)) * alpha,
                bloomAlpha = mix(0.34f, 1.0f, eased) * alpha,
                glowAlpha = mix(0.24f, 0.94f, eased) * alpha,
                collapse = 0f,
            )
        }

        private fun buildHoldState(alpha: Float): SkyFallingCylinderState {
            return SkyFallingCylinderState(
                phase = SkyFallingStage.HOLD,
                phaseProgress = 1f,
                radius = MAX_RADIUS,
                height = CYLINDER_HEIGHT,
                opacity = alpha,
                bloomAlpha = alpha,
                glowAlpha = 0.92f * alpha,
                collapse = 0f,
            )
        }

        private fun buildShrinkState(time: Float, alpha: Float): SkyFallingCylinderState {
            val phase = ((time - HOLD_END) / (MAX_AGE - HOLD_END)).coerceIn(0f, 1f)
            val collapse = smoothstep(0f, 1f, phase)
            val fade = 1f - collapse
            return SkyFallingCylinderState(
                phase = SkyFallingStage.SHRINK,
                phaseProgress = phase,
                radius = mix(MAX_RADIUS, 6f, easeOutCubic(phase)),
                height = CYLINDER_HEIGHT,
                opacity = fade * fade * alpha,
                bloomAlpha = fade * fade * fade * alpha,
                glowAlpha = fade * fade * fade * alpha,
                collapse = collapse,
            )
        }

        private fun buildPalette(source: Vector3f): SkyFallingPalette {
            val base = mixColor(source, Vector3f(0.14f, 0.36f, 1.0f), 0.74f)
            return SkyFallingPalette(
                shell = mixColor(base, Vector3f(0.34f, 0.20f, 0.96f), 0.22f),
                core = mixColor(base, Vector3f(0.90f, 0.95f, 1.0f), 0.30f),
                aura = mixColor(base, Vector3f(0.12f, 0.24f, 0.98f), 0.16f),
                impact = mixColor(base, Vector3f(0.72f, 0.82f, 1.0f), 0.18f),
            )
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

private enum class SkyFallingBlendMode {
    ALPHA,
    ADDITIVE,
}

private data class SkyFallingRenderPass(
    val blendMode: SkyFallingBlendMode,
    val color: Vector3f,
    val scale: Vector3f,
    val alpha: Float,
    val brightness: Float,
    val rimPower: Float,
    val coreBias: Float,
    val highlightStrength: Float,
    val impactStrength: Float,
    val textureScale: Float,
    val textureSpeed: Float,
    val phaseProgress: Float,
    val collapse: Float,
    val time: Float,
)

private data class SkyFallingGlowCompensation(
    val directScale: Float,
    val bloomScale: Float,
)

private data class SkyFallingPalette(
    val shell: Vector3f,
    val core: Vector3f,
    val aura: Vector3f,
    val impact: Vector3f,
)

private data class SkyFallingFrame(
    val directPasses: List<SkyFallingRenderPass>,
    val bloomPasses: List<SkyFallingRenderPass>,
)
