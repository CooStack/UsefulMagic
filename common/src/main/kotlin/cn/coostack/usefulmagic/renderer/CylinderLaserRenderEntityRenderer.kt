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

/** 负责圆柱激光实体的客户端材质、几何与 Pipeline 提交。 */
@CooAutoRegisterRenderer
class CylinderLaserRenderEntityRenderer : RenderEntityRenderer<CylinderLaserRenderEntity> {
    /** 激光本体及其辉光使用的渲染 Pipeline。 */
    override val pipeline = CooPipelines
        .MASK_BLOOM
        .intensity { e: CylinderLaserRenderEntity -> e.brightness * 16 }

    /** 根据插值端点和生命周期状态提交当前帧激光。 */
    override fun render(input: RenderInput<CylinderLaserRenderEntity>) {
        initStatic()
        val entity = input.entity
        entity.syncPreviousBeamIfUnset()
        val renderStart = entity.renderStart(input.tickDelta)
        val renderEnd = entity.renderEnd(input.tickDelta)
        val beamLength = entity.beamLength(renderStart, renderEnd)
        if (beamLength <= CylinderLaserRenderEntity.MIN_BEAM_LENGTH) {
            return
        }
        val bodyAlpha = entity.currentBodyAlpha(input.tickDelta)
        if (bodyAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val opacity = entity.currentOpacity(input.tickDelta)
        if (opacity <= MIN_VISIBLE_ALPHA) {
            return
        }
        val radius = entity.currentRadius(input.tickDelta)
        val directWeight = projectedDirectWeight(
            radius = radius,
            start = renderStart,
            end = renderEnd,
            cameraWorldPos = currentCameraWorldPos(),
            screenSize = currentScreenSize(),
        )
        val directAlpha = bodyAlpha * CylinderLaserRenderEntity.mix(0.80F, 1F, directWeight)
        if (directAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        val bloomIntensityGain = entity.brightness.coerceAtLeast(0F) * CylinderLaserRenderEntity.mix(
            0.82F,
            1.34F,
            bloomVisibleBrightness(entity, input.tickDelta),
        )
        renderPasses(
            entity = entity,
            modelMatrix = orientedModelMatrix(input.modelMatrix, renderStart, renderEnd, renderStart),
            viewMatrix = input.viewMatrix,
            projMatrix = input.projMatrix,
            beamLength = beamLength,
            radius = radius,
            passAlpha = directAlpha,
            bloomAlpha = entity.currentBloomAlpha(input.tickDelta) *
                    CylinderLaserRenderEntity.mix(0.28F, 0.62F, 1F - directWeight) *
                    (1F + (1F - directWeight) * 0.36F) * bloomIntensityGain,
            phaseProgress = entity.currentPhaseProgress(input.tickDelta),
            collapse = entity.currentVisualCollapse(input.tickDelta),
            opacity = opacity,
            time = entity.getTime(input.tickDelta),
        )
    }

    /**
     * 计算写入 mask attachment 的可见亮度权重。
     *
     * @return 位于 `0F..1F` 的辉光可见度
     */
    private fun bloomVisibleBrightness(entity: CylinderLaserRenderEntity, tickDelta: Float): Float {
        val sourceLuminance = max(entity.color.x, max(entity.color.y, entity.color.z)).coerceIn(0.08F, 1F)
        return (sourceLuminance * entity.currentBloomAlpha(tickDelta) * entity.brightness.coerceAtLeast(0F))
            .coerceIn(0F, 1F)
    }

    /** 绘制激光外层、柔光层和中心亮柱。 */
    private fun renderPasses(
        entity: CylinderLaserRenderEntity,
        modelMatrix: Matrix4f,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        beamLength: Float,
        radius: Float,
        passAlpha: Float,
        bloomAlpha: Float,
        phaseProgress: Float,
        collapse: Float,
        opacity: Float,
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
                opacity = opacity,
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
                opacity = opacity,
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
                opacity = opacity,
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
                opacity = opacity,
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
                opacity = opacity,
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
                opacity = opacity,
                time = time,
                layerMode = 1,
            )
        } finally {
            renderState.restore()
        }
    }

    /** 使用激光 shader 绘制一个圆柱材质层。 */
    private fun drawPass(
        entity: CylinderLaserRenderEntity,
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
        opacity: Float,
        time: Float,
        layerMode: Int,
    ) {
        if (passAlpha <= MIN_VISIBLE_ALPHA && maskAlpha <= MIN_VISIBLE_ALPHA) {
            return
        }
        beamShader.useOnContext {
            RenderSystem.setShaderTexture(
                0,
                ofID(
                    UsefulMagic.MOD_ID,
                    "textures/effect/straight_laser_impact_noise.png",
                ),
            )
            setInt("impactNoise", 0)
            setMatrix4("modelMatrix", modelMatrix)
            setMatrix4("viewMatrix", viewMatrix)
            setMatrix4("projMatrix", projMatrix)
            setFloat("beamRadius", beamRadius.coerceAtLeast(CylinderLaserRenderEntity.MIN_RADIUS))
            setFloat("beamLength", beamLength.coerceAtLeast(CylinderLaserRenderEntity.MIN_BEAM_LENGTH))
            setFloat3("color", passColor)
            setFloat("alpha", passAlpha)
            setFloat("brightness", brightness * entity.brightness * 0.82F)
            setFloat("maskAlpha", maskAlpha)
            setFloat("maskBrightness", maskBrightness * entity.brightness * 0.82F)
            setFloat("phaseProgress", phaseProgress)
            setFloat("collapse", collapse)
            setFloat("opacity", opacity)
            setFloat("time", time)
            setInt("layerMode", layerMode)
            beamVertexBuffer.draw()
        }
    }

    /**
     * 按屏幕投影尺寸计算直接可见层的权重。
     *
     * @return 位于 `0F..1F` 的直接绘制权重
     */
    private fun projectedDirectWeight(
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
        // 用世界半径和视距估算像素覆盖范围，避免远处细激光完全依赖低分辨率 bloom。
        val projectedRadiusPx = max(radius * 8F, 3.6F) / distance * screenHeight * 0.75F
        return CylinderLaserRenderEntity.smoothstep(5F, 18F, projectedRadiusPx)
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
    private fun mixColor(from: Vector3f, to: Vector3f, alpha: Float): Vector3f {
        val value = alpha.coerceIn(0F, 1F)
        return Vector3f(
            CylinderLaserRenderEntity.mix(from.x, to.x, value),
            CylinderLaserRenderEntity.mix(from.y, to.y, value),
            CylinderLaserRenderEntity.mix(from.z, to.z, value),
        )
    }

    /** 管理圆柱激光 renderer 共享的 GPU 资源和网格。 */
    companion object {
        /** 多个激光绘制阶段共用的最小可见透明度。 */
        private const val MIN_VISIBLE_ALPHA = 0.001F

        /** 客户端重复使用的圆柱顶点缓冲。 */
        private lateinit var beamVertexBuffer: SimpleVertexBuffer

        /** 客户端重复使用的激光 shader。 */
        private lateinit var beamShader: CooShaderProgram

        /** 标记 renderer 的 GPU 资源是否已创建。 */
        private var initialized = false

        /** 在渲染线程首次使用时创建 GPU 资源。 */
        private fun initStatic() {
            if (initialized) {
                return
            }
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
            beamShader.init()
            initialized = true
        }

        /**
         * 构建单位圆柱的侧面和端面三角形。
         *
         * @return 可上传到共享顶点缓冲的圆柱顶点
         */
        private fun buildCylinderVertices(): List<VertexData> {
            val segments = 24
            val axialSegments = 96
            val vertices = ArrayList<VertexData>((segments * axialSegments * 6) + (segments * 6))
            val bottomCenter = Vector3f(0F, 0F, 0F)
            val topCenter = Vector3f(0F, 1F, 0F)
            // 环向分段与轴向分段组成规则四边形，再拆成三角形供 GPU 直接绘制。
            for (segment in 0 until segments) {
                val angle0 = (PI.toFloat() * 2F * segment) / segments.toFloat()
                val angle1 = (PI.toFloat() * 2F * (segment + 1)) / segments.toFloat()
                val x0 = cos(angle0)
                val z0 = sin(angle0)
                val x1 = cos(angle1)
                val z1 = sin(angle1)
                for (axialSegment in 0 until axialSegments) {
                    val y0 = axialSegment.toFloat() / axialSegments.toFloat()
                    val y1 = (axialSegment + 1).toFloat() / axialSegments.toFloat()
                    val a = Vector3f(x0, y0, z0)
                    val b = Vector3f(x1, y0, z1)
                    val c = Vector3f(x1, y1, z1)
                    val d = Vector3f(x0, y1, z0)
                    appendQuad(vertices, a, b, c, d)
                }
                appendTriangle(vertices, topCenter, Vector3f(x0, 1F, z0), Vector3f(x1, 1F, z1))
                appendTriangle(vertices, bottomCenter, Vector3f(x1, 0F, z1), Vector3f(x0, 0F, z0))
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
