package fr.mosca421.worldprotector.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import static fr.mosca421.worldprotector.util.MessageUtils.sendMessage;

public final class DimensionUtils {

    private DimensionUtils(){}

    public static void giveHelpMessage(PlayerEntity player) {
        sendMessage(player, new TranslationTextComponent(TextFormatting.AQUA + "== Dimension commands help =="));
        sendMessage(player, "help.region.1");
        sendMessage(player, "help.region.2");
        sendMessage(player, "help.region.3");
        sendMessage(player, "help.region.4");
    }

}
