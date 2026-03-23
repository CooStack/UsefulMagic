package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.annotations.renderer.handle.RenderEntityHelper
import cn.coostack.cooparticlesapi.renderer.AutoRenderEntity
import cn.coostack.cooparticlesapi.renderer.RenderEntity
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
import cn.coostack.cooparticlesapi.renderer.shader.data.VertexData
import cn.coostack.cooparticlesapi.renderer.shader.glsl.IdentifierShader
import cn.coostack.cooparticlesapi.renderer.shader.vertex.SimpleVertexBuffer
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Matrix4fStack
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private enum class MonsterRoarBlendMode {
    ALPHA,
    ADDITIVE,
}

private data class MonsterRoarPass(
    val blendMode: MonsterRoarBlendMode,
    val color: Vector3f,
    val alpha: Float,
    val brightness: Float,
    val edgeBoost: Float,
    val coreGlow: Float,
    val refractionStrength: Float,
    val noiseStrength: Float,
)

@CooAutoRegister
class MonsterRoarRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos),
    WorldPassRenderEntityRenderer<MonsterRoarRenderEntity>,
    FramePostRenderEntityRenderer<MonsterRoarRenderEntity>,
    RenderEntityReleaseHook<MonsterRoarRenderEntity> {
    @CodecField
    var sourceEntityId: Int = NO_SOURCE_ENTITY

    @CodecField
    var start: Vec3 = pos

    @CodecField
    var direction: Vec3 = Vec3(0.0, 0.0, 1.0)

    @CodecField
    var color: Vector3f = Vector3f(0.52f, 0.86f, 1.0f)

    @CodecField
    var alpha: Double = 1.0

    @CodecField
    var maxDistance: Float = DEFAULT_MAX_DISTANCE

    @CodecField
    var maxRadius: Float = DEFAULT_MAX_RADIUS

    @CodecField
    var lifetime: Int = DEFAULT_LIFETIME

    @CodecField
    var fadeTicks: Int = DEFAULT_FADE_TICKS

    @CodecField
    var mouthOffset: Double = DEFAULT_MOUTH_OFFSET

    override fun initialize(instance: RenderEntityInstance<MonsterRoarRenderEntity>) {
        initStatic()
        refreshFromSource(syncNetwork = false)
        syncAnchor()
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun release(instance: RenderEntityInstance<MonsterRoarRenderEntity>) {
    }

    override fun clientTick() {
        refreshFromSource(syncNetwork = false)
        syncAnchor()
        updateLifecycle()
    }

    override fun serverTick() {
        refreshFromSource(syncNetwork = true)
        syncAnchor()
        updateLifecycle()
    }

    override fun renderLocal(input: LocalRenderInput<MonsterRoarRenderEntity>) {
        initStatic()
        refreshFromSource(syncNetwork = false)
        syncAnchor()
        val timeline = currentTimeline(input.tickDelta)
        val visibleAlpha = currentAlpha(timeline)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA || maxDistance <= MIN_DISTANCE || maxRadius <= MIN_RADIUS) {
            return
        }
        renderPasses(
            modelMatrix = orientedModelMatrix(input.modelMatrix),
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            time = timeline,
            passAlpha = visibleAlpha,
            bloomPass = false,
        )
    }

    override fun collectRenderContributions(
        input: RenderContributionInput<MonsterRoarRenderEntity>,
        collector: RenderContributionCollector,
    ) {
        val timeline = currentTimeline(input.frameContext.tickDelta)
        val visibleAlpha = currentAlpha(timeline)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        collector.submit(
            BuiltinRenderEffectDescriptors.maskBloom(
                effectId = MONSTER_ROAR_BLOOM_EFFECT_ID,
                sourceInstanceId = uuid.toString(),
                frameContext = input.frameContext,
                sourceEntity = this,
                config = buildBloomConfig(visibleAlpha),
                priority = MONSTER_ROAR_BLOOM_PRIORITY,
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
        sourceEntity: Entity,
        direction: Vec3,
        lifetime: Int = DEFAULT_LIFETIME,
        color: Vector3f = this.color,
        maxDistance: Float = DEFAULT_MAX_DISTANCE,
        maxRadius: Float = DEFAULT_MAX_RADIUS,
    ): MonsterRoarRenderEntity {
        this.sourceEntityId = sourceEntity.id
        this.direction = normalizedDirection(direction)
        this.start = resolveMouthPosition(sourceEntity, this.direction)
        this.lifetime = lifetime.coerceAtLeast(1)
        this.maxDistance = maxDistance.coerceAtLeast(MIN_DISTANCE)
        this.maxRadius = maxRadius.coerceAtLeast(MIN_RADIUS)
        this.color = Vector3f(color)
        syncAnchor()
        markDirty()
        return this
    }

    fun refresh(
        direction: Vec3,
        color: Vector3f = this.color,
    ): MonsterRoarRenderEntity {
        this.direction = normalizedDirection(direction)
        sourceEntity()?.let { source ->
            this.start = resolveMouthPosition(source, this.direction)
        }
        this.color = Vector3f(color)
        syncAnchor()
        markDirty()
        return this
    }

    fun finish() {
        canceled = true
        markDirty()
    }

    private fun renderBloomMask(
        tickDelta: Float,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
    ) {
        initStatic()
        refreshFromSource(syncNetwork = false)
        syncAnchor()
        val timeline = currentTimeline(tickDelta)
        val visibleAlpha = currentAlpha(timeline)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val modelMatrix = Matrix4fStack(16)
        RenderUtil.setRenderStackWithEntity(modelMatrix, this, tickDelta)
        renderPasses(
            modelMatrix = orientedModelMatrix(modelMatrix),
            viewMatrix = viewMatrix,
            projMatrix = projMatrix,
            time = timeline,
            passAlpha = visibleAlpha * 0.72f,
            bloomPass = true,
        )
    }

    private fun buildBloomConfig(visibleAlpha: Float): MaskBloomConfig {
        val energy = visibleAlpha.coerceIn(0.18f, 1.0f)
        return MONSTER_ROAR_BLOOM_CONFIG.copy(
            blurSigma = MONSTER_ROAR_BLOOM_CONFIG.blurSigma * mix(0.82f, 1.12f, energy),
            blurRange = MONSTER_ROAR_BLOOM_CONFIG.blurRange * mix(0.86f, 1.16f, energy),
            intensity = MONSTER_ROAR_BLOOM_CONFIG.intensity * mix(0.78f, 1.18f, energy),
            tint = Vector3f(color).lerp(Vector3f(0.88f, 0.98f, 1.0f), 0.32f),
        )
    }

    private fun refreshFromSource(syncNetwork: Boolean) {
        val source = sourceEntity() ?: return
        val resolvedDirection = when (source) {
            is MagicDragonEntity -> source.getBreathAimDirection()
            else -> source.forward
        }
        val normalized = normalizedDirection(resolvedDirection)
        val resolvedStart = resolveMouthPosition(source, normalized)
        if (resolvedStart == start && normalized == direction) {
            return
        }
        start = resolvedStart
        direction = normalized
        if (syncNetwork) {
            markDirty()
        }
    }

    private fun resolveMouthPosition(source: Entity, direction: Vec3): Vec3 {
        return when (source) {
            is MagicDragonEntity -> source.getMouthPosition(direction)
            else -> source.eyePosition.add(normalizedDirection(direction).scale(mouthOffset))
        }
    }

    private fun sourceEntity(): Entity? {
        if (sourceEntityId == NO_SOURCE_ENTITY) {
            return null
        }
        return world?.getEntity(sourceEntityId)
    }

    private fun syncAnchor() {
        val normalized = normalizedDirection(direction)
        pos = start.add(normalized.scale(maxDistance * 0.5))
        renderRange = maxDistance * 0.5 + maxRadius * 4.0 + VIEW_PADDING
    }

    private fun currentTimeline(tickDelta: Float): Float {
        return (age - 1f + tickDelta).coerceAtLeast(0f)
    }

    private fun currentAlpha(time: Float): Float {
        val baseAlpha = alpha.toFloat().coerceIn(0f, 1f)
        val fadeIn = smoothstep(0f, FADE_IN_TICKS, time)
        val fadeOutStart = lifetime.coerceAtLeast(1).toFloat()
        val fadeOutDuration = fadeTicks.coerceAtLeast(1).toFloat()
        val fadeOut = 1f - smoothstep(fadeOutStart, fadeOutStart + fadeOutDuration, time)
        return baseAlpha * fadeIn * fadeOut
    }

    private fun updateLifecycle() {
        if (sourceEntityId != NO_SOURCE_ENTITY && sourceEntity() == null) {
            canceled = true
            return
        }
        if (age > lifetime.coerceAtLeast(1) + fadeTicks.coerceAtLeast(1) + 2) {
            canceled = true
        }
    }

    private fun renderPasses(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        time: Float,
        passAlpha: Float,
        bloomPass: Boolean,
    ) {
        if (passAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val baseColor = Vector3f(color)
        val passes = if (bloomPass) {
            listOf(
                MonsterRoarPass(
                    blendMode = MonsterRoarBlendMode.ADDITIVE,
                    color = Vector3f(baseColor).lerp(Vector3f(0.92f, 0.99f, 1.0f), 0.42f),
                    alpha = (passAlpha * 0.42f).coerceAtMost(0.62f),
                    brightness = 3.8f,
                    edgeBoost = 1.26f,
                    coreGlow = 0.58f,
                    refractionStrength = 0.12f,
                    noiseStrength = 0.86f,
                ),
            )
        } else {
            listOf(
                MonsterRoarPass(
                    blendMode = MonsterRoarBlendMode.ALPHA,
                    color = baseColor,
                    alpha = (passAlpha * 0.38f).coerceAtMost(0.62f),
                    brightness = 1.38f,
                    edgeBoost = 0.72f,
                    coreGlow = 0.28f,
                    refractionStrength = 0.18f,
                    noiseStrength = 0.72f,
                ),
                MonsterRoarPass(
                    blendMode = MonsterRoarBlendMode.ADDITIVE,
                    color = Vector3f(baseColor).lerp(Vector3f(1.0f, 0.96f, 0.84f), 0.34f),
                    alpha = (passAlpha * 0.16f).coerceAtMost(0.34f),
                    brightness = 2.5f,
                    edgeBoost = 1.12f,
                    coreGlow = 0.44f,
                    refractionStrength = 0.10f,
                    noiseStrength = 0.92f,
                ),
            )
        }
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            passes.forEach { pass ->
                if (pass.alpha <= MIN_VISIBLE_ALPHA) {
                    return@forEach
                }
                when (pass.blendMode) {
                    MonsterRoarBlendMode.ALPHA -> RenderSystem.blendFunc(770, 771)
                    MonsterRoarBlendMode.ADDITIVE -> RenderSystem.blendFunc(770, 1)
                }
                drawPass(modelMatrix, viewMatrix, projMatrix, time, pass)
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
        time: Float,
        pass: MonsterRoarPass,
    ) {
        roarShader.useOnContext {
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat("coneLength", maxDistance.coerceAtLeast(MIN_DISTANCE))
            setFloat("coneRadius", maxRadius.coerceAtLeast(MIN_RADIUS))
            setFloat3("color", pass.color)
            setFloat("alpha", pass.alpha)
            setFloat("brightness", pass.brightness)
            setFloat("edgeBoost", pass.edgeBoost)
            setFloat("coreGlow", pass.coreGlow)
            setFloat("refractionStrength", pass.refractionStrength)
            setFloat("noiseStrength", pass.noiseStrength)
            setFloat("lifetime", lifetime.coerceAtLeast(1).toFloat())
            setFloat("fadeTicks", fadeTicks.coerceAtLeast(1).toFloat())
            setFloat("time", time)
            roarVertexBuffer.draw()
        }
    }

    private fun orientedModelMatrix(baseMatrix: Matrix4f): Matrix4f {
        val normalized = normalizedDirection(direction)
        return Matrix4f(baseMatrix).translate(
            (start.x - pos.x).toFloat(),
            (start.y - pos.y).toFloat(),
            (start.z - pos.z).toFloat(),
        ).rotate(
            Quaternionf().rotationTo(
                0f,
                1f,
                0f,
                normalized.x.toFloat(),
                normalized.y.toFloat(),
                normalized.z.toFloat(),
            ),
        )
    }

    companion object {
        private const val DEFAULT_MAX_DISTANCE = 9.5f
        private const val DEFAULT_MAX_RADIUS = 3.2f
        private const val DEFAULT_LIFETIME = 18
        private const val DEFAULT_FADE_TICKS = 7
        private const val DEFAULT_MOUTH_OFFSET = 0.65
        private const val FADE_IN_TICKS = 3.5f
        private const val MIN_DISTANCE = 0.08f
        private const val MIN_RADIUS = 0.02f
        private const val MIN_VISIBLE_ALPHA = 0.001f
        private const val VIEW_PADDING = 36.0
        private const val MIN_DIRECTION_LENGTH_SQR = 1.0E-6
        private const val NO_SOURCE_ENTITY = -1
        private const val MONSTER_ROAR_BLOOM_EFFECT_ID = "usefulmagic:monster_roar_bloom"
        private const val MONSTER_ROAR_BLOOM_PRIORITY = 260

        @JvmField
        val CODEC: StreamCodec<FriendlyByteBuf, RenderEntity> =
            RenderEntityHelper.generateCodec(MonsterRoarRenderEntity())

        @JvmField
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "monster_roar_render_entity")

        @JvmField
        var initialized: Boolean = false

        private val MONSTER_ROAR_BLOOM_CONFIG = MaskBloomConfig(
            blurSigma = 6.2f,
            blurRange = 4.6f,
            intensity = 1.55f,
            baseMaskIntensity = 0.0f,
            threshold = 0.018f,
            thresholdSoftness = 0.03f,
            tint = Vector3f(0.62f, 0.90f, 1.0f),
        )

        private lateinit var roarVertexBuffer: SimpleVertexBuffer
        private lateinit var roarShader: CooShaderProgram

        @JvmStatic
        fun spawn(
            world: ServerLevel,
            sourceEntity: Entity,
            direction: Vec3,
            lifetime: Int = DEFAULT_LIFETIME,
            color: Vector3f = Vector3f(0.52f, 0.86f, 1.0f),
            maxDistance: Float = DEFAULT_MAX_DISTANCE,
            maxRadius: Float = DEFAULT_MAX_RADIUS,
        ): MonsterRoarRenderEntity {
            return MonsterRoarRenderEntity(world)
                .configure(sourceEntity, direction, lifetime, color, maxDistance, maxRadius)
                .also(ServerRenderEntityManager::spawn)
        }

        @JvmStatic
        fun initStatic() {
            if (initialized) {
                return
            }
            roarVertexBuffer = SimpleVertexBuffer().apply {
                init()
                setVertexes(buildConeFieldVertices(), CooVertexFormat.POINT_FORMAT)
            }
            roarShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/vsh/monster_roar_beam.vsh"),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/fsh/monster_roar_beam.fsh"),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            roarShader.init()
            initialized = true
        }

        private fun buildConeFieldVertices(): List<VertexData> {
            val segments = 42
            val vertices = ArrayList<VertexData>(segments * 9)
            val tip = Vector3f(0f, 0f, 0f)
            val center = Vector3f(0f, 1f, 0f)

            for (segment in 0 until segments) {
                val angle0 = (Math.PI.toFloat() * 2f * segment) / segments.toFloat()
                val angle1 = (Math.PI.toFloat() * 2f * (segment + 1)) / segments.toFloat()
                val x0 = cos(angle0)
                val z0 = sin(angle0)
                val x1 = cos(angle1)
                val z1 = sin(angle1)

                val a = Vector3f(x0, 1f, z0)
                val b = Vector3f(x1, 1f, z1)

                appendTriangle(vertices, tip, a, b)
                appendTriangle(vertices, center, b, a)
            }
            return vertices
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

        private fun normalizedDirection(direction: Vec3): Vec3 {
            return if (direction.lengthSqr() <= MIN_DIRECTION_LENGTH_SQR) {
                Vec3(0.0, 0.0, 1.0)
            } else {
                direction.normalize()
            }
        }

        private fun smoothstep(edge0: Float, edge1: Float, value: Float): Float {
            if (edge0 == edge1) {
                return if (value >= edge1) 1f else 0f
            }
            val x = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
            return x * x * (3f - 2f * x)
        }

        private fun mix(from: Float, to: Float, alpha: Float): Float {
            return from + (to - from) * alpha.coerceIn(0f, 1f)
        }
    }
}
