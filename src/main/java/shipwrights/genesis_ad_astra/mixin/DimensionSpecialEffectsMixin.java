package shipwrights.genesis_ad_astra.mixin;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.client.DimensionSpecialEffectsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DimensionSpecialEffects.class, remap = false, priority = 100)
public class DimensionSpecialEffectsMixin {

    @Inject(method = "forType", at = @At("HEAD"), cancellable = true)
    private static void wrapGetForType(DimensionType p_108877_, CallbackInfoReturnable<DimensionSpecialEffects> cir) {
        cir.setReturnValue(DimensionSpecialEffectsManager.getForType(p_108877_.effectsLocation()));
    }
}