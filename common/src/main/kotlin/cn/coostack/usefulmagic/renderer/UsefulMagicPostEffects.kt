package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
import cn.coostack.cooparticlesapi.renderer.pipeline.CooShaderEffectPlayback
import cn.coostack.cooparticlesapi.renderer.pipeline.CooShaderEffects
import cn.coostack.cooparticlesapi.renderer.pipeline.CooUniformValue
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

/**
 * 注册并播放 UsefulMagic 的屏幕后处理效果。
 *
 * 服务端调用示例：
 * ```kotlin
 * UsefulMagicPostEffects.playRgbDashBlur(player, 40, 1F, 2F)
 * ```
 */
object UsefulMagicPostEffects {
    /** 火焰爆炸闪光的注册路径。 */
    private const val FLAME_EXPLODE_FLASH_ID = "flame_explode_flash"

    /** RGB 分离径向模糊的注册路径。 */
    private const val RGB_DASH_BLUR_ID = "rgb_dash_blur"

    /** 火焰爆炸闪光的注册实例。 */
    val FLAME_EXPLODE_FLASH = CooShaderEffects.register(id(FLAME_EXPLODE_FLASH_ID)) {
        fragment(shader(FLAME_EXPLODE_FLASH_ID))
        inputSceneColor("scene")
        outputToScreen()
    }

    /** 冲刺 RGB 分离径向模糊的注册实例。 */
    val RGB_DASH_BLUR = CooShaderEffects.register(id(RGB_DASH_BLUR_ID)) {
        fragment(shader(RGB_DASH_BLUR_ID))
        inputSceneColor("scene")
        outputToScreen()
    }

    /**
     * 向指定玩家播放火焰爆炸闪光。
     *
     * ```kotlin
     * val playback = UsefulMagicPostEffects.playFlameExplodeFlash(player)
     * ```
     *
     * @param player 接收效果的服务端玩家
     * @param durationTicks 持续时间，单位为 tick，最小按 1 处理
     * @param strength 闪光强度
     * @param warmth 暖色混合强度
     * @param color 闪光颜色
     * @return 可继续控制本次播放的句柄
     */
    fun playFlameExplodeFlash(
        player: ServerPlayer,
        durationTicks: Int = 18,
        strength: Float = 1F,
        warmth: Float = 1F,
        color: Vec3 = Vec3(0.8, 0.5, 0.2),
    ): CooShaderEffectPlayback {
        return FLAME_EXPLODE_FLASH.play(player) {
            duration(durationTicks.coerceAtLeast(1))
            uniform("strength", strength)
            uniform("warmth", warmth)
            uniform(
                "flashColor",
                CooUniformValue.Vec3Value(color.x.toFloat(), color.y.toFloat(), color.z.toFloat()),
            )
        }
    }

    /**
     * 向指定维度中的全部玩家播放火焰爆炸闪光。
     *
     * ```kotlin
     * UsefulMagicPostEffects.playFlameExplodeFlash(level, durationTicks = 20)
     * ```
     *
     * @param level 目标服务端维度
     * @param durationTicks 持续时间，单位为 tick，最小按 1 处理
     * @param strength 闪光强度
     * @param warmth 暖色混合强度
     * @param color 闪光颜色
     */
    fun playFlameExplodeFlash(
        level: ServerLevel,
        durationTicks: Int = 18,
        strength: Float = 1F,
        warmth: Float = 1F,
        color: Vec3 = Vec3(0.8, 0.5, 0.2),
    ) {
        level.players().forEach { player ->
            playFlameExplodeFlash(player, durationTicks, strength, warmth, color)
        }
    }

    /**
     * 向指定玩家播放冲刺 RGB 分离径向模糊。
     *
     * ```kotlin
     * val playback = UsefulMagicPostEffects.playRgbDashBlur(player, 40, 1F, 2F)
     * ```
     *
     * @param player 接收效果的服务端玩家
     * @param durationTicks 持续时间，单位为 tick，最小按 1 处理
     * @param strength 整体效果强度
     * @param chromaticStrength RGB 分离强度
     * @param blurStrength 径向模糊强度
     * @return 可继续控制本次播放的句柄
     */
    fun playRgbDashBlur(
        player: ServerPlayer,
        durationTicks: Int = 14,
        strength: Float = 1F,
        chromaticStrength: Float = 1F,
        blurStrength: Float = 1F,
    ): CooShaderEffectPlayback {
        return RGB_DASH_BLUR.play(player) {
            duration(durationTicks.coerceAtLeast(1))
            uniform("strength", strength)
            uniform("chromaticStrength", chromaticStrength)
            uniform("blurStrength", blurStrength)
        }
    }

    /**
     * 向指定维度中的全部玩家播放冲刺 RGB 分离径向模糊。
     *
     * ```kotlin
     * UsefulMagicPostEffects.playRgbDashBlur(level, durationTicks = 20)
     * ```
     *
     * @param level 目标服务端维度
     * @param durationTicks 持续时间，单位为 tick，最小按 1 处理
     * @param strength 整体效果强度
     * @param chromaticStrength RGB 分离强度
     * @param blurStrength 径向模糊强度
     */
    fun playRgbDashBlur(
        level: ServerLevel,
        durationTicks: Int = 14,
        strength: Float = 1F,
        chromaticStrength: Float = 1F,
        blurStrength: Float = 1F,
    ) {
        level.players().forEach { player ->
            playRgbDashBlur(player, durationTicks, strength, chromaticStrength, blurStrength)
        }
    }

    /**
     * 触发对象初始化并完成效果注册。
     *
     * ```kotlin
     * UsefulMagicPostEffects.init()
     * ```
     */
    fun init() = Unit

    /**
     * 创建 UsefulMagic 命名空间下的效果 ID。
     *
     * @return 使用模组命名空间的资源 ID
     */
    private fun id(path: String): ResourceLocation {
        return ofID(UsefulMagic.MOD_ID, path)
    }

    /**
     * 创建 UsefulMagic 后处理 fragment shader 的资源 ID。
     *
     * @return 指向 `shaders/post` 资源的 ID
     */
    private fun shader(path: String): ResourceLocation {
        return ofID(UsefulMagic.MOD_ID, "post/$path.fsh")
    }
}
