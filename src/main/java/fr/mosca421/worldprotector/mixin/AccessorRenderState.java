package fr.mosca421.worldprotector.mixin;


import net.minecraft.client.renderer.RenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderState.class)
public interface AccessorRenderState {
    @Accessor("VIEW_OFFSET_Z_LAYERING")
    static RenderState.LayerState getViewOffsetZLayer() {
        throw new IllegalStateException();
    }

    @Accessor("TRANSLUCENT_TRANSPARENCY")
    static RenderState.TransparencyState getTranslucentTransparency() {
        throw new IllegalStateException();
    }

    @Accessor("ITEM_ENTITY_TARGET")
    static RenderState.TargetState getItemEntityTarget() {
        throw new IllegalStateException();
    }
}