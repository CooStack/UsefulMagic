package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
import cn.coostack.cooparticlesapi.annotations.CooAutoRegisterRenderer
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
import org.joml.Vector3f
import org.lwjgl.opengl.GL33
import kotlin.math.max

/** 负责龙魔法球的球体表面、云层材质和 Pipeline 提交。 */
@CooAutoRegisterRenderer
class DragonMagicBallRenderEntityRenderer : RenderEntityRenderer<DragonMagicBallRenderEntity> {
    /** 魔法球本体及其辉光使用的渲染 Pipeline。 */
    override val pipeline = UsefulMagicShaderPipelines.maskBloom<DragonMagicBallRenderEntity>(
        renderEntityId = DragonMagicBallRenderEntity.ID,
        blurSigma = 4.6F,
        blurRange = 3.2F,
        intensity = 1.05F,
        baseMaskIntensity = 0.06F,
        threshold = 0.08F,
        thresholdSoftness = 0.03F,
    )

    /** 根据成长、脉冲和消散状态提交当前帧魔法球。 */
    override fun render(input: RenderInput<DragonMagicBallRenderEntity>) {
        initStatic()
        val entity = input.entity
        val growProgress = entity.currentGrowProgress(input.tickDelta)
        val visibleAlpha = entity.currentAlpha(input.tickDelta, growProgress)
        if (visibleAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val worldSize = entity.currentSize(input.tickDelta, growProgress)
        if (worldSize <= DragonMagicBallRenderEntity.MIN_SIZE) {
            return
        }
        val time = entity.getTime(input.tickDelta)
        val pulse = entity.currentPulse(input.tickDelta)
        val discardProgress = entity.currentDiscardProgress(input.tickDelta)
        val baseColor = entity.normalizedColor()
        val hotColor = Vector3f(baseColor).lerp(Vector3f(0.72F, 0.90F, 1.0F), 0.22F)
        val surfacePass = 0
        val cloudPass = 1
        val surfaceTexture = ofID(UsefulMagic.MOD_ID,
            "textures/effect/dragon_magic_ball_surface.png",
        )
        val cloudTexture = ofID(UsefulMagic.MOD_ID,
            "textures/effect/dragon_magic_ball_cloud.png",
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
                modelMatrix = input.modelMatrix,
                viewMatrix = input.viewMatrix,
                projMatrix = input.projMatrix,
                surfaceTexture = surfaceTexture,
                cloudTexture = cloudTexture,
                passColor = baseColor,
                scale = worldSize * 0.74F,
                passAlpha = visibleAlpha * 0.78F,
                brightness = entity.currentBrightness(
                    entity.surfaceBrightness.toFloat().coerceIn(0F, 4F) * (0.96F + pulse * 0.12F),
                ),
                time = time,
                discardProgress = discardProgress,
                passMode = surfacePass,
                renderTarget = 0,
            )

            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
            drawPass(
                modelMatrix = input.modelMatrix,
                viewMatrix = input.viewMatrix,
                projMatrix = input.projMatrix,
                surfaceTexture = surfaceTexture,
                cloudTexture = cloudTexture,
                passColor = hotColor,
                scale = worldSize * 0.78F,
                passAlpha = visibleAlpha * 0.24F,
                brightness = entity.currentBrightness(
                    entity.surfaceBrightness.toFloat().coerceIn(0F, 4F) * (1.20F + pulse * 0.28F),
                ),
                time = time * 1.18F,
                discardProgress = discardProgress,
                passMode = surfacePass,
                renderTarget = 0,
            )

            RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA)
            drawPass(
                modelMatrix = input.modelMatrix,
                viewMatrix = input.viewMatrix,
                projMatrix = input.projMatrix,
                surfaceTexture = surfaceTexture,
                cloudTexture = cloudTexture,
                passColor = Vector3f(baseColor).lerp(Vector3f(0.92F, 0.96F, 1.0F), 0.58F),
                scale = worldSize * 1.16F,
                passAlpha = visibleAlpha * entity.cloudOpacity.toFloat().coerceIn(0F, 1F),
                brightness = entity.currentBrightness(0.94F + pulse * 0.06F),
                time = time * 0.64F,
                discardProgress = discardProgress,
                passMode = cloudPass,
                renderTarget = 0,
            )
            if (entity.bloomEnabled) {
                val bloomStrength = entity.bloomIntensity.toFloat().coerceIn(0F, 4F) *
                    (0.92F + pulse * 0.18F)
                RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
                drawPass(
                    modelMatrix = input.modelMatrix,
                    viewMatrix = input.viewMatrix,
                    projMatrix = input.projMatrix,
                    surfaceTexture = surfaceTexture,
                    cloudTexture = cloudTexture,
                    passColor = Vector3f(baseColor).lerp(Vector3f(0.72F, 0.90F, 1.0F), 0.30F),
                    scale = worldSize * 0.74F,
                    passAlpha = visibleAlpha * (0.26F + pulse * 0.08F),
                    brightness = entity.currentBrightness(
                        entity.surfaceBrightness.toFloat().coerceIn(0F, 4F) * 1.36F * bloomStrength,
                    ),
                    time = time,
                    discardProgress = discardProgress,
                    passMode = 2,
                    renderTarget = 1,
                )
                drawPass(
                    modelMatrix = input.modelMatrix,
                    viewMatrix = input.viewMatrix,
                    projMatrix = input.projMatrix,
                    surfaceTexture = surfaceTexture,
                    cloudTexture = cloudTexture,
                    passColor = baseColor,
                    scale = worldSize * 0.48F,
                    passAlpha = visibleAlpha * 0.15F,
                    brightness = entity.currentBrightness(
                        entity.surfaceBrightness.toFloat().coerceIn(0F, 4F) * 1.62F * bloomStrength,
                    ),
                    time = time * 1.26F,
                    discardProgress = discardProgress,
                    passMode = 2,
                    renderTarget = 1,
                )
            }
        } finally {
            renderState.restore()
        }
    }

    /** 使用指定纹理和材质参数绘制一个球体层。 */
    private fun drawPass(
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        surfaceTexture: ResourceLocation,
        cloudTexture: ResourceLocation,
        passColor: Vector3f,
        scale: Float,
        passAlpha: Float,
        brightness: Float,
        time: Float,
        discardProgress: Float,
        passMode: Int,
        renderTarget: Int,
    ) {
        if (passAlpha <= MIN_VISIBLE_ALPHA || scale <= DragonMagicBallRenderEntity.MIN_SIZE) {
            return
        }
        ballShader.useOnContext {
            RenderSystem.setShaderTexture(0, surfaceTexture)
            RenderSystem.setShaderTexture(1, cloudTexture)
            setInt("surfaceTexture", 0)
            setInt("cloudTexture", 1)
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat3("color", passColor)
            setFloat("scale", scale.coerceAtLeast(DragonMagicBallRenderEntity.MIN_SIZE))
            setFloat("alpha", passAlpha.coerceIn(0F, 1.4F))
            setFloat("brightness", brightness.coerceIn(0F, 8F))
            setFloat("time", time)
            setFloat("discardProgress", discardProgress.coerceIn(0F, 1F))
            setInt("passMode", passMode)
            setInt("renderTarget", renderTarget)
            ballVertexBuffer.draw()
        }
    }

    /** 管理龙魔法球 renderer 共享的 GPU 资源和网格。 */
    companion object {
        /** 多个球体绘制阶段共用的最小可见透明度。 */
        private const val MIN_VISIBLE_ALPHA = 0.001F

        /** 客户端重复使用的球体顶点缓冲。 */
        private lateinit var ballVertexBuffer: SimpleVertexBuffer
        /** 客户端重复使用的魔法球 shader。 */
        private lateinit var ballShader: CooShaderProgram
        /** 标记 renderer 的 GPU 资源是否已创建。 */
        private var initialized = false

        /** 在渲染线程首次使用时创建 GPU 资源。 */
        @Synchronized
        private fun initStatic() {
            if (initialized) {
                return
            }
            UsefulMagicRenderState.preserve {
                ballVertexBuffer = SimpleVertexBuffer().apply {
                    init()
                    setVertexes(ShaderUtil.genBall(1F, 64, 96), CooVertexFormat.POINT_FORMAT)
                }
            }
            ballShader = ShaderProgramBuilder()
                .vertex(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID,
                            "core/vsh/dragon_magic_ball.vsh",
                        ),
                        GlShaderType.VERTEX,
                    ),
                )
                .fragment(
                    IdentifierShader(
                        ofID(UsefulMagic.MOD_ID,
                            "core/fsh/dragon_magic_ball.fsh",
                        ),
                        GlShaderType.FRAGMENT,
                    ),
                )
                .build()
            ballShader.init()
            initialized = true
        }
    }
}
