package fr.mosca421.worldprotector.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.mosca421.worldprotector.core.RegionFlag;
import fr.mosca421.worldprotector.data.RegionManager;
import fr.mosca421.worldprotector.util.RegionFlagUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import static fr.mosca421.worldprotector.util.MessageUtils.sendMessage;
import static fr.mosca421.worldprotector.util.MessageUtils.sendStatusMessage;

public class CommandFlag {

	private CommandFlag(){}

	public static final LiteralArgumentBuilder<CommandSource> FLAG_COMMAND = register();

	public static LiteralArgumentBuilder<CommandSource> register() {
		return Commands.literal(Command.FLAG.toString())
				.executes(ctx -> giveHelp(ctx.getSource()))
				.then(Commands.literal(Command.HELP.toString())
						.executes(ctx -> giveHelp(ctx.getSource())))
				.then(Commands.literal(Command.LIST.toString())
						.executes(ctx -> giveList(ctx.getSource())))
				.then(Commands.literal(Command.LIST.toString())
						.executes(ctx -> giveList(ctx.getSource()))
						.then(Commands.argument(Command.REGION.toString(), StringArgumentType.word())
								.suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
								.executes(ctx -> giveFlagListForRegion(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString())))))
				.then(Commands.literal(Command.ADD.toString())
						.then(Commands.argument(Command.REGION.toString(), StringArgumentType.word())
								.suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
								.then(Commands.argument(Command.FLAG.toString(), StringArgumentType.string())
										.suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionFlag.getFlags(), builder))
										.then(Commands.argument(Command.NAME.toString(), StringArgumentType.greedyString())
												.executes(ctx -> add(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString()), StringArgumentType.getString(ctx, Command.FLAG.toString()), StringArgumentType.getString(ctx, Command.NAME.toString())))))))
				.then(Commands.literal(Command.ADD.toString())
						.then(Commands.argument(Command.REGION.toString(), StringArgumentType.word())
								.suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
								.then(Commands.argument(Command.FLAG.toString(), StringArgumentType.string())
										.suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionFlag.getFlags(), builder))
										.executes(ctx -> add(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString()), StringArgumentType.getString(ctx, Command.FLAG.toString()), "")))))
				.then(Commands.literal(Command.REMOVE.toString())
						.then(Commands.argument(Command.REGION.toString(), StringArgumentType.word())
								.suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
								.then(Commands.argument(Command.FLAG.toString(), StringArgumentType.string())
										.suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getRegionFlags(ctx.getArgument(Command.REGION.toString(), String.class), ctx.getSource().getWorld().getDimensionKey()), builder))
										.executes(ctx -> remove(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString()), StringArgumentType.getString(ctx, Command.FLAG.toString()))))));
			/*
				.then(Commands.literal(Command.INFO.toString())
						.then(Commands.argument(Command.REGION.toString(), StringArgumentType.word())
								.suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
								.executes(ctx -> info(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString())))));
			 */
	}

	private static int giveHelp(CommandSource source) {
		try {
			RegionFlagUtils.giveHelpMessage(source.asPlayer());
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static int giveList(CommandSource source) {
		try {
			RegionFlagUtils.listAvailableFlags(source.asPlayer());
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static int giveFlagListForRegion(CommandSource source, String regionName) {
		try {
			RegionFlagUtils.listRegionFlags(source.asPlayer(), regionName);
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static int add(CommandSource source, String regionName, String flag, String enterOrExitFlagMsg) {
		try {
			PlayerEntity player = source.asPlayer();
			if (RegionManager.get().containsRegion(regionName)) {
				RegionManager.get().getRegion(regionName).ifPresent((region) -> {
					if (RegionFlag.contains(flag)) {
						RegionFlag regionFlag = RegionFlag.fromString(flag)
								.orElseThrow(() -> new IllegalArgumentException("Flag could not be converted to enum counterpart"));
						switch (regionFlag) {
							case ALL:
								RegionFlagUtils.addAllFlags(regionName, player);
								break;
							case ANIMAL_UNMOUNTING:
								sendMessage(player, "Unmounting flag is currently not working due to a minecraft vanilla bug. This bug is fixed in 1.17. See: https://bugs.mojang.com/browse/MC-202202.");
								break;
								/*
							case ENTER_MESSAGE_TITLE:
							case ENTER_MESSAGE_SUBTITLE:
							case EXIT_MESSAGE_TITLE:
							case EXIT_MESSAGE_SUBTITLE:
							case BLOCK_ENTER:
							case BLOCK_EXIT:
								sendMessage(player, "This flag is not yet implemented, sorry!");
								break;
								*/
							default:
								RegionFlagUtils.addFlag(region, player, flag);
								break;
						}
					} else {
						sendMessage(player, new TranslationTextComponent("message.flags.unknown", flag));
					}
				});
			} else {
				sendMessage(player, new TranslationTextComponent("message.region.unknown", regionName));
			}
		} catch (CommandSyntaxException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static int remove(CommandSource source, String regionName, String flag) {
		try {
			PlayerEntity player = source.asPlayer();
			if (RegionManager.get().containsRegion(regionName)) {
				RegionManager.get().getRegion(regionName).ifPresent(region -> {
					if (RegionFlag.contains(flag)) {
						RegionFlag regionFlag = RegionFlag.fromString(flag)
								.orElseThrow(() -> new IllegalArgumentException("Flag could not be converted to enum counterpart"));
						switch (regionFlag) {
							case ALL:
								RegionFlagUtils.removeAllFlags(regionName, player);
								break;
							case ANIMAL_UNMOUNTING:
								sendMessage(player, "Unmounting flag is currently not working due to a minecraft vanilla bug. This bug is fixed in 1.17. See: https://bugs.mojang.com/browse/MC-202202.");
								break;
								/*
							case ENTER_MESSAGE_TITLE:
							case ENTER_MESSAGE_SUBTITLE:
							case EXIT_MESSAGE_TITLE:
							case EXIT_MESSAGE_SUBTITLE:
							case BLOCK_ENTER:
							case BLOCK_EXIT:
								sendMessage(player, "This flag is not yet implemented, sorry!");
								break;
								 */
							default:
								RegionFlagUtils.removeFlag(region, player, flag);
								break;
						}
					} else {
						sendMessage(player, new TranslationTextComponent("message.flags.unknown", flag));
					}
				});

			} else {
				sendMessage(player, new TranslationTextComponent("message.region.unknown", regionName));
			}
		} catch (CommandSyntaxException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static int info(CommandSource source, String regionName) {
		try {
			PlayerEntity player = source.asPlayer();
			if (RegionManager.get().containsRegion(regionName)) {
				RegionManager.get().getRegion(regionName).ifPresent(region -> {
					String flagsInRegion = RegionFlagUtils.getFlagString(region);
					sendMessage(player, new TranslationTextComponent("Flags defined in region '" + region.getName() + "': " + flagsInRegion));
				});
			} else {
				sendStatusMessage(player, new TranslationTextComponent("message.region.unknown", regionName));
			}
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
