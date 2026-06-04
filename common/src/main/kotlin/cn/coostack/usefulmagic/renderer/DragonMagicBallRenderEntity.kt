package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.renderer.AutoRenderEntity
import cn.coostack.cooparticlesapi.renderer.client.RenderUtil
import cn.coostack.cooparticlesapi.renderer.effects.builtin.BuiltinRenderEffectDescriptors
import cn.coostack.cooparticlesapi.renderer.effects.builtin.MaskBloomConfig
import cn.coostack.cooparticlesapi.renderer.runtime.*
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
import org.joml.Vector3f
import org.lwjgl.opengl.GL33
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

@CooAutoRegister
class DragonMagicBallRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos),
    WorldPassRenderEntityRenderer<DragonMagicBallRenderEntity>,
    FramePostRenderEntityRenderer<DragonMagicBallRenderEntity>,
    RenderEntityReleaseHook<DragonMagicBallRenderEntity> {
    @CodecField
    var color: Vector3f = Vector3f(1.0f, 1.0f, 1.0f)

    @CodecField
    var size: Float = DEFAULT_SIZE

    @CodecField
    var alpha: Double = 1.0

    @CodecField
    var brightness: Float = 1.0f

    @CodecField
    var cloudOpacity: Double = 0.62

    @CodecField
    var surfaceBrightness: Double = 1.18

    @CodecField
    var bloomIntensity: Double = 0.82

    @CodecField
    var bloomEnabled: Boolean = true

    @CodecField
    var growingByScale: Boolean = true

    @CodecField
    var growingTick: Int = DEFAULT_GROWING_TICKS

    @CodecField
    var discardTick: Int = DEFAULT_DISCARD_TICKS

    @CodecField
    var discardByShrink: Boolean = true

    @CodecField
    var discarding: Boolean = false

    @CodecField
    var discardStartTick: Int = 0

    init {
        updateRenderRange()
    }

    override fun initialize(instance: RenderEntityInstance<DragonMagicBallRenderEntity>) {
        initStatic()
        updateRenderRange()
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun release(instance: RenderEntityInstance<DragonMagicBallRenderEntity>) {
    }

    override fun clientTick() {
        updateLifecycle()
    }

    override fun serverTick() {
        updateLifecycle()
    }

    override fun renderLocal(input: LocalRenderInput<DragonMagicBallRenderEntity>) {
        initStatic()
        val growProgress = currentGrowProgress(input.tickDelta)
        val visibleAlpha = currentAlpha(input.tickDelta, growProgress)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val worldSize = currentSize(input.tickDelta, growProgress)
        if (worldSize <= MIN_SIZE) {
            return
        }
        val time = getTime(input.tickDelta)
        val pulse = currentPulse(input.tickDelta)
        val baseColor = normalizedColor()
        val hotColor = Vector3f(baseColor).lerp(Vector3f(0.72f, 0.90f, 1.0f), 0.22f)

        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.depthFunc(GL33.GL_LEQUAL)
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA)
            drawPass(
                modelMatrix = input.modelMatrix,
                viewMatrix = input.viewMatrix,
                projMatrix = input.projMatrix,
                passColor = baseColor,
                scale = worldSize * 0.74f,
                passAlpha = visibleAlpha * 0.78f,
                brightness = currentBrightness(surfaceBrightness.toFloat().coerceIn(0f, 4f) * (0.96f + pulse * 0.12f)),
                time = time,
                discardProgress = currentDiscardProgress(input.tickDelta),
                passMode = PASS_SURFACE,
            )

            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
            drawPass(
                modelMatrix = input.modelMatrix,
                viewMatrix = input.viewMatrix,
                projMatrix = input.projMatrix,
                passColor = hotColor,
                scale = worldSize * 0.78f,
                passAlpha = visibleAlpha * 0.24f,
                brightness = currentBrightness(surfaceBrightness.toFloat().coerceIn(0f, 4f) * (1.20f + pulse * 0.28f)),
                time = time * 1.18f,
                discardProgress = currentDiscardProgress(input.tickDelta),
                passMode = PASS_SURFACE,
            )

            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA)
            drawPass(
                modelMatrix = input.modelMatrix,
                viewMatrix = input.viewMatrix,
                projMatrix = input.projMatrix,
                passColor = Vector3f(baseColor).lerp(Vector3f(0.92f, 0.96f, 1.0f), 0.58f),
                scale = worldSize * 1.16f,
                passAlpha = visibleAlpha * cloudOpacity.toFloat().coerceIn(0f, 1f),
                brightness = currentBrightness(0.94f + pulse * 0.06f),
                time = time * 0.64f,
                discardProgress = currentDiscardProgress(input.tickDelta),
                passMode = PASS_CLOUD,
            )
        } finally {
            RenderSystem.depthMask(true)
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableBlend()
            RenderSystem.enableDepthTest()
            RenderSystem.depthFunc(GL33.GL_LEQUAL)
            RenderSystem.enableCull()
        }
    }

    override fun collectRenderContributions(
        input: RenderContributionInput<DragonMagicBallRenderEntity>,
        collector: RenderContributionCollector,
    ) {
        if (!bloomEnabled) {
            return
        }
        val visibleAlpha = currentAlpha(input.frameContext.tickDelta)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        collector.submit(
            BuiltinRenderEffectDescriptors.maskBloom(
                effectId = DRAGON_MAGIC_BALL_BLOOM_EFFECT_ID,
                sourceInstanceId = uuid.toString(),
                frameContext = input.frameContext,
                sourceEntity = this,
                config = buildBloomConfig(input.frameContext.tickDelta),
                priority = DRAGON_MAGIC_BALL_BLOOM_PRIORITY,
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
        pos: Vec3,
        color: Vector3f = this.color,
        size: Float = this.size,
        growingByScale: Boolean = this.growingByScale,
        growingTick: Int = this.growingTick,
    ): DragonMagicBallRenderEntity {
        this.pos = pos
        this.color = Vector3f(color)
        this.size = size.coerceAtLeast(MIN_SIZE)
        this.growingByScale = growingByScale
        this.growingTick = growingTick.coerceAtLeast(1)
        updateRenderRange()
        markDirty()
        return this
    }

    fun configure(
        pos: Vec3,
        color: Vec3,
        size: Float = this.size,
        growingByScale: Boolean = this.growingByScale,
        growingTick: Int = this.growingTick,
    ): DragonMagicBallRenderEntity {
        return configure(
            pos,
            Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()),
            size,
            growingByScale,
            growingTick,
        )
    }

    fun setColor(color: Vector3f): DragonMagicBallRenderEntity {
        this.color = Vector3f(color)
        markDirty()
        return this
    }

    fun setColor(color: Vec3): DragonMagicBallRenderEntity {
        return setColor(Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()))
    }

    fun setSize(size: Float): DragonMagicBallRenderEntity {
        this.size = size.coerceAtLeast(MIN_SIZE)
        updateRenderRange()
        markDirty()
        return this
    }

    fun setBrightness(brightness: Float): DragonMagicBallRenderEntity {
        this.brightness = brightness.coerceAtLeast(0f)
        markDirty()
        return this
    }

    fun setBloomEnabled(enabled: Boolean): DragonMagicBallRenderEntity {
        bloomEnabled = enabled
        markDirty()
        return this
    }

    fun discard(shrink: Boolean = discardByShrink): DragonMagicBallRenderEntity {
        if (discarding) {
            return this
        }
        discardByShrink = shrink
        discarding = true
        discardStartTick = age
        if (discardTick <= 0) {
            canceled = true
        }
        markDirty()
        requestSync()
        return this
    }

    private fun renderBloomMask(
        tickDelta: Float,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
    ) {
        val growProgress = currentGrowProgress(tickDelta)
        val visibleAlpha = currentAlpha(tickDelta, growProgress)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val worldSize = currentSize(tickDelta, growProgress)
        if (worldSize <= MIN_SIZE) {
            return
        }
        val time = getTime(tickDelta)
        val pulse = currentPulse(tickDelta)
        val baseColor = normalizedColor()
        val hotColor = Vector3f(baseColor).lerp(Vector3f(0.72f, 0.90f, 1.0f), 0.30f)
        val modelMatrix = Matrix4fStack(16)
        RenderUtil.setRenderStackWithEntity(modelMatrix, this, tickDelta)

        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.depthFunc(GL33.GL_LEQUAL)
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
            drawPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                passColor = hotColor,
                scale = worldSize * 0.74f,
                passAlpha = visibleAlpha * (0.26f + pulse * 0.08f),
                brightness = currentBrightness(surfaceBrightness.toFloat().coerceIn(0f, 4f) * 1.36f),
                time = time,
                discardProgress = currentDiscardProgress(tickDelta),
                passMode = PASS_BLOOM,
            )
            drawPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                passColor = baseColor,
                scale = worldSize * 0.48f,
                passAlpha = visibleAlpha * 0.15f,
                brightness = currentBrightness(surfaceBrightness.toFloat().coerceIn(0f, 4f) * 1.62f),
                time = time * 1.26f,
                discardProgress = currentDiscardProgress(tickDelta),
                passMode = PASS_BLOOM,
            )
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
        passColor: Vector3f,
        scale: Float,
        passAlpha: Float,
        brightness: Float,
        time: Float,
        discardProgress: Float,
        passMode: Int,
    ) {
        if (passAlpha <= MIN_VISIBLE_ALPHA || scale <= MIN_SIZE) {
            return
        }
        ballShader.useOnContext {
            RenderSystem.setShaderTexture(0, SURFACE_TEXTURE)
            RenderSystem.setShaderTexture(1, CLOUD_TEXTURE)
            setInt("surfaceTexture", 0)
            setInt("cloudTexture", 1)
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat3("color", passColor)
            setFloat("scale", scale.coerceAtLeast(MIN_SIZE))
            setFloat("alpha", passAlpha.coerceIn(0f, 1.4f))
            setFloat("brightness", brightness.coerceIn(0f, 8f))
            setFloat("time", time)
            setFloat("discardProgress", discardProgress.coerceIn(0f, 1f))
            setInt("passMode", passMode)
            ballVertexBuffer.draw()
        }
    }

    private fun updateLifecycle() {
        updateRenderRange()
        if (!discarding) {
            return
        }
        if (age - discardStartTick >= discardTick.coerceAtLeast(0)) {
            canceled = true
            markDirty()
        }
    }

    private fun updateRenderRange() {
        renderRange = size.coerceAtLeast(MIN_SIZE).toDouble() * 24.0 + VIEW_PADDING
    }

    private fun currentSize(tickDelta: Float, growProgress: Float = currentGrowProgress(tickDelta)): Float {
        val baseSize = size.coerceAtLeast(MIN_SIZE)
        val pulse = 1f + sin((age - 1f + tickDelta) * 0.12f) * 0.018f
        val growFactor = if (growingByScale) max(0.03f, growProgress) else 1f
        if (!discarding || !discardByShrink) {
            return baseSize * pulse * growFactor
        }
        val fade = 1f - currentDiscardProgress(tickDelta)
        return baseSize * pulse * growFactor * fade.pow(1.22f)
    }

    private fun currentAlpha(tickDelta: Float, growProgress: Float = currentGrowProgress(tickDelta)): Float {
        val baseAlpha = alpha.toFloat().coerceIn(0f, 1f)
        val appearFactor = if (growingByScale) 1f else growProgress.coerceIn(0f, 1f)
        if (!discarding || discardByShrink) {
            return baseAlpha * appearFactor
        }
        val fade = 1f - currentDiscardProgress(tickDelta)
        return baseAlpha * appearFactor * fade * fade
    }

    private fun currentDiscardProgress(tickDelta: Float): Float {
        if (!discarding) {
            return 0f
        }
        val duration = discardTick.coerceAtLeast(1).toFloat()
        val elapsed = age - discardStartTick - 1f + tickDelta
        return (elapsed / duration).coerceIn(0f, 1f)
    }

    private fun currentGrowProgress(tickDelta: Float): Float {
        val duration = growingTick.coerceAtLeast(1).toFloat()
        val elapsed = age - 1f + tickDelta
        return (elapsed / duration).coerceIn(0f, 1f)
    }

    private fun currentPulse(tickDelta: Float): Float {
        return 0.5f + 0.5f * sin((age - 1f + tickDelta) * 0.30f + 0.8f)
    }

    private fun normalizedColor(): Vector3f {
        val normalized = Vector3f(
            color.x.coerceIn(0f, 1f),
            color.y.coerceIn(0f, 1f),
            color.z.coerceIn(0f, 1f),
        )
        if (max(max(normalized.x, normalized.y), normalized.z) < 0.05f) {
            return Vector3f(1.0f, 1.0f, 1.0f)
        }
        return normalized
    }

    private fun currentBrightness(base: Float): Float {
        return base * brightness.coerceAtLeast(0f)
    }

    private fun buildBloomConfig(tickDelta: Float): MaskBloomConfig {
        val sizeScale = (currentSize(tickDelta) / DEFAULT_SIZE).coerceIn(0.45f, 2.4f)
        val brightnessScale = brightness.coerceAtLeast(0f)
        val strength = bloomIntensity.toFloat().coerceIn(0f, 4f) * brightnessScale
        val pulse = currentPulse(tickDelta)
        return DRAGON_MAGIC_BALL_BLOOM_CONFIG.copy(
            blurSigma = DRAGON_MAGIC_BALL_BLOOM_CONFIG.blurSigma * sizeScale,
            blurRange = DRAGON_MAGIC_BALL_BLOOM_CONFIG.blurRange * max(0.75f, sizeScale),
            intensity = DRAGON_MAGIC_BALL_BLOOM_CONFIG.intensity * strength * (0.92f + pulse * 0.18f),
            baseMaskIntensity = DRAGON_MAGIC_BALL_BLOOM_CONFIG.baseMaskIntensity * strength,
            tint = normalizedColor(),
        )
    }

    companion object {
        private const val DEFAULT_SIZE = 1.6f
        private const val DEFAULT_GROWING_TICKS = 12
        private const val DEFAULT_DISCARD_TICKS = 16
        private const val MIN_SIZE = 0.025f
        private const val MIN_VISIBLE_ALPHA = 0.001f
        private const val VIEW_PADDING = 32.0
        private const val PASS_SURFACE = 0
        private const val PASS_CLOUD = 1
        private const val PASS_BLOOM = 2
        private const val DRAGON_MAGIC_BALL_BLOOM_EFFECT_ID = "usefulmagic:dragon_magic_ball_bloom"
        private const val DRAGON_MAGIC_BALL_BLOOM_PRIORITY = 250

        @JvmField
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "dragon_magic_ball_render_entity")

        private val SURFACE_TEXTURE: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/effect/dragon_magic_ball_surface.png")

        private val CLOUD_TEXTURE: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/effect/dragon_magic_ball_cloud.png")

        @JvmField
        var initialized: Boolean = false

        private val DRAGON_MAGIC_BALL_BLOOM_CONFIG = MaskBloomConfig(
            blurSigma = 4.6f,
            blurRange = 3.2f,
            intensity = 1.05f,
            baseMaskIntensity = 0.06f,
            threshold = 0.08f,
            thresholdSoftness = 0.03f,
            tint = Vector3f(0.50f, 0.76f, 1.0f),
        )

        private lateinit var ballVertexBuffer: SimpleVertexBuffer
        private lateinit var ballShader: CooShaderProgram

        @JvmStatic
        @Synchronized
        fun initStatic() {
            if (initialized) {
                return
            }
            ballVertexBuffer = SimpleVertexBuffer().apply {
                init()
                setVertexes(ShaderUtil.genBall(1f, 64, 96), CooVertexFormat.POINT_FORMAT)
            }
            ballShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(
                            UsefulMagic.MOD_ID,
                            "core/vsh/dragon_magic_ball.vsh",
                        ),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(
                            UsefulMagic.MOD_ID,
                            "core/fsh/dragon_magic_ball.fsh",
                        ),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            ballShader.init()
            initialized = true
        }

        @JvmStatic
        fun spawn(
            world: ServerLevel,
            pos: Vec3,
            color: Vector3f = Vector3f(1.0f, 1.0f, 1.0f),
            size: Float = DEFAULT_SIZE,
            growingByScale: Boolean = true,
            growingTick: Int = DEFAULT_GROWING_TICKS,
        ): DragonMagicBallRenderEntity {
            return DragonMagicBallRenderEntity(world)
                .configure(pos, color, size, growingByScale, growingTick)
                .also(ServerRenderEntityManager::spawn)
        }

        @JvmStatic
        fun spawn(
            world: ServerLevel,
            pos: Vec3,
            color: Vec3,
            size: Float = DEFAULT_SIZE,
            growingByScale: Boolean = true,
            growingTick: Int = DEFAULT_GROWING_TICKS,
        ): DragonMagicBallRenderEntity {
            return spawn(
                world,
                pos,
                Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()),
                size,
                growingByScale,
                growingTick,
            )
        }
    }
}
