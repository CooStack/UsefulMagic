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
import cn.coostack.cooparticlesapi.renderer.shader.glsl.IdentifierShader
import cn.coostack.cooparticlesapi.renderer.shader.utils.ShaderUtil
import cn.coostack.cooparticlesapi.renderer.shader.vertex.SimpleVertexBuffer
import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
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
class DefendCrystalRenderEntity(
    world: Level? = null,
    pos: Vec3 = Vec3.ZERO,
    @CodecField var maxRange: Double = 0.0,
) : AutoRenderEntity(world, pos),
    WorldPassRenderEntityRenderer<DefendCrystalRenderEntity>,
    FramePostRenderEntityRenderer<DefendCrystalRenderEntity>,
    RenderEntityReleaseHook<DefendCrystalRenderEntity> {
    @CodecField
    var color: Vector3f = Vector3f(0.42f, 0.72f, 1.0f)

    @CodecField
    var r: Vector3f = Vector3f(2f)

    @CodecField
    var alpha: Double = 1.0

    @CodecField
    var darkOpacity: Double = 0.12

    @CodecField
    var brightOpacity: Double = 0.86

    @CodecField
    var rimOpacity: Double = 1.22

    @CodecField
    var gridDensity: Double = 15.0

    @CodecField
    var gridWidth: Double = 0.032

    @CodecField
    var highlightWidth: Double = 0.11

    @CodecField
    var pulseStrength: Double = 0.35

    var prevR: Vector3f = Vector3f()

    @CodecField
    var over: Boolean = false

    @CodecField
    var overTick: Int = 0

    @CodecField
    var formationPos: Vec3 = pos

    init {
        if (maxRange > 0.0) {
            r.set(maxRange.toFloat())
        }
        prevR.set(r)
    }

    override fun initialize(instance: RenderEntityInstance<DefendCrystalRenderEntity>) {
        initStatic()
    }

    override fun getRenderID(): ResourceLocation = ID

    override fun release(instance: RenderEntityInstance<DefendCrystalRenderEntity>) {
    }

    override fun clientTick() {
        updateLifecycle()
    }

    override fun serverTick() {
        updateLifecycle()
    }

    override fun renderLocal(input: LocalRenderInput<DefendCrystalRenderEntity>) {
        initStatic()

        val frameAge = frameAge(input.tickDelta)
        val deployScale = deployScale(frameAge)
        val collapse = collapseProgress(frameAge)
        if (over && collapse >= 0.999f) {
            canceled = true
            return
        }

        val visibleAlpha = visibleAlpha(frameAge, collapse)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }

        val radius = interpolatedRadius(input.tickDelta)
        val radiusScale = deployScale * collapseExpansion(collapse)
        val worldRadius = barrierWorldRadius(Vector3f(radius).mul(radiusScale))
        if (worldRadius <= 0.01f) {
            return
        }

        val center = Vector3f(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
        val interiorView = isInteriorView(center, currentCameraWorldPos(), worldRadius)
        renderShield(
            modelMatrix = input.modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            radius = radius,
            deployScale = deployScale,
            collapse = collapse,
            visibleAlpha = visibleAlpha,
            time = getTime(input.tickDelta),
            interiorView = interiorView,
        )
    }

    override fun collectRenderContributions(
        input: RenderContributionInput<DefendCrystalRenderEntity>,
        collector: RenderContributionCollector,
    ) {
        val frameAge = frameAge(input.frameContext.tickDelta)
        val collapse = collapseProgress(frameAge)
        val visibleAlpha = visibleAlpha(frameAge, collapse)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        collector.submit(
            BuiltinRenderEffectDescriptors.maskBloom(
                effectId = DEFEND_CRYSTAL_BLOOM_EFFECT_ID,
                sourceInstanceId = uuid.toString(),
                frameContext = input.frameContext,
                sourceEntity = this,
                config = DEFEND_CRYSTAL_BLOOM_CONFIG.copy(tint = normalizedColor()),
                priority = DEFEND_CRYSTAL_BLOOM_PRIORITY,
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

    fun over() {
        if (over) {
            return
        }
        over = true
        overTick = age
        markDirty()
    }

    private fun updateLifecycle() {
        prevR.set(r)
        if (!over) {
            return
        }
        if (age - overTick > DISSIPATE_TICKS.toInt() && !canceled) {
            canceled = true
            markDirty()
        }
    }

    private fun renderBloomMask(
        tickDelta: Float,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
    ) {
        initStatic()
        val frameAge = frameAge(tickDelta)
        val deployScale = deployScale(frameAge)
        val collapse = collapseProgress(frameAge)
        val visibleAlpha = visibleAlpha(frameAge, collapse)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }

        val radius = interpolatedRadius(tickDelta)
        val radiusScale = deployScale * collapseExpansion(collapse)
        val worldRadius = barrierWorldRadius(Vector3f(radius).mul(radiusScale))
        if (worldRadius <= 0.01f) {
            return
        }

        val center = Vector3f(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
        if (isInteriorView(center, currentCameraWorldPos(), worldRadius)) {
            return
        }

        val baseColor = normalizedColor()
        val energyColor = Vector3f(baseColor).lerp(Vector3f(0.72f, 0.94f, 1.0f), 0.48f)
        val rimColor = Vector3f(baseColor).lerp(Vector3f(0.96f, 0.99f, 1.0f), 0.70f)
        val modelMatrix = Matrix4fStack(16)
        RenderUtil.setRenderStackWithEntity(modelMatrix, this, tickDelta)

        try {
            RenderSystem.enableDepthTest()
            RenderSystem.depthFunc(GL33.GL_LEQUAL)
            RenderSystem.disableCull()
            RenderSystem.enableBlend()
            RenderSystem.depthMask(false)
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)

            drawShieldPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                radius = radius,
                baseColor = baseColor,
                energyColor = energyColor,
                rimColor = rimColor,
                time = getTime(tickDelta),
                deployScale = deployScale,
                collapse = collapse,
                passAlpha = visibleAlpha * 0.34f,
                interiorView = false,
                passMode = 2,
            )
            drawShieldPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                radius = radius,
                baseColor = baseColor,
                energyColor = energyColor,
                rimColor = rimColor,
                time = getTime(tickDelta),
                deployScale = deployScale,
                collapse = collapse,
                passAlpha = visibleAlpha * 0.18f,
                interiorView = false,
                passMode = 1,
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

    private fun renderShield(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        radius: Vector3f,
        deployScale: Float,
        collapse: Float,
        visibleAlpha: Float,
        time: Float,
        interiorView: Boolean,
    ) {
        val baseColor = normalizedColor()
        val energyColor = Vector3f(baseColor).lerp(Vector3f(0.72f, 0.94f, 1.0f), 0.48f)
        val rimColor = Vector3f(baseColor).lerp(Vector3f(0.96f, 0.99f, 1.0f), 0.70f)
        val glowPulse = 0.92f + 0.08f * sin(time * 0.95f)

        try {
            RenderSystem.enableDepthTest()
            RenderSystem.depthFunc(GL33.GL_LEQUAL)
            RenderSystem.disableCull()
            RenderSystem.enableBlend()
            RenderSystem.depthMask(false)

            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA)
            drawShieldPass(
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                radius = radius,
                baseColor = baseColor,
                energyColor = energyColor,
                rimColor = rimColor,
                time = time,
                deployScale = deployScale,
                collapse = collapse,
                passAlpha = visibleAlpha * if (interiorView) 0.16f else 0.58f,
                interiorView = interiorView,
                passMode = 0,
            )

            if (!interiorView) {
                RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
                drawShieldPass(
                    modelMatrix = modelMatrix,
                    viewMatrix = viewMatrix,
                    projMatrix = projMatrix,
                    radius = radius,
                    baseColor = baseColor,
                    energyColor = energyColor,
                    rimColor = rimColor,
                    time = time,
                    deployScale = deployScale,
                    collapse = collapse,
                    passAlpha = visibleAlpha * glowPulse * 0.22f,
                    interiorView = interiorView,
                    passMode = 1,
                )

                val haloScales = floatArrayOf(1.018f, 1.036f, 1.058f)
                val haloAlphas = floatArrayOf(0.12f, 0.070f, 0.038f)
                for (index in haloScales.indices) {
                    drawShieldPass(
                        modelMatrix = modelMatrix,
                        viewMatrix = viewMatrix,
                        projMatrix = projMatrix,
                        radius = Vector3f(radius).mul(haloScales[index]),
                        baseColor = baseColor,
                        energyColor = energyColor,
                        rimColor = rimColor,
                        time = time,
                        deployScale = deployScale,
                        collapse = collapse,
                        passAlpha = visibleAlpha * glowPulse * haloAlphas[index],
                        interiorView = interiorView,
                        passMode = 2,
                    )
                }
            }
        } finally {
            RenderSystem.colorMask(true, true, true, true)
            RenderSystem.depthMask(true)
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableBlend()
            RenderSystem.enableDepthTest()
            RenderSystem.depthFunc(GL33.GL_LEQUAL)
            GL33.glCullFace(GL33.GL_BACK)
            RenderSystem.enableCull()
        }
    }

    private fun drawShieldPass(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        radius: Vector3f,
        baseColor: Vector3f,
        energyColor: Vector3f,
        rimColor: Vector3f,
        time: Float,
        deployScale: Float,
        collapse: Float,
        passAlpha: Float,
        interiorView: Boolean,
        passMode: Int,
    ) {
        if (passAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        barrierShader.useOnContext {
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat3("radius", radius)
            setFloat3("baseColor", baseColor)
            setFloat3("energyColor", energyColor)
            setFloat3("rimColor", rimColor)
            setFloat("time", time)
            setFloat("alpha", passAlpha)
            setFloat("deployScale", deployScale)
            setFloat("collapseProgress", collapse)
            setFloat("membraneOpacity", darkOpacity.toFloat().coerceIn(0f, 1f))
            setFloat("energyOpacity", brightOpacity.toFloat().coerceIn(0f, 2.5f))
            setFloat("rimOpacity", rimOpacity.toFloat().coerceIn(0f, 3f))
            setFloat("filamentDensity", gridDensity.toFloat().coerceIn(5f, 36f))
            setFloat("filamentWidth", gridWidth.toFloat().coerceIn(0.008f, 0.12f))
            setFloat("edgeWidth", highlightWidth.toFloat().coerceIn(0.025f, 0.28f))
            setFloat("disturbanceStrength", pulseStrength.toFloat().coerceIn(0f, 2f))
            setFloat("cameraInside", if (interiorView) 1f else 0f)
            setInt("passMode", passMode)
            barrierBuffer.draw()
        }
    }

    private fun frameAge(tickDelta: Float): Float {
        return (age - 1f + tickDelta).coerceAtLeast(0f)
    }

    private fun deployScale(frameAge: Float): Float {
        return smoothstep(0f, DEPLOY_TICKS, frameAge)
    }

    private fun collapseProgress(frameAge: Float): Float {
        if (!over) {
            return 0f
        }
        return smoothstep(0f, DISSIPATE_TICKS, frameAge - overTick.toFloat())
    }

    private fun visibleAlpha(frameAge: Float, collapse: Float): Float {
        val deployVisibility = smoothstep(0f, DEPLOY_TICKS * 0.62f, frameAge)
        val collapseVisibility = (1f - collapse).coerceIn(0f, 1f).pow(1.18f)
        return alpha.toFloat().coerceIn(0f, 1f) * deployVisibility * collapseVisibility
    }

    private fun collapseExpansion(collapse: Float): Float {
        return 1f + collapse.coerceIn(0f, 1f) * 0.10f
    }

    private fun interpolatedRadius(tickDelta: Float): Vector3f {
        return Vector3f(prevR).lerp(r, tickDelta.coerceIn(0f, 1f))
    }

    private fun currentCameraWorldPos(): Vector3f {
        val cameraPos = Minecraft.getInstance().gameRenderer.mainCamera.position
        return Vector3f(cameraPos.x.toFloat(), cameraPos.y.toFloat(), cameraPos.z.toFloat())
    }

    private fun isInteriorView(center: Vector3f, cameraWorldPos: Vector3f, worldRadius: Float): Boolean {
        return center.distance(cameraWorldPos) <= worldRadius * 0.98f
    }

    private fun normalizedColor(): Vector3f {
        return Vector3f(
            color.x.coerceIn(0f, 1f),
            color.y.coerceIn(0f, 1f),
            color.z.coerceIn(0f, 1f),
        )
    }

    companion object {
        private const val DEPLOY_TICKS = 18f
        private const val DISSIPATE_TICKS = 18f
        private const val MIN_VISIBLE_ALPHA = 0.001f
        private const val DEFEND_CRYSTAL_BLOOM_EFFECT_ID = "usefulmagic:defend_crystal_bloom"
        private const val DEFEND_CRYSTAL_BLOOM_PRIORITY = 230

        @JvmField
        val ID: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "defend_crystal_render_entity")

        @JvmField
        var initialized: Boolean = false

        private val DEFEND_CRYSTAL_BLOOM_CONFIG = MaskBloomConfig(
            blurSigma = 6.6f,
            blurRange = 4.8f,
            intensity = 1.45f,
            baseMaskIntensity = 0.0f,
            threshold = 0.0f,
            thresholdSoftness = 0.02f,
            tint = Vector3f(0.42f, 0.72f, 1.0f),
        )

        private lateinit var barrierBuffer: SimpleVertexBuffer
        private lateinit var barrierShader: CooShaderProgram

        @JvmStatic
        @Synchronized
        fun initStatic() {
            if (initialized) {
                return
            }
            barrierBuffer = SimpleVertexBuffer().apply {
                init()
                setVertexes(ShaderUtil.genBall(1f, 64, 96), CooVertexFormat.POINT_FORMAT)
            }
            barrierShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(
                            UsefulMagic.MOD_ID,
                            "core/vsh/defend_crystal_barrier.vsh",
                        ),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ResourceLocation.fromNamespaceAndPath(
                            UsefulMagic.MOD_ID,
                            "core/fsh/defend_crystal_barrier.fsh",
                        ),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            barrierShader.init()
            initialized = true
        }

        @JvmStatic
        fun barrierWorldRadius(radius: Vector3f): Float {
            return max(radius.x, max(radius.y, radius.z))
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
        fun mix(from: Float, to: Float, alpha: Float): Float {
            val t = alpha.coerceIn(0f, 1f)
            return from + (to - from) * t
        }
    }
}
