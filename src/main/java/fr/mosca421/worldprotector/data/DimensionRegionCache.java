package fr.mosca421.worldprotector.data;

import fr.mosca421.worldprotector.core.Region;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DimensionRegionCache extends HashMap<String, Region> {

    public DimensionRegionCache() {
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

    public Collection<Region> getRegions() {
        return Collections.unmodifiableCollection(this.values());
    }

    public Collection<String> getRegionNames() {
        return Collections.unmodifiableCollection(this.keySet());
    }

    public Region removeRegion(String regionName) {
        return this.remove(regionName);
    }

    public void clearRegions() {
        this.clear();
    }

    // TODO: remork to only update area? see usages
    public void updateRegion(Region newRegion) {
        if (this.containsKey(newRegion.getName())) {
            this.put(newRegion.getName(), newRegion);
        }
    }

    /**
     * Make sure region exists with RegionManager.get().containsRegion() before
     *
     * @param regionName regionName to get corresponding region object for
     * @return region object corresponding to region name
     */
    public Region getRegion(String regionName) {
        return this.get(regionName);
    }

    public void addRegion(Region region) {
        this.put(region.getName(), region);
    }

    /* Flag related methods */
    public Set<String> getFlags(String regionName) {
        if (this.containsKey(regionName)) {
            return this.get(regionName).getFlags();
        }
        return new HashSet<>();
    }

    public boolean removeFlag(Region region, String flag){
        if (this.containsKey(region.getName())) {
            return this.get(region.getName()).removeFlag(flag);
        }
        return false;
    }

    public boolean addFlag(Region region, String flag){
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
            return getRegion(regionName).getPlayers();
        }
        return new HashSet<>();
    }
}
