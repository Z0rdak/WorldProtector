package fr.mosca421.worldprotector.core;

import fr.mosca421.worldprotector.util.RegionNBTConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/**
 * The DimensionalRegion represents the only implementation of an Abstract region.
 * It is intended to be used to protect dimensions (vanilla and modded).
 */
public final class DimensionalRegion extends AbstractRegion {

    private RegistryKey<World> dimensionKey;

    public DimensionalRegion(RegistryKey<World> dimensionKey) {
        super();
        this.dimensionKey = dimensionKey;
    }

    public DimensionalRegion(CompoundNBT nbt) {
        super();
        this.deserializeNBT(nbt);
    }

    public DimensionalRegion(String dimensionKey) {
        this(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(dimensionKey)));
    }

    public final static DimensionalRegion OVERWORLD = new DimensionalRegion(World.OVERWORLD);
    public final static DimensionalRegion THE_NETHER = new DimensionalRegion(World.THE_NETHER);
    public final static DimensionalRegion THE_END = new DimensionalRegion(World.THE_END);

    public RegistryKey<World> getDimensionKey() {
        return dimensionKey;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putString(RegionNBTConstants.DIM, this.dimensionKey.getLocation().toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        String dim = nbt.getString(RegionNBTConstants.DIM);
        this.dimensionKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(dim));
    }
}
