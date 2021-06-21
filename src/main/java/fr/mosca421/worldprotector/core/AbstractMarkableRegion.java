package fr.mosca421.worldprotector.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;

public abstract class AbstractMarkableRegion extends AbstractRegion implements IMarkableRegion {

    protected String name;
    protected int priority;
    protected RegistryKey<World> dimension;
    protected AxisAlignedBB area;
    protected boolean isMuted;
    protected String areaType;
    private BlockPos tpTarget;

    public AbstractMarkableRegion(String name, AxisAlignedBB area, RegistryKey<World> dimension) {
        this();
        this.name = name;
        this.dimension = dimension;
        // TODO: area can not be set here, because are for cuboid and prism is different
        this.area = area;
    }

    protected AbstractMarkableRegion() {
        this.priority = 2;
        this.isActive = true;
        this.hasWhitelist = false;
        this.isMuted = false;
        this.players = new HashMap<>(0);
        this.flags = new HashSet<>(0);
    }

    @Override
    public abstract AxisAlignedBB getArea();

    @Override
    public abstract void setArea(AxisAlignedBB area);

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public RegistryKey<World> getDimension() {
        return dimension;
    }

    @Override
    public boolean isMuted() {
        return this.isMuted;
    }

    @Override
    public void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }

    @Override
    public BlockPos getTpTarget() {
        return this.tpTarget;
    }

    @Override
    public void setTpTarget(BlockPos tpPos) {
        this.tpTarget = tpPos;
    }

    @Override
    public CompoundNBT serializeNBT(){
        return null;
    }

    @Override
    public abstract void deserializeNBT(CompoundNBT nbt);
}
