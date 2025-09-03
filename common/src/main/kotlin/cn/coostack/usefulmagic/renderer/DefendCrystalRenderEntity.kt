package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.CooParticlesConstants
import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.renderer.RenderEntity
import cn.coostack.cooparticlesapi.renderer.client.ClientRenderPipelineManager.minecraft
import cn.coostack.cooparticlesapi.renderer.client.ShaderPipeManagers
import cn.coostack.cooparticlesapi.renderer.shader.ShaderProgramBuilder
import cn.coostack.cooparticlesapi.renderer.shader.api.glsl.GlShaderType
import cn.coostack.cooparticlesapi.renderer.shader.data.CooVertexFormat
import cn.coostack.cooparticlesapi.renderer.shader.glsl.IdentifierShader
import cn.coostack.cooparticlesapi.renderer.shader.utils.ShaderUtil
import cn.coostack.cooparticlesapi.renderer.shader.vertex.SimpleVertexBuffer
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Matrix4fStack
import org.joml.Vector3f
import org.lwjgl.opengl.GL33
import kotlin.math.min

class DefendCrystalRenderEntity(
    world: Level?, pos: Vec3, val maxRange: Double
) : RenderEntity(world, pos) {
    var color = Vector3f(100 / 255f, 100 / 255f, 140 / 255f)
    var r = Vector3f(2f)
    var alpha = 1.0
    var prevR = Vector3f()
    var over = false
    var overTick = 0
    var formationPos: Vec3 = Vec3.ZERO

    companion object {
        val ballVertexBuffer = SimpleVertexBuffer()
            .apply {
                setVertexes(ShaderUtil.genBall(1f, 64, 64), CooVertexFormat.POINT_FORMAT)
            }

        val fresnalShader = ShaderProgramBuilder()
            .vertex(
                IdentifierShader(
                    ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "core/vsh/ball_point.vsh"),
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
            ballVertexBuffer.init()
            colorShader.init()
        }

        @JvmStatic
        val CODEC: StreamCodec<FriendlyByteBuf, RenderEntity> = StreamCodec.of<FriendlyByteBuf, RenderEntity>(
            { buf, data ->
                data as DefendCrystalRenderEntity
                buf.writeDouble(data.maxRange)
                encodeBase(buf, data)
                buf.writeDouble(data.alpha)
                buf.writeVector3f(data.r)
                buf.writeVector3f(data.color)
                buf.writeBoolean(data.over)
                buf.writeInt(data.overTick)
                buf.writeVec3(data.formationPos)
            }, {
                val instance = DefendCrystalRenderEntity(null, Vec3.ZERO, it.readDouble())
                decodeBase(it, instance)
                instance.alpha = it.readDouble()
                instance.r = it.readVector3f()
                instance.color = it.readVector3f()
                instance.over = it.readBoolean()
                instance.overTick = it.readInt()
                instance.formationPos = it.readVec3()
                instance
            }
        )

        @JvmField
        val ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "defend_crystal_entity")
    }

    override fun initialize() {
        initStatic()
    }

    override fun loadProfileFromEntity(another: RenderEntity) {
        super.loadProfileFromEntity(another)
        if (another !is DefendCrystalRenderEntity) {
            return
        }
        color = another.color
        r = another.r
        alpha = another.alpha
        overTick = another.overTick
        over = another.over
        formationPos = another.formationPos
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
        if (!over) {
            val range = (maxRange / 20f) * min(age, 20)
            r = Vector3f(range.toFloat())
        } else {
            val r1 = maxRange - (maxRange / 20) * (age - overTick)
            if (r1 <= 0.0 || age - overTick > 20) {
                canceled = true
                return
            }
            r = Vector3f(r1.toFloat())
        }
        if (world?.isClientSide ?: true) {
            return
        }
        val world = world!!
        val get = world.getBlockEntity(ofFloored(formationPos))
        if (get !is FormationCoreBlockEntity
            || (!get.formation.inTriggerRangeActive && get.formation.settings.displayDefendBallOnlyTrigger)
            || !get.formation.isActiveFormation()
            || !get.formation.hasDefend
        ) {
            over()
        }
    }

    override fun render(
        matrices: Matrix4fStack,
        viewMatrix: Matrix4f,
        projMatrix: Matrix4f,
        tickDelta: Float
    ) {
        val deltaR = GraphMathHelper.lerp(Vector3f(tickDelta), prevR, r)
        RenderSystem.enableBlend()
        val camera = Minecraft.getInstance().gameRenderer.mainCamera
        matrices.pushMatrix()
        RenderSystem.depthMask(false)
        RenderSystem.blendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE);
        fresnalShader.useOnContext {
            setMatrix4("projMat", projMatrix)
            setMatrix4("viewMat", viewMatrix)
            setMatrix4("transMat", matrices.scale(deltaR))
            setFloat3("camera", pos.relativize(camera.position).toVector3f())
            setFloat3("camera_view", camera.lookVector)
            setFloat3("glowColor", color)
            // 调整这些参数以获得更好的边缘效果
            setFloat("glowIntensity", 5f)
            setFloat("glowFalloff", 5f)
            setFloat("emptyAlpha", 0.05f)
            setFloat("fsl", 0.1f)
            ballVertexBuffer.draw()
        }
        RenderSystem.depthMask(true)
        matrices.popMatrix()
    }

    fun over() {
        if (over) return
        over = true
        overTick = age
        markDirty()
    }

}