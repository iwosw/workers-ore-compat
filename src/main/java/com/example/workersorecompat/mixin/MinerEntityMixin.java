package com.example.workersorecompat.mixin;

import com.example.workersorecompat.compat.MatchResult;
import com.example.workersorecompat.compat.OreItemMatcher;
import com.talhanation.workers.entities.MinerEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinerEntity.class, remap = false)
public class MinerEntityMixin {

    // wantsToPickUp is a vanilla Mob method overridden by MinerEntity, so it must be remapped
    // (it is m_7243_ at runtime outside the dev environment).
    @Inject(method = "wantsToPickUp", at = @At("HEAD"), cancellable = true, remap = true)
    private void workersOreCompat$wantsToPickUp(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        MatchResult result = OreItemMatcher.match(itemStack);
        if (result == MatchResult.DENY) {
            cir.setReturnValue(false);
        } else if (result == MatchResult.ALLOW) {
            cir.setReturnValue(true);
        }
    }
}
