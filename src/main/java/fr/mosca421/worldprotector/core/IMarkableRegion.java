package fr.mosca421.worldprotector.core;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A markable region extends the general IProtectedRegion by allowing
 * to specify a certain area for the Region. The region can be accessed by its name.
 * The area of the region is defined by a IMarkableArea instance.
 * Additionally a markable region has a dimension it is located in.
 * <p>
 * A markable region also can be muted, has a priority to manage overlapping regions
 * and has a teleportation target.
 */
public interface IMarkableRegion extends IProtectedRegion {

   String getName();

   RegistryKey<World> getDimension();

   IMarkableArea getArea();

   void setArea(IMarkableArea area);

   boolean containsPosition(BlockPos position);

   BlockPos getTpTarget();

   void setTpTarget(BlockPos pos);

   int getPriority();

   void setPriority(int priority);

   boolean isMuted();

   void setIsMuted(boolean isMuted);
}
