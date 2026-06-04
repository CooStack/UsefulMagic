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
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.renderer.shader.ShaderProgramBuilder
import cn.coostack.cooparticlesapi.renderer.shader.api.CooShaderProgram
import cn.coostack.cooparticlesapi.renderer.shader.api.glsl.GlShaderType
import cn.coostack.cooparticlesapi.renderer.shader.data.CooVertexFormat
import cn.coostack.cooparticlesapi.renderer.shader.glsl.IdentifierShader
import cn.coostack.cooparticlesapi.renderer.shader.vertex.SimpleVertexBuffer
import cn.coostack.cooparticlesapi.renderer.utils.ShaderUtil
import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Matrix4fStack
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.opengl.GL33
import kotlin.math.max
import kotlin.math.sin

@CooAutoRegister
class MeteoriteAtmosphereFireRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos),
    WorldPassRenderEntityRenderer<MeteoriteAtmosphereFireRenderEntity>,
    FramePostRenderEntityRenderer<MeteoriteAtmosphereFireRenderEntity>,
    RenderEntityReleaseHook<MeteoriteAtmosphereFireRenderEntity> {
    @CodecField
    var direction: Vec3 = DEFAULT_DIRECTION

    @CodecField
    var color: Vector3f = Vector3f(1.0f, 0.42f, 0.10f)

    @CodecField
    var size: Float = DEFAULT_SIZE

    @CodecField
    var alpha: Double = 1.0

    @CodecField
    var lifetime: Int = DEFAULT_LIFETIME

    @CodecField
    var fadeInTicks: Int = DEFAULT_FADE_IN_TICKS

    @CodecField
    var fadeOutTicks: Int = DEFAULT_FADE_OUT_TICKS

    @CodecField
    var flowSpeed: Float = DEFAULT_FLOW_SPEED

    @CodecField
    var bloomStrength: Float = 1.0f

    @CodecField
    var discarding: Boolean = false

    @CodecField
    var discardStartTick: Int = 0

    @CodecField
    var discardTicks: Int = DEFAULT_FADE_OUT_TICKS

    init {
        updateRenderRange()
    }

    override fun initialize(instance: RenderEntityInstance<MeteoriteAtmosphereFireRenderEntity>) {
        initStatic()
        updateRenderRange()
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun release(instance: RenderEntityInstance<MeteoriteAtmosphereFireRenderEntity>) {
    }

    override fun clientTick() {
        updateLifecycle()
    }

    override fun serverTick() {
        updateLifecycle()
    }

    override fun renderLocal(input: LocalRenderInput<MeteoriteAtmosphereFireRenderEntity>) {
        initStatic()
        val visibleAlpha = currentAlpha(input.tickDelta)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val radius = size.coerceAtLeast(MIN_SIZE)
        val time = currentTime(input.tickDelta)
        val pulse = currentPulse(input.tickDelta)
        renderPasses(
            modelMatrix = orientedModelMatrix(input.modelMatrix),
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            passes = listOf(
                MeteoriteAtmosphereFirePass(
                    blendMode = MeteoriteAtmosphereFireBlendMode.ALPHA,
                    radius = Vector3f(radius * 1.02f, radius * 0.82f, radius * 1.02f),
                    color = normalizedColor(),
                    alpha = visibleAlpha * 0.46f,
                    brightness = 1.28f,
                    frontPower = 1.10f,
                    noiseScale = 1.0f,
                    time = time,
                    pulse = pulse,
                    passMode = PASS_SURFACE,
                ),
                MeteoriteAtmosphereFirePass(
                    blendMode = MeteoriteAtmosphereFireBlendMode.ADDITIVE,
                    radius = Vector3f(radius * 0.72f, radius * 0.54f, radius * 0.72f),
                    color = hotColor(),
                    alpha = visibleAlpha * 0.28f,
                    brightness = 2.65f,
                    frontPower = 1.75f,
                    noiseScale = 1.28f,
                    time = time * 1.18f,
                    pulse = pulse,
                    passMode = PASS_CORE,
                ),
            ),
        )
    }

    override fun collectRenderContributions(
        input: RenderContributionInput<MeteoriteAtmosphereFireRenderEntity>,
        collector: RenderContributionCollector,
    ) {
        val visibleAlpha = currentAlpha(input.frameContext.tickDelta)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        collector.submit(
            BuiltinRenderEffectDescriptors.maskBloom(
                effectId = METEORITE_ATMOSPHERE_FIRE_BLOOM_EFFECT_ID,
                sourceInstanceId = uuid.toString(),
                frameContext = input.frameContext,
                sourceEntity = this,
                config = METEORITE_ATMOSPHERE_FIRE_BLOOM_CONFIG.copy(
                    intensity = METEORITE_ATMOSPHERE_FIRE_BLOOM_CONFIG.intensity * bloomStrength.coerceIn(0f, 4f),
                    tint = normalizedColor(),
                ),
                priority = METEORITE_ATMOSPHERE_FIRE_BLOOM_PRIORITY,
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

    fun configure(
        center: Vec3,
        moveDirection: Vec3,
        meteoriteSize: Float,
        flameColor: Vector3f = color,
        maxLifetime: Int = lifetime,
        fadeIn: Int = fadeInTicks,
        fadeOut: Int = fadeOutTicks,
        opacity: Double = alpha,
    ): MeteoriteAtmosphereFireRenderEntity {
        pos = center
        direction = safeDirection(moveDirection)
        size = meteoriteSize.coerceAtLeast(MIN_SIZE)
        color = Vector3f(flameColor)
        lifetime = maxLifetime.coerceAtLeast(1)
        fadeInTicks = fadeIn.coerceAtLeast(0)
        fadeOutTicks = fadeOut.coerceAtLeast(0)
        discardTicks = fadeOutTicks
        discarding = false
        discardStartTick = 0
        alpha = opacity.coerceIn(0.0, 1.0)
        updateRenderRange()
        markDirty()
        return this
    }

    fun moveTo(center: Vec3, moveDirection: Vec3 = direction): MeteoriteAtmosphereFireRenderEntity {
        pos = center
        direction = safeDirection(moveDirection)
        markDirty()
        return this
    }

    fun finish() {
        discard()
    }

    fun discard(fadeTicks: Int = fadeOutTicks): MeteoriteAtmosphereFireRenderEntity {
        if (discarding || canceled) {
            return this
        }
        discardTicks = fadeTicks.coerceAtLeast(0)
        discardStartTick = age
        discarding = true
        if (discardTicks == 0) {
            canceled = true
        }
        markDirty()
        return this
    }

    private fun renderBloomMask(
        tickDelta: Float,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
    ) {
        initStatic()
        val visibleAlpha = currentAlpha(tickDelta)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val radius = size.coerceAtLeast(MIN_SIZE)
        val time = currentTime(tickDelta)
        val pulse = currentPulse(tickDelta)
        val modelMatrix = Matrix4fStack(16)
        RenderUtil.setRenderStackWithEntity(modelMatrix, this, tickDelta)
        renderPasses(
            modelMatrix = orientedModelMatrix(modelMatrix),
            viewMatrix = viewMatrix,
            projMatrix = projMatrix,
            passes = listOf(
                MeteoriteAtmosphereFirePass(
                    blendMode = MeteoriteAtmosphereFireBlendMode.ADDITIVE,
                    radius = Vector3f(radius * 1.10f, radius * 0.88f, radius * 1.10f),
                    color = normalizedColor(),
                    alpha = visibleAlpha * 0.42f * bloomStrength.coerceIn(0f, 4f),
                    brightness = 2.8f,
                    frontPower = 1.06f,
                    noiseScale = 0.92f,
                    time = time,
                    pulse = pulse,
                    passMode = PASS_BLOOM,
                ),
                MeteoriteAtmosphereFirePass(
                    blendMode = MeteoriteAtmosphereFireBlendMode.ADDITIVE,
                    radius = Vector3f(radius * 0.76f, radius * 0.58f, radius * 0.76f),
                    color = hotColor(),
                    alpha = visibleAlpha * 0.26f * bloomStrength.coerceIn(0f, 4f),
                    brightness = 4.2f,
                    frontPower = 1.68f,
                    noiseScale = 1.20f,
                    time = time * 1.16f,
                    pulse = pulse,
                    passMode = PASS_CORE,
                ),
            ),
        )
    }

    private fun renderPasses(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        passes: List<MeteoriteAtmosphereFirePass>,
    ) {
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.depthFunc(GL33.GL_LEQUAL)
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            passes.forEach { pass ->
                if (pass.alpha <= MIN_VISIBLE_ALPHA) {
                    return@forEach
                }
                when (pass.blendMode) {
                    MeteoriteAtmosphereFireBlendMode.ALPHA -> RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA)
                    MeteoriteAtmosphereFireBlendMode.ADDITIVE -> RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
                }
                drawPass(modelMatrix, viewMatrix, projMatrix, pass)
            }
        } finally {
            RenderSystem.depthMask(true)
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableBlend()
            RenderSystem.enableDepthTest()
            RenderSystem.depthFunc(GL33.GL_LEQUAL)
            RenderSystem.enableCull()
        }
    }

    private fun drawPass(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        pass: MeteoriteAtmosphereFirePass,
    ) {
        fireShader.useOnContext {
            RenderSystem.setShaderTexture(0, FIRE_TEXTURE)
            setInt("fireTexture", 0)
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat3("radius", pass.radius)
            setFloat3("color", pass.color)
            setFloat("alpha", pass.alpha.coerceIn(0f, 2f))
            setFloat("brightness", pass.brightness.coerceIn(0f, 8f))
            setFloat("frontPower", pass.frontPower.coerceIn(0.1f, 4f))
            setFloat("noiseScale", pass.noiseScale.coerceAtLeast(0.05f))
            setFloat("flowSpeed", flowSpeed.coerceIn(0f, 5f))
            setFloat("time", pass.time)
            setFloat("pulse", pass.pulse)
            setInt("passMode", pass.passMode)
            fireVertexBuffer.draw()
        }
    }

    private fun updateLifecycle() {
        updateRenderRange()
        if (discarding) {
            if (age - discardStartTick >= discardTicks.coerceAtLeast(0)) {
                canceled = true
                markDirty()
            }
            return
        }
        if (age > lifetime.coerceAtLeast(1)) {
            canceled = true
            markDirty()
        }
    }

    private fun updateRenderRange() {
        renderRange = size.coerceAtLeast(MIN_SIZE).toDouble() * 18.0 + 48.0
    }

    private fun currentTime(tickDelta: Float): Float {
        return (age - 1f + tickDelta).coerceAtLeast(0f)
    }

    private fun currentAlpha(tickDelta: Float): Float {
        val time = currentTime(tickDelta)
        val maxLifetime = lifetime.coerceAtLeast(1).toFloat()
        val fadeIn = fadeInTicks.coerceAtLeast(0).toFloat()
        val fadeOut = fadeOutTicks.coerceAtLeast(0).toFloat().coerceAtMost(maxLifetime)
        val inAlpha = if (fadeIn <= 0f) 1f else smoothstep(0f, fadeIn, time)
        val outStart = maxLifetime - fadeOut
        val outAlpha = if (fadeOut <= 0f || time <= outStart) {
            1f
        } else {
            1f - smoothstep(0f, fadeOut, time - outStart)
        }
        val discardAlpha = if (!discarding) {
            1f
        } else {
            val discardDuration = discardTicks.coerceAtLeast(0).toFloat()
            if (discardDuration <= 0f) {
                0f
            } else {
                1f - smoothstep(0f, discardDuration, time - discardStartTick.toFloat())
            }
        }
        return alpha.toFloat().coerceIn(0f, 1f) * inAlpha * outAlpha * discardAlpha
    }

    private fun currentPulse(tickDelta: Float): Float {
        return 0.5f + 0.5f * sin(currentTime(tickDelta) * 0.38f)
    }

    private fun orientedModelMatrix(source: Matrix4f): Matrix4f {
        return Matrix4f(source).rotate(directionRotation())
    }

    private fun directionRotation(): Quaternionf {
        return Quaternionf().rotationTo(Vector3f(0f, 1f, 0f), safeDirectionVector())
    }

    private fun safeDirectionVector(): Vector3f {
        val dir = safeDirection(direction)
        return Vector3f(dir.x.toFloat(), dir.y.toFloat(), dir.z.toFloat())
    }

    private fun normalizedColor(): Vector3f {
        return Vector3f(
            color.x.coerceIn(0f, 1f),
            color.y.coerceIn(0f, 1f),
            color.z.coerceIn(0f, 1f),
        )
    }

    private fun hotColor(): Vector3f {
        return Vector3f(normalizedColor()).lerp(Vector3f(1.0f, 0.92f, 0.58f), 0.54f)
    }

    companion object {
        private const val DEFAULT_SIZE = 1.35f
        private const val DEFAULT_LIFETIME = 80
        private const val DEFAULT_FADE_IN_TICKS = 6
        private const val DEFAULT_FADE_OUT_TICKS = 14
        private const val DEFAULT_FLOW_SPEED = 1.0f
        private const val MIN_SIZE = 0.05f
        private const val MIN_VISIBLE_ALPHA = 0.001f
        private const val PASS_SURFACE = 0
        private const val PASS_CORE = 1
        private const val PASS_BLOOM = 2
        private const val METEORITE_ATMOSPHERE_FIRE_BLOOM_EFFECT_ID = "usefulmagic:meteorite_atmosphere_fire_bloom"
        private const val METEORITE_ATMOSPHERE_FIRE_BLOOM_PRIORITY = 245
        private val DEFAULT_DIRECTION = Vec3(0.0, -1.0, 0.0)

        @JvmField
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "meteorite_atmosphere_fire_render_entity")

        private val FIRE_TEXTURE: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/effect/meteorite_atmosphere_fire.png")

        private val METEORITE_ATMOSPHERE_FIRE_BLOOM_CONFIG = MaskBloomConfig(
            blurSigma = 7.2f,
            blurRange = 5.4f,
            intensity = 1.85f,
            baseMaskIntensity = 0.04f,
            threshold = 0.02f,
            thresholdSoftness = 0.03f,
            tint = Vector3f(1.0f, 0.45f, 0.16f),
        )

        @JvmField
        var initialized: Boolean = false

        private lateinit var fireVertexBuffer: SimpleVertexBuffer
        private lateinit var fireShader: CooShaderProgram

        @JvmStatic
        @Synchronized
        fun initStatic() {
            if (initialized) {
                return
            }
            fireVertexBuffer = SimpleVertexBuffer().apply {
                init()
                setVertexes(ShaderUtil.genBall(1f, 64, 96), CooVertexFormat.POINT_FORMAT)
            }
            fireShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(
                            UsefulMagic.MOD_ID,
                            "core/vsh/meteorite_atmosphere_fire.vsh",
                        ),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(
                            UsefulMagic.MOD_ID,
                            "core/fsh/meteorite_atmosphere_fire.fsh",
                        ),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            fireShader.init()
            initialized = true
        }

        @JvmStatic
        fun spawn(
            world: ServerLevel,
            center: Vec3,
            moveDirection: Vec3,
            size: Float,
            color: Vector3f = Vector3f(1.0f, 0.42f, 0.10f),
            lifetime: Int = DEFAULT_LIFETIME,
            fadeInTicks: Int = DEFAULT_FADE_IN_TICKS,
            fadeOutTicks: Int = DEFAULT_FADE_OUT_TICKS,
            alpha: Double = 1.0,
        ): MeteoriteAtmosphereFireRenderEntity {
            return MeteoriteAtmosphereFireRenderEntity(world)
                .configure(center, moveDirection, size, color, lifetime, fadeInTicks, fadeOutTicks, alpha)
                .also(ServerRenderEntityManager::spawn)
        }

        @JvmStatic
        fun safeDirection(direction: Vec3): Vec3 {
            if (direction.lengthSqr() <= 1.0E-8) {
                return DEFAULT_DIRECTION
            }
            return direction.normalize()
        }

        @JvmStatic
        fun smoothstep(edge0: Float, edge1: Float, value: Float): Float {
            if (edge0 == edge1) {
                return if (value >= edge1) 1f else 0f
            }
            val x = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
            return x * x * (3f - 2f * x)
        }

        @JvmStatic
        fun maxComponent(vector: Vector3f): Float {
            return max(vector.x, max(vector.y, vector.z))
        }
    }
}

private enum class MeteoriteAtmosphereFireBlendMode {
    ALPHA,
    ADDITIVE,
}

private data class MeteoriteAtmosphereFirePass(
    val blendMode: MeteoriteAtmosphereFireBlendMode,
    val radius: Vector3f,
    val color: Vector3f,
    val alpha: Float,
    val brightness: Float,
    val frontPower: Float,
    val noiseScale: Float,
    val time: Float,
    val pulse: Float,
    val passMode: Int,
)
