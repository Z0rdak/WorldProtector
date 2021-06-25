package fr.mosca421.worldprotector;

import fr.mosca421.worldprotector.command.CommandsRegister;
import fr.mosca421.worldprotector.data.RegionManager;
import fr.mosca421.worldprotector.registry.ItemRegister;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(WorldProtector.MODID)
public class WorldProtector {

	public static final String MODID = "worldprotector";
	public static final Logger LOGGER = LogManager.getLogger();

	public WorldProtector() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::isInRegion);
		MinecraftForge.EVENT_BUS.register(this);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ItemRegister.ITEMS.register(modEventBus);
	}

	public static final ItemGroup WORLD_PROTECTOR_TAB = new ItemGroup(MODID) {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(ItemRegister.EMBLEM.get());
		}
	};

	@SubscribeEvent
	public void serverStarting(FMLServerStartingEvent event) {
		CommandsRegister.init(event.getServer().getCommandManager().getDispatcher());
		RegionManager.onServerStarting(event);
	}

	/*
	private boolean enter = false;
	private String exitMessage = "";
	private String exitMessageSmall = "";
	*/

	// FIXME: isInRegion works not as intended - the flags only work for one region at a time.
	// if mupltiple regions are overlapping only 1 enter and exit message is displayer.
	// the flag for entering and leaving has to be saved per region

	@SubscribeEvent
	public void isInRegion(PlayerTickEvent event) {
		/*
		if (event.player instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.player;
			List<CuboidRegion> regions = RegionUtils.getHandlingRegionsFor(player.getPosition(), RegionUtils.getDimension(player.world));
			for (CuboidRegion region : regions) {
				if (region.getFlags().contains("enter-message")) {
					try {
						if (!enter) {
							player.connection.sendPacket(new STitlePacket(STitlePacket.Type.SUBTITLE,
									// Changed: .updateForEntity -> func_240645_a_
									TextComponentUtils.func_240645_a_(player.getCommandSource(),
											new StringTextComponent(region.getEnterMessageSmall().replace("&", "�")),
											player, 0)));
							player.connection.sendPacket(new STitlePacket(STitlePacket.Type.TITLE,
									// Changed: .updateForEntity -> func_240645_a_
									TextComponentUtils.func_240645_a_(player.getCommandSource(),
											new StringTextComponent(region.getEnterMessage().replace("&", "�")), player,
											0),
									10, 10, 10));
							if (region.getFlags().contains("exit-message")) {
								exitMessage = region.getExitMessage();
								exitMessageSmall = region.getExitMessageSmall();
							} else {
								region.setExitMessage("");
								region.setExitMessageSmall("");
								exitMessage = "";
								exitMessageSmall = "";
							}
							enter = true;
						}
						return;
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
					}
				} else {
					region.setEnterMessage("");
					region.setEnterMessageSmall("");
				}
			}
			if (enter) {
				enter = false;
				if (!exitMessage.equals(""))
					try {
						player.connection.sendPacket(new STitlePacket(STitlePacket.Type.SUBTITLE,
								// Changed: .updateForEntity -> func_240645_a_
								TextComponentUtils.func_240645_a_(player.getCommandSource(),
										new StringTextComponent(exitMessageSmall.replace("&", "�")), player, 0)));
						player.connection.sendPacket(new STitlePacket(STitlePacket.Type.TITLE,
								// Changed: .updateForEntity -> func_240645_a_
								TextComponentUtils.func_240645_a_(player.getCommandSource(),
										new StringTextComponent(exitMessage.replace("&", "�")), player, 0),
								10, 10, 10));
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
					}
			}
		}
		*/
	}
}