package fr.mosca421.worldprotector.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.mosca421.worldprotector.core.RegionFlag;
import fr.mosca421.worldprotector.util.RegionUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.PlayerEntity;

public class CommandDimension {


    public static final LiteralArgumentBuilder<CommandSource> DIMENSION_COMMAND = register();

    private CommandDimension() {
    }

    // TODO: bane of nightmares
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal(Command.DIMENSION.toString())
                .executes(ctx -> giveHelp(ctx.getSource()))
                .then(Commands.literal(Command.HELP.toString())
                        .executes(ctx -> giveHelp(ctx.getSource())))
                .then(Commands.literal(Command.INFO.toString())
                        .executes(ctx -> giveDimensionInfo(ctx.getSource())))
                .then(Commands.literal(Command.FLAG.toString())
                        .then(Commands.argument(Command.ADD.toString(), StringArgumentType.word())
                                .then(Commands.argument(Command.FLAG.toString(), StringArgumentType.string())
                                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionFlag.getFlags(), builder))
                                        .then(Commands.argument(Command.DIMENSION.toString(), StringArgumentType.string())
                                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionUtils.getQuotedDimensionList(), builder))
                                                .executes(ctx -> addFlag(ctx.getSource(), StringArgumentType.getString(ctx, Command.FLAG.toString()), StringArgumentType.getString(ctx, Command.FLAG.toString())))))))
                .then(Commands.literal(Command.FLAG.toString())
                        .then(Commands.argument(Command.REMOVE.toString(), StringArgumentType.word())
                                .then(Commands.argument(Command.FLAG.toString(), StringArgumentType.string())
                                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionFlag.getFlags(), builder))
                                        .then(Commands.argument(Command.DIMENSION.toString(), StringArgumentType.string())
                                                .suggests((ctx, builder) -> ISuggestionProvider.suggest(RegionUtils.getQuotedDimensionList(), builder)))
                                        .executes(ctx -> removeFlag(ctx.getSource(), StringArgumentType.getString(ctx, Command.FLAG.toString()), StringArgumentType.getString(ctx, Command.FLAG.toString()))))))
                ;
    }

    private static int removeFlag(CommandSource source, String flag, String dim) {
        try {
            PlayerEntity player = source.asPlayer();
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int removeFlag(CommandSource source, String flag) {
        try {
            return removeFlag(source, flag, source.asPlayer().world.getDimensionKey().getLocation().toString());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int addFlag(CommandSource source, String flag) {
        try {
            return addFlag(source, flag, source.asPlayer().world.getDimensionKey().getLocation().toString());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int addFlag(CommandSource source, String flag, String dim) {
        try {
            PlayerEntity player = source.asPlayer();

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int giveHelp(CommandSource source) {

        return 0;
    }

    private static int giveDimensionInfo(CommandSource source) {
        return 0;
    }
}
