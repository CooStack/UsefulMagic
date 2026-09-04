package cn.coostack.usefulmagic.mixin;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 用于跨加载器注册自定义原版游戏规则。
 * {@link GameRules#register} 是 private static, 这里通过 Invoker 暴露出来。
 */
@Mixin(GameRules.class)
public interface GameRulesInvoker {
    @Invoker("register")
    static <T extends GameRules.Value<T>> GameRules.Key<T> usefulmagic$register(
            String name,
            GameRules.Category category,
            GameRules.Type<T> type
    ) {
        throw new AssertionError();
    }
}
