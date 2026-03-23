package cn.coostack.usefulmagic.mixin;

import cn.coostack.usefulmagic.effects.UsefulMagicEffects;
import cn.coostack.usefulmagic.extend.PlayerExtendKt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        var self = (Player) (Object) this;
        compound.putInt("current_mana", PlayerExtendKt.getMana(self));
        compound.putInt("max_mana", PlayerExtendKt.getMaxMana(self));
        compound.putInt("mana_absorption_rate", PlayerExtendKt.getManaAbsorptionRate(self));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        var self = (Player) (Object) this;
        if (compound.contains("current_mana")) {
            var mana = compound.getInt("current_mana");
            PlayerExtendKt.setMana(self, mana);
        }
        if (compound.contains("max_mana")) {
            var maxMana = compound.getInt("max_mana");
            PlayerExtendKt.setMaxMana(self, maxMana);
        }
        if (compound.contains("mana_absorption_rate")) {
            var manaAbsorptionRate = compound.getInt("mana_absorption_rate");
            PlayerExtendKt.setManaAbsorptionRate(self, manaAbsorptionRate);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        // 这里进行恢复设置
        var self = (Player) (Object) this;
        if (self.tickCount % 20 != 0) {
            return;
        }
        var currentMana = PlayerExtendKt.getMana(self);
        var absorptionRate = PlayerExtendKt.getManaAbsorptionRate(self);
        var maxMana = PlayerExtendKt.getMaxMana(self);

        if (UsefulMagicEffects.INSTANCE.isMagicSealed(self)) {
            return;
        }

        PlayerExtendKt.setMana(self, Math.min(maxMana, currentMana + absorptionRate));
    }
}
