package fr.mosca421.worldprotector.data;

import fr.mosca421.worldprotector.core.CuboidRegion;
import fr.mosca421.worldprotector.core.DimensionalRegion;
import fr.mosca421.worldprotector.core.IMarkableRegion;
import fr.mosca421.worldprotector.core.RegionFlag;
import fr.mosca421.worldprotector.util.RegionNBTConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.stream.Collectors;

public class DimensionRegionCache extends HashMap<String, IMarkableRegion> implements INBTSerializable<CompoundNBT> {

    public static final String WHITELIST = "whitelist"; // boolean
    public static final String FLAGS = "flags"; // list
    public static final String REGIONS = "regions";  //compound
    public static final String PROTECTORS = "protectors";
    private DimensionalRegion dimensionalRegion;

    public DimensionRegionCache(IMarkableRegion region) {
        this();
        addRegion(region);
    }

    public DimensionRegionCache(DimensionalRegion region) {
        this();
        this.dimensionalRegion = region;
    }

    public DimensionRegionCache() {
        super();
    }

    public DimensionRegionCache(CompoundNBT nbt) {
        this();
        deserializeNBT(nbt);
    }

    /* Dimensional Region methods - Start */

    public boolean addDimFlag(String flag) {
        return this.dimensionalRegion.addFlag(flag);
    }

    public boolean removeDimFlag(String flag) {
        return this.dimensionalRegion.removeFlag(flag);
    }

    public Collection<String> getDimensionFlags() {
        return Collections.unmodifiableCollection(this.dimensionalRegion.getFlags());
    }

    public boolean hasFlagActive(String flag) {
        return this.dimensionalRegion.containsFlag(flag);
    }

    // TODO: ?
    public boolean hasFlagActive(RegionFlag flag) {
        if (this.dimensionalRegion.containsFlag(flag.toString())) {
            return !this.dimensionalRegion.hasWhitelist();
        } else {
            return this.dimensionalRegion.hasWhitelist();
        }
    }

    public boolean hasWhitelist() {
        return this.dimensionalRegion.hasWhitelist();
    }

    public void setHasWhitelist(boolean hasWhitelist) {
        this.dimensionalRegion.setHasWhitelist(hasWhitelist);
    }

    public void setDimActiveState(boolean setActive) {
        this.dimensionalRegion.setIsActive(setActive);
    }

    public boolean isDimActive() {
        return this.dimensionalRegion.isActive();
    }

    public Collection<String> getDimPlayersNames() {
        return this.dimensionalRegion.getPlayers().values();
    }

    public boolean addDimPlayer(PlayerEntity player) {
        return this.dimensionalRegion.addPlayer(player);
    }

    public boolean removeDimPlayer(PlayerEntity player) {
        return this.dimensionalRegion.removePlayer(player);
    }

    public boolean dimPermits(PlayerEntity player) {
        return this.dimensionalRegion.permits(player);
    }


    public boolean removeDimFlag(IMarkableRegion region, String flag) {
        if (this.containsKey(region.getName())) {
            return this.get(region.getName()).removeFlag(flag);
        }
        return false;
    }

    public boolean addDimFlag(IMarkableRegion region, String flag) {
        if (this.containsKey(region.getName())) {
            return this.get(region.getName()).addFlag(flag);
        }
        return false;
    }

    /* Dimensional Region methods - End */

    public boolean isActive(String regionName) {
        if (this.containsKey(regionName)) {
            return getRegion(regionName).isActive();
        }
        return false;
    }

    public boolean setIsActive(String regionName, boolean active) {
        if (this.containsKey(regionName)) {
            getRegion(regionName).setIsActive(active);
            return true;
        }
        return false;
    }

    public boolean setIsMuted(String regionName, boolean isMuted) {
        if (this.containsKey(regionName)) {
            getRegion(regionName).setIsMuted(isMuted);
            return true;
        }
        return false;
    }

    public Collection<IMarkableRegion> getRegions() {
        return Collections.unmodifiableCollection(this.values());
    }

    public Collection<String> getRegionNames() {
        return Collections.unmodifiableCollection(this.keySet());
    }

    public IMarkableRegion removeRegion(String regionName) {
        return this.remove(regionName);
    }

    public void clearRegions() {
        this.clear();
    }

    // TODO: rework to only update area?
    public void updateRegion(IMarkableRegion newRegion) {
        if (this.containsKey(newRegion.getName())) {
            this.put(newRegion.getName(), newRegion);
        }
    }

    public void setActiveStateForRegions(boolean activeState) {
        values().forEach(region -> region.setIsActive(activeState));
    }

    /**
     * Make sure region exists with RegionManager.get().containsRegion() before
     *
     * @param regionName regionName to get corresponding region object for
     * @return region object corresponding to region name
     */
    public IMarkableRegion getRegion(String regionName) {
        return this.get(regionName);
    }

    public void addRegion(IMarkableRegion region) {
        this.put(region.getName(), region);
    }

    /* Flag related methods */
    public Set<String> getFlags(String regionName) {
        if (this.containsKey(regionName)) {
            return this.get(regionName).getFlags();
        }
        return new HashSet<>();
    }

    public List<String> addFlags(String regionName, List<String> flags) {
        if (this.containsKey(regionName)) {
            return flags.stream()
                    .filter(flag -> this.get(regionName).addFlag(flag))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public List<String> removeFlags(String regionName, List<String> flags) {
        if (this.containsKey(regionName)) {
            return flags.stream()
                    .filter(flag -> this.get(regionName).removeFlag(flag))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /* Player related methods */

    public boolean addPlayer(String regionName, PlayerEntity player){
        if (this.containsKey(regionName)) {
            return this.get(regionName).addPlayer(player);
        }
        return false;
    }

    public List<PlayerEntity> addPlayers(String regionName, List<PlayerEntity> players){
        if (this.containsKey(regionName)) {
            return players.stream()
                    .filter(player -> this.get(regionName).addPlayer(player))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    public boolean removePlayer(String regionName, PlayerEntity player){
        if (this.containsKey(regionName)) {
            return this.get(regionName).removePlayer(player);
        }
        return false;
    }

    public List<PlayerEntity> removePlayers(String regionName, List<PlayerEntity> players){
        if (this.containsKey(regionName)) {
            return players.stream()
                    .filter(player -> this.get(regionName).removePlayer(player))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public boolean forbidsPlayer(String regionName, PlayerEntity player){
        if (this.containsKey(regionName)) {
            return this.get(regionName).forbids(player);
        }
        return true;
    }

    public Set<String> getPlayers(String regionName) {
        if (this.containsKey(regionName)) {
            return new HashSet<>(getRegion(regionName).getPlayers().values());
        }
        return new HashSet<>();
    }

    public static CompoundNBT serializeCache(DimensionRegionCache dimensionRegionCache) {
        CompoundNBT dimCache = new CompoundNBT();
        for (Map.Entry<String, IMarkableRegion> regionEntry : dimensionRegionCache.entrySet()) {
            dimCache.put(regionEntry.getKey(), regionEntry.getValue().serializeNBT());
        }
        return dimCache;
    }

    public static DimensionRegionCache deserialize(CompoundNBT nbt) {
        DimensionRegionCache dimCache = new DimensionRegionCache();
        for (String regionKey : nbt.keySet()) {
            CompoundNBT regionNbt = nbt.getCompound(regionKey);
            CuboidRegion region = new CuboidRegion(regionNbt);
            dimCache.addRegion(region);
        }
        return dimCache;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        CompoundNBT regions = new CompoundNBT();
        for (Map.Entry<String, IMarkableRegion> regionEntry : this.entrySet()) {
            regions.put(regionEntry.getKey(), regionEntry.getValue().serializeNBT());
        }
        nbt.put(REGIONS, regions);
        nbt.put(RegionNBTConstants.DIM_REGION, this.dimensionalRegion.serializeNBT());
        return nbt;
    }

    private ListNBT toNBTList(Collection<String> list) {
        ListNBT nbtList = new ListNBT();
        nbtList.addAll(list.stream()
                .map(StringNBT::valueOf)
                .collect(Collectors.toList()));
        return nbtList;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        CompoundNBT dimensionalRegion = nbt.getCompound(RegionNBTConstants.DIM_REGION);
        this.dimensionalRegion = new DimensionalRegion(dimensionalRegion);
        CompoundNBT regions = nbt.getCompound(REGIONS);
        for (String regionKey : regions.keySet()) {
            CompoundNBT regionNbt = regions.getCompound(regionKey);
            CuboidRegion region = new CuboidRegion(regionNbt);
            this.addRegion(region);
        }
    }

    public DimensionalRegion getDimensionalRegion() {
        return this.dimensionalRegion;
    }
}
