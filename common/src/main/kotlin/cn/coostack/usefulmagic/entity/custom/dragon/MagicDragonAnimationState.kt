package cn.coostack.usefulmagic.entity.custom.dragon

import software.bernie.geckolib.animation.RawAnimation

/**
 * `MagicDragonEntity` 的 GeckoLib 动画统一入口。
 *
 * 使用方式：
 * 1. 在技能开始时调用 `entity.playSkillAnimation(...)`
 * 2. 在技能结束时调用 `entity.clearSkillAnimation()`
 * 3. 如果要新增一个动作，只需要：
 *    a. 在 `magic_dragon.animation.json` 里新增动画名
 *    b. 在这个枚举里补一个常量，并选择合适的播放方式
 *    c. 在技能或 Goal 里调用它
 *
 * 基本示例：
 * ```kotlin
 * entity.playSkillAnimation(MagicDragonSkillAnimationState.OPEN_MOUTH)
 * // 做你的蓄力或判定
 * entity.playSkillAnimation(MagicDragonSkillAnimationState.BREATH_FORWARD)
 * // 技能结束
 * entity.clearSkillAnimation()
 * ```
 *
 * 约定：
 * - `NONE` 表示交回给实体自身的基础移动动画
 * - 枚举名是代码侧入口，`animationName` 必须和动画 JSON 中的动作名保持一致
 */
enum class MagicDragonAnimationState(
    val animationName: String,
    val durationTicks: Int,
    private val playback: Playback
) {
    NONE("animation.model.idle", 0, Playback.PLAY_ONCE), // 无显式动作，交给基础移动逻辑
    WALK("animation.model.walk", 120, Playback.LOOP), // 行走
    TAKE_OFF("animation.model.take_off", 40, Playback.HOLD), // 起飞
    FLY("animation.model.fly", 25, Playback.LOOP), // 飞行
    OPEN_MOUTH("animation.model.open_mouth", 40, Playback.HOLD), // 张嘴
    BREATH_FORWARD("animation.model.breath_forward", 40, Playback.LOOP), // 正面吐息
    BREATH_TURN_LEFT("animation.model.breath_turn_left", 40, Playback.HOLD), // 左转吐息
    BREATH_LEFT("animation.model.breath_left", 40, Playback.LOOP), // 左侧吐息
    BREATH_LEFT_RESET("animation.model.breath_left_reset", 40, Playback.HOLD), // 左回正
    BREATH_TURN_RIGHT("animation.model.breath_turn_right", 40, Playback.HOLD), // 右转吐息
    BREATH_RIGHT("animation.model.breath_right", 40, Playback.LOOP), // 右侧吐息
    BREATH_RIGHT_RESET("animation.model.breath_right_reset", 40, Playback.HOLD), // 右回正
    BREATH_FINISH_AND_FLY("animation.model.breath_finish_and_fly", 40, Playback.HOLD), // 结束吐息，抬头飞行
    WALK_BREATH("animation.model.walk_breath", 120, Playback.LOOP), // 行走吐息
    WALK_OPEN_MOUTH("animation.model.walk_open_mouth", 120, Playback.LOOP), // 行走张嘴
    WALK_CLOSE_MOUTH("animation.model.walk_close_mouth", 120, Playback.LOOP), // 行走闭嘴
    BURST_UP_OPEN_WINGS("animation.model.burst_up_open_wings", 40, Playback.PLAY_ONCE), // 向上爆发打开翅膀
    DIZZY_FALL("animation.model.dizzy_fall", 15, Playback.PLAY_ONCE), // 眩晕倒下
    DIZZY_IDLE("animation.model.dizzy_idle", 11, Playback.LOOP), // 眩晕待机
    DIZZY_RECOVER("animation.model.dizzy_recover", 18, Playback.PLAY_ONCE); // 眩晕后重新站起来

    val rawAnimation: RawAnimation by lazy {
        when (playback) {
            Playback.LOOP -> RawAnimation.begin().thenLoop(animationName)
            Playback.HOLD -> RawAnimation.begin().thenPlayAndHold(animationName)
            Playback.PLAY_ONCE -> RawAnimation.begin().thenPlay(animationName)
        }
    }

    companion object {
        fun fromOrdinal(ordinal: Int): MagicDragonAnimationState {
            return entries.getOrElse(ordinal) { NONE }
        }
    }

    private enum class Playback {
        LOOP,
        HOLD,
        PLAY_ONCE
    }
}
