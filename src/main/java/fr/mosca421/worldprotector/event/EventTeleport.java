package fr.mosca421.worldprotector.event;

import fr.mosca421.worldprotector.WorldProtector;
import fr.mosca421.worldprotector.core.IMarkableRegion;
import fr.mosca421.worldprotector.core.RegionFlag;
import fr.mosca421.worldprotector.util.MessageUtils;
import fr.mosca421.worldprotector.util.RegionUtils;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = WorldProtector.MODID)
public class EventTeleport {

    private EventTeleport() {
    }

    @SubscribeEvent
    public static void onEnderTeleportTo(EntityTeleportEvent event) {
        World world = event.getEntity().world;
        if (!world.isRemote()) {
            BlockPos teleportTarget = new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ());
            BlockPos teleportSource = event.getEntity().getPosition();
            List<IMarkableRegion> regionsTo = RegionUtils.getHandlingRegionsFor(teleportTarget, world);
            List<IMarkableRegion> regionsFrom = RegionUtils.getHandlingRegionsFor(teleportSource, world);
            // handle player teleportation using ender pearls
            if (event instanceof EntityTeleportEvent.EnderPearl) {
                EntityTeleportEvent.EnderPearl enderPearlEvent = (EntityTeleportEvent.EnderPearl) event;
                handlePlayerEnderPearlUse(enderPearlEvent, regionsFrom, regionsTo);
            }
            if (event instanceof EntityTeleportEvent.EnderEntity) {
                EntityTeleportEvent.EnderEntity enderEntityEvent = (EntityTeleportEvent.EnderEntity) event;
                // handle enderman teleportation
                if (enderEntityEvent.getEntityLiving() instanceof EndermanEntity) {
                    handleEndermanTp(enderEntityEvent, regionsFrom, regionsTo);
                }
                // handle shulker teleportation
                if (enderEntityEvent.getEntityLiving() instanceof ShulkerEntity) {
                    handleShulkerTp(enderEntityEvent, regionsFrom, regionsTo);
                }
            }
        }
    }

    private static void handlePlayerEnderPearlUse(EntityTeleportEvent.EnderPearl event, List<IMarkableRegion> regionsFrom, List<IMarkableRegion> regionsTo) {
        PlayerEntity player = event.getPlayer();
        for (IMarkableRegion region : regionsFrom) {
            if (region.containsFlag(RegionFlag.USE_ENDERPEARL_FROM_REGION) && region.forbids(player)) {
                event.setCanceled(true);
                if (!region.isMuted()) {
                    MessageUtils.sendStatusMessage(player, "message.event.teleport.ender_pearl.from_region");
                }
                // refund pearl
                int count = player.getHeldItem(player.getActiveHand()).getCount();
                player.getHeldItem(player.getActiveHand()).setCount(count + 1);
                return;
            }
        }
        for (IMarkableRegion region : regionsFrom) {
            if (region.containsFlag(RegionFlag.USE_ENDERPEARL_TO_REGION) && region.forbids(player)) {
                event.setCanceled(true);
                if (!region.isMuted()) {
                    MessageUtils.sendStatusMessage(player, "message.event.teleport.ender_pearl.to_region");
                }
                // refund pearl
                int count = player.getHeldItem(player.getActiveHand()).getCount();
                player.getHeldItem(player.getActiveHand()).setCount(count + 1);
                return;
            }
        }

    }

    private static void handleShulkerTp(EntityTeleportEvent.EnderEntity event, List<IMarkableRegion> regionsFrom, List<IMarkableRegion> regionsTo) {
        boolean shulkerTpFromProhibited = regionsFrom.stream()
                .anyMatch(region -> region.containsFlag(RegionFlag.SHULKER_TELEPORT_FROM_REGION));
        if (shulkerTpFromProhibited) {
            event.setCanceled(true);
        }
        boolean shulkerTpToProhibited = regionsTo.stream()
                .anyMatch(region -> region.containsFlag(RegionFlag.SHULKER_TELEPORT_TO_REGION));
        if (shulkerTpToProhibited) {
            event.setCanceled(true);
        }
    }

    private static void handleEndermanTp(EntityTeleportEvent.EnderEntity event, List<IMarkableRegion> regionsFrom, List<IMarkableRegion> regionsTo) {
        boolean endermanTpFromProhibited = regionsFrom.stream()
                .anyMatch(region -> region.containsFlag(RegionFlag.ENDERMAN_TELEPORT_FROM_REGION));
        if (endermanTpFromProhibited) {
            event.setCanceled(true);
        }
        boolean endermanTpToProhibited = regionsTo.stream()
                .anyMatch(region -> region.containsFlag(RegionFlag.ENDERMAN_TELEPORT_TO_REGION));
        if (endermanTpToProhibited) {
            event.setCanceled(true);
        }
    }
}
