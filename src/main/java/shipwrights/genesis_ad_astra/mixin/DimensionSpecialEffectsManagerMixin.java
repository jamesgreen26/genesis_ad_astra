package shipwrights.genesis_ad_astra.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import earth.terrarium.adastra.common.planets.AdAstraData;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.DimensionSpecialEffectsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import shipwrights.genesis.GenesisMod;
import shipwrights.genesis.client.PlanetDimensionEffects;

@Mixin(value = DimensionSpecialEffectsManager.class, remap = false)
public class DimensionSpecialEffectsManagerMixin {

    @Shadow
    private static ImmutableMap<ResourceLocation, DimensionSpecialEffects> EFFECTS;

    @WrapMethod(method = "getForType")
    private static DimensionSpecialEffects wrapGetForType(ResourceLocation type, Operation<DimensionSpecialEffects> original) {
        if (AdAstraData.planets().keySet().stream().anyMatch(levelResourceKey -> type.equals(levelResourceKey.location()))) {
            return EFFECTS.get(GenesisMod.GENERIC_PLANET_ID);
        }
        return original.call(type);
    }
}
