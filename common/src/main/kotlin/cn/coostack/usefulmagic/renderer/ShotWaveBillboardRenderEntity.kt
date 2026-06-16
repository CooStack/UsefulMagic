package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.renderer.AutoRenderEntity
import cn.coostack.cooparticlesapi.renderer.runtime.LocalRenderInput
import cn.coostack.cooparticlesapi.renderer.runtime.RenderEntityInstance
import cn.coostack.cooparticlesapi.renderer.runtime.RenderEntityReleaseHook
import cn.coostack.cooparticlesapi.renderer.runtime.WorldPassRenderEntityRenderer
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
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.PI
import kotlin.random.Random

@CooAutoRegister
class ShotWaveBillboardRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos),
    WorldPassRenderEntityRenderer<ShotWaveBillboardRenderEntity>,
    RenderEntityReleaseHook<ShotWaveBillboardRenderEntity> {
    @CodecField
    var fadeInTick: Int = DEFAULT_FADE_IN_TICK

    @CodecField
    var fadeOutTick: Int = DEFAULT_FADE_OUT_TICK

    @CodecField
    var limitScale: Float = DEFAULT_LIMIT_SCALE

    @CodecField
    var initialScale: Float = DEFAULT_INITIAL_SCALE

    @CodecField
    var timeoutTick: Int = DEFAULT_TIMEOUT_TICK

    @CodecField
    var scaleSpeed: Float = DEFAULT_SCALE_SPEED

    @CodecField
    var roll: Float = randomRoll()

    @CodecField
    var rollSpeed: Float = DEFAULT_ROLL_SPEED

    @CodecField
    var useWave2Texture: Boolean = DEFAULT_USE_WAVE_2_TEXTURE

    @CodecField
    var alpha: Double = 1.0

    @CodecField
    var discardFadeTick: Int = DEFAULT_FADE_OUT_TICK

    @CodecField
    var discarding: Boolean = false

    @CodecField
    var discardStartTick: Int = 0

    @CodecField
    var discardStartAlpha: Float = 1.0f

    @Deprecated("Use limitScale instead.")
    var maxScale: Float
        get() = limitScale
        set(value) {
            limitScale = value
        }

    init {
        syncRenderRange()
    }

    override fun initialize(instance: RenderEntityInstance<ShotWaveBillboardRenderEntity>) {
        initStatic()
        syncRenderRange()
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun release(instance: RenderEntityInstance<ShotWaveBillboardRenderEntity>) {
    }

    override fun clientTick() {
        updateLifecycle()
        syncRenderRange()
    }

    override fun serverTick() {
        updateLifecycle()
        syncRenderRange()
    }

    override fun renderLocal(input: LocalRenderInput<ShotWaveBillboardRenderEntity>) {
        initStatic()
        val visibleAlpha = currentAlpha(input.tickDelta)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val scale = currentScale(input.tickDelta)
        if (scale <= MIN_SCALE) {
            return
        }
        renderBillboard(
            modelMatrix = input.modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            inverseViewRotationMatrix = Matrix3f(input.viewMatrix).invert(),
            scale = scale,
            visibleAlpha = visibleAlpha,
            time = currentTimeline(input.tickDelta),
        )
    }

    fun configure(
        fadeInTick: Int = this.fadeInTick,
        fadeOutTick: Int = this.fadeOutTick,
        limitScale: Float = this.limitScale,
        timeoutTick: Int = this.timeoutTick,
        scaleSpeed: Float = this.scaleSpeed,
        roll: Float? = null,
        alpha: Double = this.alpha,
        initialScale: Float? = null,
        useWave2Texture: Boolean = this.useWave2Texture,
        rollSpeed: Float = this.rollSpeed,
    ): ShotWaveBillboardRenderEntity {
        this.fadeInTick = fadeInTick.coerceAtLeast(0)
        this.fadeOutTick = fadeOutTick.coerceAtLeast(0)
        this.timeoutTick = timeoutTick.coerceAtLeast(0)
        this.limitScale = limitScale.coerceAtLeast(MIN_SCALE)
        this.scaleSpeed = normalizeScaleSpeed(scaleSpeed)
        this.initialScale = normalizeInitialScale(
            initialScale ?: defaultInitialScale(this.limitScale, this.scaleSpeed, totalDurationTicks()),
            this.limitScale,
            this.scaleSpeed,
        )
        this.roll = roll ?: randomRoll()
        this.rollSpeed = rollSpeed
        this.useWave2Texture = useWave2Texture
        this.alpha = alpha.coerceIn(0.0, 1.0)
        this.discardFadeTick = this.fadeOutTick
        this.discarding = false
        this.discardStartTick = 0
        this.discardStartAlpha = 1.0f
        syncRenderRange()
        markDirty()
        return this
    }

    fun discard(fadeTicks: Int = fadeOutTick): ShotWaveBillboardRenderEntity {
        startFadeOut(fadeTicks, sync = true)
        return this
    }

    private fun renderBillboard(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        inverseViewRotationMatrix: Matrix3f,
        scale: Float,
        visibleAlpha: Float,
        time: Float,
    ) {
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.blendFunc(770, 1)
        RenderSystem.depthMask(false)
        try {
            shotWaveShader.useOnContext {
                RenderSystem.setShaderTexture(0, if (useWave2Texture) SHOT_WAVE_2_TEXTURE else SHOT_WAVE_TEXTURE)
                setMatrix4("modelMatrix", modelMatrix)
                setMatrix4("viewMatrix", viewMatrix)
                setMatrix4("projMatrix", projMatrix)
                setMatrix3f("inverseViewRotationMatrix", inverseViewRotationMatrix)
                setFloat2("scale", Vector2f(scale, scale))
                setFloat("roll", roll + time * rollSpeed)
                setFloat("alpha", visibleAlpha)
                setFloat("time", time)
                setInt("shotWaveTexture", 0)
                shotWaveVertexBuffer.draw()
            }
        } finally {
            RenderSystem.depthMask(true)
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableBlend()
            RenderSystem.enableCull()
        }
    }

    private fun updateLifecycle() {
        val scale = currentScale(0f)
        if (!discarding && reachedLimit(scale)) {
            startFadeOut(fadeOutTick, sync = false)
            return
        }
        if (discarding && age - discardStartTick >= discardFadeTick.coerceAtLeast(0)) {
            canceled = true
            return
        }
        if (age > totalDurationTicks() + 1) {
            canceled = true
        }
    }

    private fun syncRenderRange() {
        renderRange = expectedMaxScale().coerceAtLeast(MIN_SCALE).toDouble() *
                RENDER_RANGE_SCALE + RENDER_RANGE_PADDING
    }

    private fun expectedMaxScale(): Float {
        if (scaleSpeed <= 0f) {
            return maxOf(initialScale, limitScale)
        }
        return maxOf(initialScale, limitScale, initialScale + totalDurationTicks().coerceAtLeast(1) * scaleSpeed)
    }

    private fun totalDurationTicks(): Int {
        return fadeInTick.coerceAtLeast(0) + timeoutTick.coerceAtLeast(0) + fadeOutTick.coerceAtLeast(0)
    }

    private fun currentTimeline(tickDelta: Float): Float {
        return (age - 1f + tickDelta).coerceAtLeast(0f)
    }

    private fun currentScale(tickDelta: Float): Float {
        val rawScale = initialScale + currentTimeline(tickDelta) * scaleSpeed
        return if (scaleSpeed >= 0f) {
            rawScale
        } else {
            rawScale.coerceAtLeast(limitScale)
        }.coerceAtLeast(0f)
    }

    private fun currentAlpha(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val scheduledAlpha = scheduledAlpha(time)
        if (discarding) {
            val elapsed = age - discardStartTick - 1f + tickDelta
            val fade = if (discardFadeTick <= 0) {
                0f
            } else {
                1f - (elapsed / discardFadeTick.toFloat()).coerceIn(0f, 1f)
            }
            return discardStartAlpha * fade
        }
        return scheduledAlpha
    }

    private fun scheduledAlpha(time: Float): Float {
        val fadeIn = fadeInTick.coerceAtLeast(0).toFloat()
        val alive = timeoutTick.coerceAtLeast(0).toFloat()
        val fadeOut = fadeOutTick.coerceAtLeast(0).toFloat()
        val fadeOutStart = fadeIn + alive
        val baseAlpha = alpha.toFloat().coerceIn(0f, 1f)
        val inAlpha = if (fadeIn <= 0f) 1f else smoothstep(0f, fadeIn, time)
        val outAlpha = if (fadeOut <= 0f) {
            if (time <= fadeOutStart) 1f else 0f
        } else {
            1f - smoothstep(fadeOutStart, fadeOutStart + fadeOut, time)
        }
        return baseAlpha * inAlpha * outAlpha
    }

    private fun startFadeOut(fadeTicks: Int, sync: Boolean) {
        if (discarding || canceled) {
            return
        }
        discardFadeTick = fadeTicks.coerceAtLeast(0)
        discardStartAlpha = scheduledAlpha(currentTimeline(0f))
        discardStartTick = age
        discarding = true
        if (discardFadeTick == 0) {
            canceled = true
        }
        markDirty()
        if (sync) {
            requestSync()
        }
    }

    private fun reachedLimit(scale: Float): Boolean {
        return if (scaleSpeed >= 0f) {
            scale >= limitScale.coerceAtLeast(MIN_SCALE)
        } else {
            scale <= limitScale.coerceAtLeast(MIN_SCALE)
        }
    }

    private fun normalizeScaleSpeed(value: Float): Float {
        return when {
            value > 0f -> value.coerceAtLeast(MIN_SCALE_SPEED)
            value < 0f -> value.coerceAtMost(-MIN_SCALE_SPEED)
            else -> MIN_SCALE_SPEED
        }
    }

    private fun normalizeInitialScale(value: Float, limit: Float, speed: Float): Float {
        val scale = value.coerceAtLeast(MIN_SCALE)
        return if (speed >= 0f && scale >= limit) {
            MIN_SCALE
        } else if (speed < 0f && scale <= limit) {
            defaultInitialScale(limit, speed, totalDurationTicks())
        } else {
            scale
        }
    }

    companion object {
        private const val DEFAULT_FADE_IN_TICK = 4
        private const val DEFAULT_FADE_OUT_TICK = 8
        private const val DEFAULT_LIMIT_SCALE = 8.0f
        private const val DEFAULT_INITIAL_SCALE = 0.0f
        private const val DEFAULT_TIMEOUT_TICK = 12
        private const val DEFAULT_SCALE_SPEED = 0.34f
        private const val DEFAULT_ROLL_SPEED = 0.16f
        private const val DEFAULT_USE_WAVE_2_TEXTURE = true
        private const val MIN_VISIBLE_ALPHA = 0.001f
        private const val MIN_SCALE = 0.001f
        private const val MIN_SCALE_SPEED = 0.0001f
        private const val BILLBOARD_EXTENT = 1.0f
        private const val RENDER_RANGE_SCALE = 2.4
        private const val RENDER_RANGE_PADDING = 16.0

        @JvmField
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "shot_wave_billboard_render_entity")

        @JvmField
        val SHOT_WAVE_TEXTURE: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/effect/shot_wave.png")

        @JvmField
        val SHOT_WAVE_2_TEXTURE: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/effect/shot_wave_2.png")

        @JvmField
        var initialized: Boolean = false

        private lateinit var shotWaveVertexBuffer: SimpleVertexBuffer
        private lateinit var shotWaveShader: CooShaderProgram

        @JvmStatic
        fun spawn(
            world: ServerLevel,
            pos: Vec3,
            fadeInTick: Int = DEFAULT_FADE_IN_TICK,
            fadeOutTick: Int = DEFAULT_FADE_OUT_TICK,
            limitScale: Float = DEFAULT_LIMIT_SCALE,
            timeoutTick: Int = DEFAULT_TIMEOUT_TICK,
            scaleSpeed: Float = DEFAULT_SCALE_SPEED,
            roll: Float? = null,
            alpha: Double = 1.0,
            initialScale: Float? = null,
            useWave2Texture: Boolean = DEFAULT_USE_WAVE_2_TEXTURE,
            rollSpeed: Float = DEFAULT_ROLL_SPEED,
        ): ShotWaveBillboardRenderEntity {
            return ShotWaveBillboardRenderEntity(world, pos)
                .configure(
                    fadeInTick = fadeInTick,
                    fadeOutTick = fadeOutTick,
                    limitScale = limitScale,
                    timeoutTick = timeoutTick,
                    scaleSpeed = scaleSpeed,
                    roll = roll,
                    alpha = alpha,
                    initialScale = initialScale,
                    useWave2Texture = useWave2Texture,
                    rollSpeed = rollSpeed,
                )
                .also(ServerRenderEntityManager::spawn)
        }

        private fun defaultInitialScale(limit: Float, speed: Float, durationTicks: Int): Float {
            return if (speed >= 0f) {
                DEFAULT_INITIAL_SCALE
            } else {
                limit + -speed * durationTicks.coerceAtLeast(1)
            }
        }

        @JvmStatic
        fun initStatic() {
            if (initialized) {
                return
            }
            shotWaveVertexBuffer = SimpleVertexBuffer().apply {
                init()
                setVertexes(buildQuadVertices(), CooVertexFormat.POINT_TEXTURE_UV_FORMAT)
            }
            shotWaveShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/vsh/shot_wave_billboard.vsh"),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/fsh/shot_wave_billboard.fsh"),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            shotWaveShader.init()
            initialized = true
        }

        private fun buildQuadVertices(): List<VertexData> {
            return listOf(
                VertexData(Vector3f(-BILLBOARD_EXTENT, -BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f(0f, 0f)),
                VertexData(Vector3f(BILLBOARD_EXTENT, -BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f(1f, 0f)),
                VertexData(Vector3f(BILLBOARD_EXTENT, BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f(1f, 1f)),
                VertexData(Vector3f(-BILLBOARD_EXTENT, -BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f(0f, 0f)),
                VertexData(Vector3f(BILLBOARD_EXTENT, BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f(1f, 1f)),
                VertexData(Vector3f(-BILLBOARD_EXTENT, BILLBOARD_EXTENT, 0f), Vector4f(), Vector2f(0f, 1f)),
            )
        }

        private fun randomRoll(): Float {
            return Random.nextFloat() * (PI.toFloat() * 2f)
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
