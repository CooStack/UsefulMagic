package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.renderer.AutoRenderEntity
import cn.coostack.cooparticlesapi.renderer.client.ClientRenderPipelineManager
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
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Matrix4fStack
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import cn.coostack.cooparticlesapi.extend.*
import java.lang.Math
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private data class StraightLaserProjectedBlend(
    val projectedRadiusPx: Float,
    val directWeight: Float,
)

@CooAutoRegister
class StraightLaserRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
) : AutoRenderEntity(world, pos),
    WorldPassRenderEntityRenderer<StraightLaserRenderEntity>,
    FramePostRenderEntityRenderer<StraightLaserRenderEntity>,
    RenderEntityReleaseHook<StraightLaserRenderEntity> {
    @CodecField
    var end: Vec3 = pos

    @CodecField
    var previousStart: Vec3 = pos

    @CodecField
    var previousEnd: Vec3 = pos

    @CodecField
    var sourceEntityId: Int = NO_SOURCE_ENTITY

    @CodecField
    var phaseTicks: Int = DEFAULT_PHASE_TICKS

    @CodecField
    var lifetime: Int = DEFAULT_LIFETIME

    @CodecField
    var color: Vector3f = Vector3f(0.28f, 0.82f, 1.0f)

    @CodecField
    var alpha: Double = 1.0

    @CodecField
    var brightness: Float = 1.0f

    @CodecField
    var maxRadius: Float = DEFAULT_MAX_RADIUS

    override fun initialize(instance: RenderEntityInstance<StraightLaserRenderEntity>) {
        initStatic()
        syncAnchor()
        syncPreviousBeamIfUnset()
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun release(instance: RenderEntityInstance<StraightLaserRenderEntity>) {
    }

    override fun clientTick() {
        syncAnchor()
        updateLifecycle()
    }

    override fun serverTick() {
        syncAnchor()
        updateLifecycle()
    }

    override fun renderLocal(input: LocalRenderInput<StraightLaserRenderEntity>) {
        initStatic()
        syncAnchor()
        val renderStart = renderStart(input.tickDelta)
        val renderEnd = renderEnd(input.tickDelta)
        val beamLength = beamLength(renderStart, renderEnd)
        if (beamLength <= MIN_BEAM_LENGTH) {
            return
        }
        val bodyAlpha = currentBodyAlpha(input.tickDelta)
        if (bodyAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val radius = currentRadius(input.tickDelta)
        val blend = projectedBlend(
            radius = radius,
            start = renderStart,
            end = renderEnd,
            cameraWorldPos = currentCameraWorldPos(),
            viewRotationMatrix = Matrix3f(input.viewMatrix),
            inverseViewRotationMatrix = Matrix3f(input.viewMatrix).invert(),
            projMatrix = input.projMatrix,
            screenSize = currentScreenSize(),
        )
        val directAlpha = bodyAlpha * mix(0.80f, 1.0f, blend.directWeight)
        if (directAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        renderPasses(
            modelMatrix = orientedModelMatrix(input.modelMatrix, renderStart, renderEnd, renderStart),
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            beamLength = beamLength,
            radius = radius,
            passAlpha = directAlpha,
            phaseProgress = currentPhaseProgress(input.tickDelta),
            collapse = currentCollapse(input.tickDelta),
            time = getTime(input.tickDelta),
            bloomPass = false,
        )
    }

    override fun collectRenderContributions(
        input: RenderContributionInput<StraightLaserRenderEntity>,
        collector: RenderContributionCollector,
    ) {
        collector.submit(
            BuiltinRenderEffectDescriptors.maskBloom(
                effectId = LASER_BLOOM_EFFECT_ID,
                sourceInstanceId = uuid.toString(),
                frameContext = input.frameContext,
                sourceEntity = this,
                config = buildBloomMaskConfig(input.frameContext.tickDelta),
                priority = LASER_BLOOM_PRIORITY,
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
        start: Vec3,
        end: Vec3,
        tick: Int,
        lifetime: Int,
    ): StraightLaserRenderEntity {
        this.sourceEntityId = NO_SOURCE_ENTITY
        this.previousStart = this.pos
        this.previousEnd = this.end
        this.end = end
        this.pos = start
        this.phaseTicks = tick.coerceAtLeast(1)
        this.lifetime = lifetime.coerceAtLeast(0)
        syncAnchor()
        markDirty()
        return this
    }

    fun configure(
        sourceEntity: Entity,
        end: Vec3,
        tick: Int,
        lifetime: Int,
        color: Vector3f = this.color,
        maxRadius: Float = this.maxRadius,
        brightness: Float = this.brightness,
    ): StraightLaserRenderEntity {
        this.sourceEntityId = NO_SOURCE_ENTITY
        this.previousStart = this.pos
        this.previousEnd = this.end
        this.pos = sourceEntity.position()
        this.end = end
        this.phaseTicks = tick.coerceAtLeast(1)
        this.lifetime = lifetime.coerceAtLeast(0)
        setColor(color)
        setMaxRadius(maxRadius)
        setBrightness(brightness)
        syncAnchor()
        markDirty()
        return this
    }

    fun configure(
        sourceEntity: Entity,
        end: Vec3,
        tick: Int,
        lifetime: Int,
        color: Vec3,
        maxRadius: Float = this.maxRadius,
        brightness: Float = this.brightness,
    ): StraightLaserRenderEntity {
        return configure(
            sourceEntity = sourceEntity,
            end = end,
            tick = tick,
            lifetime = lifetime,
            color = Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()),
            maxRadius = maxRadius,
            brightness = brightness,
        )
    }

    fun configure(
        start: Vec3,
        end: Vec3,
        tick: Int,
        lifetime: Int,
        color: Vector3f,
        maxRadius: Float = this.maxRadius,
        brightness: Float = this.brightness,
    ): StraightLaserRenderEntity {
        configure(start, end, tick, lifetime)
        setColor(color)
        setMaxRadius(maxRadius)
        setBrightness(brightness)
        return this
    }

    fun configure(
        start: Vec3,
        end: Vec3,
        tick: Int,
        lifetime: Int,
        color: Vec3,
        maxRadius: Float = this.maxRadius,
        brightness: Float = this.brightness,
    ): StraightLaserRenderEntity {
        return configure(
            start = start,
            end = end,
            tick = tick,
            lifetime = lifetime,
            color = Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()),
            maxRadius = maxRadius,
            brightness = brightness,
        )
    }

    fun setColor(color: Vec3): StraightLaserRenderEntity {
        return setColor(Vector3f(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()))
    }

    fun setColor(color: Vector3f): StraightLaserRenderEntity {
        this.color = Vector3f(color)
        markDirty()
        return this
    }

    fun setMaxRadius(maxRadius: Float): StraightLaserRenderEntity {
        this.maxRadius = maxRadius.coerceAtLeast(MIN_RADIUS)
        markDirty()
        return this
    }

    fun setBrightness(brightness: Float): StraightLaserRenderEntity {
        this.brightness = brightness.coerceAtLeast(0f)
        markDirty()
        return this
    }

    fun updateBeam(start: Vec3, end: Vec3): StraightLaserRenderEntity {
        this.sourceEntityId = NO_SOURCE_ENTITY
        if (this.pos == start && this.end == end) {
            return this
        }
        this.previousStart = this.pos
        this.previousEnd = this.end
        this.pos = start
        this.end = end
        syncAnchor()
        markDirty()
        return this
    }

    fun discard(): StraightLaserRenderEntity {
        val collapseStart = phaseTicks.coerceAtLeast(1) + lifetime.coerceAtLeast(0)
        if (age < collapseStart) {
            age = collapseStart
            requestSync()
        }
        return this
    }

    private fun renderBloomMask(
        tickDelta: Float,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
    ) {
        syncAnchor()
        val renderStart = renderStart(tickDelta)
        val renderEnd = renderEnd(tickDelta)
        val beamLength = beamLength(renderStart, renderEnd)
        if (beamLength <= MIN_BEAM_LENGTH) {
            return
        }
        val bloomAlpha = currentBloomAlpha(tickDelta)
        if (bloomAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val radius = currentRadius(tickDelta)
        val blend = projectedBlend(
            radius = radius,
            start = renderStart,
            end = renderEnd,
            cameraWorldPos = currentCameraWorldPos(),
            viewRotationMatrix = Matrix3f(viewMatrix),
            inverseViewRotationMatrix = Matrix3f(viewMatrix).invert(),
            projMatrix = projMatrix,
            screenSize = currentScreenSize(),
        )
        val maskAlpha = bloomAlpha *
                mix(0.28f, 0.62f, 1.0f - blend.directWeight) *
                max(0.62f, farBloomIntensityScale(blend.projectedRadiusPx, 18.0f, 2.0f))
        if (maskAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val modelMatrix = Matrix4fStack(16)
        RenderUtil.setRenderStackWithEntity(modelMatrix, this, tickDelta)
        renderPasses(
            modelMatrix = orientedModelMatrix(modelMatrix, renderStart, renderEnd, renderStart),
            viewMatrix = viewMatrix,
            projMatrix = projMatrix,
            beamLength = beamLength,
            radius = radius,
            passAlpha = maskAlpha,
            phaseProgress = currentPhaseProgress(tickDelta),
            collapse = currentCollapse(tickDelta),
            time = getTime(tickDelta),
            bloomPass = true,
        )
    }

    private fun buildBloomMaskConfig(tickDelta: Float): MaskBloomConfig {
        val bloomAlpha = currentBloomAlpha(tickDelta).coerceIn(0f, 1.16f)
        val sourceLuminance = max(color.x, max(color.y, color.z)).coerceIn(0.08f, 1.0f)
        val brightnessScale = brightness.coerceAtLeast(0f)
        val visibleBrightness = (sourceLuminance * bloomAlpha * brightnessScale).coerceIn(0f, 1.0f)
        val radiusScale = (currentRadius(tickDelta) / DEFAULT_MAX_RADIUS).coerceIn(0.45f, 1.8f)
        return LASER_BLOOM_CONFIG.copy(
            blurSigma = LASER_BLOOM_CONFIG.blurSigma * mix(0.72f, 1.18f, visibleBrightness) * radiusScale,
            blurRange = LASER_BLOOM_CONFIG.blurRange * mix(0.78f, 1.28f, visibleBrightness) * radiusScale,
            intensity = LASER_BLOOM_CONFIG.intensity * mix(0.82f, 1.34f, visibleBrightness) * brightnessScale,
            baseMaskIntensity = LASER_BLOOM_CONFIG.baseMaskIntensity * mix(0.66f, 1.0f, visibleBrightness) * brightnessScale,
            threshold = mix(0.05f, 0.015f, visibleBrightness),
            tint = mixColor(color, Vector3f(0.84f, 0.96f, 1.0f), 0.34f),
        )
    }

    private fun sourceEntity(): Entity? {
        if (sourceEntityId == NO_SOURCE_ENTITY) {
            return null
        }
        return world?.getEntity(sourceEntityId)
    }

    private fun syncAnchor() {
    }

    private fun renderStart(tickDelta: Float): Vec3 {
        return previousStart.lerp(pos, tickDelta.toDouble().coerceIn(0.0, 1.0))
    }

    private fun renderEnd(tickDelta: Float): Vec3 {
        return previousEnd.lerp(end, tickDelta.toDouble().coerceIn(0.0, 1.0))
    }

    private fun syncPreviousBeamIfUnset() {
        if (previousStart == Vec3.ZERO && pos != Vec3.ZERO) {
            previousStart = pos
        }
        if (previousEnd == Vec3.ZERO && end != Vec3.ZERO) {
            previousEnd = end
        }
    }

    private fun beamDirection(start: Vec3 = pos, end: Vec3 = this.end): Vec3 {
        val delta = end - start
        return if (delta.lengthSqr() <= MIN_DIRECTION_LENGTH_SQR) {
            Vec3(0.0, 1.0, 0.0)
        } else {
            delta.normalize()
        }
    }

    private fun beamLength(start: Vec3 = pos, end: Vec3 = this.end): Float {
        return start.distanceTo(end).toFloat().coerceAtLeast(MIN_BEAM_LENGTH)
    }

    private fun updateLifecycle() {
        previousEnd = end
        previousStart = pos
        if (age > totalDurationTicks() + 1) {
            canceled = true
        }
    }

    private fun totalDurationTicks(): Int {
        val grow = phaseTicks.coerceAtLeast(1)
        return grow + lifetime.coerceAtLeast(0) + grow
    }

    private fun currentTimeline(tickDelta: Float): Float {
        return (age - 1f + tickDelta).coerceAtLeast(0f)
    }

    private fun currentRadius(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val growDuration = phaseTicks.coerceAtLeast(1).toFloat()
        val holdEnd = growDuration + lifetime.coerceAtLeast(0).toFloat()
        return when {
            time < growDuration -> {
                val grow = easeOutCubic(smoothstep(0f, growDuration, time))
                mix(MIN_RADIUS, maxRadius.coerceAtLeast(MIN_RADIUS), grow)
            }

            time < holdEnd -> maxRadius.coerceAtLeast(MIN_RADIUS)
            else -> {
                val collapse = currentCollapse(tickDelta)
                mix(maxRadius.coerceAtLeast(MIN_RADIUS), MIN_RADIUS, collapse)
            }
        }
    }

    private fun currentBodyAlpha(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val growDuration = phaseTicks.coerceAtLeast(1).toFloat()
        val holdEnd = growDuration + lifetime.coerceAtLeast(0).toFloat()
        val baseAlpha = alpha.toFloat().coerceIn(0f, 1f)
        return when {
            time < growDuration -> {
                val grow = easeOutCubic(smoothstep(0f, growDuration, time))
                mix(0.24f, 1.0f, grow) * baseAlpha
            }

            time < holdEnd -> baseAlpha
            else -> {
                val fade = 1f - currentCollapse(tickDelta)
                fade * fade * baseAlpha
            }
        }
    }

    private fun currentBloomAlpha(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val growDuration = phaseTicks.coerceAtLeast(1).toFloat()
        val holdEnd = growDuration + lifetime.coerceAtLeast(0).toFloat()
        val baseAlpha = alpha.toFloat().coerceIn(0f, 1f)
        return when {
            time < growDuration -> mix(0.34f, 1.0f, easeOutCubic(smoothstep(0f, growDuration, time))) * baseAlpha
            time < holdEnd -> mix(1.0f, 1.16f, currentPulse(tickDelta)) * baseAlpha
            else -> {
                val fade = 1f - currentCollapse(tickDelta)
                fade * fade * fade * baseAlpha
            }
        }
    }

    private fun currentPhaseProgress(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val growDuration = phaseTicks.coerceAtLeast(1).toFloat()
        val holdEnd = growDuration + lifetime.coerceAtLeast(0).toFloat()
        return when {
            time < growDuration -> smoothstep(0f, growDuration, time)
            time < holdEnd -> 1f
            else -> 1f - currentCollapse(tickDelta)
        }
    }

    private fun currentCollapse(tickDelta: Float): Float {
        val time = currentTimeline(tickDelta)
        val growDuration = phaseTicks.coerceAtLeast(1).toFloat()
        val holdEnd = growDuration + lifetime.coerceAtLeast(0).toFloat()
        return if (time < holdEnd) {
            0f
        } else {
            smoothstep(0f, growDuration, time - holdEnd)
        }
    }

    private fun currentPulse(tickDelta: Float): Float {
        return 0.5f + 0.5f * kotlin.math.sin(currentTimeline(tickDelta) * 0.84f + 0.6f)
    }

    private fun renderPasses(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        beamLength: Float,
        radius: Float,
        passAlpha: Float,
        phaseProgress: Float,
        collapse: Float,
        time: Float,
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
                    beamLength = beamLength,
                    beamRadius = radius,
                    passColor = color,
                    passAlpha = (passAlpha * 0.72f).coerceAtMost(0.96f),
                    brightness = 2.25f,
                    phaseProgress = phaseProgress,
                    collapse = collapse,
                    time = time,
                    layerMode = LAYER_OUTER_BLOOM,
                )
                drawPass(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    beamLength = beamLength,
                    beamRadius = radius * 1.18f,
                    passColor = mixColor(color, Vector3f(1.0f, 0.97f, 0.90f), 0.24f),
                    passAlpha = (passAlpha * 0.38f).coerceAtMost(0.62f),
                    brightness = 1.55f,
                    phaseProgress = phaseProgress,
                    collapse = collapse,
                    time = time,
                    layerMode = LAYER_OUTER_BLOOM,
                )
                drawPass(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    beamLength = beamLength,
                    beamRadius = radius * 0.30f,
                    passColor = mixColor(color, Vector3f(1.0f, 0.99f, 0.94f), 0.58f),
                    passAlpha = (passAlpha * 0.20f).coerceAtMost(0.32f),
                    brightness = 1.92f,
                    phaseProgress = phaseProgress,
                    collapse = collapse,
                    time = time,
                    layerMode = LAYER_INNER_GLOW,
                )
            } else {
                RenderSystem.blendFunc(770, 771)
                drawPass(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    beamLength = beamLength,
                    beamRadius = radius,
                    passColor = color,
                    passAlpha = (passAlpha * 0.34f).coerceAtMost(0.48f),
                    brightness = 0.64f,
                    phaseProgress = phaseProgress,
                    collapse = collapse,
                    time = time,
                    layerMode = LAYER_OUTER_TEXTURE,
                )
                drawPass(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    beamLength = beamLength,
                    beamRadius = radius * 1.18f,
                    passColor = mixColor(color, Vector3f(1.0f, 0.97f, 0.90f), 0.28f),
                    passAlpha = (passAlpha * 0.10f).coerceAtMost(0.18f),
                    brightness = 0.74f,
                    phaseProgress = phaseProgress,
                    collapse = collapse,
                    time = time,
                    layerMode = LAYER_OUTER_TEXTURE,
                )
                RenderSystem.blendFunc(770, 1)
                drawPass(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    beamLength = beamLength,
                    beamRadius = radius * 0.30f,
                    passColor = mixColor(color, Vector3f(1.0f, 0.98f, 0.92f), 0.62f),
                    passAlpha = (passAlpha * 0.12f).coerceAtMost(0.22f),
                    brightness = 1.16f,
                    phaseProgress = phaseProgress,
                    collapse = collapse,
                    time = time,
                    layerMode = LAYER_INNER_GLOW,
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
        beamLength: Float,
        beamRadius: Float,
        passColor: Vector3f,
        passAlpha: Float,
        brightness: Float,
        phaseProgress: Float,
        collapse: Float,
        time: Float,
        layerMode: Int,
    ) {
        if (passAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        ensureBeamGeometry(beamLength)
        beamShader.useOnContext {
            RenderSystem.setShaderTexture(0, LASER_IMPACT_NOISE_TEXTURE)
            setInt("impactNoise", 0)
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat("beamRadius", beamRadius.coerceAtLeast(MIN_RADIUS))
            setFloat("beamLength", beamLength.coerceAtLeast(MIN_BEAM_LENGTH))
            setFloat3("color", passColor)
            setFloat("alpha", passAlpha)
            setFloat("brightness", brightness * this@StraightLaserRenderEntity.brightness * BRIGHTNESS_MULTIPLIER)
            setFloat("phaseProgress", phaseProgress)
            setFloat("collapse", collapse)
            setFloat("time", time)
            setInt("layerMode", layerMode)
            beamVertexBuffer.draw()
        }
    }

    private fun midpointPosition(start: Vec3, end: Vec3): Vector3f {
        return Vector3f(
            ((start.x + end.x) * 0.5).toFloat(),
            ((start.y + end.y) * 0.5).toFloat(),
            ((start.z + end.z) * 0.5).toFloat(),
        )
    }

    private fun glowWorldRadius(radius: Float): Float {
        return max(radius * 8.0f, 3.6f)
    }

    private fun projectedBlend(
        radius: Float,
        start: Vec3,
        end: Vec3,
        cameraWorldPos: Vector3f,
        viewRotationMatrix: Matrix3f,
        inverseViewRotationMatrix: Matrix3f,
        projMatrix: Matrix4f,
        screenSize: Vector2f,
    ): StraightLaserProjectedBlend {
        val distance = Vector3f(midpointPosition(start, end)).sub(cameraWorldPos).length().coerceAtLeast(0.125f)
        val screenHeight = screenSize.y.coerceAtLeast(1.0f)
        val projectedRadiusPx = glowWorldRadius(radius) / distance * screenHeight * 0.75f
        return StraightLaserProjectedBlend(
            projectedRadiusPx = projectedRadiusPx,
            directWeight = smoothstep(5.0f, 18.0f, projectedRadiusPx),
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

    private fun orientedModelMatrix(
        baseMatrix: Matrix4f,
        start: Vec3 = pos,
        end: Vec3 = this.end,
        anchor: Vec3 = pos,
    ): Matrix4f {
        val direction = beamDirection(start, end)
        return Matrix4f(baseMatrix).translate(
            (start.x - anchor.x).toFloat(),
            (start.y - anchor.y).toFloat(),
            (start.z - anchor.z).toFloat(),
        ).rotate(
            Quaternionf().rotationTo(
                0f,
                1f,
                0f,
                direction.x.toFloat(),
                direction.y.toFloat(),
                direction.z.toFloat(),
            ),
        )
    }

    private fun currentCameraWorldPos(): Vector3f {
        val cameraPos = Minecraft.getInstance().gameRenderer.mainCamera.position
        return Vector3f(cameraPos.x.toFloat(), cameraPos.y.toFloat(), cameraPos.z.toFloat())
    }

    private fun currentScreenSize(): Vector2f {
        val renderTarget = Minecraft.getInstance().mainRenderTarget
        val width = ClientRenderPipelineManager.currentRenderWidth().takeIf { it > 0 }
            ?: renderTarget.width.takeIf { it > 0 }
            ?: 0
        val height = ClientRenderPipelineManager.currentRenderHeight().takeIf { it > 0 }
            ?: renderTarget.height.takeIf { it > 0 }
            ?: 0
        return Vector2f(width.toFloat(), height.toFloat())
    }

    companion object {
        private const val DEFAULT_PHASE_TICKS = 6
        private const val DEFAULT_LIFETIME = 12
        private const val DEFAULT_MAX_RADIUS = 0.22f
        private const val MIN_RADIUS = 0.01f
        private const val MIN_BEAM_LENGTH = 0.05f
        private const val CONE_LENGTH_FRACTION = 0.10f
        private const val MAX_CONE_LENGTH = 10.0f
        private const val MIN_VISIBLE_ALPHA = 0.001f
        private const val BRIGHTNESS_MULTIPLIER = 0.82f
        private const val VIEW_PADDING = 48.0
        private const val MIN_DIRECTION_LENGTH_SQR = 1.0E-6
        private const val NO_SOURCE_ENTITY = -1
        private const val LASER_BLOOM_EFFECT_ID = "usefulmagic:straight_laser_bloom"
        private const val LASER_BLOOM_PRIORITY = 270
        private const val LAYER_OUTER_TEXTURE = 0
        private const val LAYER_INNER_GLOW = 1
        private const val LAYER_OUTER_BLOOM = 2

        @JvmField
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "straight_laser_render_entity")

        private val LASER_IMPACT_NOISE_TEXTURE: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "textures/effect/straight_laser_impact_noise.png")

        @JvmField
        var initialized: Boolean = false

        private val LASER_BLOOM_CONFIG = MaskBloomConfig(
            blurSigma = 9.2f,
            blurRange = 6.4f,
            intensity = 2.55f,
            baseMaskIntensity = 0.24f,
            threshold = 0.015f,
            thresholdSoftness = 0.028f,
            tint = Vector3f(0.78f, 0.94f, 1.0f),
        )

        private lateinit var beamVertexBuffer: SimpleVertexBuffer
        private lateinit var beamShader: CooShaderProgram
        private var beamGeometryConeRatio = -1f

        @JvmStatic
        fun spawn(
            world: ServerLevel,
            sourceEntity: Entity,
            end: Vec3,
            tick: Int,
            lifetime: Int,
            color: Vec3 = Vec3(0.28, 0.82, 1.0),
            maxRadius: Float = DEFAULT_MAX_RADIUS,
            brightness: Float = 1.0f,
        ): StraightLaserRenderEntity {
            return StraightLaserRenderEntity(world)
                .configure(sourceEntity, end, tick, lifetime, color, maxRadius, brightness)
                .also(ServerRenderEntityManager::spawn)
        }

        @JvmStatic
        fun spawn(
            world: ServerLevel,
            sourceEntity: Entity,
            end: Vec3,
            tick: Int,
            lifetime: Int,
            color: Vector3f,
            maxRadius: Float = DEFAULT_MAX_RADIUS,
            brightness: Float = 1.0f,
        ): StraightLaserRenderEntity {
            return StraightLaserRenderEntity(world)
                .configure(sourceEntity, end, tick, lifetime, color, maxRadius, brightness)
                .also(ServerRenderEntityManager::spawn)
        }

        @JvmStatic
        fun spawn(
            world: ServerLevel,
            start: Vec3,
            end: Vec3,
            tick: Int,
            lifetime: Int,
            color: Vec3 = Vec3(0.28, 0.82, 1.0),
            maxRadius: Float = DEFAULT_MAX_RADIUS,
            brightness: Float = 1.0f,
        ): StraightLaserRenderEntity {
            return StraightLaserRenderEntity(world)
                .configure(start, end, tick, lifetime, color, maxRadius, brightness)
                .also(ServerRenderEntityManager::spawn)
        }

        @JvmStatic
        fun spawn(
            world: ServerLevel,
            start: Vec3,
            end: Vec3,
            tick: Int,
            lifetime: Int,
            color: Vector3f,
            maxRadius: Float = DEFAULT_MAX_RADIUS,
            brightness: Float = 1.0f,
        ): StraightLaserRenderEntity {
            return StraightLaserRenderEntity(world)
                .configure(start, end, tick, lifetime, color, maxRadius, brightness)
                .also(ServerRenderEntityManager::spawn)
        }

        @JvmStatic
        fun initStatic() {
            if (initialized) {
                return
            }
            beamVertexBuffer = SimpleVertexBuffer().apply {
                init()
                setVertexes(buildCylinderVertices(CONE_LENGTH_FRACTION), CooVertexFormat.POINT_FORMAT)
            }
            beamShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/vsh/straight_laser_beam.vsh"),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/fsh/straight_laser_beam.fsh"),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            beamShader.init()
            initialized = true
        }

        private fun ensureBeamGeometry(beamLength: Float) {
            val coneRatio = (MAX_CONE_LENGTH / beamLength.coerceAtLeast(MIN_BEAM_LENGTH))
                .coerceAtMost(CONE_LENGTH_FRACTION)
                .coerceIn(0.001f, 1.0f)
            if (abs(beamGeometryConeRatio - coneRatio) <= 0.0001f) {
                return
            }
            beamVertexBuffer.setVertexes(buildCylinderVertices(coneRatio), CooVertexFormat.POINT_FORMAT)
            beamGeometryConeRatio = coneRatio
        }

        private fun buildCylinderVertices(coneEndRatio: Float): List<VertexData> {
            val segments = 24
            val axialSegments = 96
            val yStops = buildAxialStops(axialSegments, coneEndRatio)
            val vertices = ArrayList<VertexData>(segments * (yStops.size - 1) * 6)

            for (segment in 0 until segments) {
                val angle0 = (Math.PI.toFloat() * 2f * segment) / segments.toFloat()
                val angle1 = (Math.PI.toFloat() * 2f * (segment + 1)) / segments.toFloat()
                val x0 = cos(angle0)
                val z0 = sin(angle0)
                val x1 = cos(angle1)
                val z1 = sin(angle1)

                for (axialSegment in 0 until yStops.size - 1) {
                    val y0 = yStops[axialSegment]
                    val y1 = yStops[axialSegment + 1]
                    val scale0 = coneRadiusScale(y0, coneEndRatio)
                    val scale1 = coneRadiusScale(y1, coneEndRatio)

                    val a = Vector3f(x0 * scale0, y0, z0 * scale0)
                    val b = Vector3f(x1 * scale0, y0, z1 * scale0)
                    val c = Vector3f(x1 * scale1, y1, z1 * scale1)
                    val d = Vector3f(x0 * scale1, y1, z0 * scale1)

                    appendQuad(vertices, a, b, c, d)
                }
            }
            return vertices
        }

        private fun buildAxialStops(axialSegments: Int, coneEndRatio: Float): List<Float> {
            val stops = ArrayList<Float>(axialSegments + 2)
            for (index in 0..axialSegments) {
                val y = index.toFloat() / axialSegments.toFloat()
                if (stops.none { abs(it - y) <= 0.0001f }) {
                    stops += y
                }
            }
            if (stops.none { abs(it - coneEndRatio) <= 0.0001f }) {
                stops += coneEndRatio
            }
            stops.sort()
            return stops
        }

        private fun coneRadiusScale(y: Float, coneEndRatio: Float): Float {
            if (y >= coneEndRatio) {
                return 1.0f
            }
            return smoothstep(0.0f, coneEndRatio, y)
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

        private fun mixColor(from: Vector3f, to: Vector3f, alpha: Float): Vector3f {
            val t = alpha.coerceIn(0f, 1f)
            return Vector3f(
                mix(from.x, to.x, t),
                mix(from.y, to.y, t),
                mix(from.z, to.z, t),
            )
        }
    }
}
