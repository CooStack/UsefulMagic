package cn.coostack.usefulmagic.mixin.client;

import cn.coostack.usefulmagic.extend.EntityExtendKt;
import cn.coostack.usefulmagic.items.weapon.wands.MagicWand;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @WrapOperation(
        method = "renderArmWithItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z")
    )
    private boolean usefulmagic$renderChargingAsUsing(
        AbstractClientPlayer player,
        Operation<Boolean> original,
        AbstractClientPlayer argPlayer,
        float partialTicks,
        float pitch,
        InteractionHand hand,
        float swingProgress,
        ItemStack stack
    ) {
        return original.call(player) || usefulmagic$shouldRenderChargingPose(player, hand, stack);
    }

    @WrapOperation(
        method = "renderArmWithItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUseItemRemainingTicks()I")
    )
    private int usefulmagic$chargingRemainingTicks(
        AbstractClientPlayer player,
        Operation<Integer> original,
        AbstractClientPlayer argPlayer,
        float partialTicks,
        float pitch,
        InteractionHand hand,
        float swingProgress,
        ItemStack stack
    ) {
        if (usefulmagic$shouldRenderChargingPose(player, hand, stack)) {
            ItemStack chargeStack = usefulmagic$getChargingItem((LocalPlayer) player, stack);
            int duration = Math.max(1, chargeStack.getUseDuration(player));
            int chargingTick = EntityExtendKt.getChargingTick((Entity) player);
            return Math.max(1, duration - chargingTick);
        }
        return original.call(player);
    }

    @WrapOperation(
        method = "renderArmWithItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUsedItemHand()Lnet/minecraft/world/InteractionHand;")
    )
    private InteractionHand usefulmagic$chargingUsedHand(
        AbstractClientPlayer player,
        Operation<InteractionHand> original,
        AbstractClientPlayer argPlayer,
        float partialTicks,
        float pitch,
        InteractionHand hand,
        float swingProgress,
        ItemStack stack
    ) {
        if (usefulmagic$shouldRenderChargingPose(player, hand, stack)) {
            return hand;
        }
        return original.call(player);
    }

    private static boolean usefulmagic$shouldRenderChargingPose(AbstractClientPlayer player, InteractionHand hand, ItemStack stack) {
        if (!(player instanceof LocalPlayer localPlayer)) {
            return false;
        }
        InteractionHand chargingHand = usefulmagic$getChargingHand(localPlayer);
        if (chargingHand != hand) {
            return false;
        }
        ItemStack chargingItem = usefulmagic$getChargingItem(localPlayer, stack);
        if (chargingItem.isEmpty()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(chargingItem, stack)
            || ItemStack.isSameItem(chargingItem, stack)
            || stack.getItem() instanceof MagicWand;
    }

    private static InteractionHand usefulmagic$getChargingHand(LocalPlayer player) {
        if (Minecraft.getInstance().player != player) {
            return null;
        }
        if (!EntityExtendKt.getCharging((Entity) player)) {
            return null;
        }
        ItemStack chargedItem = EntityExtendKt.getChargedItem((Entity) player);
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
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

        if (mainHandItem.getItem() instanceof MagicWand) {
            return InteractionHand.MAIN_HAND;
        }
        if (offHandItem.getItem() instanceof MagicWand) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static ItemStack usefulmagic$getChargingItem(LocalPlayer player, ItemStack fallback) {
        ItemStack chargedItem = EntityExtendKt.getChargedItem((Entity) player);
        if (!chargedItem.isEmpty()) {
            return chargedItem;
        }
        return fallback;
    }
}
