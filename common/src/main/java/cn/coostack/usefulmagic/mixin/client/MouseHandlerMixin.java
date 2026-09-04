package cn.coostack.usefulmagic.mixin.client;

import cn.coostack.usefulmagic.gui.magicexchange.MagicExchangeOverlay;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在法术轮盘活动时接管鼠标滚轮和视角转动，同时保留原版鼠标帧的收尾处理。
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    /** 原版水平方向的平滑镜头状态。 */
    @Shadow
    @Final
    private SmoothDouble smoothTurnX;

    /** 原版垂直方向的平滑镜头状态。 */
    @Shadow
    @Final
    private SmoothDouble smoothTurnY;

    /** 当前鼠标处理帧是否需要禁止玩家视角转动。 */
    @Unique
    private boolean usefulmagic$suppressCameraTurn;

    /** 在法术轮盘活动时消费滚轮输入，避免同时切换快捷栏。 */
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void usefulmagic$onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        if (MagicExchangeOverlay.onMouseScroll(yOffset)) {
            ci.cancel();
        }
    }

    /** 在原版处理累计鼠标位移前锁定本帧的视角抑制状态。 */
    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
    private void usefulmagic$captureCameraTurnSuppression(CallbackInfo ci) {
        usefulmagic$suppressCameraTurn = MagicExchangeOverlay.shouldSuppressCameraTurn();
    }

    /**
     * 禁止轮盘选择期间的玩家转向，并清空平滑镜头惯性，防止关闭轮盘后视角漂移。
     */
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void usefulmagic$suppressCameraTurn(double frameTime, CallbackInfo ci) {
        if (usefulmagic$suppressCameraTurn) {
            smoothTurnX.reset();
            smoothTurnY.reset();
            ci.cancel();
        }
    }
}
