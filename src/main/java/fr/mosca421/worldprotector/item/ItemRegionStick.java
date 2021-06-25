package fr.mosca421.worldprotector.item;

import fr.mosca421.worldprotector.WorldProtector;
import fr.mosca421.worldprotector.data.RegionManager;
import fr.mosca421.worldprotector.util.RegionPlayerUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static fr.mosca421.worldprotector.util.MessageUtils.sendStatusMessage;

public class ItemRegionStick extends Item {

	public ItemRegionStick() {
		super(new Properties()
				.maxStackSize(1)
				.group(WorldProtector.WORLD_PROTECTOR_TAB));
	}

	// nbt keys
	public static final String REGION_IDX = "region_idx";
	private static final String LAST_DIM = "last_dim";
	public static final String MODE = "mode";
	public static final String REGION = "region";

	public static final String MODE_ADD = "add";
	public static final String MODE_REMOVE = "remove";

	private static List<String> cachedRegions;
	private static int regionCount = -1;

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (Screen.hasShiftDown()) {
			tooltip.add(new TranslationTextComponent("help.tooltip.region-stick.detail.1"));
			tooltip.add(new TranslationTextComponent("help.tooltip.region-stick.detail.2"));
			tooltip.add(new TranslationTextComponent("help.tooltip.region-stick.detail.3"));
			tooltip.add(new TranslationTextComponent("help.tooltip.region-stick.detail.4")
					.mergeStyle(TextFormatting.GRAY));
		} else {
			tooltip.add(new TranslationTextComponent("help.tooltip.region-stick.simple.1"));
			tooltip.add(new TranslationTextComponent("help.tooltip.details.shift")
					.mergeStyle(TextFormatting.DARK_BLUE)
					.mergeStyle(TextFormatting.ITALIC));
		}
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
		if (!player.world.isRemote) {
			if ((entity instanceof PlayerEntity)) {
				String mode = stack.getTag().getString(MODE);
				PlayerEntity hitPlayer = (PlayerEntity) entity;
				String regionName = stack.getTag().getString(REGION);
				switch (mode) {
					case MODE_ADD:
						RegionPlayerUtils.addPlayer(regionName, player, hitPlayer);
						break;
					case MODE_REMOVE:
						RegionPlayerUtils.removePlayer(regionName, player, hitPlayer);
						break;
					default:
						/* should not happen */
						break;
				}
			}
		}
		return true; // false will damage entity
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 25;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
		if (!worldIn.isRemote) {
			// No functionality yet
		}
		return stack;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (!worldIn.isRemote) {
			ItemStack regionStick = playerIn.getHeldItem(handIn);
			if (!playerIn.hasPermissionLevel(4) || !playerIn.isCreative()) {
				sendStatusMessage(playerIn, new TranslationTextComponent("item.usage.permission")
						.mergeStyle(TextFormatting.RED));
				return ActionResult.resultFail(regionStick);
			}
			if (playerIn.getHeldItemOffhand().getItem() instanceof ItemRegionStick) {
				return ActionResult.resultFail(regionStick);
			}
			if (playerIn.getActiveHand() == Hand.MAIN_HAND) {
				if (playerIn.isSneaking()) {
					switchMode(regionStick);
					return new ActionResult<>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
				} else {
					if (cycleRegion(regionStick)) {
						return new ActionResult<>(ActionResultType.SUCCESS, regionStick);
					}
					sendStatusMessage(playerIn, new TranslationTextComponent("message.region.info.no_regions")
							.mergeStyle(TextFormatting.RED));
					new ActionResult<>(ActionResultType.FAIL, playerIn.getHeldItem(handIn));
				}
			}
		}
		return new ActionResult<>(ActionResultType.FAIL, playerIn.getHeldItem(handIn));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		return ActionResultType.FAIL;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (!worldIn.isRemote) {
			if (stack.hasTag()) {
				// update region name list
				String dim = worldIn.getDimensionKey().getLocation().toString();
				String nbtDim = stack.getTag().getString(LAST_DIM);
				stack.getTag().putString(LAST_DIM, dim);
				List<String> regionNames = new ArrayList<>(RegionManager.get().getRegionNames(worldIn.getDimensionKey()));
				if (regionCount != regionNames.size() || !dim.equals(nbtDim)) {
					cachedRegions = regionNames;
					regionCount = cachedRegions.size();
					if (stack.getTag().contains(REGION_IDX)) {
						int regionIndex = stack.getTag().getInt(REGION_IDX);
						regionIndex = Math.max(0, Math.min(regionIndex, regionCount - 1));
						stack.getTag().putInt(REGION_IDX, regionIndex);
					} else {
						stack.getTag().putInt(REGION_IDX, 0);
					}

				}
				setDisplayName(stack, stack.getTag().getString(REGION), stack.getTag().getString(MODE));
			} else {
				// init nbt tag of RegionStick
				CompoundNBT nbt = new CompoundNBT();
				nbt.putString(MODE, MODE_ADD);
				nbt.putInt(REGION_IDX, 0);
				nbt.putString(LAST_DIM, worldIn.getDimensionKey().getLocation().toString());
				if (regionCount > 0) {
					nbt.putString(REGION, cachedRegions.get(0));
				} else {
					nbt.putString(REGION, "N/A");
				}
				stack.setTag(nbt);
			}
		}
	}

	private void setDisplayName(ItemStack regionStick, String region, String mode){
        regionStick.setDisplayName(new StringTextComponent(TextFormatting.AQUA + "CuboidRegion Stick [" + region + ", " + mode + "]"));
    }

	public String getMode(ItemStack regionStick){
		return regionStick.getTag().getString(MODE);
	}

	private void setMode(ItemStack regionStick, String mode){
		regionStick.getTag().putString(MODE, mode);
	}

	public String getRegion(ItemStack regionStick) {
		return regionStick.getTag().getString(REGION);
	}

	private void setRegion(ItemStack regionStick, String region){
		regionStick.getTag().putString(REGION, region);
	}

	private boolean cycleRegion(ItemStack regionStick){
		if (regionCount > 0) {
			int regionIndex = regionStick.getTag().getInt(REGION_IDX);
			// get region and set display name
			String selectedRegion = cachedRegions.get(regionIndex);
			setDisplayName(regionStick, selectedRegion, getMode(regionStick));
			// write region nbt
			setRegion(regionStick, selectedRegion);
			// increase region index and write nbt
			regionIndex = (regionIndex + 1) % (regionCount);
			regionStick.getTag().putInt(REGION_IDX, regionIndex);
			return true;
		} else {
			return false;
		}
	}

	private void switchMode(ItemStack regionStick) {
		String mode = getMode(regionStick);
		String region = getRegion(regionStick);
		switch(mode){
			case MODE_ADD:
				setMode(regionStick, MODE_REMOVE);
				setDisplayName(regionStick, region, MODE_REMOVE);
				break;
			case MODE_REMOVE:
				setMode(regionStick, MODE_ADD);
				setDisplayName(regionStick, region, MODE_ADD);
				break;
			default:
				/* should not happen */
				break;
		}
	}
}
