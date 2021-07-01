package fr.mosca421.worldprotector.data;

import fr.mosca421.worldprotector.WorldProtector;
import fr.mosca421.worldprotector.api.event.RegionEvent;
import fr.mosca421.worldprotector.core.DimensionalRegion;
import fr.mosca421.worldprotector.core.IMarkableRegion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.*;
import java.util.stream.Collectors;

public class RegionManager extends WorldSavedData {

    public static final String TAG_REGIONS = "regions";
    private static final String DATA_NAME = WorldProtector.MODID;
    /**
     * Dimension -> { RegionName -> IMarkableRegion }
     */
    private static final Map<RegistryKey<World>, DimensionRegionCache> regionMap = new HashMap<>();
    // Data instance
    private static RegionManager clientRegionCopy = new RegionManager();

    static {
        regionMap.put(World.OVERWORLD, new DimensionRegionCache(DimensionalRegion.OVERWORLD));
        regionMap.put(World.THE_NETHER, new DimensionRegionCache(DimensionalRegion.THE_NETHER));
        regionMap.put(World.THE_END, new DimensionRegionCache(DimensionalRegion.THE_END));
    }

    private RegionManager() {
        super(DATA_NAME);
    }

    public static RegionManager get() {
        if (clientRegionCopy == null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                ServerWorld overworld = server.getWorld(World.OVERWORLD);
                if (overworld != null && !overworld.isRemote) {
                    DimensionSavedDataManager storage = overworld.getSavedData();
                    clientRegionCopy = storage.getOrCreate(RegionManager::new, DATA_NAME);
                }

            }
        }
        return clientRegionCopy;
    }

    public static void onServerStarting(FMLServerStartingEvent event) {
        try {
            ServerWorld world = Objects.requireNonNull(event.getServer().getWorld(World.OVERWORLD));
            if (!world.isRemote) {
                DimensionSavedDataManager storage = world.getSavedData();
                RegionManager data = storage.getOrCreate(RegionManager::new, DATA_NAME);
                storage.set(data);
                clientRegionCopy = data;
                WorldProtector.LOGGER.info("Loaded " + data.getAllRegionNames().size() + " regions for " + data.getDimensionList().size() + " different dimensions");
            }
        } catch (NullPointerException npe) {
            WorldProtector.LOGGER.error("Loading dimension regions failed");
        }
    }


    /**
     * Reads region compound nbt and puts it back into the regionMap
     *
     * @param nbt
     */
    @Override
    public void read(CompoundNBT nbt) {
        clearRegions();
        CompoundNBT dimensionRegions = nbt.getCompound(TAG_REGIONS);
        for (String dimKey : dimensionRegions.keySet()) {
            CompoundNBT dimRegionMap = dimensionRegions.getCompound(dimKey);
            DimensionRegionCache dimCache = new DimensionRegionCache(dimRegionMap);
            RegistryKey<World> dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(dimKey));
            regionMap.put(dimension, dimCache);
        }
    }

    /**
     * Writes the content (regions) of the regionMap to a compound nbt object
     *
     * @param compound
     * @return
     */
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        CompoundNBT dimRegionNbtData = new CompoundNBT();
        for (Map.Entry<RegistryKey<World>, DimensionRegionCache> entry : regionMap.entrySet()) {
            String dim = entry.getKey().getLocation().toString();
            CompoundNBT dimCompound = entry.getValue().serializeNBT();
            dimRegionNbtData.put(dim, dimCompound);
        }
        compound.put(TAG_REGIONS, dimRegionNbtData);
        // TODO: version string
        return compound;
    }

    public Optional<DimensionRegionCache> getRegionsForDim(RegistryKey<World> dim) {
        if (regionMap.containsKey(dim)) {
            return Optional.of(regionMap.get(dim));
        }
        return Optional.empty();
    }

    private Optional<RegistryKey<World>> getDimensionOfRegion(String regionName) {
        return regionMap.entrySet().stream()
                .filter(entry -> entry.getValue().containsKey(regionName))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public Optional<IMarkableRegion> getRegionInDim(RegistryKey<World> dim, String regionName) {
        if (regionMap.containsKey(dim) && regionMap.get(dim).containsKey(regionName)) {
            return Optional.of(regionMap.get(dim).get(regionName));
        }
        return Optional.empty();
    }

    public void addRegionToDim(IMarkableRegion region) {
        RegistryKey<World> dim = region.getDimension();
        if (regionMap.containsKey(dim)) {
            regionMap.get(dim).put(region.getName(), region);
        } else {
            DimensionRegionCache initMapForDim = new DimensionRegionCache(region);
            regionMap.put(dim, initMapForDim);
        }
        markDirty();
    }

    public boolean isActive(String regionName) {
        Optional<DimensionRegionCache> maybeCache = getRegionDimCacheForRegion(regionName);
        if (maybeCache.isPresent()) {
            DimensionRegionCache cache = maybeCache.get();
            return cache.isActive(regionName);
        }
        return false;
    }

    public boolean setActiveState(String regionName, boolean active) {
        Optional<IMarkableRegion> maybeRegion = getRegion(regionName);
        if (maybeRegion.isPresent()) {
            IMarkableRegion region = maybeRegion.get();
            boolean wasUpdated = regionMap.get(region.getDimension()).setIsActive(regionName, active);
            if (wasUpdated) {
                markDirty();
            }
            return wasUpdated;
        } else {
            return false;
        }
    }

    public boolean setMutedState(String regionName, boolean isMuted) {
        Optional<IMarkableRegion> maybeRegion = getRegion(regionName);
        if (maybeRegion.isPresent()) {
            IMarkableRegion region = maybeRegion.get();
            boolean wasUpdated = regionMap.get(region.getDimension()).setIsMuted(regionName, isMuted);
            if (wasUpdated) {
                markDirty();
            }
            return wasUpdated;
        } else {
            return false;
        }
    }

    public Collection<IMarkableRegion> getAllRegionsFor(RegistryKey<World> dim) {
        if (regionMap.containsKey(dim)) {
            return regionMap.get(dim).getRegions();
        }
        return new ArrayList<>(0);
    }

    public Collection<IMarkableRegion> getAllRegions() {
        return regionMap.values().stream()
                .flatMap(regionCache -> regionCache.getRegions().stream())
                .collect(Collectors.toList());
    }

    public Collection<IMarkableRegion> getAllRegionsSorted() {
        return regionMap.values().stream()
                .flatMap(regionCache -> regionCache.getRegions()
                        .stream()
                        .sorted(Comparator.comparing(IMarkableRegion::getName)))
                .collect(Collectors.toList());
    }

    public Collection<String> getAllRegionNames() {
        return regionMap.values().stream()
                .flatMap(regionCache -> regionCache.getRegionNames().stream())
                .collect(Collectors.toList());
    }

    public Collection<String> getRegionNames(RegistryKey<World> dim) {
        if (regionMap.containsKey(dim)) {
            return regionMap.get(dim).getRegionNames();
        }
        return Collections.emptySet();
    }

    public Collection<IMarkableRegion> getRegions(RegistryKey<World> dim) {
        if (regionMap.containsKey(dim)) {
            return regionMap.get(dim).getRegions();
        }
        return Collections.emptySet();
    }

    public boolean removeRegion(String regionName, RegistryKey<World> dim) {
        if (regionMap.containsKey(dim)) {
            IMarkableRegion removed = regionMap.get(dim).remove(regionName);
            markDirty();
            return removed != null;
        }
        return false;
    }

    public Collection<String> getDimensionList() {
        return regionMap.keySet().stream()
                .map(entry -> entry.getLocation().toString())
                .collect(Collectors.toList());
    }

    public Collection<RegistryKey<World>> getDimensions() {
        return regionMap.keySet();
    }

    public void clearRegions() {
        regionMap.forEach((dim, cache) -> cache.clearRegions());
        markDirty();
    }

    private void removeRegion(IMarkableRegion region) {
        if (containsDimensionFor(region)) {
            regionMap.get(region.getDimension()).remove(region.getName());
            markDirty();
        }
    }

    public IMarkableRegion removeRegion(String regionName, PlayerEntity player) {
        Optional<IMarkableRegion> maybeRegion = getRegion(regionName);
        if (maybeRegion.isPresent()) {
            IMarkableRegion region = maybeRegion.get();
            IMarkableRegion removed = regionMap.get(region.getDimension()).removeRegion(regionName);
            markDirty();
            MinecraftForge.EVENT_BUS.post(new RegionEvent.RemoveRegionEvent(region, player));
            return removed;
        } else {
            return null;
        }
    }

    public Optional<IMarkableRegion> getRegion(String regionName) {
        return regionMap.values().stream()
                .filter(regionCache -> regionCache.containsKey(regionName)) // one remaining
                .map(regionCache -> regionCache.getRegion(regionName))
                .findFirst();
    }

    public Optional<DimensionRegionCache> getRegionDimCacheForRegion(String regionName) {
        return regionMap.values().stream()
                .filter(regionCache -> regionCache.containsKey(regionName))
                .findFirst();
    }

    public DimensionRegionCache getRegionDimCache(RegistryKey<World> dim) {
        if (regionMap.containsKey(dim)) {
            return regionMap.get(dim);
        }
        return null;
    }

    public boolean containsRegion(String regionName, RegistryKey<World> dim) {
        if (regionMap.containsKey(dim)) {
            return regionMap.get(dim).containsKey(regionName);
        }
        return false;
    }

    public boolean containsRegion(String regionName) {
        return regionMap.values().stream()
                .anyMatch(regionCache -> regionCache.containsKey(regionName));
    }

    public void setActiveStateForRegionsInDim(RegistryKey<World> dim, boolean activeState) {
        if (regionMap.containsKey(dim)) {
            regionMap.get(dim).setActiveStateForRegions(activeState);
            markDirty();
        }
    }

    /**
     * Prefer this getRegion
     *
     * @param regionName
     * @param dim
     * @return
     */
    public Optional<IMarkableRegion> getRegion(String regionName, RegistryKey<World> dim) {
        if (regionMap.containsKey(dim) && regionMap.get(dim).containsKey(regionName)) {
            return Optional.of(regionMap.get(dim).getRegion(regionName));
        }
        return Optional.empty();
    }

    public boolean containsDimensionFor(IMarkableRegion region) {
        return regionMap.containsKey(region.getDimension());
    }

    public Set<String> getRegionFlags(String regionName, RegistryKey<World> dim) {
        if (regionMap.containsKey(dim)) {
            return regionMap.get(dim).getFlags(regionName);
        }
        return new HashSet<>();
    }

    public void updateRegion(IMarkableRegion newRegion, PlayerEntity player) {
        RegistryKey<World> dim = newRegion.getDimension();
        if (regionMap.containsKey(dim)) {
            regionMap.get(dim).updateRegion(newRegion);
            MinecraftForge.EVENT_BUS.post(new RegionEvent.UpdateRegionEvent(newRegion, player));
            markDirty();
        }
    }

    private DimensionRegionCache getCache(IMarkableRegion region) {
        return regionMap.get(region.getDimension());
    }

    public boolean removeFlag(IMarkableRegion region, String flag) {
        if (containsDimensionFor(region)) {
            boolean wasRemoved = getCache(region).removeDimFlag(region, flag);
            if (wasRemoved) {
                markDirty();
            }
            return wasRemoved;
        }
        return false;
    }

    public List<String> removeFlags(String regionName, List<String> flags) {
        Optional<DimensionRegionCache> maybeCache = getRegionDimCacheForRegion(regionName);
        if (maybeCache.isPresent()) {
            List<String> removed = maybeCache.get().removeFlags(regionName, flags);
            markDirty();
            return removed;
        } else {
            return new ArrayList<>();
        }
    }

    public List<String> addFlags(String regionName, List<String> flags) {
        Optional<DimensionRegionCache> maybeCache = getRegionDimCacheForRegion(regionName);
        if (maybeCache.isPresent()) {
            List<String> added = maybeCache.get().addFlags(regionName, flags);
            markDirty();
            return added;
        } else {
            return new ArrayList<>();
        }
    }

    /* Player related methods */

    public boolean addFlag(IMarkableRegion region, String flag) {
        if (containsDimensionFor(region)) {
            boolean wasAdded = getCache(region).addDimFlag(region, flag);
            if (wasAdded) {
                markDirty();
            }
            return wasAdded;
        }
        return false;
    }

    public Set<String> getRegionPlayers(String regionName, RegistryKey<World> dim) {
        if (regionMap.containsKey(dim) && regionMap.get(dim).containsKey(regionName)) {
            return regionMap.get(dim).getPlayers(regionName);
        }
        return new HashSet<>();
    }

    public boolean addPlayer(String regionName, PlayerEntity player) {
        Optional<DimensionRegionCache> maybeCache = getRegionDimCacheForRegion(regionName);
        if (maybeCache.isPresent()) {
            boolean wasAdded = maybeCache.get().addPlayer(regionName, player);
            if (wasAdded) {
                markDirty();
            }
            return wasAdded;
        }
        return false;
    }

    public List<PlayerEntity> addPlayers(String regionName, List<PlayerEntity> playersToAdd) {
        Optional<DimensionRegionCache> maybeCache = getRegionDimCacheForRegion(regionName);
        if (maybeCache.isPresent()) {
            List<PlayerEntity> added = maybeCache.get().addPlayers(regionName, playersToAdd);
            markDirty();
            return added;
        } else {
            return new ArrayList<>();
        }
    }

    public boolean removePlayer(String regionName, PlayerEntity player) {
        Optional<DimensionRegionCache> maybeCache = getRegionDimCacheForRegion(regionName);
        if (maybeCache.isPresent()) {
            boolean wasRemoved = maybeCache.get().removePlayer(regionName, player);
            if (wasRemoved) {
                markDirty();
            }
            return wasRemoved;
        }
        return false;
    }

    public List<PlayerEntity> removePlayers(String regionName, List<PlayerEntity> playersToRemove) {
        Optional<DimensionRegionCache> maybeCache = getRegionDimCacheForRegion(regionName);
        if (maybeCache.isPresent()) {
            List<PlayerEntity> removed = maybeCache.get().removePlayers(regionName, playersToRemove);
            markDirty();
            return removed;
        } else {
            return new ArrayList<>();
        }
    }

    public boolean forbidsPlayer(String regionName, PlayerEntity player) {
        Optional<DimensionRegionCache> maybeCache = getRegionDimCacheForRegion(regionName);
        return maybeCache
                .map(dimensionRegionCache -> dimensionRegionCache.forbidsPlayer(regionName, player))
                .orElse(true);
    }

    // Data

    public Set<String> getRegionPlayers(String regionName) {
        Optional<DimensionRegionCache> maybeCache = getRegionDimCacheForRegion(regionName);
        if (maybeCache.isPresent()) {
            DimensionRegionCache regionCache = maybeCache.get();
            if (regionCache.containsKey(regionName)) {
                return new HashSet<>(regionCache.getRegion(regionName).getPlayers().values());
            }
        }
        return new HashSet<>();
    }

    public void addFlagToDim(RegistryKey<World> dimension, String flag) {
        if (regionMap.containsKey(dimension)) {
            regionMap.get(dimension).addDimFlag(flag);
        }
    }

    public void removeFlagFromDim(RegistryKey<World> dimension, String flag) {
        if (regionMap.containsKey(dimension)) {
            regionMap.get(dimension).removeDimFlag(flag);
        }
    }

    public void setDimHasWhitelist(RegistryKey<World> dimension, boolean setWhitelist) {
        if (regionMap.containsKey(dimension)) {
            regionMap.get(dimension).setHasWhitelist(setWhitelist);
        }
    }

    public void addRegion(IMarkableRegion region, PlayerEntity player) {
        if (regionMap.containsKey(region.getDimension())) {
            regionMap.get(region.getDimension()).addRegion(region);
        } else {
            DimensionRegionCache newCache = new DimensionRegionCache(region);
            regionMap.put(region.getDimension(), newCache);
        }
        MinecraftForge.EVENT_BUS.post(new RegionEvent.CreateRegionEvent(region, player));
        markDirty();
    }
}
