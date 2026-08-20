package com.example.workersorecompat.mixin;

import com.example.workersorecompat.compat.MatchResult;
import com.example.workersorecompat.compat.OreBlockMatcher;
import com.talhanation.workers.entities.workarea.MiningArea;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MiningArea.class, remap = false)
public class MiningAreaMixin {

    @Inject(method = "isOre", at = @At("HEAD"), cancellable = true, remap = false)
    private void workersOreCompat$isOre(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        MatchResult result = OreBlockMatcher.match(state);
        if (result == MatchResult.DENY) {
            cir.setReturnValue(false);
        } else if (result == MatchResult.ALLOW) {
            cir.setReturnValue(true);
        }
    }
}
