package fr.mosca421.worldprotector.core;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMarkableRegion extends IProtectedRegion {

   String getName();

   RegistryKey<World> getDimension();

   AxisAlignedBB getArea();

   void setArea(AxisAlignedBB areaFromNBT);

   boolean containsPosition(BlockPos position);

   BlockPos getTpTarget();

   void setTpTarget(BlockPos pos);

   int getPriority();

   void setPriority(int priority);

   boolean isMuted();

   void setIsMuted(boolean isMuted);
}
