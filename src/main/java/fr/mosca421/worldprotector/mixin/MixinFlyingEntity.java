package fr.mosca421.worldprotector.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(FlyingEntity.class)
public abstract class MixinFlyingEntity extends MobEntity {

    protected MixinFlyingEntity(EntityType<? extends MobEntity> type, World worldIn) {
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
