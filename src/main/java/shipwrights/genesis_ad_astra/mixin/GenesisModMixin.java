package shipwrights.genesis_ad_astra.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import shipwrights.genesis.GenesisMod;

@Mixin(value = GenesisMod.class, remap = false)
public class GenesisModMixin {

    @WrapOperation(
            method = "refreshEntityScaling",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setNoGravity(Z)V"
            ),
            remap = true
    )
    private static void doNotDisableGravity(Entity instance, boolean p_20243_, Operation<Void> original) {
        // do nothing
    }
}
