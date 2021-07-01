package fr.mosca421.worldprotector.core;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

/**
 *
 */
public interface IMarkableArea extends INBTSerializable<CompoundNBT> {

    boolean contains(BlockPos pos);

    AreaType getAreaType();
}
