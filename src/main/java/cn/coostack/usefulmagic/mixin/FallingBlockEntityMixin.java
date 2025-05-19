package cn.coostack.usefulmagic.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityMixin {
    @Accessor("block")
    BlockState getBlockState();

    @Accessor("block")
    void setBlockState(BlockState state);

}
