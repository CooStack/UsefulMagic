package cn.coostack.usefulmagic.mixin;

import cn.coostack.usefulmagic.effects.UsefulMagicEffects;
import cn.coostack.usefulmagic.extend.EntityExtendKt;
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes;
import cn.coostack.usefulmagic.items.UsefulMagicItems;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Unique
    private static final float USEFULMAGIC_MAX_YAW_ROTATION_PER_TICK = 5.0F;
    @Unique
    private static final float USEFULMAGIC_MAX_PITCH_ROTATION_PER_TICK = 5.0F;

    @Unique
    private int usefulmagic$rotationLimitTick = Integer.MIN_VALUE;
    @Unique
    private float usefulmagic$rotationLimitBaseYaw;
    @Unique
    private float usefulmagic$rotationLimitBasePitch;

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Vec3 usefulmagic$freezeMove(Vec3 pos) {
        var self = (Entity) (Object) this;
        if (UsefulMagicEffects.INSTANCE.isFrozen(self)) {
            return Vec3.ZERO;
        }
        return pos;
    }

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void usefulmagic$limitLightBeamRotation(double yRot, double xRot, CallbackInfo ci) {
        var self = (Entity) (Object) this;
        if (!(self instanceof Player player)) {
            return;
        }
        if (!usefulmagic$shouldLimitLightBeamRotation(player)) {
            return;
        }

        usefulmagic$syncRotationLimitBase(player);

        float targetYaw = player.getYRot() + (float) yRot * 0.15F;
        float targetPitch = Mth.clamp(player.getXRot() + (float) xRot * 0.15F, -90.0F, 90.0F);
        float limitedYaw = usefulmagic$limitRotation(
            usefulmagic$rotationLimitBaseYaw,
            targetYaw,
            USEFULMAGIC_MAX_YAW_ROTATION_PER_TICK
        );
        float limitedPitch = usefulmagic$limitRotation(
            usefulmagic$rotationLimitBasePitch,
            targetPitch,
            USEFULMAGIC_MAX_PITCH_ROTATION_PER_TICK
        );

        player.setYRot(limitedYaw);
        player.setXRot(Mth.clamp(limitedPitch, -90.0F, 90.0F));
        if (player.getVehicle() != null) {
            player.getVehicle().onPassengerTurned(player);
        }
        ci.cancel();
    }

    @Unique
    private void usefulmagic$syncRotationLimitBase(Player player) {
        if (usefulmagic$rotationLimitTick == player.tickCount) {
            return;
        }
        usefulmagic$rotationLimitTick = player.tickCount;
        usefulmagic$rotationLimitBaseYaw = player.yRotO;
        usefulmagic$rotationLimitBasePitch = player.xRotO;
    }

    @Unique
    private static boolean usefulmagic$shouldLimitLightBeamRotation(Player player) {
        if (!EntityExtendKt.getCharging(player)) {
            return false;
        }
        var wand = EntityExtendKt.getChargedItem(player);
        var magic = wand.get(UsefulMagicDataComponentTypes.WAND_MAGIC.get());
        return magic != null && magic.is(UsefulMagicItems.LIGHT_BEAM_MAGIC.getItem());
    }

    @Unique
    private static float usefulmagic$limitRotation(float previous, float target, float maxDelta) {
        float delta = Mth.degreesDifference(previous, target);
        if (Math.abs(delta) <= maxDelta) {
            return previous + delta;
        }
        return previous + Math.copySign(maxDelta, delta);
    }
}
