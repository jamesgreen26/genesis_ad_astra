package shipwrights.genesis_ad_astra.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import earth.terrarium.adastra.api.events.AdAstraEvents;
import earth.terrarium.adastra.common.config.AdAstraConfig;
import earth.terrarium.adastra.common.systems.GravityApiImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(value = GravityApiImpl.class, remap = false)
public class GravityApiMixin {

    @WrapMethod(method = "getGravity(Lnet/minecraft/resources/ResourceKey;)F")
    private float wrapGetGravity0(ResourceKey<Level> level, Operation<Float> original) {
        if (level.location().equals(ResourceLocation.parse("genesis:moon"))) {
            return 0.2f;
        } else {
            return original.call(level);
        }
    }


    @WrapMethod(method = "getGravity(Lnet/minecraft/world/entity/Entity;)F")
    private float wrapGetGravity(Entity entity, Operation<Float> original) {

        if (AdAstraConfig.disableGravity) {
            return 1.0F;
        }

        Level level = entity.level();

        double x = entity.getX();
        double y = entity.getEyeY();
        double z = entity.getZ();

        AtomicReference<Float> bestGravity = new AtomicReference<>(this.getGravity(level, BlockPos.containing(x, y, z)));
        AtomicReference<Float> bestScore = new AtomicReference<>(Math.abs(bestGravity.get() - 1.0F));

        VSGameUtilsKt.transformToNearbyShipsAndWorld(
                level,
                x, y, z,
                8,
                (tx, ty, tz) -> {

                    float g = this.getGravity(level, BlockPos.containing(tx, ty, tz));
                    float score = Math.abs(g - 1.0F);

                    if (score < bestScore.get()) {
                        bestScore.set(score);
                        bestGravity.set(g);
                    }
                }
        );

        return AdAstraEvents.EntityGravityEvent.fire(entity, bestGravity.get());
    }

    @Shadow
    public float getGravity(Level level, BlockPos containing) {
        return 0;
    }
}
