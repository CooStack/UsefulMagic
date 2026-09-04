package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.annotations.CooAutoRegisterRenderer
import cn.coostack.cooparticlesapi.renderer.pipeline.CooPipelines
import cn.coostack.cooparticlesapi.renderer.runtime.RenderEntityRenderer
import cn.coostack.cooparticlesapi.renderer.runtime.RenderInput
import cn.coostack.cooparticlesapi.renderer.shader.ShaderProgramBuilder
import cn.coostack.cooparticlesapi.renderer.shader.api.CooShaderProgram
import cn.coostack.cooparticlesapi.renderer.shader.api.glsl.GlShaderType
import cn.coostack.cooparticlesapi.renderer.shader.data.CooVertexFormat
import cn.coostack.cooparticlesapi.renderer.shader.glsl.IdentifierShader
import cn.coostack.cooparticlesapi.renderer.shader.vertex.SimpleVertexBuffer
import cn.coostack.cooparticlesapi.renderer.utils.ShaderUtil
import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL33
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.max
import kotlin.math.sin

/** 负责防御水晶屏障的材质、球体几何体和 Pipeline 提交。 */
@CooAutoRegisterRenderer
class DefendCrystalRenderEntityRenderer : RenderEntityRenderer<DefendCrystalRenderEntity> {
    /** 屏障本体及其辉光使用的渲染 Pipeline。 */
    override val pipeline = CooPipelines.MASK_BLOOM
        .intensity(60f)
//        UsefulMagicShaderPipelines.maskBloom<DefendCrystalRenderEntity>(
//            renderEntityId = DefendCrystalRenderEntity.ID,
//            blurSigma = 66F,
//            blurRange = 48F,
//            intensity = 3.45F,
//            baseMaskIntensity = 12f,
//            threshold = 0.0f,
//            thresholdSoftness = 0.02F,
//        )

    /** 根据部署和消散状态提交当前帧屏障。 */
    override fun render(input: RenderInput<DefendCrystalRenderEntity>) {
        initStatic()
        val entity = input.entity
        val frameAge = entity.frameAge(input.tickDelta)
        val deployScale = entity.deployScale(frameAge)
        val collapse = entity.collapseProgress(frameAge)
        if (entity.finishCollapse(collapse)) {
            return
        }
        val visibleAlpha = entity.visibleAlpha(frameAge, collapse)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val radius = entity.interpolatedRadius(input.tickDelta)
        val radiusScale = deployScale * entity.collapseExpansion(collapse)
        val worldRadius = barrierWorldRadius(radius * radiusScale)
        if (worldRadius <= 0.01F) {
            return
        }
        val center = Vector3f(entity.pos.x.toFloat(), entity.pos.y.toFloat(), entity.pos.z.toFloat())
        val interiorView = center.distance(currentCameraWorldPos()) <= worldRadius * 0.98F
        renderShield(
            entity = entity,
            modelMatrix = input.modelMatrix,
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            radius = radius,
            deployScale = deployScale,
            collapse = collapse,
            visibleAlpha = visibleAlpha,
            time = entity.getTime(input.tickDelta),
            interiorView = interiorView,
        )
    }

    /** 绘制屏障的主体、能量层和边缘层。 */
    private fun renderShield(
        entity: DefendCrystalRenderEntity,
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
        val baseColor = normalizedColor(entity.color)
        val energyColor = Vector3f(baseColor).lerp(Vector3f(0.72F, 0.94F, 1.0F), 0.48F)
        val rimColor = Vector3f(baseColor).lerp(Vector3f(0.96F, 0.99F, 1.0F), 0.70F)
        val glowPulse = 0.92F + 0.08F * sin(time * 0.95F)
        val renderState = UsefulMagicRenderState.capture()
        try {
            RenderSystem.enableDepthTest()
            RenderSystem.depthFunc(GL33.GL_LEQUAL)
            RenderSystem.disableCull()
            RenderSystem.enableBlend()
            RenderSystem.depthMask(false)

            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA)
            drawShieldPass(
                entity = entity,
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
                passAlpha = visibleAlpha * if (interiorView) 0.16F else 0.58F,
                interiorView = interiorView,
                passMode = 0,
                renderTarget = 0,
            )

            if (!interiorView) {
                RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
                drawShieldPass(
                    entity = entity,
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
                    passAlpha = visibleAlpha * glowPulse * 0.22F,
                    interiorView = false,
                    passMode = 1,
                    renderTarget = 0,
                )

                val haloScales = floatArrayOf(1.018F, 1.036F, 1.058F)
                val haloAlphas = floatArrayOf(0.12F, 0.070F, 0.038F)
                for (index in haloScales.indices) {
                    drawShieldPass(
                        entity = entity,
                        modelMatrix = modelMatrix,
                        viewMatrix = viewMatrix,
                        projMatrix = projMatrix,
                        radius = radius * haloScales[index],
                        baseColor = baseColor,
                        energyColor = energyColor,
                        rimColor = rimColor,
                        time = time,
                        deployScale = deployScale,
                        collapse = collapse,
                        passAlpha = visibleAlpha * glowPulse * haloAlphas[index],
                        interiorView = false,
                        passMode = 2,
                        renderTarget = 0,
                    )
                }
                drawShieldPass(
                    entity = entity,
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
                    passAlpha = visibleAlpha * 0.34F,
                    interiorView = false,
                    passMode = 2,
                    renderTarget = 1,
                )
                drawShieldPass(
                    entity = entity,
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
                    passAlpha = visibleAlpha * 0.18F,
                    interiorView = false,
                    passMode = 1,
                    renderTarget = 1,
                )
            }
        } finally {
            renderState.restore()
        }
    }

    /** 使用屏障 shader 绘制一个球体材质层。 */
    private fun drawShieldPass(
        entity: DefendCrystalRenderEntity,
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
        renderTarget: Int,
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
            setFloat("membraneOpacity", entity.darkOpacity.toFloat().coerceIn(0F, 1F))
            setFloat("energyOpacity", entity.brightOpacity.toFloat().coerceIn(0F, 2.5F))
            setFloat("rimOpacity", entity.rimOpacity.toFloat().coerceIn(0F, 3F))
            setFloat("filamentDensity", entity.gridDensity.toFloat().coerceIn(5F, 36F))
            setFloat("filamentWidth", entity.gridWidth.toFloat().coerceIn(0.008F, 0.12F))
            setFloat("edgeWidth", entity.highlightWidth.toFloat().coerceIn(0.025F, 0.28F))
            setFloat("disturbanceStrength", entity.pulseStrength.toFloat().coerceIn(0F, 2F))
            setFloat("cameraInside", if (interiorView) 1F else 0F)
            setInt("passMode", passMode)
            setInt("renderTarget", renderTarget)
            barrierBuffer.draw()
        }
    }

    /**
     * 获取当前主相机的世界坐标。
     *
     * @return 主相机世界坐标的单精度副本
     */
    private fun currentCameraWorldPos(): Vector3f {
        val cameraPos = Minecraft.getInstance().gameRenderer.mainCamera.position
        return Vector3f(cameraPos.x.toFloat(), cameraPos.y.toFloat(), cameraPos.z.toFloat())
    }

    /**
     * 把同步颜色限制到 shader 接受的范围。
     *
     * @return 可直接传给 shader 的 RGB 颜色副本
     */
    private fun normalizedColor(color: Vector3f): Vector3f {
        return Vector3f(
            color.x.coerceIn(0F, 1F),
            color.y.coerceIn(0F, 1F),
            color.z.coerceIn(0F, 1F),
        )
    }

    /**
     * 计算非均匀屏障半径对应的最大世界半径。
     *
     * @return 三个轴向半径中的最大非负值
     */
    private fun barrierWorldRadius(radius: Vector3f): Float {
        return max(radius.x, max(radius.y, radius.z))
    }

    /** 管理防御水晶 renderer 共享的 GPU 资源和网格。 */
    companion object {
        /** 多个屏障绘制阶段共用的最小可见透明度。 */
        private const val MIN_VISIBLE_ALPHA = 0.001F

        /** 客户端重复使用的屏障顶点缓冲。 */
        private lateinit var barrierBuffer: SimpleVertexBuffer

        /** 客户端重复使用的屏障 shader。 */
        private lateinit var barrierShader: CooShaderProgram

        /** 标记 renderer 的 GPU 资源是否已创建。 */
        private var initialized = false

        /** 在渲染线程首次使用时创建 GPU 资源。 */
        @Synchronized
        private fun initStatic() {
            if (initialized) {
                return
            }
            UsefulMagicRenderState.preserve {
                barrierBuffer = SimpleVertexBuffer().apply {
                    init()
                    setVertexes(ShaderUtil.genBall(1F, 64, 96), CooVertexFormat.POINT_FORMAT)
                }
            }
            barrierShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ofID(
                            UsefulMagic.MOD_ID,
                            "core/vsh/defend_crystal_barrier.vsh",
                        ),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ofID(
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
    }
}
