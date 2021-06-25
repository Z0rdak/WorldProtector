package fr.mosca421.worldprotector.mixin;

import net.minecraft.entity.*;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(AgeableEntity.class)
public abstract class MixinAgeableEntity extends CreatureEntity {

    protected MixinAgeableEntity(EntityType<? extends CreatureEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerWorld server) {
        if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(this, server.getDimensionKey())) {
            return null;
        }
        return super.changeDimension(server);
    }
}
