package cn.coostack.usefulmagic.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {
    @Accessor("block")
    BlockState getBlockState();

    @Accessor("block")
    void setBlockState(BlockState state);

    @Accessor("destroyedOnLanding")
    boolean getDestroyedOnLanding();

    @Accessor("destroyedOnLanding")
    void setDestroyedOnLanding(boolean landing);

}
