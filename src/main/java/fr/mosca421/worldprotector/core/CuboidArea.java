package fr.mosca421.worldprotector.core;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class CuboidArea implements IMarkableArea {

    // nbt keys
    public static final String MIN_X = "minX";
    public static final String MIN_Y = "minY";
    public static final String MIN_Z = "minZ";
    public static final String MAX_X = "maxX";
    public static final String MAX_Y = "maxY";
    public static final String MAX_Z = "maxZ";

    private final AreaType areaType;
    private AxisAlignedBB area;

    public CuboidArea(AxisAlignedBB area) {
        this();
        this.area = area;
    }

    public CuboidArea(CompoundNBT nbt) {
        this();
        this.deserializeNBT(nbt);
    }

    private CuboidArea() {
        this.areaType = AreaType.CUBOID;
    }

    @Override
    public boolean contains(BlockPos pos) {
        // INFO: this.area.contains(x,y,z); does not work, because the max checks are exclusive by default.
        // TODO: Maybe replace with net.minecraft.util.math.MutableBoundingBox::intersectsWith which has inclusive checks
        return pos.getX() >= area.minX && pos.getX() <= area.maxX
                && pos.getY() >= this.area.minY && pos.getY() <= this.area.maxY
                && pos.getZ() >= this.area.minZ && pos.getZ() <= this.area.maxZ;
    }

    @Override
    public AreaType getAreaType() {
        return areaType;
    }

    public AxisAlignedBB getArea() {
        return area;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
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
        this.area = new AxisAlignedBB(nbt.getInt(MIN_X), nbt.getInt(MIN_Y), nbt.getInt(MIN_Z),
                nbt.getInt(MAX_X), nbt.getInt(MAX_Y), nbt.getInt(MAX_Z));
    }
}
