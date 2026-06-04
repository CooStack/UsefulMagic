package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.renderer.backend.RenderBackendCapability
import cn.coostack.cooparticlesapi.renderer.post.CooPostEffectTypes
import cn.coostack.cooparticlesapi.renderer.post.PostEffectInstance
import cn.coostack.cooparticlesapi.renderer.post.PostEffectLifecycle
import cn.coostack.cooparticlesapi.renderer.post.PostEffectParamValue
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3

object UsefulMagicPostEffects {

    private fun id(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, path)
    }

    private fun shader(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "post/$path.fsh")
    }

    val FLAME_EXPLODE_FLASH = CooPostEffectTypes.register(
        id("flame_explode_flash")
    ) {
        screenQuad()
        require(RenderBackendCapability.FINAL_FRAME_POST)
        require(RenderBackendCapability.SCENE_COLOR_COPY)
        pass(
            "explode_flash",
            shader("flame_explode_flash")
        ) {
            inputSceneColor("scene")

            uniform("time") {
                it.params["time"] ?: PostEffectParamValue.FloatValue(0f)
            }
            uniform("strength") {
                it.params["strength"] ?: PostEffectParamValue.FloatValue(1f)
            }
            uniform("warmth") {
                it.params["warmth"] ?: PostEffectParamValue.FloatValue(1f)
            }
            uniform("flashColor") {
                it.params["flashColor"] ?: PostEffectParamValue.Vec3Value(0.8, 0.5, 0.2)
            }
            outputToFinalScreen()
        }
        outputToFinalScreen()
    }

    val RGB_DASH_BLUR = CooPostEffectTypes.register(
        id("rgb_dash_blur")
    ) {
        screenQuad()
        require(RenderBackendCapability.FINAL_FRAME_POST)
        require(RenderBackendCapability.SCENE_COLOR_COPY)
        pass(
            "rgb_dash_blur",
            shader("rgb_dash_blur")
        ) {
            inputSceneColor("scene")

            uniform("time") {
                it.params["time"] ?: PostEffectParamValue.FloatValue(0f)
            }
            uniform("strength") {
                it.params["strength"] ?: PostEffectParamValue.FloatValue(1f)
            }
            uniform("chromaticStrength") {
                it.params["chromaticStrength"] ?: PostEffectParamValue.FloatValue(1f)
            }
            uniform("blurStrength") {
                it.params["blurStrength"] ?: PostEffectParamValue.FloatValue(1f)
            }

            outputToFinalScreen()
        }
        outputToFinalScreen()
    }

    fun flameExplodeFlash(
        durationTicks: Int = 18,
        strength: Float = 1f,
        warmth: Float = 1f,
        color: Vec3 = Vec3(0.8, 0.5, 0.2)
    ): PostEffectInstance {
        return FLAME_EXPLODE_FLASH.create(
            lifecycle = PostEffectLifecycle(durationTicks = durationTicks.coerceAtLeast(1))
        ).bindScreen().params {
            float("strength", strength)
            float("warmth", warmth)
            vec3("flashColor", color.x, color.y, color.z)
        }
    }

    /**
     * 创建冲刺用 RGB 分离径向模糊后处理。
     *
     * @param durationTicks 效果持续 tick 数，小于 1 时按 1 tick 处理；生命周期会驱动 shader 的 progress 淡出。
     * @param strength 整体效果强度，影响模糊、色散、中心提亮和边缘压暗的共同脉冲。
     * @param chromaticStrength RGB 色散强度，越大红绿蓝通道分离偏移越明显。
     * @param blurStrength 向屏幕中心方向采样的径向模糊强度，越大拖影越长、最终模糊混合越明显。
     */
    fun rgbDashBlur(
        durationTicks: Int = 14,
        strength: Float = 1f,
        chromaticStrength: Float = 1f,
        blurStrength: Float = 1f
    ): PostEffectInstance {
        return RGB_DASH_BLUR.create(
            lifecycle = PostEffectLifecycle(durationTicks = durationTicks.coerceAtLeast(1))
        ).bindScreen().params {
            float("strength", strength)
            float("chromaticStrength", chromaticStrength)
            float("blurStrength", blurStrength)
        }
    }

    fun init() = Unit

}
