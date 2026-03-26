package shipwrights.genesis_ad_astra.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import shipwrights.genesis.GenesisMod;

@Mixin(value = GenesisMod.class, remap = false)
public class GenesisModMixin {

    @Redirect(
            method = "refreshEntityScaling",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setNoGravity(Z)V"
            )
    )
    private static void doNotDisableGravity(Entity instance, boolean bool) {
        // do nothing
    }
}
