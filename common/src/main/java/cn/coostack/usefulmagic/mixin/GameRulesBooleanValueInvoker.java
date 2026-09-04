package cn.coostack.usefulmagic.mixin;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@link GameRules.BooleanValue#create(boolean)} 是包级私有 static, 这里通过 Invoker 暴露出来,
 * 用于构造自定义布尔游戏规则的 Type。
 */
@Mixin(GameRules.BooleanValue.class)
public interface GameRulesBooleanValueInvoker {
    @Invoker("create")
    static GameRules.Type<GameRules.BooleanValue> usefulmagic$create(boolean defaultValue) {
        throw new AssertionError();
    }
}
