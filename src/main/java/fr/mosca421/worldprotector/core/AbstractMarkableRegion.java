package fr.mosca421.worldprotector.core;

import fr.mosca421.worldprotector.util.RegionNBTConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/**
 * The AbstractMarkableRegion represents a abstract implementation for a markable region.
 * This can be used to implement different types of regions which define their area in a different way.
 */
public abstract class AbstractMarkableRegion extends AbstractRegion implements IMarkableRegion {

    public final static int DEFAULT_PRIORITY = 2;

    protected String name;
    protected int priority;
    protected RegistryKey<World> dimension;
    protected IMarkableArea area;
    protected boolean isMuted;
    protected AreaType areaType;
    protected BlockPos tpTarget;

    public AbstractMarkableRegion(String name, IMarkableArea area, RegistryKey<World> dimension) {
        this();
        this.name = name;
        this.dimension = dimension;
        this.area = area;
        this.areaType = area.getAreaType();
    }

    protected AbstractMarkableRegion() {
        this.priority = DEFAULT_PRIORITY;
        this.isMuted = false;
    }

    public abstract IMarkableArea getArea();

    public abstract void setArea(IMarkableArea area);

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public RegistryKey<World> getDimension() {
        return dimension;
    }

    @Override
    public boolean isMuted() {
        return this.isMuted;
    }

    @Override
    public void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }

    @Override
    public BlockPos getTpTarget() {
        return new BlockPos(this.tpTarget);
    }

    @Override
    public void setTpTarget(BlockPos tpPos) {
        this.tpTarget = tpPos;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putString(RegionNBTConstants.NAME, name);
        nbt.putInt(RegionNBTConstants.TP_X, this.tpTarget.getX());
        nbt.putInt(RegionNBTConstants.TP_Y, this.tpTarget.getY());
        nbt.putInt(RegionNBTConstants.TP_Z, this.tpTarget.getZ());
        nbt.putInt(RegionNBTConstants.PRIORITY, priority);
        nbt.putString(RegionNBTConstants.DIM, dimension.getLocation().toString());
        nbt.putBoolean(RegionNBTConstants.MUTED, isMuted);
        nbt.putString(RegionNBTConstants.AREA_TYPE, this.areaType.toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        this.name = nbt.getString(RegionNBTConstants.NAME);
        this.tpTarget = new BlockPos(nbt.getInt(RegionNBTConstants.TP_X),
                nbt.getInt(RegionNBTConstants.TP_Y),
                nbt.getInt(RegionNBTConstants.TP_Z));
        this.priority = nbt.getInt(RegionNBTConstants.PRIORITY);
        this.dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY,
                new ResourceLocation(nbt.getString(RegionNBTConstants.DIM)));
        this.isMuted = nbt.getBoolean(RegionNBTConstants.MUTED);
        this.areaType = AreaType.valueOf(nbt.getString(RegionNBTConstants.AREA_TYPE));
    }
}
