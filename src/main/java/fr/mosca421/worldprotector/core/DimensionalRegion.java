package fr.mosca421.worldprotector.core;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public final class DimensionalRegion extends AbstractRegion {

    private final RegistryKey<World> dimensionKey;

    public DimensionalRegion(RegistryKey<World> dimensionKey) {
        super();
        this.dimensionKey = dimensionKey;
    }
    public DimensionalRegion(String dimensionKey){
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
        // TODO
        return null;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        // TODO
    }
}
