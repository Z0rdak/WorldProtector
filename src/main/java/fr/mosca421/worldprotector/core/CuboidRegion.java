package fr.mosca421.worldprotector.core;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A cuboid regions represents it's area as a simple rectangular cuboid (a BlockBox).
 * The region is marked with two blocks representing the bounding box of the area.
 */
public final class CuboidRegion extends AbstractMarkableRegion {

	// nbt keys
	public static final String MIN_X = "minX";
	public static final String MIN_Y = "minY";
	public static final String MIN_Z = "minZ";
	public static final String MAX_X = "maxX";
	public static final String MAX_Y = "maxY";
	public static final String MAX_Z = "maxZ";

	public CuboidRegion(CompoundNBT nbt) {
		deserializeNBT(nbt);
	}

	public CuboidRegion(String name, AxisAlignedBB area, RegistryKey<World> dimension) {
		super(name, area, dimension);
		this.area = area;
		this.tpTarget = new BlockPos((int) this.area.getCenter().getX(),
				(int) this.area.getCenter().getY(),
				(int) this.area.getCenter().getZ());
	}

	public CuboidRegion(String name, AxisAlignedBB area, BlockPos tpPos, RegistryKey<World> dimension) {
		super(name, area, dimension);
		this.tpTarget = tpPos;
	}

	@Override
	public AxisAlignedBB getArea() {
		return this.area;
	}

	@Override
	public void setArea(AxisAlignedBB area) {
		this.area = area;
	}

	@Override
	public boolean containsPosition(BlockPos position) {
		int x = position.getX();
		int y = position.getY();
		int z = position.getZ();
		// INFO: this.area.contains(x,y,z); does not work, because the max checks are exclusive by default.
		// TODO: Maybe replace with net.minecraft.util.math.MutableBoundingBox::intersectsWith which has inclusive checks
		return x >= this.area.minX && x <= this.area.maxX
				&& y >= this.area.minY && y <= this.area.maxY
				&& z >= this.area.minZ && z <= this.area.maxZ;
	}

	private AxisAlignedBB areaFromNBT(CompoundNBT nbt){
		return new AxisAlignedBB(
				nbt.getInt(MIN_X), nbt.getInt(MIN_Y), nbt.getInt(MIN_Z),
				nbt.getInt(MAX_X), nbt.getInt(MAX_Y), nbt.getInt(MAX_Z)
		);
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		nbt.putInt(MIN_X, (int) this.area.minX);
		nbt.putInt(MIN_Y, (int) this.area.minY);
		nbt.putInt(MIN_Z, (int) this.area.minZ);
		nbt.putInt(MAX_X, (int) this.area.maxX);
		nbt.putInt(MAX_Y, (int) this.area.maxY);
		nbt.putInt(MAX_Z, (int) this.area.maxZ);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		super.deserializeNBT(nbt);
		this.area = areaFromNBT(nbt);
	}

}
