package cn.coostack.usefulmagic.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HopperBlockEntity.class)
public class HooperBlockEntityMixin {

    @ModifyVariable(
            method = "isFullContainer",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            )
    )
    private static ItemStack modifyItemStack(ItemStack original, Container container, Direction direction) {
        int max = container.getMaxStackSize();
        int minMaxStackSize = Math.min(max, original.getMaxStackSize());
        if (original.getCount() >= minMaxStackSize) { // 直接替换
            ItemStack fake = original.copy();
            fake.setCount(fake.getMaxStackSize());
            return fake;
        }
        return original;
    }
}
