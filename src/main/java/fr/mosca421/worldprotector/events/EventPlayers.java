package fr.mosca421.worldprotector.events;

import fr.mosca421.worldprotector.WorldProtector;
import fr.mosca421.worldprotector.core.Region;
import fr.mosca421.worldprotector.core.RegionFlag;
import fr.mosca421.worldprotector.utils.FlagsUtils;
import fr.mosca421.worldprotector.utils.RegionsUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = WorldProtector.MODID)
public class EventPlayers {

	private EventPlayers(){}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		if (event.getTarget() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getTarget();
			List<Region> regions = RegionsUtils.getHandlingRegionsFor(player.getPosition(), RegionsUtils.getDimension(player.world));
			for (Region region : regions) {
				if (region.getFlags().contains(RegionFlag.DAMAGE_PLAYERS.toString()) && FlagsUtils.isOp(player)) {
					event.getPlayer().sendMessage(new TranslationTextComponent("world.pvp.player"), event.getPlayer().getUniqueID());
					event.setCanceled(true);
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPickupItem(ItemPickupEvent event) {
		List<Region> regions = RegionsUtils.getHandlingRegionsFor(event.getPlayer().getPosition(), RegionsUtils.getDimension(event.getPlayer().world));
		for (Region region : regions) {
			if (region.getFlags().contains(RegionFlag.ITEM_PICKUP.toString()) && !RegionsUtils.isInRegion(region.getName(), event.getPlayer())) {
				event.getPlayer().sendMessage(new TranslationTextComponent("world.pickup.player"), event.getPlayer().getUniqueID());
				event.setCanceled(true);
			}

		}
	}

	@SubscribeEvent
	public static void onHurt(LivingHurtEvent event) {
		if (event.getEntityLiving() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
			List<Region> regions = RegionsUtils.getHandlingRegionsFor(player.getPosition(), RegionsUtils.getDimension(player.world));
			for (Region region : regions) {
				if (region.getFlags().contains(RegionFlag.INVINCIBLE.toString())) {
					event.setCanceled(true);
					return;
				}

			}
		}
	}

	@SubscribeEvent
	public static void onFall(LivingFallEvent event) {
		if (event.getEntityLiving() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
			List<Region> regions = RegionsUtils.getHandlingRegionsFor(player.getPosition(), RegionsUtils.getDimension(player.world));
			for (Region region : regions) {
				if (region.getFlags().contains(RegionFlag.FALL_DAMAGE.toString())) {
					event.setCanceled(true);
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public static void onSendChat(ServerChatEvent event) {
		if (event.getPlayer() != null) {
			ServerPlayerEntity player = event.getPlayer();
			List<Region> regions = RegionsUtils.getHandlingRegionsFor(player.getPosition(), RegionsUtils.getDimension(player.world));
			for (Region region : regions) {
				if (region.getFlags().contains(RegionFlag.SEND_MESSAGE.toString()) && !RegionsUtils.isInRegion(region.getName(), player)) {
					event.setCanceled(true);
					event.getPlayer().sendMessage(new TranslationTextComponent("world.speak.player"), event.getPlayer().getUniqueID());
				}
			}
		}
	}

	@SubscribeEvent
	public static void playerDropItem(ItemTossEvent event) {
		List<Region> regions = RegionsUtils.getHandlingRegionsFor(event.getPlayer().getPosition(), RegionsUtils.getDimension(event.getPlayer().world));
		for (Region region : regions) {
			if (region.getFlags().contains(RegionFlag.ITEM_DROP.toString()) && !RegionsUtils.isInRegion(region.getName(), event.getPlayer())) {
				event.setCanceled(true);
				event.getPlayer().inventory.addItemStackToInventory(event.getEntityItem().getItem());
				event.getPlayer().sendMessage(new TranslationTextComponent("world.drop.player"), event.getPlayer().getUniqueID());
			}
		}
	}
}
