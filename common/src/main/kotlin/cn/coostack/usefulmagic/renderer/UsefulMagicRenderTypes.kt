package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.compat.IrisCompat
import cn.coostack.cooparticlesapi.compat.iris.RenderTypeIrisSupposerRegistry
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager

object UsefulMagicRenderTypes {
    private const val EYE_BORDER_RENDER_TYPE = "usefulmagic_dragon_circle_eye_border"
    private const val EYE_BORDER_SHADER = "usefulmagic_dragon_circle_eye_border"

    private val eyeBorderCache = LinkedHashMap<ResourceLocation, RenderType>()

    private lateinit var eyeBorderShader: ShaderInstance

    fun init(resourceManager: ResourceManager) {
        release()
        eyeBorderShader = ShaderInstance(
            resourceManager,
            EYE_BORDER_SHADER,
            DefaultVertexFormat.NEW_ENTITY
        )
        IrisCompat.markUnskippable(eyeBorderShader)
        RenderTypeIrisSupposerRegistry.register(EYE_BORDER_RENDER_TYPE, EYE_BORDER_SHADER)
        eyeBorderCache.clear()
    }

    fun release() {
        if (::eyeBorderShader.isInitialized) {
            eyeBorderShader.close()
        }
        eyeBorderCache.clear()
    }

    fun dragonCircleEyeBorder(texture: ResourceLocation): RenderType {
        if (!::eyeBorderShader.isInitialized) {
            init(net.minecraft.client.Minecraft.getInstance().resourceManager)
        }
        return eyeBorderCache.getOrPut(texture) {
            RenderTypeIrisSupposerRegistry.register(EYE_BORDER_RENDER_TYPE, EYE_BORDER_SHADER)
            val state = RenderType.CompositeState.builder()
                .setShaderState(
                    RenderStateShard.ShaderStateShard {
                        eyeBorderShader.also(IrisCompat::markUnskippable)
                    }
                )
                .setTextureState(RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(true)

            IrisCompat.wrapEntityRenderType(
                RenderType.create(
                    EYE_BORDER_RENDER_TYPE,
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    1536,
                    true,
                    true,
                    state
                )
            )
        }
    }
}
