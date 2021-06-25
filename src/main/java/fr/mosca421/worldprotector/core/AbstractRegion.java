package fr.mosca421.worldprotector.core;

import fr.mosca421.worldprotector.util.RegionPlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.*;

/**
 * A abstract region represents the basic implementation of a IProtectedRegion.
 * This abstraction can be used for markable regions as well as regions without
 * a area (dimensions).
 */
public abstract class AbstractRegion implements IProtectedRegion {

    protected Set<String> flags;
    protected Map<UUID, String> players;
    protected boolean hasWhitelist;
    protected boolean isActive;

    public AbstractRegion() {
        this.flags = new HashSet<>(0);
        this.players = new HashMap<>(0);
        this.hasWhitelist = false;
        this.isActive = true;
    }

    @Override
    public boolean addFlag(String flag) {
        return this.flags.add(flag);
    }

    @Override
    public boolean removeFlag(String flag) {
        return this.flags.remove(flag);
    }

    @Override
    public boolean containsFlag(String flag) {
        return this.flags.contains(flag);
    }

    @Override
    public boolean containsFlag(RegionFlag flag) {
        return this.containsFlag(flag.toString());
    }

    @Override
    public Set<String> getFlags() {
        return Collections.unmodifiableSet(this.flags);
    }

    @Override
    public boolean addPlayer(PlayerEntity player) {
        if (this.players.containsKey(player.getUniqueID())) {
            return false;
        } else {
            this.players.put(player.getUniqueID(), player.getDisplayName().getString());
            return true;
        }
    }

    @Override
    public boolean removePlayer(PlayerEntity player) {
        return this.players.remove(player.getUniqueID()) != null;
    }

    @Override
    public boolean hasWhitelist() {
        return this.hasWhitelist;
    }

    @Override
    public void setHasWhitelist(boolean hasWhitelist){
        this.hasWhitelist = hasWhitelist;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public Map<UUID, String> getPlayers() {
        return Collections.unmodifiableMap(this.players);
    }

    /**
     * Checks if the player is defined in the regions player list OR whether the player is an operator.
     * Usually this check is needed when an event occurs and it needs to be checked whether
     * the player has a specific permission to perform an action in the region.
     *
     * @param player to be checked
     * @return true if player is in region list or is an operator, false otherwise
     */
    @Override
    public boolean permits(PlayerEntity player) {
        if (RegionPlayerUtils.isOp(player)) {
            return true;
        }
        return players.containsKey(player.getUniqueID());
    }

    @Override
    public boolean forbids(PlayerEntity player) {
        return !this.permits(player);
    }

    @Override
    public abstract CompoundNBT serializeNBT();

    @Override
    public abstract void deserializeNBT(CompoundNBT nbt);
}
