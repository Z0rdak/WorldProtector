package fr.mosca421.worldprotector.mixin;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(AbstractMinecartEntity.class)
public abstract class MixinAbstractMinecartEntity extends Entity implements net.minecraftforge.common.extensions.IForgeEntityMinecart {

    public MixinAbstractMinecartEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
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
