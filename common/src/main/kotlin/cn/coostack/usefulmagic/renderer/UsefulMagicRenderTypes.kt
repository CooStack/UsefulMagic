package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.compat.IrisCompat
import cn.coostack.cooparticlesapi.compat.iris.RenderTypeIrisSupposerRegistry
import com.mojang.blaze3d.vertex.DefaultVertexFormat
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
            IrisCompat.wrapEntityRenderType(
                UsefulMagicRenderTypeFactory.dragonCircleEyeBorder(
                    EYE_BORDER_RENDER_TYPE,
                    texture
                ) {
                    eyeBorderShader.also(IrisCompat::markUnskippable)
                }
            )
        }
    }
}
