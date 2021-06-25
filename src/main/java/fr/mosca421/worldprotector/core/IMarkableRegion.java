package fr.mosca421.worldprotector.core;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A markable region extends the general IProtectedRegion by allowing
 * to specify a certain area for the CuboidRegion. The region can be accessed by its name.
 * For now the area can only be marked by using a AxisAlignedBB. This will be
 * abstracted to provide a more general representation in the future.
 * Additionally a markable region has a dimension it is located in.
 * <p>
 * A markable region also can be muted, has a priority to manage overlapping regions
 * and has a teleportation target.
 */
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
