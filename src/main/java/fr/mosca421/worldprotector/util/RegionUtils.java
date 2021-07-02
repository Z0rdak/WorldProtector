package fr.mosca421.worldprotector.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.mosca421.worldprotector.core.CuboidArea;
import fr.mosca421.worldprotector.core.CuboidRegion;
import fr.mosca421.worldprotector.core.IMarkableRegion;
import fr.mosca421.worldprotector.data.RegionManager;
import fr.mosca421.worldprotector.item.ItemRegionMarker;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static fr.mosca421.worldprotector.util.MessageUtils.*;

public final class RegionUtils {

	private RegionUtils() {
	}

	public static void removeRegion(String regionName, PlayerEntity player) {
		if (RegionManager.get().removeRegion(regionName, player) != null) {
			sendMessage(player, new TranslationTextComponent("message.region.remove", regionName));
		} else {
			sendStatusMessage(player, new TranslationTextComponent("message.region.unknown", regionName));
		}
	}

	public static void removeAllRegions(PlayerEntity player) {
		RegionManager.get().clearRegions();
		sendMessage(player, new TranslationTextComponent("message.region.removeall"));
	}

	public static void createRegion(String regionName, PlayerEntity player, ItemStack item) {
		if (regionName.contains(" ")) {
			sendStatusMessage(player, "message.region.define.error");
			return;
		}
		if (item.getItem() instanceof ItemRegionMarker) {
			if (item.getTag() != null) {
				if (item.getTag().getBoolean(ItemRegionMarker.VALID)) {
					// TODO: area type
					AxisAlignedBB regionArea = getAreaFromNBT(item.getTag());
					BlockPos tpPos = getTpTargetFromNBT(item.getTag());
					CuboidRegion region = new CuboidRegion(regionName, regionArea, tpPos, player.world.getDimensionKey());
					RegionManager.get().addRegion(region, player);
					item.getTag().putBoolean(ItemRegionMarker.VALID, false); // reset flag for consistent command behaviour
					sendMessage(player, new TranslationTextComponent("message.region.define", regionName));
				} else {
					sendStatusMessage(player, "message.item-hand.choose");
				}
			}
		} else {
			sendMessage(player, "message.item-hand.take");
		}
	}

	public static void redefineRegion(String regionName, PlayerEntity player, ItemStack item) {
		if (item.getItem() instanceof ItemRegionMarker) {
			if (item.getTag() != null) {
				if (item.getTag().getBoolean(ItemRegionMarker.VALID)) {
					if (RegionManager.get().containsRegion(regionName)) {
						RegionManager.get().getRegion(regionName).ifPresent(region -> {
							switch (region.getArea().getAreaType()) {
								case CUBOID:
									region.setArea(new CuboidArea(getAreaFromNBT(item.getTag())));
									break;
								// TODO:
								case POLYGON_2D:
									break;
							}
							region.setTpTarget(getTpTargetFromNBT(item.getTag()));
							RegionManager.get().updateRegion(new CuboidRegion(region.serializeNBT()), player);
							item.getTag().putBoolean(ItemRegionMarker.VALID, false); // reset flag for consistent command behaviour
							sendMessage(player, new TranslationTextComponent("message.region.redefine", regionName));
						});

					}
					else {
						sendStatusMessage(player, new TranslationTextComponent("message.region.unknown", regionName));
					}
				} else {
					sendStatusMessage(player, "message.item-hand.choose");
				}
			}
		} else {
			sendStatusMessage(player, "message.item-hand.take");
		}
	}

	public static void setRegionPriority(String regionName, int priority, PlayerEntity player) {
		if (RegionManager.get().containsRegion(regionName)) {
			RegionManager.get().getRegion(regionName).ifPresent(region -> {
				region.setPriority(priority);
				sendMessage(player, new TranslationTextComponent("message.region.setpriority", priority, regionName));
			});
			RegionManager.get().markDirty();
		} else {
			sendStatusMessage(player, "message.region.unknown");
		}
	}

	public static void activate(String regionName, PlayerEntity player) {
		if (RegionManager.get().setActiveState(regionName, true)) {
			sendMessage(player, new TranslationTextComponent( "message.region.activate", regionName));
		} else {
			sendStatusMessage(player, new TranslationTextComponent("message.region.unknown", regionName));
		}
	}

	public static void activateAll(Collection<IMarkableRegion> regions, PlayerEntity player) {
		List<IMarkableRegion> deactiveRegions = regions.stream()
				.filter(region -> !region.isActive())
				.collect(Collectors.toList());
		deactiveRegions.forEach(region -> region.setIsActive(true));
		RegionManager.get().markDirty();
		List<String> activatedRegions = deactiveRegions.stream()
				.map(IMarkableRegion::getName)
				.collect(Collectors.toList());
		String regionString = String.join(", ", activatedRegions);
		if (!activatedRegions.isEmpty()) {
			sendMessage(player, new TranslationTextComponent("message.region.activate.multiple", regionString));
		} else {
			sendStatusMessage(player, "message.region.activate.none");
		}
	}

	public static void deactivate(String regionName, PlayerEntity player) {
		if (RegionManager.get().setActiveState(regionName, false)) {
			sendMessage(player, new TranslationTextComponent( "message.region.deactivate", regionName));
		} else {
			sendStatusMessage(player, new TranslationTextComponent("message.region.unknown", regionName));
		}
	}

	public static void deactivateAll(Collection<IMarkableRegion> regions, PlayerEntity player) {
		List<IMarkableRegion> activeRegions = regions.stream()
				.filter(IMarkableRegion::isActive)
				.collect(Collectors.toList());
		activeRegions.forEach(region -> region.setIsActive(false));
		RegionManager.get().markDirty();
		List<String> deactivatedRegions = activeRegions.stream()
				.map(IMarkableRegion::getName)
				.collect(Collectors.toList());
		String regionString = String.join(", ", deactivatedRegions);
		if (!deactivatedRegions.isEmpty()) {
			sendMessage(player, new TranslationTextComponent("message.region.deactivate.multiple", regionString));
		} else {
			sendStatusMessage(player, "message.region.deactivate.none");
		}
	}

	public static void muteRegion(String regionName, PlayerEntity player) {
		if (RegionManager.get().setMutedState(regionName, true)) {
			sendMessage(player, new TranslationTextComponent("message.region.mute", regionName));
		} else {
			sendStatusMessage(player, new TranslationTextComponent("message.region.unknown", regionName));
		}
	}

	public static void unmuteRegion(String regionName, PlayerEntity player) {
		if (RegionManager.get().setMutedState(regionName, false)) {
			sendMessage(player, new TranslationTextComponent("message.region.unmute", regionName));
		} else {
			sendStatusMessage(player, new TranslationTextComponent("message.region.unknown", regionName));
		}
	}

	public static Collection<String> getRegionsAroundPlayer(PlayerEntity player) {
		BlockPos playerPos = player.getPosition();
		RegistryKey<World> dim = player.world.getDimensionKey();
		return RegionManager.get().getRegions(dim).stream()
				.filter(region -> region.containsPosition(playerPos))
				.map(IMarkableRegion::getName)
				.collect(Collectors.toList());
	}

	public static void listRegionsAroundPlayer(PlayerEntity player) {
		Collection<String> regions = getRegionsAroundPlayer(player);
		if (regions.isEmpty()) {
			sendMessage(player, new TranslationTextComponent("You are not standing in any region!"));
			return;
		}
		BlockPos pos = player.getPosition();
		sendMessage(player, new TranslationTextComponent(""));
		sendMessage(player, new TranslationTextComponent(TextFormatting.AQUA + "== Regions around [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] =="));
		regions.forEach(regionName -> sendRegionInfoCommand(regionName, player));
	}

	public static void teleportToRegion(String regionName, PlayerEntity player, CommandSource source) {
		if (RegionManager.get().containsRegion(regionName)) {
			RegionManager.get().getRegion(regionName).ifPresent(region -> {
				try {
					BlockPos target = region.getTpTarget();
					String command = "execute in " + region.getDimension().getLocation() + " run tp " + player.getName().getString() + " " + target.getX() + " " + target.getY() + " " + target.getZ();
					sendStatusMessage(player, new TranslationTextComponent("message.region.teleport", region.getName()));
					source.getServer().getCommandManager().getDispatcher().execute(command, source);
				} catch (CommandSyntaxException e) {
					e.printStackTrace();
				}
			});
		} else {
			sendStatusMessage(player, new TranslationTextComponent("message.region.unknown", regionName));
		}
	}

	public static void giveHelpMessage(PlayerEntity player) {
		sendMessage(player, new TranslationTextComponent(TextFormatting.AQUA + "== WorldProtector Help =="));
		sendMessage(player, "help.region.1");
		sendMessage(player, "help.region.2");
		sendMessage(player, "help.region.3");
		sendMessage(player, "help.region.4");
		sendMessage(player, "help.region.4a");
		sendMessage(player, "help.region.5");
		sendMessage(player, "help.region.5a");
		sendMessage(player, "help.region.6");
		sendMessage(player, "help.region.7");
		sendMessage(player, "help.region.8");
		sendMessage(player, "help.region.9");
		sendMessage(player, "help.region.10");
	}

	public static void giveRegionInfo(PlayerEntity player, String regionName) {
		if (RegionManager.get().containsRegion(regionName)) {
			RegionManager.get().getRegion(regionName).ifPresent(region -> {
				String noFlagsText = new TranslationTextComponent("message.region.info.noflags").getString();
				String noPlayersText = new TranslationTextComponent("message.region.info.noplayers").getString();
				sendMessage(player, new StringTextComponent(TextFormatting.AQUA + "== Region '" + regionName + "' information =="));
				sendDimensionTeleportLink(player, region, new TranslationTextComponent("message.region.list.entry", region.getName()));
				sendMessage(player, new TranslationTextComponent("message.region.info.area", region.getArea().toString().substring(4)));
				sendMessage(player, new TranslationTextComponent("message.region.info.priority", region.getPriority()));

				if (region.getPlayers().isEmpty()) {
					sendMessage(player, new TranslationTextComponent("message.region.info.players", noPlayersText));
				} else {
					IFormattableTextComponent playerListLink = new StringTextComponent(": ")
							.appendSibling(TextComponentUtils.wrapWithSquareBrackets(new StringTextComponent(region.getPlayers().size() + " player(s)"))
									.setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.GREEN))
											.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wp player list " + regionName))
											.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("List players in region '" + regionName + "'")))));
					sendMessage(player, new TranslationTextComponent("message.region.info.players", playerListLink));
				}
				if (region.getFlags().isEmpty()) {
					sendMessage(player, new TranslationTextComponent("message.region.info.flags", noFlagsText));
				} else {
					IFormattableTextComponent playerListLink = new StringTextComponent(": ")
							.appendSibling(TextComponentUtils.wrapWithSquareBrackets(new StringTextComponent(region.getFlags().size() + " flag(s)"))
									.setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.GREEN))
											.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wp flag list " + regionName))
											.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("List flags in region '" + regionName + "'")))));
					sendMessage(player, new TranslationTextComponent("message.region.info.flags", playerListLink));
				}

				IFormattableTextComponent activeLink = new StringTextComponent(": ")
						.appendSibling(TextComponentUtils.wrapWithSquareBrackets(region.isActive()
								? new TranslationTextComponent("message.region.info.active.true")
								: new TranslationTextComponent("message.region.info.active.false"))
								.setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.GREEN))
										.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wp region " + (region.isActive() ? "deactivate" : "activate") + " " + regionName))
										.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent((region.isActive() ? "Deactivate" : "Activate") + " region '" + regionName + "'")))));

				IFormattableTextComponent muteLink = new StringTextComponent("")
						.appendSibling(TextComponentUtils.wrapWithSquareBrackets(region.isMuted()
								? new TranslationTextComponent("message.region.info.muted.true")
								: new TranslationTextComponent("message.region.info.muted.false"))
								.setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.GREEN))
										.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wp region " + (region.isMuted() ? "unmute" : "mute") + " " + regionName))
										.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent((region.isMuted() ? "Unmute" : "Mute") + " region '" + regionName + "'")))));

				sendMessage(player, new TranslationTextComponent("message.region.info.active", activeLink, muteLink));
			});
		} else {
			sendStatusMessage(player, new TranslationTextComponent("message.region.unknown", regionName));
		}

	}

	public static void giveRegionList(PlayerEntity player) {
		Collection<IMarkableRegion> regions = RegionManager.get().getAllRegionsSorted();
		if (regions.isEmpty()) {
			sendStatusMessage(player, "message.region.info.no_regions");
			return;
		}
		regions.forEach(region -> sendDimensionTeleportLink(player, region, new TranslationTextComponent("message.region.list.entry", region.getName())));
	}

	public static void giveRegionListForDim(PlayerEntity player, String dim) {
		List<IMarkableRegion> regionsForDim = RegionManager.get().getAllRegions()
				.stream()
				.filter(region -> region.getDimension().getLocation().toString().equals(dim))
				.sorted(Comparator.comparing(IMarkableRegion::getName))
				.collect(Collectors.toList());
		if (regionsForDim.isEmpty()) {
			sendMessage(player, new TranslationTextComponent("message.region.info.regions_for_dim", dim));
			return;
		}
		regionsForDim.forEach(region -> sendDimensionTeleportLink(player, region, new TranslationTextComponent("message.region.list.entry", region.getName())));
	}

	public static Collection<String> getDimensionList() {
		return RegionManager.get().getDimensionList();
	}

	public static Collection<String> getQuotedDimensionList() {
		return getDimensionList().stream()
				.map(dim -> "'" + dim + "'")
				.collect(Collectors.toList());
	}

	public static List<IMarkableRegion> getHandlingRegionsFor(Entity entity, IWorld world) {
		return getHandlingRegionsFor(entity.getPosition(), ((World) world).getDimensionKey());
	}

	public static List<IMarkableRegion> getHandlingRegionsFor(BlockPos position, World world) {
		return getHandlingRegionsFor(position, world.getDimensionKey());
	}

	public static List<IMarkableRegion> getHandlingRegionsFor(BlockPos position, RegistryKey<World> dimension) {
		int maxPriority = 1;
		List<IMarkableRegion> handlingRegions = new ArrayList<>();
		List<IMarkableRegion> filteredRegions = RegionManager.get().getRegions(dimension).stream()
				.filter(IMarkableRegion::isActive)
				.filter(region -> region.containsPosition(position))
				.collect(Collectors.toList());
		for (IMarkableRegion region : filteredRegions) {
			if (region.getPriority() == maxPriority) {
				handlingRegions.add(region);
			} else if (region.getPriority() > maxPriority) {
				handlingRegions.clear();
				maxPriority = region.getPriority();
				handlingRegions.add(region);
			}
		}
		return handlingRegions;
	}

	private static AxisAlignedBB getAreaFromNBT(CompoundNBT nbtTag) {
		return new AxisAlignedBB(
				nbtTag.getInt(ItemRegionMarker.X1), nbtTag.getInt(ItemRegionMarker.Y1), nbtTag.getInt(ItemRegionMarker.Z1),
				nbtTag.getInt(ItemRegionMarker.X2), nbtTag.getInt(ItemRegionMarker.Y2), nbtTag.getInt(ItemRegionMarker.Z2));
	}

	private static BlockPos getTpTargetFromNBT(CompoundNBT nbtTag) {
		if (nbtTag.getBoolean(ItemRegionMarker.TP_TARGET_SET)) {
			return new BlockPos(nbtTag.getInt(ItemRegionMarker.TP_X), nbtTag.getInt(ItemRegionMarker.TP_Y), nbtTag.getInt(ItemRegionMarker.TP_Z));
		} else {
			AxisAlignedBB area = getAreaFromNBT(nbtTag);
			int centerX = (int) area.getCenter().getX();
			int centerY = (int) area.getCenter().getY();
			int centerZ = (int) area.getCenter().getZ();
			return new BlockPos(centerX, centerY, centerZ);
		}
	}
}
