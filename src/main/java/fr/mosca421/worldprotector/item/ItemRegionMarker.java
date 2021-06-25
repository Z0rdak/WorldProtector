package fr.mosca421.worldprotector.item;

import fr.mosca421.worldprotector.WorldProtector;
import fr.mosca421.worldprotector.util.ExpandUtils;
import fr.mosca421.worldprotector.util.MessageUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

public class ItemRegionMarker extends Item {

	public ItemRegionMarker() {
		super(new Item.Properties()
				.maxStackSize(1)
				.group(WorldProtector.WORLD_PROTECTOR_TAB));
	}

	// nbt keys
	public static final String VALID = "valid";
	public static final String Y_DEFAULT_LOW = "y_low_default";
	public static final String Y_DEFAULT_HIGH = "y_high_default";
	public static final String CYCLE_POINT_ID = "id";
	public static final String X1 = "x1";
	public static final String Y1 = "y1";
	public static final String Z1 = "z1";
	public static final String X2 = "x2";
	public static final String Y2 = "y2";
	public static final String Z2 = "z2";
	public static final String TP_X = "tp_x";
	public static final String TP_Y = "tp_y";
	public static final String TP_Z = "tp_z";
	public static final String TP_TARGET_SET = "tp_target_set";

	public static final int FIRST = 0;
	public static final int SECOND = 1;

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (Screen.hasShiftDown()) {
			tooltip.add(new TranslationTextComponent("help.tooltip.region-marker.detail.1"));
			tooltip.add(new TranslationTextComponent("help.tooltip.region-marker.detail.2"));
			tooltip.add(new TranslationTextComponent("help.tooltip.region-marker.optional.1")
					.mergeStyle(TextFormatting.GRAY));
			tooltip.add(new TranslationTextComponent("help.tooltip.region-marker.optional.2")
					.mergeStyle(TextFormatting.GRAY));
			tooltip.add(new TranslationTextComponent("help.tooltip.region-marker.detail.3"));
			tooltip.add(new TranslationTextComponent("help.tooltip.region-marker.detail.4")
					.mergeStyle(TextFormatting.RED));
		} else {
			tooltip.add(new TranslationTextComponent("help.tooltip.region-marker.simple.1"));
			tooltip.add(new TranslationTextComponent("help.tooltip.region-marker.simple.2"));
			tooltip.add(new TranslationTextComponent("help.tooltip.details.shift")
					.mergeStyle(TextFormatting.DARK_BLUE)
					.mergeStyle(TextFormatting.ITALIC));
		}
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
		WorldProtector.LOGGER.debug(entityLiving);
		if (!worldIn.isRemote){
			if(entityLiving instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity) entityLiving;
				int yLow = (int) stack.getTag().getDouble(Y_DEFAULT_LOW);
				int yHigh = (int) stack.getTag().getDouble(Y_DEFAULT_HIGH);
				ExpandUtils.expandVert(player, stack, yLow, yHigh);
				player.getCooldownTracker().setCooldown(this, 20);
			}
		}
		return stack;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {

	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (!worldIn.isRemote) {
			ItemStack markStick = playerIn.getHeldItem(handIn);
			if (!playerIn.hasPermissionLevel(4) || !playerIn.isCreative()) {
				MessageUtils.sendStatusMessage(playerIn, new TranslationTextComponent("item.usage.permission")
						.mergeStyle(TextFormatting.RED));
				return ActionResult.resultFail(markStick);
			}
			if (handIn == Hand.MAIN_HAND && isValidRegion(markStick)) {
				playerIn.setActiveHand(handIn);
				return super.onItemRightClick(worldIn, playerIn, handIn);
			}
			return ActionResult.resultFail(markStick);
		}
		return ActionResult.resultFail(playerIn.getHeldItem(handIn));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		Hand hand = context.getHand();
		BlockPos pos = context.getPos();
		if (!world.isRemote) {
			ItemStack playerHeldItem = player.getHeldItem(hand);
			if (player.isSneaking()) {
				CompoundNBT playerItemTag = playerHeldItem.getTag();
				playerItemTag.putInt(TP_X, pos.getX());
				playerItemTag.putInt(TP_Y, pos.getY() + 1);
				playerItemTag.putInt(TP_Z, pos.getZ());
				playerItemTag.putBoolean(TP_TARGET_SET, true);
				MessageUtils.sendStatusMessage(player, new StringTextComponent("Teleport target: [" +
						player.getHeldItem(hand).getTag().getInt(X1) + ", " +
						player.getHeldItem(hand).getTag().getInt(Y1) + ", " +
						player.getHeldItem(hand).getTag().getInt(Z1) + "]")
						.mergeStyle(TextFormatting.WHITE));
			} else {
				if (playerHeldItem.hasTag()) {
					CompoundNBT playerItemTag = playerHeldItem.getTag();
					switch (playerItemTag.getInt(CYCLE_POINT_ID)) {
						case FIRST:
							playerItemTag.putInt(X1, pos.getX());
							playerItemTag.putInt(Y1, pos.getY());
							playerItemTag.putInt(Z1, pos.getZ());
							playerItemTag.putInt(CYCLE_POINT_ID, SECOND);
							playerItemTag.putBoolean(VALID, false);
							MessageUtils.sendStatusMessage(player, new StringTextComponent("Position 1: [" +
									player.getHeldItem(hand).getTag().getInt(X1) + ", " +
									player.getHeldItem(hand).getTag().getInt(Y1) + ", " +
									player.getHeldItem(hand).getTag().getInt(Z1) + "]")
									.mergeStyle(TextFormatting.WHITE));
							break;
						case SECOND:
							playerItemTag.putInt(X2, pos.getX());
							playerItemTag.putInt(Y2, pos.getY());
							playerItemTag.putInt(Z2, pos.getZ());
							playerItemTag.putInt(CYCLE_POINT_ID, 0);
							playerItemTag.putBoolean(VALID, true);
							MessageUtils.sendStatusMessage(player, new StringTextComponent("Position 2: [" +
									player.getHeldItem(hand).getTag().getInt(X2) + ", " +
									player.getHeldItem(hand).getTag().getInt(Y2) + ", " +
									player.getHeldItem(hand).getTag().getInt(Z2) + "]")
									.mergeStyle(TextFormatting.WHITE));
							break;
						default:
							// Never reached
							break;
					}
				}
			}
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (!worldIn.isRemote && !stack.hasTag()) {
            WorldProtector.LOGGER.info("CuboidRegion Marker nbt initialized");
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt(CYCLE_POINT_ID, 0);
            nbt.putBoolean(VALID, false);
            nbt.putDouble(Y_DEFAULT_LOW, 0);
            nbt.putDouble(Y_DEFAULT_HIGH, 255);
            nbt.putBoolean(TP_TARGET_SET, false);
            stack.setTag(nbt);
        }
	}

	private boolean isValidRegion(ItemStack markStick){
		return markStick.getTag().getBoolean(VALID);
	}

	public void setDefaultYValues(ItemStack regionMarker, int yLow, int yHigh) {
		CompoundNBT itemTag = regionMarker.getTag();
		itemTag.putDouble(Y_DEFAULT_LOW, yLow);
		itemTag.putDouble(Y_DEFAULT_HIGH, yHigh);
	}
}
