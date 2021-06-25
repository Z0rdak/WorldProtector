package fr.mosca421.worldprotector.event;

import fr.mosca421.worldprotector.WorldProtector;
import fr.mosca421.worldprotector.core.IMarkableRegion;
import fr.mosca421.worldprotector.core.IProtectedRegion;
import fr.mosca421.worldprotector.core.RegionFlag;
import fr.mosca421.worldprotector.data.DimensionRegionCache;
import fr.mosca421.worldprotector.data.RegionManager;
import fr.mosca421.worldprotector.util.MessageUtils;
import fr.mosca421.worldprotector.util.RegionUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.function.BiPredicate;

@Mod.EventBusSubscriber(modid = WorldProtector.MODID)
public class EventMobs {

	private EventMobs() {
	}

	public static <T extends Entity> boolean isActionPermittedInRegion(IProtectedRegion region, T entity, BiPredicate<IProtectedRegion, T> flagPredicate) {
		return region.hasWhitelist() != flagPredicate.test(region, entity);
	}

	public static <T extends PlayerEntity> boolean isActionAndPlayerPermittedInRegion(IProtectedRegion region, T entity, BiPredicate<IProtectedRegion, T> flagPredicate) {
		return region.hasWhitelist() != flagPredicate.test(region, entity);
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		DimensionRegionCache cache = RegionManager.get().getRegionDimCache(event.getWorld().getDimensionKey());
		boolean setCanceled = true;
		/*
		if (cache != null) {
			setCanceled = isActionPermittedInRegion(cache, event.getEntity(), EventMobs::checkMobSpawning);
		}
		*/

		List<IMarkableRegion> affectedRegions = RegionUtils.getHandlingRegionsFor(event.getEntity().getPosition(), event.getWorld());
		for (IMarkableRegion region : affectedRegions) {
			setCanceled = setCanceled && isActionPermittedInRegion(region, event.getEntity(), EventMobs::checkMobSpawning);
		}
		event.setCanceled(setCanceled);

	}

	private static boolean checkMobSpawning(IProtectedRegion region, Entity eventEntity) {
		return region.containsFlag(RegionFlag.SPAWNING_ALL.toString()) && eventEntity instanceof MobEntity
				|| region.containsFlag(RegionFlag.SPAWNING_ANIMAL.toString()) && isAnimal(eventEntity)
				|| region.containsFlag(RegionFlag.SPAWNING_GOLEM.toString()) && eventEntity instanceof IronGolemEntity
				|| region.containsFlag(RegionFlag.SPAWNING_MONSTERS.toString()) && isMonster(eventEntity)
				|| region.containsFlag(RegionFlag.SPAWNING_XP.toString()) && eventEntity instanceof ExperienceOrbEntity;
	}

	@SubscribeEvent
	public static void onBreedingAttempt(BabyEntitySpawnEvent event) {
		if (event.getCausedByPlayer() != null) {
			PlayerEntity player = event.getCausedByPlayer();
			DimensionRegionCache cache = RegionManager.get().getRegionDimCache(event.getCausedByPlayer().world.getDimensionKey());
			boolean setCanceled = true;
			/*
			if (cache != null) {
				setCanceled = isActionAndPlayerPermittedInRegion(cache, player, (region, playerEntity) -> region.containsFlag(RegionFlag.ANIMAL_BREEDING.toString()) && region.permitsPlayer(playerEntity));
			}
			 */

			if (!player.world.isRemote) {
				List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(event.getParentB().getPosition(), event.getParentB().world);
				for (IMarkableRegion region : regions) {
					boolean isCanceled = isActionAndPlayerPermittedInRegion(region, player, (r, playerEntity) -> r.containsFlag(RegionFlag.ANIMAL_BREEDING.toString()) && r.permits(playerEntity));
					setCanceled = setCanceled && isCanceled;
					if (!region.isMuted() && isCanceled) {
						MessageUtils.sendStatusMessage(player, "message.event.mobs.breed_animals");
					}
				}
			}
			event.setCanceled(setCanceled);
		}


		// TODO: test if this is fired when animals are bred without player interaction
	}

	@SubscribeEvent
	public static void onAnimalTameAttempt(AnimalTameEvent event) {
		AnimalEntity animal = event.getAnimal();
		PlayerEntity player = event.getTamer();
		if (!player.world.isRemote) {
			List<IMarkableRegion> regions = RegionUtils.getHandlingRegionsFor(animal.getPosition(), event.getAnimal().world);
			for (IMarkableRegion region : regions) {
				event.setCanceled(true);
				if (!region.isMuted()) {
					MessageUtils.sendStatusMessage(player, "message.event.mobs.tame_animal");
				}
				return;
			}
		}
	}

	public static boolean isAnimal(Entity entity){
		return entity instanceof AnimalEntity || entity instanceof WaterMobEntity;
	}

	public static boolean isMonster(Entity entity){
		return entity instanceof MonsterEntity
				|| entity instanceof SlimeEntity
				|| entity instanceof FlyingEntity
				|| entity instanceof EnderDragonEntity
				|| entity instanceof ShulkerEntity;
	}

	private static boolean regionContainsEntity(IMarkableRegion region, Entity entity){
		return region.getArea().contains(entity.getPositionVec());
	}

	@SubscribeEvent
	public static void onAttackEntityAnimal(AttackEntityEvent event) {
		PlayerEntity player = event.getPlayer();
		Entity eventEntity = event.getTarget();
		List<IMarkableRegion> affectedRegions = RegionUtils.getHandlingRegionsFor(event.getTarget().getPosition(), event.getTarget().world);
		if (!event.getTarget().world.isRemote) {
			if (isAnimal(eventEntity)) {
				for (IMarkableRegion region : affectedRegions) {
					boolean flagDamageAnimals = region.containsFlag(RegionFlag.ATTACK_ANIMALS.toString());
					if (flagDamageAnimals && regionContainsEntity(region, eventEntity) && region.forbids(player)) {
						if (!region.isMuted()) {
							MessageUtils.sendStatusMessage(player, new TranslationTextComponent("message.event.mobs.hurt_animal"));
						}
						event.setCanceled(true);
					}
				}
			}

			if (isMonster(eventEntity)) {
				for (IMarkableRegion region : affectedRegions) {
					boolean flagDamageMonsters = region.containsFlag(RegionFlag.ATTACK_MONSTERS.toString());
					if (flagDamageMonsters && regionContainsEntity(region, eventEntity) && region.forbids(player)) {
						if (!region.isMuted()) {
							MessageUtils.sendStatusMessage(player, new TranslationTextComponent("message.event.mobs.hurt_monster"));
						}
						event.setCanceled(true);
					}
				}
			}

			if (event.getTarget() instanceof VillagerEntity) { // exclude pesky wandering trader >:-)
				VillagerEntity villager = (VillagerEntity) event.getTarget();
				for (IMarkableRegion region : affectedRegions) {
					boolean flagDamageMonsters = region.containsFlag(RegionFlag.ATTACK_VILLAGERS.toString());
					if (flagDamageMonsters && regionContainsEntity(region, villager) && region.forbids(player)) {
						if (!region.isMuted()) {
							MessageUtils.sendStatusMessage(player, new TranslationTextComponent("message.event.mobs.hurt_villager"));
						}
						event.setCanceled(true);
					}
				}
			}
		}
	}

}
