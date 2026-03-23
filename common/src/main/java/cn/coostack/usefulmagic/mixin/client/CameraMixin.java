package cn.coostack.usefulmagic.mixin.client;

import cn.coostack.usefulmagic.extend.EntityExtendKt;
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes;
import cn.coostack.usefulmagic.items.UsefulMagicItems;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(
        method = "setup",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
            shift = At.Shift.BEFORE
        )
    )
    private void usefulmagic$smoothLimitedLightBeamCamera(
        BlockGetter level,
        Entity entity,
        boolean detached,
        boolean thirdPersonReverse,
        float partialTick,
        CallbackInfo ci
    ) {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (!usefulmagic$shouldSmoothLightBeamCamera(player)) {
            return;
        }

        float yaw = Mth.rotLerp(partialTick, player.yRotO, player.getYRot());
        float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        setRotation(yaw, Mth.clamp(pitch, -90.0F, 90.0F));
    }

    private static boolean usefulmagic$shouldSmoothLightBeamCamera(Player player) {
        if (!EntityExtendKt.getCharging(player)) {
            return false;
        }
        var wand = EntityExtendKt.getChargedItem(player);
        var magic = wand.get(UsefulMagicDataComponentTypes.WAND_MAGIC.get());
        return magic != null && magic.is(UsefulMagicItems.LIGHT_BEAM_MAGIC.getItem());
    }
}
