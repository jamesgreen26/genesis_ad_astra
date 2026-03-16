package shipwrights.genesis_ad_astra.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.common.systems.OxygenApiImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import shipwrights.genesis.GenesisMod;
import shipwrights.genesis.space.planet_properties.PlanetProperties;

@Mixin(value = OxygenApiImpl.class, remap = false)
public class OxygenApiMixin {

    @WrapMethod(method = "hasOxygen(Lnet/minecraft/resources/ResourceKey;)Z")
    private boolean hasOxygen(ResourceKey<Level> level, Operation<Boolean> original) {
        PlanetProperties planetProperties = PlanetProperties.get(level.location());

        if (GenesisMod.shouldCancelVoidDamage(level.location())) {
            return false;
        } else if (planetProperties != null) {
            return planetProperties.atmosphere().isBreathable();
        } else  {
            return original.call(level);
        }
    }

    @WrapMethod(method = "hasOxygen(Lnet/minecraft/world/entity/Entity;)Z")
    private boolean hasOxygenWrap(Entity entity, Operation<Boolean> original) {

        if (original.call(entity)) {
            return true;
        }

        Level level = entity.level();
        Vec3 pos = entity.position();

        final boolean[] result = {false};

        VSGameUtilsKt.transformToNearbyShipsAndWorld(
                level,
                pos.x, pos.y, pos.z,
                8,
                (x, y, z) -> {
                    if (result[0]) return;

                    BlockPos checkPos = BlockPos.containing(x, y, z);
                    if (OxygenApi.API.hasOxygen(level, checkPos)) {
                        result[0] = true;
                    }
                    if (VSGameUtilsKt.isBlockInShipyard(level, checkPos)) {
                        for (Direction direction : Direction.values()) {
                            if (OxygenApi.API.hasOxygen(level, checkPos.offset(direction.getNormal()))) {
                                result[0] = true;
                                break;
                            }
                        }
                    }
                });

        return result[0];
    }
}
