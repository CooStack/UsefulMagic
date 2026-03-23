package cn.coostack.usefulmagic.mixin;

import cn.coostack.usefulmagic.extend.EntityExtendKt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void usefulmagic$bridgeChargingUseState(CallbackInfoReturnable<Boolean> cir) {
        if (this.usefulmagic$shouldRenderChargeUsing()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getUseItem", at = @At("HEAD"), cancellable = true)
    private void usefulmagic$bridgeChargingUseItem(CallbackInfoReturnable<ItemStack> cir) {
        if (!this.usefulmagic$shouldRenderChargeUsing()) {
            return;
        }
        cir.setReturnValue(this.usefulmagic$getChargingItem());
    }

    @Inject(method = "getUsedItemHand", at = @At("HEAD"), cancellable = true)
    private void usefulmagic$bridgeChargingUseHand(CallbackInfoReturnable<InteractionHand> cir) {
        if (!this.usefulmagic$shouldRenderChargeUsing()) {
            return;
        }
        cir.setReturnValue(this.usefulmagic$findChargingHand());
    }

    @Inject(method = "getUseItemRemainingTicks", at = @At("HEAD"), cancellable = true)
    private void usefulmagic$bridgeChargingUseTicks(CallbackInfoReturnable<Integer> cir) {
        if (!this.usefulmagic$shouldRenderChargeUsing()) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack chargingItem = this.usefulmagic$getChargingItem();
        int duration = chargingItem.isEmpty() ? 72000 : chargingItem.getUseDuration(self);
        int chargingTick = EntityExtendKt.getChargingTick((Entity) self);
        cir.setReturnValue(Math.max(1, duration - chargingTick));
    }

    private boolean usefulmagic$shouldRenderChargeUsing() {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide) {
            return false;
        }
        if (!EntityExtendKt.getCharging((Entity) self)) {
            return false;
        }
        return !this.usefulmagic$getChargingItem().isEmpty();
    }

    private ItemStack usefulmagic$getChargingItem() {
        LivingEntity self = (LivingEntity) (Object) this;
        InteractionHand hand = this.usefulmagic$findChargingHand();
        ItemStack current = self.getItemInHand(hand);
        if (!current.isEmpty()) {
            return current;
        }
        return EntityExtendKt.getChargedItem((Entity) self);
    }

    private InteractionHand usefulmagic$findChargingHand() {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack chargedItem = EntityExtendKt.getChargedItem((Entity) self);
        ItemStack mainHandItem = self.getMainHandItem();
        ItemStack offHandItem = self.getOffhandItem();
        if (!chargedItem.isEmpty()) {
            boolean mainExact = ItemStack.isSameItemSameComponents(chargedItem, mainHandItem);
            boolean offExact = ItemStack.isSameItemSameComponents(chargedItem, offHandItem);
            if (mainExact != offExact) {
                return mainExact ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            }

            boolean mainType = ItemStack.isSameItem(chargedItem, mainHandItem);
            boolean offType = ItemStack.isSameItem(chargedItem, offHandItem);
            if (mainType != offType) {
                return mainType ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            }
        }

        if (!mainHandItem.isEmpty()) {
            return InteractionHand.MAIN_HAND;
        }
        if (!offHandItem.isEmpty()) {
            return InteractionHand.OFF_HAND;
        }
        return InteractionHand.MAIN_HAND;
    }
}
