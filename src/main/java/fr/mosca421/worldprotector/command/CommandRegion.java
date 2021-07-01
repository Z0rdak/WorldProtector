package fr.mosca421.worldprotector.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.mosca421.worldprotector.data.RegionManager;
import fr.mosca421.worldprotector.util.RegionUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;

public class CommandRegion {

    public static final LiteralArgumentBuilder<CommandSource> REGION_COMMAND = register();

    private CommandRegion() {
    }

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal(Command.REGION.toString())
                .executes(ctx -> giveHelp(ctx.getSource()))
                .then(Commands.literal(Command.HELP.toString())
                        .executes(ctx -> giveHelp(ctx.getSource())))
                .then(Commands.literal(Command.LIST.toString())
                        .executes(ctx -> giveRegionList(ctx.getSource()))
                        .then(Commands.argument(Command.DIMENSION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionUtils.getQuotedDimensionList(), builder))
                                .executes(ctx -> giveRegionListForDim(ctx.getSource(), StringArgumentType.getString(ctx, Command.DIMENSION.toString())))))
                .then(Commands.literal(Command.INFO.toString())
                        .executes(ctx -> listRegionsAround(ctx.getSource()))
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .executes(ctx -> info(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString())))))
                .then(Commands.literal(Command.DEFINE.toString())
                        .executes(ctx -> giveHelp(ctx.getSource()))
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .executes(ctx -> define(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString())))))
                .then(Commands.literal(Command.REDEFINE.toString())
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .executes(ctx -> redefine(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString())))))
                .then(Commands.literal(Command.REMOVE.toString())
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .executes(ctx -> remove(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString()))))
                        .then(Commands.literal(Command.ALL.toString()).executes(ctx -> removeAll(ctx.getSource()))))
                .then(Commands.literal(Command.TELEPORT.toString())
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .executes(ctx -> teleport(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString())))))
                .then(Commands.literal(Command.TELEPORT_SHORT.toString())
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .executes(ctx -> teleport(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString())))))
                .then(Commands.literal(Command.ACTIVATE.toString())
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .executes(ctx -> activeRegion(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString()))))
                        .then(Commands.literal(Command.ALL.toString()).executes(ctx -> activateAll(ctx.getSource()))))
                .then(Commands.literal(Command.ACTIVATE.toString())
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .executes(ctx -> activeRegion(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString())))))
                .then(Commands.literal(Command.DEACTIVATE.toString())
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .executes(ctx -> deactivateRegion(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString())))))
                .then(Commands.literal(Command.DEACTIVATE.toString())
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .executes(ctx -> deactivateRegion(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString()))))
                        .then(Commands.literal(Command.ALL.toString()).executes(ctx -> deactivateAll(ctx.getSource()))))
                .then(Commands.literal(Command.MUTE.toString())
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .executes(ctx -> muteRegion(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString())))))
                .then(Commands.literal(Command.UNMUTE.toString())
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .executes(ctx -> unmuteRegion(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString())))))
                .then(Commands.literal(Command.SET_PRIORITY.toString())
                        .then(Commands.argument(Command.REGION.toString(), StringArgumentType.string())
                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionManager.get().getAllRegionNames(), builder))
                                .then(Commands.argument(Command.PRIORITY.toString(), IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                                        .executes(ctx -> setPriority(ctx.getSource(), StringArgumentType.getString(ctx, Command.REGION.toString()), IntegerArgumentType.getInteger(ctx, Command.PRIORITY.toString()))))));
    }

    private static int info(CommandSource source, String regionName) {
        try {
            RegionUtils.giveRegionInfo(source.asPlayer(), regionName);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int activeRegion(CommandSource source, String regionName) {
        try {
            RegionUtils.activate(regionName, source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // TODO: deactivate and activate all in specified dimension
    private static int activateAll(CommandSource source) {
        try {
            RegionUtils.activateAll(RegionManager.get().getAllRegions(), source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int deactivateRegion(CommandSource source, String regionName) {
        try {
            RegionUtils.deactivate(regionName, source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // TODO: deactivate and activate all in specified dimension
    private static int deactivateAll(CommandSource source) {
        try {
            RegionUtils.deactivateAll(RegionManager.get().getAllRegions(), source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int muteRegion(CommandSource source, String regionName) {
        try {
            RegionUtils.muteRegion(regionName, source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int unmuteRegion(CommandSource source, String regionName) {
        try {
            RegionUtils.unmuteRegion(regionName, source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int giveHelp(CommandSource source) {
        try {
            RegionUtils.giveHelpMessage(source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int giveRegionList(CommandSource source) {
        try {
            RegionUtils.giveRegionList(source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int giveRegionListForDim(CommandSource source, String dim) {
        try {
            RegionUtils.giveRegionListForDim(source.asPlayer(), dim);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int define(CommandSource source, String regionName) {

        try {
            RegionUtils.createRegion(regionName, source.asPlayer(), source.asPlayer().getHeldItemMainhand());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int redefine(CommandSource source, String regionName) {
        try {
            RegionUtils.redefineRegion(regionName, source.asPlayer(), source.asPlayer().getHeldItemMainhand());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int remove(CommandSource source, String regionName) {
        try {
            RegionUtils.removeRegion(regionName, source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }


    private static int removeAll(CommandSource source) {
        try {
            RegionUtils.removeAllRegions(source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int listRegionsAround(CommandSource source) {
        try {
            RegionUtils.listRegionsAroundPlayer(source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int teleport(CommandSource source, String regionName) {
        try {
            RegionUtils.teleportToRegion(regionName, source.asPlayer(), source);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int setPriority(CommandSource source, String region, int priority) {
        try {
            RegionUtils.setRegionPriority(region, priority, source.asPlayer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
