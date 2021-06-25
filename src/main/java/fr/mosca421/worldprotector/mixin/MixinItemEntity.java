package fr.mosca421.worldprotector.mixin;

import fr.mosca421.worldprotector.WorldProtector;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.INameable;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {

    public MixinItemEntity(EntityType<?> entityTypeIn, World worldIn) {
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
