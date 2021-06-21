package fr.mosca421.worldprotector.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.mosca421.worldprotector.WorldProtector;
import fr.mosca421.worldprotector.core.IMarkableRegion;
import fr.mosca421.worldprotector.core.RegionFlag;
import fr.mosca421.worldprotector.item.ItemRegionMarker;
import fr.mosca421.worldprotector.util.MessageUtils;
import fr.mosca421.worldprotector.util.RegionUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static fr.mosca421.worldprotector.util.MessageUtils.sendStatusMessage;

@Mod.EventBusSubscriber(modid = WorldProtector.MODID)
public class EventPlayers {

    private EventPlayers() {
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!event.getPlayer().world.isRemote) {
            if (event.getTarget() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) event.getTarget();
                List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(player.getPosition(), player.world);
                for (IMarkableRegion region : regions) {
                    if (region.containsFlag(RegionFlag.ATTACK_PLAYERS.toString()) && region.forbids(player)) {
                        if (!region.isMuted()) {
                            sendStatusMessage(player, "message.event.player.pvp");
                        }
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPickupItem(EntityItemPickupEvent event) {
        if (!event.getPlayer().world.isRemote) {
            List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(event.getPlayer().getPosition(), event.getPlayer().world);
            for (IMarkableRegion region : regions) {
                if (region.containsFlag(RegionFlag.ITEM_PICKUP.toString()) && region.forbids(event.getPlayer())) {
                    if (!region.isMuted()) {
                        sendStatusMessage(event.getPlayer(), "message.event.player.pickup_item");
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLevelChange(PlayerXpEvent.LevelChange event) {
        if (!event.getPlayer().world.isRemote) {
            PlayerEntity player = event.getPlayer();
            List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(player.getPosition(), player.world);
            for (IMarkableRegion region : regions) {
                if (region.containsFlag(RegionFlag.LEVEL_FREEZE) && region.forbids(player)) {
                    if (!region.isMuted()) {
                        sendStatusMessage(player, "message.event.player.level_freeze");
                    }
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

	@SubscribeEvent
    public static void onPlayerXPChange(PlayerXpEvent.XpChange event) {
        if (!event.getPlayer().world.isRemote) {
            PlayerEntity player = event.getPlayer();
            List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(player.getPosition(), player.world);
            for (IMarkableRegion region : regions) {
                if (region.containsFlag(RegionFlag.XP_FREEZE.toString()) && region.forbids(player)) {
                    if (!region.isMuted()) {
                        MessageUtils.sendStatusMessage(player, "message.protection.player.xp_freeze");
                    }
                    event.setCanceled(true);
                    event.setAmount(0);
                    return;
                }

            }
        }
    }



	@SubscribeEvent
	public static void onPlayerXpPickup(PlayerXpEvent.PickupXp event){
		if (!event.getPlayer().world.isRemote) {
            PlayerEntity player = event.getPlayer();
            List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(player.getPosition(), player.world);
            for (IMarkableRegion region : regions) {
                if (region.containsFlag(RegionFlag.XP_PICKUP.toString()) && region.forbids(player)) {
                    if (!region.isMuted()) {
                        MessageUtils.sendStatusMessage(player, "message.protection.player.xp_pickup");
                    }
                    event.setCanceled(true);
                    event.getOrb().remove();
                    return;
                }

            }
        }
	}

    // TODO: replace with AttackEntityEvent for players and use LivingHurtEvent for enitites
    // TODO: Separate flags for Villagers, Animals, Monsters, Player
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(player.getPosition(), player.world);
            for (IMarkableRegion region : regions) {
                if (region.containsFlag(RegionFlag.INVINCIBLE.toString())) {
                    event.setCanceled(true);
                    return;
                }

            }
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntityLiving();
        List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(entity.getPosition(), entity.world);
        for (IMarkableRegion region : regions) {
            // prevent fall damage for all entities
            if (region.containsFlag(RegionFlag.FALL_DAMAGE.toString())) {
                event.setCanceled(true); // same result as event.setDamageMultiplier(0.0f);
                return;
            }
            if (entity instanceof PlayerEntity && region.containsFlag(RegionFlag.FALL_DAMAGE_PLAYERS)) {
                event.setDamageMultiplier(0.0f);
                return;
            }
            if (entity instanceof AbstractVillagerEntity && region.containsFlag(RegionFlag.FALL_DAMAGE_VILLAGERS)) {
                event.setDamageMultiplier(0.0f);
                return;
            }
            if (EventMobs.isAnimal(entity) && region.containsFlag(RegionFlag.FALL_DAMAGE_ANIMALS)) {
                event.setDamageMultiplier(0.0f);
                return;
            }
            if (EventMobs.isMonster(entity)) {
                event.setDamageMultiplier(0.0f);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onAnvilRepair(AnvilRepairEvent event) {
        if (!event.getPlayer().world.isRemote) {
            ItemStack rightIn = event.getIngredientInput();
            ItemStack leftIn = event.getItemInput();
            PlayerEntity player = event.getPlayer();
            if (leftIn.getItem() instanceof ItemRegionMarker && rightIn.getItem() instanceof AirItem) {
                String regionName = event.getItemResult().getDisplayName().getString();
                if (!player.hasPermissionLevel(4) || !player.isCreative()) {
                    sendStatusMessage(player, "message.event.players.anvil_region_defined");
                } else {
                    RegionUtils.createRegion(regionName, player, event.getItemResult());
                }
            }
        }
    }

    @SubscribeEvent
    // message send to server but not distributed to all clients
    public static void onSendChat(ServerChatEvent event) {
        if (event.getPlayer() != null) {
            ServerPlayerEntity player = event.getPlayer();
            List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(player.getPosition(), player.world);
            for (IMarkableRegion region : regions) {
                if (region.containsFlag(RegionFlag.SEND_MESSAGE.toString()) && region.forbids(player)) {
                    event.setCanceled(true);
                    if (!region.isMuted()) {
                        sendStatusMessage(player, new TranslationTextComponent("message.event.player.speak"));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onChatSend(ClientChatEvent event) {
        // can only prevent sending commands/chat for all/global
        // Possible place for a profanity filter
    }

    @SubscribeEvent
    public static void onCommandSend(CommandEvent event) {
        try {
            event.getParseResults().getContext().getSource().assertIsEntity();
            PlayerEntity player = event.getParseResults().getContext().getSource().asPlayer();
            BlockPos playerPos = player.getPosition();
            List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(playerPos, player.world);
            for (IMarkableRegion region : regions) {
                if (region.containsFlag(RegionFlag.EXECUTE_COMMAND.toString()) && region.forbids(player)) {
                    event.setCanceled(true);
                    if (!region.isMuted()) {
                        MessageUtils.sendStatusMessage(player, "message.event.player.execute-commands");
                    }
                    return;
                }
            }
            // TODO: add command list to block only specific commands, regardless of mod and permission of command
            // event.getParseResults().getContext().getNodes().forEach(node -> WorldProtector.LOGGER.debug(node.getNode().getName()));
        } catch (CommandSyntaxException e) {
            // Most likely thrown because command was not send by a player.
            // This is fine because we don't want this flag to be triggered from non-players
        }
    }

    @SubscribeEvent
    public static void onPlayerSleep(SleepingTimeCheckEvent event) {
        PlayerEntity player = event.getPlayer();
        List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(player.getPosition(), player.world);
        for (IMarkableRegion region : regions) {
            if (region.containsFlag(RegionFlag.SLEEP.toString()) && region.forbids(player)) {
                if (!region.isMuted()) {
                    MessageUtils.sendStatusMessage(player, "message.event.player.sleep");
                }
                event.setResult(Event.Result.DENY);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onSetSpawn(PlayerSetSpawnEvent event) {
        BlockPos newSpawn = event.getNewSpawn();
        PlayerEntity player = event.getPlayer();
        List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(player.getPosition(), player.world);
        if (newSpawn != null) {
            // attempt to set spawn
            for (IMarkableRegion region : regions) {
                if (region.containsFlag(RegionFlag.SET_SPAWN.toString()) && region.forbids(player)) {
                    event.setCanceled(true);
                    if (!region.isMuted()) {
                        MessageUtils.sendStatusMessage(player, "message.event.player.set_spawn");
                    }
                    return;
                }
            }
        } /*
        else {
            // attempt to reset spawn
            for (IMarkableRegion region : regions) {
                // TODO: not working?
                if (region.containsFlag(RegionFlag.RESET_SPAWN.toString()) && region.forbids(player)) {
                    event.setCanceled(true);
                    MessageUtils.sendStatusMessage(player, "message.event.player.reset_spawn");
                    return;
                }
            }

        }
        */
    }

    @SubscribeEvent
    public static void onPlayerDropItem(ItemTossEvent event) {
        PlayerEntity player = event.getPlayer();
        List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(player.getPosition(), player.world);
        for (IMarkableRegion region : regions) {
            if (region.containsFlag(RegionFlag.ITEM_DROP.toString()) && region.forbids(player)) {
                event.setCanceled(true);
                player.inventory.addItemStackToInventory(event.getEntityItem().getItem());
                if (!region.isMuted()) {
                    MessageUtils.sendStatusMessage(player, "message.event.player.drop_item");
                }
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onEntityMountAttempt(EntityMountEvent event) {
        if (!event.getWorldObj().isRemote) {
            Entity entityBeingMounted = event.getEntityBeingMounted();
            // could be mob that dismounts because entity being mounted dies?
            boolean playerAttemptsMounting = event.getEntityMounting() instanceof PlayerEntity;
            if (playerAttemptsMounting) {
                PlayerEntity player = (PlayerEntity) event.getEntityMounting();
                List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(entityBeingMounted, event.getWorldObj());
                for (IMarkableRegion region : regions) {
					/*
					TODO: Wait for 1.17: https://bugs.mojang.com/browse/MC-202202
					if (event.isDismounting() && region.containsFlag(RegionFlag.ANIMAL_UNMOUNTING) && region.forbids(player)) {
						event.setCanceled(true); // Does not correctly unmount player
						if (!region.isMuted()) {
						    sendStatusMessage(player, "message.event.player.unmount");
						}
					}
					*/
                    if (event.isMounting() && region.containsFlag(RegionFlag.ANIMAL_MOUNTING) && region.forbids(player)) {
                        event.setCanceled(true);
                        if (!region.isMuted()) {
                            sendStatusMessage(player, "message.event.player.mount");
                        }
                    }
                }
            }
        }
    }
}
