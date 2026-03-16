package shipwrights.genesis_ad_astra.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import earth.terrarium.adastra.api.events.AdAstraEvents;
import earth.terrarium.adastra.api.systems.TemperatureApi;
import earth.terrarium.adastra.common.config.AdAstraConfig;
import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import earth.terrarium.adastra.common.systems.TemperatureApiImpl;
import earth.terrarium.adastra.common.tags.ModEntityTypeTags;
import earth.terrarium.adastra.common.tags.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(value = TemperatureApiImpl.class, remap = false)
public class TemperatureApiMixin {

    @Unique
    private static short mmc$bestTemperature(ServerLevel level, Vec3 pos) {

        short worldTemp = TemperatureApi.API.getTemperature(level);

        // if world is already livable, use it
        if (worldTemp >= -50 && worldTemp <= 70) {
            return worldTemp;
        }

        final short[] result = {worldTemp};

        VSGameUtilsKt.transformToNearbyShipsAndWorld(
                level,
                pos.x, pos.y, pos.z,
                8,
                (x, y, z) -> {

                    BlockPos shipPos = BlockPos.containing(x, y, z);
                    short temp = TemperatureApi.API.getTemperature(level, shipPos);

                    if (temp >= -50 && temp <= 70) {
                        result[0] = temp;
                    }

                    if (VSGameUtilsKt.isBlockInShipyard(level, shipPos)) {
                        for (Direction direction : Direction.values()) {
                            temp = TemperatureApi.API.getTemperature(level, shipPos.offset(direction.getNormal()));

                            if (temp >= -50 && temp <= 70) {
                                result[0] = temp;
                                break;
                            }
                        }
                    }
                });

        return result[0];
    }

    @WrapMethod(method = "entityTick")
    private void entityTickWrap(ServerLevel level, LivingEntity entity, Operation<Void> original) {
        if (!AdAstraConfig.disableTemperature) {
            if (!entity.getType().is(ModEntityTypeTags.CAN_SURVIVE_IN_SPACE)) {
                if (!SpaceSuitItem.hasFullSet(entity, ModItemTags.SPACE_RESISTANT_ARMOR)) {
                    short temperature = mmc$bestTemperature(level, entity.getEyePosition());

                    if (temperature > 70) {
                        if (entity.getType().is(ModEntityTypeTags.CAN_SURVIVE_EXTREME_HEAT)) {
                            return;
                        }

                        if (SpaceSuitItem.hasFullSet(entity, ModItemTags.HEAT_RESISTANT_ARMOR)) {
                            return;
                        }

                        if (entity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                            return;
                        }

                        if (AdAstraEvents.HotTemperatureTickEvent.fire(level, entity)) {
                            burnEntity(entity);
                        }
                    } else if (temperature < -50) {
                        if (entity.getType().is(ModEntityTypeTags.CAN_SURVIVE_EXTREME_COLD)) {
                            return;
                        }

                        if (SpaceSuitItem.hasFullSet(entity, ModItemTags.FREEZE_RESISTANT_ARMOR)) {
                            return;
                        }

                        if (AdAstraEvents.ColdTemperatureTickEvent.fire(level, entity)) {
                            this.freezeEntity(entity, level);
                        }
                    }
                }
            }
        }
    }


    @Shadow
    private void burnEntity(LivingEntity entity) {}

    @Shadow
    private void freezeEntity(LivingEntity entity, ServerLevel level) {}
}
