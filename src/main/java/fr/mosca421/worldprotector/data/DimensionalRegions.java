package fr.mosca421.worldprotector.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Collection;

/**
 * Utility class to manage dimensional regions of DimensionalRegionCaches accessed through the RegionManager.
 */
public class DimensionalRegions {

    public static Collection<String> getDimFlags(String dimKey) {
        return getDimFlags(keyToDim(dimKey));
    }

    public static Collection<String> getDimFlags(RegistryKey<World> dim) {
        return RegionManager.get().getDimFlags(dim);
    }

    public static Collection<String> getDimPlayers(String dimKey) {
        return getDimPlayers(keyToDim(dimKey));
    }

    public static Collection<String> getDimPlayers(RegistryKey<World> dim) {
        return RegionManager.get().getDimPlayers(dim);
    }

    public static boolean doesDimPermit(String dimKey, PlayerEntity player) {
        return doesDimPermit(keyToDim(dimKey), player);
    }

    public static boolean doesDimPermit(RegistryKey<World> dim, PlayerEntity player) {
        return RegionManager.get().doesDimPermit(dim, player);
    }

    public static boolean doesDimContain(String dimKey, String flag) {
        return doesDimContain(keyToDim(dimKey), flag);
    }

    public static boolean doesDimContain(RegistryKey<World> dim, String flag) {
        return RegionManager.get().doesDimContain(dim, flag);
    }

    public static boolean hasDimWhitelist(String dimKey) {
        return hasDimWhitelist(keyToDim(dimKey));
    }

    public static boolean hasDimWhitelist(RegistryKey<World> dim) {
        return RegionManager.get().hasDimWhitelist(dim);
    }

    public static boolean isDimRegionActive(String dimKey) {
        return isDimRegionActive(keyToDim(dimKey));
    }

    public static boolean isDimRegionActive(RegistryKey<World> dim) {
        return RegionManager.get().isDimRegionActive(dim);
    }

    public static void setDimActiveState(String dimKey, boolean setActive) {
        setDimActiveState(keyToDim(dimKey), setActive);
    }

    public static void setDimActiveState(RegistryKey<World> dim, boolean setActive) {
        RegionManager.get().setDimActiveState(dim, setActive);
    }

    public static void setDimHasWhitelist(String dimKey, boolean setActive) {
        setDimHasWhitelist(keyToDim(dimKey), setActive);
    }

    public static void setDimHasWhitelist(RegistryKey<World> dim, boolean setActive) {
        RegionManager.get().setDimHasWhitelist(dim, setActive);
    }

    public static boolean addDimFlag(String dimKey, String flag) {
        return addDimFlag(keyToDim(dimKey), flag);
    }

    public static boolean addDimFlag(RegistryKey<World> dim, String flag) {
        return RegionManager.get().addDimFlag(dim, flag);
    }

    public static boolean removeDimFlag(String dimKey, String flag) {
        return removeDimFlag(keyToDim(dimKey), flag);
    }

    public static boolean removeDimFlag(RegistryKey<World> dim, String flag) {
        return RegionManager.get().removeDimFlag(dim, flag);
    }

    public static boolean removeDimPlayer(String dimKey, PlayerEntity playerEntity) {
        return removeDimPlayer(keyToDim(dimKey), playerEntity);
    }

    public static boolean removeDimPlayer(RegistryKey<World> dim, PlayerEntity playerEntity) {
        return RegionManager.get().removeDimPlayer(dim, playerEntity);
    }

    public static boolean addDimPlayer(String dimKey, PlayerEntity playerEntity) {
        return addDimPlayer(keyToDim(dimKey), playerEntity);
    }

    public static boolean addDimPlayer(RegistryKey<World> dim, PlayerEntity playerEntity) {
        return RegionManager.get().addDimPlayer(dim, playerEntity);
    }

    private static RegistryKey<World> keyToDim(String dimKey) {
        return RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(dimKey));
    }
}
