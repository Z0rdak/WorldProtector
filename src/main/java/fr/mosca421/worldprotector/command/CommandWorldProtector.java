package fr.mosca421.worldprotector.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import static fr.mosca421.worldprotector.util.MessageUtils.sendMessage;

public class CommandWorldProtector {

    private CommandWorldProtector() {
    }

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal(Command.WP.toString())
                .requires(cs -> cs.hasPermissionLevel(4))
                .executes(ctx -> giveHelp(ctx.getSource()))
                .then(Commands.literal(Command.HELP.toString())
                        .executes(ctx -> giveHelp(ctx.getSource())))
                .then(CommandExpand.EXPAND_COMMAND)
                .then(CommandRegion.REGION_COMMAND)
                .then(CommandFlag.FLAG_COMMAND);
    }

    private static int giveHelp(CommandSource source) {
        try {
            PlayerEntity player = source.asPlayer();
            sendMessage(player, new TranslationTextComponent(TextFormatting.BLUE + "==WorldProtector Help=="));
            sendMessage(player, "help.wp.1");
            sendMessage(player, "help.wp.2");
            sendMessage(player, "help.wp.3");
            sendMessage(player, "help.wp.4");
            sendMessage(player, new TranslationTextComponent(TextFormatting.BLUE + "==WorldProtector Help=="));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;

    }

}