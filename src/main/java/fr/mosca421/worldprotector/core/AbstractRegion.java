package fr.mosca421.worldprotector.core;

import fr.mosca421.worldprotector.util.RegionNBTConstants;
import fr.mosca421.worldprotector.util.RegionPlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

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
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean(RegionNBTConstants.WHITELIST, this.hasWhitelist);
        nbt.putBoolean(RegionNBTConstants.ACTIVE, isActive);
        // serialize flag data
        ListNBT flagsNBT = new ListNBT();
        flagsNBT.addAll(flags.stream()
                .map(StringNBT::valueOf)
                .collect(Collectors.toSet()));
        nbt.put(RegionNBTConstants.FLAGS, flagsNBT);
        // serialize player data
        ListNBT playerList = nbt.getList(RegionNBTConstants.PLAYERS, Constants.NBT.TAG_COMPOUND);
        players.forEach((uuid, name) -> {
            CompoundNBT playerNBT = new CompoundNBT();
            playerNBT.putUniqueId(RegionNBTConstants.UUID, uuid);
            playerNBT.putString(RegionNBTConstants.NAME, name);
            playerList.add(playerNBT);
        });
        nbt.put(RegionNBTConstants.PLAYERS, playerList);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.isActive = nbt.getBoolean(RegionNBTConstants.ACTIVE);
        this.hasWhitelist = nbt.getBoolean(RegionNBTConstants.WHITELIST);
        // deserialize flag data
        this.flags.clear();
        ListNBT flagsList = nbt.getList(RegionNBTConstants.FLAGS, Constants.NBT.TAG_STRING);
        for (int i = 0; i < flagsList.size(); i++) {
            flags.add(flagsList.getString(i));
        }
        // deserialize player data
        this.players.clear();
        ListNBT playerLists = nbt.getList(RegionNBTConstants.PLAYERS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < playerLists.size(); i++) {
            CompoundNBT playerMapping = playerLists.getCompound(i);
            players.put(playerMapping.getUniqueId(RegionNBTConstants.UUID),
                    playerMapping.getString(RegionNBTConstants.NAME));
        }
    }
}
