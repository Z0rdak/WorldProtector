package fr.mosca421.worldprotector;

import fr.mosca421.worldprotector.command.CommandsRegister;
import fr.mosca421.worldprotector.data.RegionManager;
import fr.mosca421.worldprotector.registry.ItemRegister;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
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

}