package fr.mosca421.worldprotector.data;

import fr.mosca421.worldprotector.core.IMarkableRegion;
import fr.mosca421.worldprotector.core.Region;
import fr.mosca421.worldprotector.core.RegionFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.stream.Collectors;

public class DimensionRegionCache extends HashMap<String, IMarkableRegion> implements INBTSerializable<CompoundNBT> {

    public static final String WHITELIST = "whitelist"; // boolean
    public static final String FLAGS = "flags"; // list
    public static final String REGIONS = "regions";  //compound
    public static final String PROTECTORS = "protectors";
    private final Map<UUID, String> protectors;
    private Collection<String> dimensionFlags;
    private boolean hasWhitelist;

    public DimensionRegionCache(IMarkableRegion region) {
        this();
        addRegion(region);
    }

    public DimensionRegionCache() {
        super();
        this.dimensionFlags = new ArrayList<>(0);
        this.protectors = new HashMap<>(0);
        this.hasWhitelist = true;
    }

    public DimensionRegionCache(CompoundNBT nbt) {
        this();
        deserializeNBT(nbt);
    }

    public boolean addFlag(String flag) {
        return this.dimensionFlags.add(flag);
    }

    public boolean removeFlag(String flag) {
        return this.dimensionFlags.remove(flag);
    }

    public Collection<String> getDimensionFlags() {
        return Collections.unmodifiableCollection(this.dimensionFlags);
    }

    public boolean hasFlagActive(String flag) {
        return this.dimensionFlags.contains(flag);
    }

    public boolean hasFlagActive(RegionFlag flag) {
        if (this.dimensionFlags.contains(flag.toString())) {
            return !this.hasWhitelist;
        } else {
            return this.hasWhitelist;
        }
    }

    public boolean hasWhitelist() {
        return this.hasWhitelist;
    }

    public void setHasWhitelist(boolean hasWhitelist) {
        this.hasWhitelist = hasWhitelist;
    }

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

    public boolean removeFlag(IMarkableRegion region, String flag) {
        if (this.containsKey(region.getName())) {
            return this.get(region.getName()).removeFlag(flag);
        }
        return false;
    }

    public boolean addFlag(IMarkableRegion region, String flag) {
        if (this.containsKey(region.getName())) {
            return this.get(region.getName()).addFlag(flag);
        }
        return false;
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
            Region region = new Region(regionNbt);
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
        nbt.put(FLAGS, toNBTList(this.dimensionFlags));
        nbt.put(PROTECTORS, toNBTList(this.dimensionFlags));
        nbt.putBoolean(WHITELIST, this.hasWhitelist);
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
        CompoundNBT regions = nbt.getCompound(REGIONS);
        for (String regionKey : regions.keySet()) {
            CompoundNBT regionNbt = regions.getCompound(regionKey);
            Region region = new Region(regionNbt);
            this.addRegion(region);
        }

        this.dimensionFlags.clear();
        ListNBT flagsNBT = nbt.getList(FLAGS, Constants.NBT.TAG_STRING);
        if (flagsNBT.size() == 0) {
            this.dimensionFlags = new ArrayList<>(0);
        } else {
            for (int i = 0; i < flagsNBT.size(); i++) {
                this.dimensionFlags.add(flagsNBT.getString(i));
            }
        }

        this.protectors.clear();
        // TODO
        /*
        ListNBT protectorsNBT = nbt.getList(PROTECTORS, Constants.NBT.TAG_STRING);
        if (flagsNBT.size() == 0) {
            this.protectors = new ArrayList<>(0);
        } else {
            for (int i = 0; i < protectorsNBT.size(); i++) {
                this.protectors.add(protectorsNBT.getString(i));
            }
        }

         */

        this.hasWhitelist = nbt.getBoolean(WHITELIST);
    }
}
