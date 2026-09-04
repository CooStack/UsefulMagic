package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.annotations.CooAutoRegisterRenderer
import cn.coostack.cooparticlesapi.renderer.client.ClientRenderPipelineManager
import cn.coostack.cooparticlesapi.renderer.pipeline.CooPipelines
import cn.coostack.cooparticlesapi.renderer.runtime.RenderEntityRenderer
import cn.coostack.cooparticlesapi.renderer.runtime.RenderInput
import cn.coostack.cooparticlesapi.renderer.shader.ShaderProgramBuilder
import cn.coostack.cooparticlesapi.renderer.shader.api.CooShaderProgram
import cn.coostack.cooparticlesapi.renderer.shader.api.glsl.GlShaderType
import cn.coostack.cooparticlesapi.renderer.shader.data.CooVertexFormat
import cn.coostack.cooparticlesapi.renderer.shader.data.VertexData
import cn.coostack.cooparticlesapi.renderer.shader.glsl.IdentifierShader
import cn.coostack.cooparticlesapi.renderer.shader.texture.IdentifierTexture
import cn.coostack.cooparticlesapi.renderer.shader.texture.SimpleTextures
import cn.coostack.cooparticlesapi.renderer.shader.vertex.SimpleVertexBuffer
import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
import org.joml.*
import org.lwjgl.opengl.GL33
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/** 负责直线激光实体的客户端材质、几何与 Pipeline 提交。 */
@CooAutoRegisterRenderer
class StraightLaserRenderEntityRenderer : RenderEntityRenderer<StraightLaserRenderEntity> {
    /** 激光本体及其辉光使用的渲染 Pipeline。 */
    override val pipeline =
//        UsefulMagicShaderPipelines.maskBloom<StraightLaserRenderEntity>(
//        StraightLaserRenderEntity.ID, 8f, 7f, 2f, 1f, 0.1f
//    )
        CooPipelines.MASK_BLOOM
            .bloomSoftKnee(0.02f)
            .bloomThreshold(0.6f)
            .intensity { e: StraightLaserRenderEntity -> e.brightness * 14F }

    /** 根据插值端点和生命周期状态提交当前帧激光。 */
    override fun render(input: RenderInput<StraightLaserRenderEntity>) {
        initStatic()
        val entity = input.entity
        val renderStart = entity.renderStart(input.tickDelta)
        val renderEnd = entity.renderEnd(input.tickDelta)
        val beamLength = entity.beamLength(renderStart, renderEnd)
        if (beamLength <= StraightLaserRenderEntity.MIN_BEAM_LENGTH) {
            return
        }
        val bodyAlpha = entity.currentBodyAlpha(input.tickDelta)
        if (bodyAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val radius = entity.currentRadius(input.tickDelta)
        val projectedRadiusPx = projectedRadiusPx(
            radius = radius,
            start = renderStart,
            end = renderEnd,
            cameraWorldPos = currentCameraWorldPos(),
            screenSize = currentScreenSize(),
        )
        val directWeight = StraightLaserRenderEntity.smoothstep(5F, 18F, projectedRadiusPx)
        val directAlpha = bodyAlpha * StraightLaserRenderEntity.mix(0.80F, 1F, directWeight)
        if (directAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val bloomIntensityGain = entity.brightness.coerceAtLeast(0F) * StraightLaserRenderEntity.mix(
            0.82F,
            1.34F,
            bloomVisibleBrightness(entity, input.tickDelta),
        )
        val bloomAlpha = entity.currentBloomAlpha(input.tickDelta) *
                StraightLaserRenderEntity.mix(0.28F, 0.62F, 1F - directWeight) *
                max(0.62F, farBloomIntensityScale(projectedRadiusPx, 18F, 2F)) * bloomIntensityGain
        renderPasses(
            entity = entity,
            modelMatrix = orientedModelMatrix(input.modelMatrix, renderStart, renderEnd, renderStart),
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            beamLength = beamLength,
            radius = radius,
            passAlpha = directAlpha,
            bloomAlpha = bloomAlpha,
            phaseProgress = entity.currentPhaseProgress(input.tickDelta),
            collapse = entity.currentCollapse(input.tickDelta),
            time = entity.getTime(input.tickDelta),
        )
    }

    /**
     * 计算写入 mask attachment 的可见亮度权重。
     *
     * @return 位于 `0F..1F` 的辉光可见度
     */
    private fun bloomVisibleBrightness(entity: StraightLaserRenderEntity, tickDelta: Float): Float {
        val sourceLuminance = max(entity.color.x, max(entity.color.y, entity.color.z)).coerceIn(0.08F, 1F)
        return (sourceLuminance * entity.currentBloomAlpha(tickDelta) * entity.brightness.coerceAtLeast(0F))
            .coerceIn(0F, 1F)
    }

    /** 绘制激光外层、柔光层和中心亮柱。 */
    private fun renderPasses(
        entity: StraightLaserRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        beamLength: Float,
        radius: Float,
        passAlpha: Float,
        bloomAlpha: Float,
        phaseProgress: Float,
        collapse: Float,
        time: Float,
    ) {
        if (passAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val renderState = UsefulMagicRenderState.capture()
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.depthMask(false)
        try {
            beamShader.useOnContext {
                beamTextures.drawWith {
                    RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA)
                    drawPass(
                        entity = entity,
                        modelMatrix = modelMatrix,
                        viewMatrix = viewMatrix,
                        projMatrix = projMatrix,
                        beamLength = beamLength,
                        beamRadius = radius,
                        passColor = entity.color,
                        passAlpha = (passAlpha * 0.34F).coerceAtMost(0.48F),
                        brightness = 0.64F,
                        maskAlpha = 0F,
                        maskBrightness = 0F,
                        phaseProgress = phaseProgress,
                        collapse = collapse,
                        time = time,
                        layerMode = 0,
                    )
                    drawPass(
                        entity = entity,
                        modelMatrix = modelMatrix,
                        viewMatrix = viewMatrix,
                        projMatrix = projMatrix,
                        beamLength = beamLength,
                        beamRadius = radius * 1.18F,
                        passColor = mixColor(entity.color, Vector3f(1F, 0.97F, 0.90F), 0.28F),
                        passAlpha = (passAlpha * 0.10F).coerceAtMost(0.18F),
                        brightness = 0.74F,
                        maskAlpha = 0F,
                        maskBrightness = 0F,
                        phaseProgress = phaseProgress,
                        collapse = collapse,
                        time = time,
                        layerMode = 0,
                    )
                    RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE)
                    drawPass(
                        entity = entity,
                        modelMatrix = modelMatrix,
                        viewMatrix = viewMatrix,
                        projMatrix = projMatrix,
                        beamLength = beamLength,
                        beamRadius = radius * 0.30F,
                        passColor = mixColor(entity.color, Vector3f(1F, 0.98F, 0.92F), 0.62F),
                        passAlpha = (passAlpha * 0.12F).coerceAtMost(0.22F),
                        brightness = 1.16F,
                        maskAlpha = 0F,
                        maskBrightness = 0F,
                        phaseProgress = phaseProgress,
                        collapse = collapse,
                        time = time,
                        layerMode = 1,
                    )
                    drawPass(
                        entity = entity,
                        modelMatrix = modelMatrix,
                        viewMatrix = viewMatrix,
                        projMatrix = projMatrix,
                        beamLength = beamLength,
                        beamRadius = radius,
                        passColor = entity.color,
                        passAlpha = 0F,
                        brightness = 0F,
                        maskAlpha = (bloomAlpha * 0.72F).coerceAtMost(0.96F),
                        maskBrightness = 2.25F,
                        phaseProgress = phaseProgress,
                        collapse = collapse,
                        time = time,
                        layerMode = 0,
                    )
                    drawPass(
                        entity = entity,
                        modelMatrix = modelMatrix,
                        viewMatrix = viewMatrix,
                        projMatrix = projMatrix,
                        beamLength = beamLength,
                        beamRadius = radius * 1.18F,
                        passColor = mixColor(entity.color, Vector3f(1F, 0.97F, 0.90F), 0.24F),
                        passAlpha = 0F,
                        brightness = 0F,
                        maskAlpha = (bloomAlpha * 0.38F).coerceAtMost(0.62F),
                        maskBrightness = 1.55F,
                        phaseProgress = phaseProgress,
                        collapse = collapse,
                        time = time,
                        layerMode = 0,
                    )
                    drawPass(
                        entity = entity,
                        modelMatrix = modelMatrix,
                        viewMatrix = viewMatrix,
                        projMatrix = projMatrix,
                        beamLength = beamLength,
                        beamRadius = radius * 0.30F,
                        passColor = mixColor(entity.color, Vector3f(1F, 0.99F, 0.94F), 0.58F),
                        passAlpha = 0F,
                        brightness = 0F,
                        maskAlpha = (bloomAlpha * 0.20F).coerceAtMost(0.32F),
                        maskBrightness = 1.92F,
                        phaseProgress = phaseProgress,
                        collapse = collapse,
                        time = time,
                        layerMode = 1,
                    )
                }
            }
        } finally {
            renderState.restore()
        }
    }

    /** 在已绑定的 shader 和纹理上下文中绘制一个圆柱材质层。 */
    private fun drawPass(
        entity: StraightLaserRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        beamLength: Float,
        beamRadius: Float,
        passColor: Vector3f,
        passAlpha: Float,
        brightness: Float,
        maskAlpha: Float,
        maskBrightness: Float,
        phaseProgress: Float,
        collapse: Float,
        time: Float,
        layerMode: Int,
    ) {
        if (passAlpha <= MIN_VISIBLE_ALPHA && maskAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        beamShader.apply {
            setInt("impactNoise", 0)
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat("beamRadius", beamRadius.coerceAtLeast(StraightLaserRenderEntity.MIN_RADIUS))
            setFloat("beamLength", beamLength.coerceAtLeast(StraightLaserRenderEntity.MIN_BEAM_LENGTH))
            setFloat("coneEndRatio", coneRatio(beamLength))
            setFloat3("color", passColor)
            setFloat("alpha", passAlpha)
            setFloat("brightness", brightness * entity.brightness * 0.82F)
            setFloat("maskAlpha", maskAlpha)
            setFloat("maskBrightness", maskBrightness * entity.brightness * 0.82F)
            setFloat("phaseProgress", phaseProgress)
            setFloat("collapse", collapse)
            setFloat("time", time)
            setInt("layerMode", layerMode)
            beamVertexBuffer.draw()
        }
    }

    /**
     * 计算激光外层在屏幕上的投影半径。
     *
     * @return 激光外层的近似投影像素半径
     */
    private fun projectedRadiusPx(
        radius: Float,
        start: Vec3,
        end: Vec3,
        cameraWorldPos: Vector3f,
        screenSize: Vector2f,
    ): Float {
        val midpoint = (start + end) * 0.5
        val midpointPosition = Vector3f(midpoint.x.toFloat(), midpoint.y.toFloat(), midpoint.z.toFloat())
        val distance = (midpointPosition - cameraWorldPos).length().coerceAtLeast(0.125F)
        val screenHeight = screenSize.y.coerceAtLeast(1F)
        // 用世界半径和视距估算像素覆盖范围，远处细激光据此增加直接层和 bloom 补偿。
        return max(radius * 8F, 3.6F) / distance * screenHeight * 0.75F
    }

    /**
     * 计算远距离激光的泛光补偿。
     *
     * @return 随投影尺寸减小而增加的 mask 强度倍率
     */
    private fun farBloomIntensityScale(
        projectedRadiusPx: Float,
        compensationFadeStartPx: Float,
        maxCompensationPx: Float,
    ): Float {
        val farWeight = 1F - StraightLaserRenderEntity.smoothstep(4F, compensationFadeStartPx, projectedRadiusPx)
        return 1F + farWeight * maxCompensationPx * 0.18F
    }

    /**
     * 构建从起点朝向终点的模型矩阵。
     *
     * @return 以起点为原点且局部 Y 轴指向终点的模型矩阵
     */
    private fun orientedModelMatrix(
        baseMatrix: Matrix4f,
        start: Vec3,
        end: Vec3,
        anchor: Vec3,
    ): Matrix4f {
        val delta = end - start
        val direction = if (delta.lengthSqr() <= 0.000001) {
            Vec3(0.0, 1.0, 0.0)
        } else {
            delta.normalize()
        }
        // 单位网格沿局部 Y 轴生成，先平移到起点，再把 Y 轴旋转到激光方向。
        return Matrix4f(baseMatrix).translate(
            (start.x - anchor.x).toFloat(),
            (start.y - anchor.y).toFloat(),
            (start.z - anchor.z).toFloat(),
        ).rotate(
            Quaternionf().rotationTo(
                0F,
                1F,
                0F,
                direction.x.toFloat(),
                direction.y.toFloat(),
                direction.z.toFloat(),
            ),
        )
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
     * 获取 Pipeline 当前使用的渲染尺寸。
     *
     * @return 当前渲染目标的宽高
     */
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

    /**
     * 在线性空间混合两种颜色。
     *
     * @return 按限制后的权重混合得到的颜色
     */
    private fun mixColor(
        from: Vector3f,
        to: Vector3f,
        alpha: Float,
    ): Vector3f {
        val value = alpha.coerceIn(0F, 1F)
        return Vector3f(
            StraightLaserRenderEntity.mix(from.x, to.x, value),
            StraightLaserRenderEntity.mix(from.y, to.y, value),
            StraightLaserRenderEntity.mix(from.z, to.z, value),
        )
    }

    /** 管理直线激光 renderer 共享的 GPU 资源和网格。 */
    companion object {
        /** 多个激光绘制阶段共用的最小可见透明度。 */
        private const val MIN_VISIBLE_ALPHA = 0.001F

        /** 客户端重复使用的圆柱顶点缓冲。 */
        private lateinit var beamVertexBuffer: SimpleVertexBuffer

        /** 客户端重复使用的激光 shader。 */
        private lateinit var beamShader: CooShaderProgram

        /** 客户端重复使用的噪声纹理。 */
        private lateinit var beamTextures: SimpleTextures

        /** 标记 renderer 的 GPU 资源是否已创建。 */
        private var initialized = false

        /** 在渲染线程首次使用时创建 GPU 资源。 */
        private fun initStatic() {
            if (!initialized) {
                UsefulMagicRenderState.preserve {
                    beamVertexBuffer = SimpleVertexBuffer().apply {
                        init()
                        setVertexes(buildCylinderVertices(), CooVertexFormat.POINT_FORMAT)
                    }
                }
                beamShader = ShaderProgramBuilder()
                    .vertex(
                        IdentifierShader(
                            ofID(UsefulMagic.MOD_ID, "core/vsh/straight_laser_beam.vsh"),
                            GlShaderType.VERTEX,
                        ),
                    )
                    .fragment(
                        IdentifierShader(
                            ofID(UsefulMagic.MOD_ID, "core/fsh/straight_laser_beam.fsh"),
                            GlShaderType.FRAGMENT,
                        ),
                    )
                    .build()
                beamTextures = SimpleTextures().apply {
                    addTexture(
                        IdentifierTexture(
                            ofID(UsefulMagic.MOD_ID, "effect/straight_laser_impact_noise.png"),
                        ),
                    )
                }
                initialized = true
            }
            if (beamShader.program == 0) {
                beamShader.init()
                beamTextures.init()
            }
        }

        /** @return 当前激光长度对应的锥化区域比例 */
        private fun coneRatio(beamLength: Float): Float {
            return (10F / beamLength.coerceAtLeast(StraightLaserRenderEntity.MIN_BEAM_LENGTH))
                .coerceAtMost(0.10F)
                .coerceIn(0.001F, 1F)
        }

        /**
         * 构建由 vertex shader 动态锥化的圆柱侧面三角形。
         *
         * @return 可上传到共享顶点缓冲的分段圆柱顶点
         */
        private fun buildCylinderVertices(): List<VertexData> {
            val segments = 24
            val yStops = floatArrayOf(
                0F,
                0.001F,
                0.0025F,
                0.005F,
                0.01F,
                0.025F,
                0.05F,
                0.075F,
                0.10F,
                1F,
            )
            val vertices = ArrayList<VertexData>(segments * (yStops.size - 1) * 6)
            // 起点附近使用密集轴向采样，让 vertex shader 的锥化区域保持平滑。
            for (segment in 0 until segments) {
                val angle0 = (PI.toFloat() * 2F * segment) / segments.toFloat()
                val angle1 = (PI.toFloat() * 2F * (segment + 1)) / segments.toFloat()
                val x0 = cos(angle0)
                val z0 = sin(angle0)
                val x1 = cos(angle1)
                val z1 = sin(angle1)
                for (axialSegment in 0 until yStops.lastIndex) {
                    val y0 = yStops[axialSegment]
                    val y1 = yStops[axialSegment + 1]
                    val a = Vector3f(x0, y0, z0)
                    val b = Vector3f(x1, y0, z1)
                    val c = Vector3f(x1, y1, z1)
                    val d = Vector3f(x0, y1, z0)
                    appendQuad(vertices, a, b, c, d)
                }
            }
            return vertices
        }

        /** 向顶点列表追加由两个三角形组成的四边形。 */
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

        /** 向顶点列表追加一个三角形。 */
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
    }
}
