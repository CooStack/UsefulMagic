package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.renderer.RenderEntity
import cn.coostack.cooparticlesapi.renderer.client.ShaderPipeManagers
import cn.coostack.cooparticlesapi.renderer.shader.ShaderProgramBuilder
import cn.coostack.cooparticlesapi.renderer.shader.api.glsl.GlShaderType
import cn.coostack.cooparticlesapi.renderer.shader.data.CooVertexFormat
import cn.coostack.cooparticlesapi.renderer.shader.glsl.IdentifierShader
import cn.coostack.cooparticlesapi.renderer.shader.utils.ShaderUtil
import cn.coostack.cooparticlesapi.renderer.shader.vertex.SimpleVertexBuffer
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.usefulmagic.UsefulMagic
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Matrix4fStack
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import kotlin.math.pow

class SkyFallingRenderEntity(world: Level?, pos: Vec3) : RenderEntity(world, pos) {
    companion object {
        val cylinderVertexBuffer = SimpleVertexBuffer()
            .apply {
                setVertexes(ShaderUtil.genCylinder(1f, 72f, 1f), CooVertexFormat.POINT_FORMAT)
            }

        val bloom = ShaderPipeManagers.simpleBloom

        val fresnalShader = ShaderProgramBuilder()
            .vertex(
                IdentifierShader(
                    ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/vsh/cylinder_point.vsh"),
                    GlShaderType.VERTEX
                )
            )
            .fragment(
                IdentifierShader(
                    ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/fsh/fresnal.fsh"),
                    GlShaderType.FRAGMENT
                )
            )
            .build()
        val colorShader = ShaderProgramBuilder()
            .vertex("core/vertex/point.vsh")
            .fragment(
                IdentifierShader(
                    ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/fsh/color.fsh"),
                    GlShaderType.FRAGMENT
                )
            )
            .build()


        @JvmField
        internal var initialized = false

        @JvmStatic
        fun initStatic() {
            if (initialized) {
                return
            }
            initialized = true
            fresnalShader.init()
            cylinderVertexBuffer.init()
            colorShader.init()
        }

        @JvmStatic
        val CODEC: StreamCodec<FriendlyByteBuf, RenderEntity> = StreamCodec.of<FriendlyByteBuf, RenderEntity>(
            { buf, data ->
                encodeBase(buf, data)
                data as SkyFallingRenderEntity
                buf.writeDouble(data.alpha)
                buf.writeFloat(data.speed)
                buf.writeVector3f(data.r)
                buf.writeVector3f(data.color)
            }, {
                val instance = SkyFallingRenderEntity(null, Vec3.ZERO)
                decodeBase(it, instance)
                instance.alpha = it.readDouble()
                instance.speed = it.readFloat()
                instance.r = it.readVector3f()
                instance.color = it.readVector3f()
                instance
            }
        )

        @JvmField
        val ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "sky_falling_render")
    }

    var color = Vector3f(100 / 255f, 10 / 255f, 236 / 255f)
    var r = Vector3f(1f)
    var alpha = 1.0
    var prevR = Vector3f()
    val maxAge = 180
    var speed = 0.0f
    override fun initialize() {
        initStatic()
    }

    override fun loadProfileFromEntity(another: RenderEntity) {
        super.loadProfileFromEntity(another)
        if (another !is SkyFallingRenderEntity) {
            return
        }
        color = another.color
        r = another.r
        alpha = another.alpha
        speed = another.speed
    }

    override fun getCodec(): StreamCodec<FriendlyByteBuf, RenderEntity> {
        return CODEC
    }

    override fun getRenderID(): ResourceLocation {
        return ID
    }

    override fun release() {
    }

    override fun tick() {
        super.tick()
        this.prevR = r

        if (age in 0..80) {
            val x = age.toFloat() - 1
            val y = -(21f / 3200) * (x - 80).pow(2) + 48
            r = Vector3f(y)
            r.y = 360f
        }

        if (age > maxAge) {
            canceled = true
        }
        if (age > maxAge - 10) {
            val x = 10 + age - maxAge
            val y = 0.48f * (x - 10f).pow(2)
            r = Vector3f(y)
            r.y = 360f
        }
    }

    override fun render(
        matrices: Matrix4fStack,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        tickDelta: Float
    ) {
        val deltaR = GraphMathHelper.lerp(Vector3f(tickDelta), prevR, r)
        RenderSystem.disableCull()
        RenderSystem.enableDepthTest()
        bloom.writeFrame {
            matrices.pushMatrix()
            colorShader.useOnContext {
                setMatrix4("projMat", projMatrix)
                setMatrix4("viewMat", viewMatrix)
                setMatrix4("transMat", matrices.scale(vecMinValue(deltaR, 0f)))
                setFloat3("color", color)
                setFloat("alpha", alpha.toFloat())
                cylinderVertexBuffer.draw()
            }
            matrices.popMatrix()
        }

        matrices.pushMatrix()
        RenderSystem.enableBlend()
        fresnalShader.useOnContext {
            setMatrix4("projMat", projMatrix)
            setMatrix4("viewMat", viewMatrix)
            setMatrix4(
                "transMat",
                matrices.scale(
                    vecMinValue(
                        Vector3f(GraphMathHelper.lerp((age - 10 + tickDelta) / 20f, 0f, 1.2f)).add(
                            deltaR
                        ), 0f
                    )
                )
            )
            val camera = Minecraft.getInstance().gameRenderer.mainCamera
            setFloat3("camera", camera.position.toVector3f())
            setFloat3("glowColor", color)
            setFloat("glowIntensity", 10f)
            setFloat("glowFalloff", 5f)
            setFloat("fsl", 1.3f)
            cylinderVertexBuffer.draw()
        }
        matrices.popMatrix()
        RenderSystem.disableDepthTest()
        bloom.render()
    }

    private fun vecMinValue(vec: Vector3f, min: Float): Vector3f {
        return Vector3f(vec.x.coerceAtLeast(min), vec.y.coerceAtLeast(min), vec.z.coerceAtLeast(min))
    }
}