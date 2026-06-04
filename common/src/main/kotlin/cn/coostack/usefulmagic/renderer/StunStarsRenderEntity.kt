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
import cn.coostack.cooparticlesapi.renderer.shader.data.VertexData
import cn.coostack.cooparticlesapi.renderer.shader.glsl.IdentifierShader
import cn.coostack.cooparticlesapi.renderer.shader.vertex.SimpleVertexBuffer
import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.*
import org.lwjgl.opengl.GL33
import kotlin.math.*

@CooAutoRegister
class StunStarsRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos),
    WorldPassRenderEntityRenderer<StunStarsRenderEntity>,
    FramePostRenderEntityRenderer<StunStarsRenderEntity>,
    RenderEntityReleaseHook<StunStarsRenderEntity> {
    @CodecField
    var color: Vector3f = Vector3f(1.0f, 0.92f, 0.42f)

    @CodecField
    var alpha: Double = 1.0

    @CodecField
    var brightness: Float = DEFAULT_BRIGHTNESS

    @CodecField
    var orbitRadius: Float = DEFAULT_ORBIT_RADIUS

    @CodecField
    var verticalOffset: Float = DEFAULT_VERTICAL_OFFSET

    @CodecField
    var starScale: Float = DEFAULT_STAR_SCALE

    @CodecField
    var trailLength: Float = DEFAULT_TRAIL_LENGTH

    @CodecField
    var trailWidth: Float = DEFAULT_TRAIL_WIDTH

    @CodecField
    var appearTicks: Int = DEFAULT_APPEAR_TICKS

    @CodecField
    var discardFadeTicks: Int = DEFAULT_DISCARD_FADE_TICKS

    @CodecField
    var discarding: Boolean = false

    @CodecField
    var discardStartTick: Int = 0

    @CodecField
    var orbitSpeed: Float = DEFAULT_ORBIT_SPEED

    @CodecField
    var starSpinSpeed: Float = DEFAULT_STAR_SPIN_SPEED

    init {
        updateRenderRange()
    }

    override fun initialize(instance: RenderEntityInstance<StunStarsRenderEntity>) {
        initStatic()
        updateRenderRange()
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun release(instance: RenderEntityInstance<StunStarsRenderEntity>) {
    }

    override fun clientTick() {
        updateLifecycle()
    }

    override fun serverTick() {
        updateLifecycle()
    }

    override fun renderLocal(input: LocalRenderInput<StunStarsRenderEntity>) {
        initStatic()
        val visibleAlpha = currentAlpha(input.tickDelta)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        renderStars(
            modelMatrix = input.modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            tickDelta = input.tickDelta,
            passAlpha = visibleAlpha,
            bloomPass = false,
        )
    }

    override fun collectRenderContributions(
        input: RenderContributionInput<StunStarsRenderEntity>,
        collector: RenderContributionCollector,
    ) {
        val visibleAlpha = currentAlpha(input.frameContext.tickDelta)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        collector.submit(
            BuiltinRenderEffectDescriptors.maskBloom(
                effectId = STUN_STARS_BLOOM_EFFECT_ID,
                sourceInstanceId = uuid.toString(),
                frameContext = input.frameContext,
                sourceEntity = this,
                config = buildBloomConfig(input.frameContext.tickDelta),
                priority = STUN_STARS_BLOOM_PRIORITY,
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
        alpha: Double = this.alpha,
        brightness: Float = this.brightness,
        orbitRadius: Float = this.orbitRadius,
        verticalOffset: Float = this.verticalOffset,
        starScale: Float = this.starScale,
        trailLength: Float = this.trailLength,
        trailWidth: Float = this.trailWidth,
        appearTicks: Int = this.appearTicks,
        discardFadeTicks: Int = this.discardFadeTicks,
        orbitSpeed: Float = this.orbitSpeed,
        starSpinSpeed: Float = this.starSpinSpeed,
    ): StunStarsRenderEntity {
        this.pos = pos
        this.color = Vector3f(color)
        this.alpha = alpha.coerceIn(0.0, 1.0)
        this.brightness = brightness.coerceIn(0f, MAX_BRIGHTNESS)
        this.orbitRadius = orbitRadius.coerceAtLeast(MIN_SIZE)
        this.verticalOffset = verticalOffset
        this.starScale = starScale.coerceAtLeast(MIN_SIZE)
        this.trailLength = trailLength.coerceAtLeast(MIN_SIZE)
        this.trailWidth = trailWidth.coerceAtLeast(MIN_SIZE)
        this.appearTicks = appearTicks.coerceAtLeast(1)
        this.discardFadeTicks = discardFadeTicks.coerceAtLeast(1)
        this.orbitSpeed = orbitSpeed
        this.starSpinSpeed = starSpinSpeed
        updateRenderRange()
        markDirty()
        return this
    }

    fun setColor(color: Vector3f): StunStarsRenderEntity {
        this.color = Vector3f(color)
        markDirty()
        return this
    }

    fun setColor(color: Vec3): StunStarsRenderEntity {
        return setColor(Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()))
    }

    fun withBrightness(value: Float): StunStarsRenderEntity {
        brightness = value.coerceIn(0f, MAX_BRIGHTNESS)
        markDirty()
        return this
    }

    fun withTrailLength(value: Float): StunStarsRenderEntity {
        trailLength = value.coerceAtLeast(MIN_SIZE)
        updateRenderRange()
        markDirty()
        return this
    }

    fun withStarScale(value: Float): StunStarsRenderEntity {
        starScale = value.coerceAtLeast(MIN_SIZE)
        updateRenderRange()
        markDirty()
        return this
    }

    fun moveTo(pos: Vec3): StunStarsRenderEntity {
        if (this.pos.distanceToSqr(pos) <= MIN_POSITION_SYNC_DISTANCE_SQR) {
            return this
        }
        this.pos = pos
        markDirty()
        requestSync()
        return this
    }

    fun setDiscardFadeTicks(ticks: Int): StunStarsRenderEntity {
        discardFadeTicks = ticks.coerceAtLeast(1)
        markDirty()
        return this
    }

    fun discard(fadeTicks: Int = discardFadeTicks): StunStarsRenderEntity {
        if (discarding) {
            return this
        }
        discardFadeTicks = fadeTicks.coerceAtLeast(1)
        discarding = true
        discardStartTick = age
        markDirty()
        requestSync()
        return this
    }

    private fun renderBloomMask(tickDelta: Float, viewMatrix: Matrix4f, projMatrix: Matrix4f) {
        val visibleAlpha = currentAlpha(tickDelta)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val modelMatrix = Matrix4fStack(16)
        RenderUtil.setRenderStackWithEntity(modelMatrix, this, tickDelta)
        renderStars(
            modelMatrix = modelMatrix,
            viewMatrix = viewMatrix,
            projMatrix = projMatrix,
            tickDelta = tickDelta,
            passAlpha = visibleAlpha,
            bloomPass = true,
        )
    }

    private fun renderStars(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        tickDelta: Float,
        passAlpha: Float,
        bloomPass: Boolean,
    ) {
        val baseColor = normalizedColor()
        val time = timeline(tickDelta)
        val appearScale = currentAppearScale(tickDelta)
        val viewRotationMatrix = Matrix3f(viewMatrix)
        val inverseViewRotationMatrix = Matrix3f(viewRotationMatrix).invert()
        val passBrightness = (if (bloomPass) 2.8f else 1.65f) * brightness.coerceIn(0f, MAX_BRIGHTNESS)
        val starAlpha = passAlpha * if (bloomPass) 0.58f else 0.88f
        val trailAlpha = passAlpha * if (bloomPass) 0.50f else 0.38f

        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.depthFunc(GL33.GL_LEQUAL)
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
            repeat(STAR_COUNT) { index ->
                val phase = time * orbitSpeed + FULL_TURN * index / STAR_COUNT
                renderOrbitTrail(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    inverseViewRotationMatrix = inverseViewRotationMatrix,
                    baseColor = baseColor,
                    phase = phase,
                    time = time,
                    starIndex = index,
                    appearScale = appearScale,
                    trailAlpha = trailAlpha,
                    trailBrightness = passBrightness * 0.72f,
                    bloomPass = bloomPass,
                )
            }
            repeat(STAR_COUNT) { index ->
                val phase = time * orbitSpeed + FULL_TURN * index / STAR_COUNT
                val starOffset = orbitOffset(phase, time, index, appearScale)
                drawBillboard(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    inverseViewRotationMatrix = inverseViewRotationMatrix,
                    texture = STAR_TEXTURE,
                    centerOffset = starOffset,
                    scale = Vector2f(
                        starScale * appearScale * if (bloomPass) 1.34f else 1.0f,
                        starScale * appearScale * if (bloomPass) 1.34f else 1.0f,
                    ),
                    spin = time * starSpinSpeed + index * 0.73f,
                    color = baseColor,
                    alpha = starAlpha,
                    brightness = passBrightness,
                    mode = 0f,
                    trailFade = 1f,
                    time = time,
                )
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

    private fun renderOrbitTrail(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        inverseViewRotationMatrix: Matrix3f,
        baseColor: Vector3f,
        phase: Float,
        time: Float,
        starIndex: Int,
        appearScale: Float,
        trailAlpha: Float,
        trailBrightness: Float,
        bloomPass: Boolean,
    ) {
        if (trailLength <= MIN_SIZE || trailWidth <= MIN_SIZE) {
            return
        }
        val radius = orbitRadius.coerceAtLeast(MIN_SIZE)
        val direction = if (orbitSpeed >= 0f) 1f else -1f
        val phaseSpan = (trailLength / radius).coerceIn(0.04f, MAX_TRAIL_PHASE_SPAN)
        val segmentCount = (ceil(phaseSpan / TRAIL_SEGMENT_PHASE_STEP).toInt() + 1)
            .coerceIn(MIN_TRAIL_SEGMENTS, MAX_TRAIL_SEGMENTS)
        val segmentPhaseStep = phaseSpan / segmentCount
        val widthScale = trailWidth * appearScale * if (bloomPass) 1.18f else 0.82f
        var previousOffset = orbitOffset(phase, time, starIndex, appearScale)

        for (segment in 1..segmentCount) {
            val progress = segment.toFloat() / segmentCount.toFloat()
            val lag = segmentPhaseStep * segment * direction
            val laggedPhase = phase - lag
            val laggedTime = time - segmentPhaseStep * segment / orbitSpeed.absoluteValue.coerceAtLeast(0.001f)
            val segmentOffset = orbitOffset(laggedPhase, laggedTime, starIndex, appearScale)
            val segmentFade = (1f - progress).let { it * it } * trailVisibility(starIndex, laggedTime)
            if (segmentFade > MIN_VISIBLE_ALPHA) {
                drawTrailSegment(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    inverseViewRotationMatrix = inverseViewRotationMatrix,
                    texture = TRAIL_TEXTURE,
                    startOffset = segmentOffset,
                    endOffset = previousOffset,
                    width = widthScale * (0.38f + 0.62f * segmentFade),
                    color = baseColor,
                    alpha = trailAlpha * segmentFade * if (bloomPass) 0.92f else 0.68f,
                    brightness = trailBrightness,
                    trailFade = segmentFade,
                    time = laggedTime,
                )
            }
            previousOffset = segmentOffset
        }
    }

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
    ) {
        if (alpha <= MIN_VISIBLE_ALPHA || width <= MIN_SIZE || startOffset.distanceSquared(endOffset) <= MIN_SIZE * MIN_SIZE) {
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
            setFloat2("scale", Vector2f(1f, 1f))
            setFloat("spin", 0f)
            setFloat("depthOffset", 0.0f)
            setFloat3("segmentStart", startOffset)
            setFloat3("segmentEnd", endOffset)
            setFloat("segmentWidth", width)
            setFloat3("color", color)
            setFloat("alpha", alpha.coerceIn(0f, 1.4f))
            setFloat("brightness", brightness.coerceIn(0f, 6f))
            setFloat("mode", 1f)
            setFloat("trailFade", trailFade.coerceIn(0f, 1f))
            setFloat("time", time)
            billboardVertexBuffer.draw()
        }
    }

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
        mode: Float,
        trailFade: Float,
        time: Float,
    ) {
        if (alpha <= MIN_VISIBLE_ALPHA || scale.x <= MIN_SIZE || scale.y <= MIN_SIZE) {
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
            setFloat("depthOffset", 0.0f)
            setFloat3("segmentStart", Vector3f())
            setFloat3("segmentEnd", Vector3f())
            setFloat("segmentWidth", 0f)
            setFloat3("color", color)
            setFloat("alpha", alpha.coerceIn(0f, 1.4f))
            setFloat("brightness", brightness.coerceIn(0f, 6f))
            setFloat("mode", mode)
            setFloat("trailFade", trailFade.coerceIn(0f, 1f))
            setFloat("time", time)
            billboardVertexBuffer.draw()
        }
    }

    private fun updateLifecycle() {
        updateRenderRange()
        if (discarding && age - discardStartTick >= discardFadeTicks.coerceAtLeast(1)) {
            canceled = true
            markDirty()
        }
    }

    private fun updateRenderRange() {
        renderRange = max(32.0, (orbitRadius + starScale + trailLength).toDouble() * 16.0)
    }

    private fun currentAlpha(tickDelta: Float): Float {
        val baseAlpha = alpha.toFloat().coerceIn(0f, 1f)
        if (!discarding) {
            return baseAlpha
        }
        val elapsed = age - discardStartTick - 1f + tickDelta
        val fade = 1f - smoothstep(0f, discardFadeTicks.coerceAtLeast(1).toFloat(), elapsed)
        return baseAlpha * fade
    }

    private fun currentAppearScale(tickDelta: Float): Float {
        val appear = smoothstep(0f, appearTicks.coerceAtLeast(1).toFloat(), timeline(tickDelta))
        return appear.coerceAtLeast(0.03f)
    }

    private fun timeline(tickDelta: Float): Float {
        return (age - 1f + tickDelta).coerceAtLeast(0f)
    }

    private fun orbitOffset(phase: Float, time: Float, index: Int, appearScale: Float): Vector3f {
        val bob = sin(time * 0.18f + index * 1.7f) * 0.055f
        return Vector3f(
            cos(phase) * orbitRadius * appearScale,
            verticalOffset * appearScale + bob * appearScale,
            sin(phase) * orbitRadius * appearScale,
        )
    }

    private fun trailVisibility(index: Int, time: Float): Float {
        return 0.82f + 0.18f * sin(time * 0.34f + index * 2.1f)
    }

    private fun normalizedColor(): Vector3f {
        val normalized = Vector3f(
            color.x.coerceIn(0f, 1f),
            color.y.coerceIn(0f, 1f),
            color.z.coerceIn(0f, 1f),
        )
        if (max(max(normalized.x, normalized.y), normalized.z) < 0.05f) {
            return Vector3f(1.0f, 0.92f, 0.42f)
        }
        return normalized
    }

    private fun buildBloomConfig(tickDelta: Float): MaskBloomConfig {
        val pulse = 0.5f + 0.5f * sin(timeline(tickDelta) * 0.22f)
        val brightnessMultiplier = brightness.coerceIn(0f, MAX_BRIGHTNESS)
        return STUN_STARS_BLOOM_CONFIG.copy(
            intensity = STUN_STARS_BLOOM_CONFIG.intensity * (0.92f + pulse * 0.18f) * brightnessMultiplier,
            baseMaskIntensity = STUN_STARS_BLOOM_CONFIG.baseMaskIntensity * brightnessMultiplier,
            tint = normalizedColor(),
        )
    }

    companion object {
        private const val STAR_COUNT = 3
        private const val DEFAULT_APPEAR_TICKS = 6
        private const val DEFAULT_DISCARD_FADE_TICKS = 12
        private const val DEFAULT_ORBIT_RADIUS = 0.72f
        private const val DEFAULT_VERTICAL_OFFSET = 0.35f
        private const val DEFAULT_STAR_SCALE = 0.24f
        private const val DEFAULT_TRAIL_LENGTH = 0.95f
        private const val DEFAULT_TRAIL_WIDTH = 0.105f
        private const val DEFAULT_BRIGHTNESS = 1.0f
        private const val DEFAULT_ORBIT_SPEED = 0.22f
        private const val DEFAULT_STAR_SPIN_SPEED = 0.30f
        private const val MIN_SIZE = 0.01f
        private const val MAX_BRIGHTNESS = 4.0f
        private const val MIN_TRAIL_SEGMENTS = 5
        private const val MAX_TRAIL_SEGMENTS = 18
        private const val TRAIL_SEGMENT_PHASE_STEP = 0.075f
        private const val MAX_TRAIL_PHASE_SPAN = PI.toFloat() * 0.9f
        private const val MIN_VISIBLE_ALPHA = 0.001f
        private const val MIN_POSITION_SYNC_DISTANCE_SQR = 1.0E-6
        private const val FULL_TURN = (PI.toFloat() * 2f)
        private const val STUN_STARS_BLOOM_EFFECT_ID = "usefulmagic:stun_stars_bloom"
        private const val STUN_STARS_BLOOM_PRIORITY = 245

        @JvmField
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "stun_stars_render_entity")

        private val STAR_TEXTURE: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/effect/star.png")

        private val TRAIL_TEXTURE: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/effect/stun_star_trail.png")

        private val STUN_STARS_BLOOM_CONFIG = MaskBloomConfig(
            blurSigma = 7.4f,
            blurRange = 5.2f,
            intensity = 2.2f,
            baseMaskIntensity = 0.20f,
            threshold = 0.01f,
            thresholdSoftness = 0.025f,
            tint = Vector3f(1.0f, 0.92f, 0.42f),
        )

        @JvmField
        var initialized: Boolean = false

        private lateinit var billboardVertexBuffer: SimpleVertexBuffer
        private lateinit var billboardShader: CooShaderProgram

        @JvmStatic
        @Synchronized
        fun initStatic() {
            if (initialized) {
                return
            }
            billboardVertexBuffer = SimpleVertexBuffer().apply {
                init()
                setVertexes(buildQuadVertices(), CooVertexFormat.POINT_FORMAT)
            }
            billboardShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(
                            UsefulMagic.MOD_ID,
                            "core/vsh/stun_stars_billboard.vsh",
                        ),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(
                            UsefulMagic.MOD_ID,
                            "core/fsh/stun_stars_billboard.fsh",
                        ),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            billboardShader.init()
            initialized = true
        }

        @JvmStatic
        fun spawn(
            world: ServerLevel,
            pos: Vec3,
            color: Vector3f = Vector3f(1.0f, 0.92f, 0.42f),
            brightness: Float = DEFAULT_BRIGHTNESS,
            appearTicks: Int = DEFAULT_APPEAR_TICKS,
            discardFadeTicks: Int = DEFAULT_DISCARD_FADE_TICKS,
            orbitRadius: Float = DEFAULT_ORBIT_RADIUS,
            verticalOffset: Float = DEFAULT_VERTICAL_OFFSET,
            starScale: Float = DEFAULT_STAR_SCALE,
            trailLength: Float = DEFAULT_TRAIL_LENGTH,
        ): StunStarsRenderEntity {
            return StunStarsRenderEntity(world)
                .configure(
                    pos = pos,
                    color = color,
                    brightness = brightness,
                    appearTicks = appearTicks,
                    discardFadeTicks = discardFadeTicks,
                    orbitRadius = orbitRadius,
                    verticalOffset = verticalOffset,
                    starScale = starScale,
                    trailLength = trailLength,
                )
                .also(ServerRenderEntityManager::spawn)
        }

        @JvmStatic
        fun spawn(
            world: ServerLevel,
            pos: Vec3,
            color: Vec3,
            brightness: Float = DEFAULT_BRIGHTNESS,
            appearTicks: Int = DEFAULT_APPEAR_TICKS,
            discardFadeTicks: Int = DEFAULT_DISCARD_FADE_TICKS,
            orbitRadius: Float = DEFAULT_ORBIT_RADIUS,
            verticalOffset: Float = DEFAULT_VERTICAL_OFFSET,
            starScale: Float = DEFAULT_STAR_SCALE,
            trailLength: Float = DEFAULT_TRAIL_LENGTH,
        ): StunStarsRenderEntity {
            return spawn(
                world = world,
                pos = pos,
                color = Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()),
                brightness = brightness,
                appearTicks = appearTicks,
                discardFadeTicks = discardFadeTicks,
                orbitRadius = orbitRadius,
                verticalOffset = verticalOffset,
                starScale = starScale,
                trailLength = trailLength,
            )
        }

        private fun buildQuadVertices(): List<VertexData> {
            return listOf(
                VertexData(Vector3f(-1f, -1f, 0f), Vector4f(), Vector2f()),
                VertexData(Vector3f(1f, -1f, 0f), Vector4f(), Vector2f()),
                VertexData(Vector3f(1f, 1f, 0f), Vector4f(), Vector2f()),
                VertexData(Vector3f(-1f, -1f, 0f), Vector4f(), Vector2f()),
                VertexData(Vector3f(1f, 1f, 0f), Vector4f(), Vector2f()),
                VertexData(Vector3f(-1f, 1f, 0f), Vector4f(), Vector2f()),
            )
        }

        private fun smoothstep(edge0: Float, edge1: Float, value: Float): Float {
            if (edge0 == edge1) {
                return if (value >= edge1) 1f else 0f
            }
            val x = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
            return x * x * (3f - 2f * x)
        }
    }
}
