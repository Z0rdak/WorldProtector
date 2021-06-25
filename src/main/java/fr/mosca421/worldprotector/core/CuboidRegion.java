package fr.mosca421.worldprotector.core;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.stream.Collectors;

/**
 * A cuboid regions represents it's area as a simple rectangular cuboid (a BlockBox).
 * The region is marked with two blocks representing the bounding box of the area.
 */
public final class CuboidRegion extends AbstractMarkableRegion {

	// nbt keys
	public static final String TP_X = "tp_x";
	public static final String TP_Y = "tp_y";
	public static final String TP_Z = "tp_z";
	public static final String NAME = "name";
	public static final String UUID = "uuid";
	public static final String DIM = "dimension";
	public static final String MIN_X = "minX";
	public static final String MIN_Y = "minY";
	public static final String MIN_Z = "minZ";
	public static final String MAX_X = "maxX";
	public static final String MAX_Y = "maxY";
	public static final String MAX_Z = "maxZ";

	public static final String PRIORITY = "priority";
	public static final String ACTIVE = "active";
	public static final String PLAYERS = "players";
	public static final String FLAGS = "flags";
	public static final String ENTER_MSG_1 = "enter_msg";
	public static final String ENTER_MSG_2 = "enter_msg_small";
	public static final String EXIT_MSG_1 = "exit_msg";
	public static final String EXIT_MSG_2 = "exit_msg_small";
	public static final String MUTED = "muted";
	public static final String VERSION = "version";
	public static final String DATA_VERSION = "2.1.5.2";

	public CuboidRegion(CompoundNBT nbt) {
		deserializeNBT(nbt);
	}

	public CuboidRegion(String name, AxisAlignedBB area, RegistryKey<World> dimension) {
		super(name, area, dimension);
		this.area = area;
		this.tpTarget = new BlockPos((int) this.area.getCenter().getX(),
				(int) this.area.getCenter().getY(),
				(int) this.area.getCenter().getZ());
	}

	public CuboidRegion(String name, AxisAlignedBB area, BlockPos tpPos, RegistryKey<World> dimension) {
		super(name, area, dimension);
		this.tpTarget = tpPos;
	}

	@Override
	public AxisAlignedBB getArea() {
		return this.area;
	}

	@Override
	public void setArea(AxisAlignedBB area) {
		this.area = area;
	}

	@Override
	public boolean containsPosition(BlockPos position) {
		int x = position.getX();
		int y = position.getY();
		int z = position.getZ();
		// INFO: this.area.contains(x,y,z); does not work, because the max checks are exclusive by default.
		// TODO: Maybe replace with net.minecraft.util.math.MutableBoundingBox::intersectsWith which has inclusive checks
		return x >= this.area.minX && x <= this.area.maxX
				&& y >= this.area.minY && y <= this.area.maxY
				&& z >= this.area.minZ && z <= this.area.maxZ;
	}

	private AxisAlignedBB areaFromNBT(CompoundNBT nbt){
		return new AxisAlignedBB(
				nbt.getInt(MIN_X), nbt.getInt(MIN_Y), nbt.getInt(MIN_Z),
				nbt.getInt(MAX_X), nbt.getInt(MAX_Y), nbt.getInt(MAX_Z)
		);
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		// TODO: put version in main nbt
		nbt.putString(VERSION, DATA_VERSION);
		nbt.putString(NAME, name);
		nbt.putInt(MIN_X, (int) this.area.minX);
		nbt.putInt(MIN_Y, (int) this.area.minY);
		nbt.putInt(MIN_Z, (int) this.area.minZ);
		nbt.putInt(MAX_X, (int) this.area.maxX);
		nbt.putInt(MAX_Y, (int) this.area.maxY);
		nbt.putInt(MAX_Z, (int) this.area.maxZ);
		nbt.putInt(TP_X, this.tpTarget.getX());
		nbt.putInt(TP_Y, this.tpTarget.getY());
		nbt.putInt(TP_Z, this.tpTarget.getZ());
		nbt.putInt(PRIORITY, priority);
		nbt.putString(DIM, dimension.getLocation().toString());
		nbt.putBoolean(ACTIVE, isActive);
		nbt.putBoolean(MUTED, isMuted);
		ListNBT flagsNBT = new ListNBT();
		flagsNBT.addAll(flags.stream()
				.map(StringNBT::valueOf)
				.collect(Collectors.toSet()));
		nbt.put(FLAGS, flagsNBT);

		// serialize player data
		ListNBT playerList = nbt.getList(PLAYERS, NBT.TAG_COMPOUND);
		players.forEach( (uuid, name) -> {
			CompoundNBT playerNBT = new CompoundNBT();
			playerNBT.putUniqueId(UUID, uuid);
			playerNBT.putString(NAME, name);
			playerList.add(playerNBT);
		});
		nbt.put(PLAYERS, playerList);
		return nbt;
	}

	private CompoundNBT migrateRegionData(CompoundNBT nbt){
		// TODO:

		nbt.putString(VERSION, DATA_VERSION);

		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.name = nbt.getString(NAME);
		this.area = areaFromNBT(nbt);
		this.tpTarget = new BlockPos(nbt.getInt(TP_X), nbt.getInt(TP_Y), nbt.getInt(TP_Z));
		this.priority = nbt.getInt(PRIORITY);
		this.dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(nbt.getString(DIM)));
		this.isActive = nbt.getBoolean(ACTIVE);
		this.isMuted = nbt.getBoolean(MUTED);
		this.flags.clear();
		ListNBT flagsList = nbt.getList(FLAGS, NBT.TAG_STRING);
		for (int i = 0; i < flagsList.size(); i++) {
			flags.add(flagsList.getString(i));
		}
		// deserialize player data
		this.players.clear();
		ListNBT playerLists = nbt.getList(PLAYERS, NBT.TAG_COMPOUND);
		for (int i = 0; i < playerLists.size(); i++) {
			CompoundNBT playerMapping = playerLists.getCompound(i);
			players.put(playerMapping.getUniqueId(UUID), playerMapping.getString(NAME));
		}
	}

}
