package fr.mosca421.worldprotector.api.event;

import fr.mosca421.worldprotector.core.IMarkableRegion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public abstract class RegionEvent extends Event {

    private final IMarkableRegion region;
    private final PlayerEntity player;

    public RegionEvent(IMarkableRegion region, PlayerEntity player) {
        this.region = region;
        this.player = player;
    }

    public IMarkableRegion getRegion() {
        return region;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public static class CreateRegionEvent extends RegionEvent {

        public CreateRegionEvent(IMarkableRegion region, PlayerEntity player) {
            super(region, player);
        }
    }

    public static class RemoveRegionEvent extends RegionEvent {

        public RemoveRegionEvent(IMarkableRegion region, PlayerEntity player) {
            super(region, player);
        }
    }

    public static class UpdateRegionEvent extends RegionEvent {

        public UpdateRegionEvent(IMarkableRegion region, PlayerEntity player) {
            super(region, player);
        }
    }
}


