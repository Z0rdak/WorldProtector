package fr.mosca421.worldprotector.event;

import fr.mosca421.worldprotector.WorldProtector;
import fr.mosca421.worldprotector.core.IMarkableRegion;
import fr.mosca421.worldprotector.core.RegionFlag;
import fr.mosca421.worldprotector.util.MessageUtils;
import fr.mosca421.worldprotector.util.RegionUtils;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.ContainerMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = WorldProtector.MODID)
public class EventInteract {

	private EventInteract(){}

	@SubscribeEvent
	public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(event.getPos(), event.getWorld());
		if (!event.getWorld().isRemote) {
			for (IMarkableRegion region : regions) {
				PlayerEntity player = event.getPlayer();
				TileEntity targetEntity = event.getWorld().getTileEntity(event.getPos());
				boolean isLockableTileEntity = targetEntity instanceof LockableTileEntity;
				boolean isEnderChest = targetEntity instanceof EnderChestTileEntity;
				boolean isContainer = targetEntity instanceof LecternTileEntity || isLockableTileEntity;
				boolean isPlayerProhibited = region.forbids(player);

				BlockRayTraceResult pos = event.getHitVec();
				if (pos != null && pos.getType() == RayTraceResult.Type.BLOCK) {
					BlockPos bPos = pos.getPos();
					Block target = event.getWorld().getBlockState(bPos).getBlock();
					boolean isUsableBlock = target instanceof AbstractButtonBlock ||
							target instanceof DoorBlock ||
							target instanceof TrapDoorBlock ||
							target instanceof LeverBlock ||
							target instanceof NoteBlock ||
							target instanceof FenceGateBlock ||
							target instanceof DaylightDetectorBlock ||
							target instanceof RedstoneDiodeBlock ||
							target instanceof LecternBlock ||
							target instanceof BeaconBlock ||
							target instanceof BrewingStandBlock;

					if (region.containsFlag(RegionFlag.USE) && isPlayerProhibited && isUsableBlock) {
						if (!player.isSneaking()) {
							event.setCanceled(true);
							if (!region.isMuted()) {
								MessageUtils.sendStatusMessage(player, "message.event.interact.use");
							}
							return;
						}
					}
				}
				// TODO: player can still activate pressure plates, trip wires and observers (by placing blocks: prohibit this with place flag)

				// check for ender chest access
				if (region.containsFlag(RegionFlag.ENDER_CHEST_ACCESS) && isEnderChest && isPlayerProhibited) {
					if (!player.isSneaking()) {
						event.setCanceled(true);
						if (!region.isMuted()) {
							MessageUtils.sendStatusMessage(player, "message.event.interact.access_ender_chest");
						}
						return;
					}
				}
				// check for container access
				if (region.containsFlag(RegionFlag.CONTAINER_ACCESS) && isContainer && isPlayerProhibited) {
					if (!player.isSneaking()) {
						event.setCanceled(true);
						if (!region.isMuted()) {
							MessageUtils.sendStatusMessage(player, "message.event.interact.access_container");
						}
						return;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
		List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(event.getPos(), event.getWorld());
		if (!event.getWorld().isRemote) {
			for (IMarkableRegion region : regions) {
				PlayerEntity player = event.getPlayer();
				boolean containsChestAccess = region.containsFlag(RegionFlag.CONTAINER_ACCESS);
				boolean playerHasPermission = region.permits(player);
				boolean isMinecartContainer = event.getTarget() instanceof ContainerMinecartEntity;

				if (containsChestAccess && !playerHasPermission && isMinecartContainer) {
					event.setCanceled(true);
					if (!region.isMuted()) {
						MessageUtils.sendStatusMessage(player, "message.event.interact.access_container");
					}
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public static void onSteppedOnActivator(BlockEvent.NeighborNotifyEvent event) {
		Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
		BlockPos pos = event.getPos();
		boolean cancelEvent = false;
		// tripwire does not work yet
		if (block instanceof AbstractPressurePlateBlock
			/*	|| block instanceof TripWireHookBlock
				|| block instanceof TripWireBlock*/) {
			// TODO: check for tripwire blocks in a row and surpress updates
			List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(pos, (World) event.getWorld());
			for (IMarkableRegion region : regions) {
				if (region.containsFlag(RegionFlag.USE)) {
					AxisAlignedBB areaAbovePressurePlate = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
					List<PlayerEntity> players = ((World) event.getWorld()).getEntitiesWithinAABB(EntityType.PLAYER, areaAbovePressurePlate, (player) -> true);
					for (PlayerEntity player : players) {
						cancelEvent = cancelEvent || region.forbids(player);
						if (cancelEvent && !region.isMuted()) {
							MessageUtils.sendStatusMessage(player, "message.event.interact.use");
						}
						event.setCanceled(cancelEvent);
					}
				}
			}
		}
	}
}
