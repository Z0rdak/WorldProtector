package fr.mosca421.worldprotector.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IProtectedRegion extends INBTSerializable<CompoundNBT> {

    public boolean containsFlag(String flag);

    public boolean hasBlacklist();

    public boolean permitsPlayer(PlayerEntity player);


}
