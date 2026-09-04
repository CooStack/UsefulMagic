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
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.opengl.GL33
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.sin

/** 负责陨石大气火焰实体的客户端材质、几何体与绘制状态。 */
@CooAutoRegisterRenderer
class MeteoriteAtmosphereFireRenderEntityRenderer : RenderEntityRenderer<MeteoriteAtmosphereFireRenderEntity> {
    /** 火焰本体及其辉光使用的渲染 Pipeline。 */
    override val pipeline = CooPipelines.MASK_BLOOM
        .bloomThreshold(0.02f)
        .bloomSoftKnee(0.03f)
        .intensity(16f)

    /** 根据方向、尺寸和透明度提交当前帧火焰。 */
    override fun render(input: RenderInput<MeteoriteAtmosphereFireRenderEntity>) {
        initStatic()
        val entity = input.entity
        val visibleAlpha = entity.currentAlpha(input.tickDelta)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val bloomStrength = entity.bloomStrength.coerceIn(0F, 4F)
        val radius = entity.size.coerceAtLeast(MeteoriteAtmosphereFireRenderEntity.MIN_SIZE)
        val time = entity.currentTime(input.tickDelta)
        val pulse = 0.5F + 0.5F * sin(time * 0.38F)
        renderPasses(
            entity = entity,
            modelMatrix = orientedModelMatrix(input.modelMatrix, entity),
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            radius = radius,
            visibleAlpha = visibleAlpha,
            bloomStrength = bloomStrength,
            time = time,
            pulse = pulse,
        )
    }

    /** 绘制火焰外层、主体和亮核。 */
    private fun renderPasses(
        entity: MeteoriteAtmosphereFireRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        radius: Float,
        visibleAlpha: Float,
        bloomStrength: Float,
        time: Float,
        pulse: Float,
    ) {
        val normalizedColor = normalizedColor(entity.color)
        val fireTexture = ofID(
            UsefulMagic.MOD_ID,
            "textures/effect/meteorite_atmosphere_fire.png",
        )
        val renderState = UsefulMagicRenderState.capture()
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.depthFunc(GL33.GL_LEQUAL)
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA)
            drawPass(
                entity = entity,
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                texture = fireTexture,
                radius = Vector3f(radius * 1.02F, radius * 0.82F, radius * 1.02F),
                color = normalizedColor,
                alpha = visibleAlpha * 0.46F,
                brightness = 1.28F,
                frontPower = 1.10F,
                noiseScale = 1.0F,
                time = time,
                pulse = pulse,
                passMode = 0,
                renderTarget = 0,
            )
            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
            drawPass(
                entity = entity,
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                texture = fireTexture,
                radius = Vector3f(radius * 0.72F, radius * 0.54F, radius * 0.72F),
                color = Vector3f(normalizedColor).lerp(Vector3f(1.0F, 0.92F, 0.58F), 0.54F),
                alpha = visibleAlpha * 0.28F,
                brightness = 2.65F,
                frontPower = 1.75F,
                noiseScale = 1.28F,
                time = time * 1.18F,
                pulse = pulse,
                passMode = 1,
                renderTarget = 0,
            )
            drawPass(
                entity = entity,
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                texture = fireTexture,
                radius = Vector3f(radius * 1.10F, radius * 0.88F, radius * 1.10F),
                color = normalizedColor,
                alpha = visibleAlpha * 0.42F * bloomStrength,
                brightness = 2.8F * bloomStrength,
                frontPower = 1.06F,
                noiseScale = 0.92F,
                time = time,
                pulse = pulse,
                passMode = 2,
                renderTarget = 1,
            )
            drawPass(
                entity = entity,
                modelMatrix = modelMatrix,
                viewMatrix = viewMatrix,
                projMatrix = projMatrix,
                texture = fireTexture,
                radius = Vector3f(radius * 0.76F, radius * 0.58F, radius * 0.76F),
                color = Vector3f(normalizedColor).lerp(Vector3f(1.0F, 0.92F, 0.58F), 0.54F),
                alpha = visibleAlpha * 0.26F * bloomStrength,
                brightness = 4.2F * bloomStrength,
                frontPower = 1.68F,
                noiseScale = 1.20F,
                time = time * 1.16F,
                pulse = pulse,
                passMode = 1,
                renderTarget = 1,
            )
        } finally {
            renderState.restore()
        }
    }

    /** 使用火焰 shader 绘制一个球面材质层。 */
    private fun drawPass(
        entity: MeteoriteAtmosphereFireRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        texture: ResourceLocation,
        radius: Vector3f,
        color: Vector3f,
        alpha: Float,
        brightness: Float,
        frontPower: Float,
        noiseScale: Float,
        time: Float,
        pulse: Float,
        passMode: Int,
        renderTarget: Int,
    ) {
        if (alpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        fireShader.useOnContext {
            RenderSystem.setShaderTexture(0, texture)
            setInt("fireTexture", 0)
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat3("radius", radius)
            setFloat3("color", color)
            setFloat("alpha", alpha.coerceIn(0F, 2F))
            setFloat("brightness", brightness.coerceIn(0F, 8F))
            setFloat("frontPower", frontPower.coerceIn(0.1F, 4F))
            setFloat("noiseScale", noiseScale.coerceAtLeast(0.05F))
            setFloat("flowSpeed", entity.flowSpeed.coerceIn(0F, 5F))
            setFloat("time", time)
            setFloat("pulse", pulse)
            setInt("passMode", passMode)
            setInt("renderTarget", renderTarget)
            fireVertexBuffer.draw()
        }
    }

    /**
     * 构建沿实体方向旋转的模型矩阵。
     *
     * @return 局部负 Y 轴对齐火焰方向的模型矩阵
     */
    private fun orientedModelMatrix(
        source: Matrix4f,
        entity: MeteoriteAtmosphereFireRenderEntity,
    ): Matrix4f {
        val direction = MeteoriteAtmosphereFireRenderEntity.safeDirection(entity.direction)
        val directionVector = Vector3f(direction.x.toFloat(), direction.y.toFloat(), direction.z.toFloat())
        return Matrix4f(source).rotate(
            Quaternionf().rotationTo(Vector3f(0F, 1F, 0F), directionVector),
        )
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

    /** 管理陨石大气火焰 renderer 共享的 GPU 资源和网格。 */
    companion object {
        /** 多个火焰绘制阶段共用的最小可见透明度。 */
        private const val MIN_VISIBLE_ALPHA = 0.001F

        /** 复用的火焰球面顶点缓冲。 */
        private lateinit var fireVertexBuffer: SimpleVertexBuffer

        /** 复用的火焰材质程序。 */
        private lateinit var fireShader: CooShaderProgram

        /** 标记客户端渲染资源是否已经完成初始化。 */
        private var initialized = false

        /** 在首次绘制时创建 Coo shader 与球面网格。 */
        @Synchronized
        private fun initStatic() {
            if (initialized) {
                return
            }
            UsefulMagicRenderState.preserve {
                fireVertexBuffer = SimpleVertexBuffer().apply {
                    init()
                    setVertexes(ShaderUtil.genBall(1F, 64, 96), CooVertexFormat.POINT_FORMAT)
                }
            }
            fireShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ofID(
                            UsefulMagic.MOD_ID,
                            "core/vsh/meteorite_atmosphere_fire.vsh",
                        ),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ofID(
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
    }
}
