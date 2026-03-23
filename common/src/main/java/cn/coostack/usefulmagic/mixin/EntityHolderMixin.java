package cn.coostack.usefulmagic.mixin;

import cn.coostack.usefulmagic.data.tracked.CooDataTracker;
import cn.coostack.usefulmagic.data.tracked.CooTrackerHolder;
import cn.coostack.usefulmagic.data.tracked.TrackerManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("preview")
@Mixin(Entity.class)
public class EntityHolderMixin implements CooTrackerHolder {
    @Unique
    private final CooDataTracker tracker = new CooDataTracker();

    @Unique
    @Override
    public @NotNull CooDataTracker getCooTracker() {
        return tracker;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(EntityType entityType, Level level, CallbackInfo ci) {
        var self = (Entity) (Object) this;
        // 直接塞到holder里面
        TrackerManager.INSTANCE.applyHolder(self);
    }

}
