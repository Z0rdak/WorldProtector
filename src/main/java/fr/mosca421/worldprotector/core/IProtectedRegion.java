package fr.mosca421.worldprotector.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IProtectedRegion extends INBTSerializable<CompoundNBT> {

    boolean addFlag(String flag);

    boolean removeFlag(String flag);

    boolean containsFlag(String flag);

    boolean containsFlag(RegionFlag flag);

    Set<String> getFlags();

    boolean addPlayer(PlayerEntity player);

    boolean removePlayer(PlayerEntity player);

    boolean hasWhitelist();

    void setHasWhitelist(boolean hasWhitelist);

    boolean permitsPlayer(PlayerEntity player);

    boolean isActive();

    void setIsActive(boolean isActive);

    Map<UUID, String> getPlayers();

    boolean permits(PlayerEntity player);

    boolean forbids(PlayerEntity player);

}
