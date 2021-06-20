package fr.mosca421.worldprotector.core;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.mosca421.worldprotector.WorldProtector;
import fr.mosca421.worldprotector.item.ItemRegionMarker;
import fr.mosca421.worldprotector.mixin.AccessorRenderState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.stream.Collectors;

public final class RegionTileRenderer {

    public static final RenderType LINE_1_NO_DEPTH;
    public static final RenderType LINE_4_NO_DEPTH;
    public static final RenderType LINE_5_NO_DEPTH;
    public static final RenderType LINE_8_NO_DEPTH;
    private static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = AccessorRenderState.getTranslucentTransparency();
    static {

        RenderState.DepthTestState noDepth = new RenderState.DepthTestState("always", GL11.GL_ALWAYS);
        RenderState.WriteMaskState colorMask = new RenderState.WriteMaskState(true, false);
        RenderState.LayerState viewOffsetZLayering = AccessorRenderState.getViewOffsetZLayer();
        RenderState.CullState disableCull = new RenderState.CullState(false);
        RenderState.TargetState itemTarget = AccessorRenderState.getItemEntityTarget();
        RenderType.State  glState = RenderType.State.getBuilder()
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .target(itemTarget)
                .cull(disableCull).build(false);

        LINE_1_NO_DEPTH = RenderType.makeType(WorldProtector.MODID + ":" + "line_1_no_depth", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 128, glState);
        glState = RenderType.State.getBuilder().line(new RenderState.LineState(OptionalDouble.of(4))).layer(viewOffsetZLayering).transparency(TRANSLUCENT_TRANSPARENCY).writeMask(colorMask).depthTest(noDepth).build(false);
        LINE_4_NO_DEPTH = RenderType.makeType(WorldProtector.MODID + ":" + "line_4_no_depth", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 128, glState);
        glState = RenderType.State.getBuilder().line(new RenderState.LineState(OptionalDouble.of(5))).layer(viewOffsetZLayering).transparency(TRANSLUCENT_TRANSPARENCY).writeMask(colorMask).depthTest(noDepth).build(false);
        LINE_5_NO_DEPTH = RenderType.makeType(WorldProtector.MODID + ":" + "line_5_no_depth", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 64, glState);
        glState = RenderType.State.getBuilder().line(new RenderState.LineState(OptionalDouble.of(8))).layer(viewOffsetZLayering).transparency(TRANSLUCENT_TRANSPARENCY).writeMask(colorMask).depthTest(noDepth).build(false);
        LINE_8_NO_DEPTH = RenderType.makeType(WorldProtector.MODID + ":" + "line_8_no_depth", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 64, glState);

    }

    private static final IRenderTypeBuffer.Impl LINE_BUFFERS = IRenderTypeBuffer.getImpl(Util.make(() -> {
        Map<RenderType, BufferBuilder> ret = new IdentityHashMap<>();
        ret.put(LINE_1_NO_DEPTH, new BufferBuilder(LINE_1_NO_DEPTH.getBufferSize()));
        ret.put(LINE_4_NO_DEPTH, new BufferBuilder(LINE_4_NO_DEPTH.getBufferSize()));
        ret.put(LINE_5_NO_DEPTH, new BufferBuilder(LINE_5_NO_DEPTH.getBufferSize()));
        ret.put(LINE_8_NO_DEPTH, new BufferBuilder(LINE_8_NO_DEPTH.getBufferSize()));
        return ret;
    }), Tessellator.getInstance().getBuffer());

    private RegionTileRenderer() {}

    public static void onWorldRenderLast(RenderWorldLastEvent event) {
        MatrixStack ms = event.getMatrixStack();
        ms.push();

        PlayerEntity player = Minecraft.getInstance().player;
        int color = 0xFF000000 | MathHelper.hsvToRGB(System.currentTimeMillis() % 200 / 200F, 0.6F, 1F);

        if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemRegionMarker) {
            ItemRegionMarker regionMarker = ((ItemRegionMarker) player.getHeldItemMainhand().getItem());


            // TODO: get pos for rendering
            BlockPos coords = new BlockPos(0,0,0);
            if (coords != null) {
                renderBlockOutlineAt(ms, LINE_BUFFERS, coords, color);
            }
        }

        if (!player.getHeldItemOffhand().isEmpty() && player.getHeldItemOffhand().getItem() instanceof ItemRegionMarker) {
            ItemRegionMarker regionMarker = ((ItemRegionMarker) player.getHeldItemMainhand().getItem());
            // TODO: get pos for rendering
            List<BlockPos> renderPosList = new ArrayList<>(0);
            renderPosList.add(new BlockPos(1,1,1));
            renderPosList.add(new BlockPos(2,2,2));
            renderPosList.add(new BlockPos(3,3,3));

            renderPosList.stream()
                    .filter(Objects::nonNull)
                    .forEach(pos -> renderBlockOutlineAt(ms, LINE_BUFFERS, pos, color));
        }

        ms.pop();
        RenderSystem.disableDepthTest();
        LINE_BUFFERS.finish();
    }

    private static void renderBlockOutlineAt(MatrixStack ms, IRenderTypeBuffer buffers, BlockPos pos, int color) {
        renderBlockOutlineAt(ms, buffers, pos, color, false);
    }

    private static void renderBlockOutlineAt(MatrixStack ms, IRenderTypeBuffer buffers, BlockPos pos, int color, boolean thick) {
        double renderPosX = Minecraft.getInstance().getRenderManager().info.getProjectedView().getX();
        double renderPosY = Minecraft.getInstance().getRenderManager().info.getProjectedView().getY();
        double renderPosZ = Minecraft.getInstance().getRenderManager().info.getProjectedView().getZ();

        ms.push();
        ms.translate(pos.getX() - renderPosX, pos.getY() - renderPosY, pos.getZ() - renderPosZ + 1);

        World world = Minecraft.getInstance().world;
        BlockState state = world.getBlockState(pos);
        List<AxisAlignedBB> list;

        VoxelShape shape = state.getShape(world, pos);
        list = shape.toBoundingBoxList().stream()
                .map(b -> b.offset(pos))
                .collect(Collectors.toList());

        if (!list.isEmpty()) {
            ms.scale(1F, 1F, 1F);

            IVertexBuilder buffer = buffers.getBuffer(thick ? LINE_5_NO_DEPTH : LINE_1_NO_DEPTH);
            for (AxisAlignedBB axis : list) {
                axis = axis.offset(-pos.getX(), -pos.getY(), -(pos.getZ() + 1));
                renderBlockOutline(ms.getLast().getMatrix(), buffer, axis, color);
            }

            buffer = buffers.getBuffer(thick ? LINE_8_NO_DEPTH : LINE_4_NO_DEPTH);
            int alpha = 64;
            color = (color & ~0xff000000) | (alpha << 24);
            for (AxisAlignedBB axis : list) {
                axis = axis.offset(-pos.getX(), -pos.getY(), -(pos.getZ() + 1));
                renderBlockOutline(ms.getLast().getMatrix(), buffer, axis, color);
            }
        }

        ms.pop();
    }

    private static void renderBlockOutline(Matrix4f mat, IVertexBuilder buffer, AxisAlignedBB aabb, int color) {
        float ix = (float) aabb.minX;
        float iy = (float) aabb.minY;
        float iz = (float) aabb.minZ;
        float ax = (float) aabb.maxX;
        float ay = (float) aabb.maxY;
        float az = (float) aabb.maxZ;
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        buffer.pos(mat, ix, iy, iz).color(r, g, b, a).endVertex();
        buffer.pos(mat, ix, ay, iz).color(r, g, b, a).endVertex();

        buffer.pos(mat, ix, ay, iz).color(r, g, b, a).endVertex();
        buffer.pos(mat, ax, ay, iz).color(r, g, b, a).endVertex();

        buffer.pos(mat, ax, ay, iz).color(r, g, b, a).endVertex();
        buffer.pos(mat, ax, iy, iz).color(r, g, b, a).endVertex();

        buffer.pos(mat, ax, iy, iz).color(r, g, b, a).endVertex();
        buffer.pos(mat, ix, iy, iz).color(r, g, b, a).endVertex();

        buffer.pos(mat, ix, iy, az).color(r, g, b, a).endVertex();
        buffer.pos(mat, ix, ay, az).color(r, g, b, a).endVertex();

        buffer.pos(mat, ix, iy, az).color(r, g, b, a).endVertex();
        buffer.pos(mat, ax, iy, az).color(r, g, b, a).endVertex();

        buffer.pos(mat, ax, iy, az).color(r, g, b, a).endVertex();
        buffer.pos(mat, ax, ay, az).color(r, g, b, a).endVertex();

        buffer.pos(mat, ix, ay, az).color(r, g, b, a).endVertex();
        buffer.pos(mat, ax, ay, az).color(r, g, b, a).endVertex();

        buffer.pos(mat, ix, iy, iz).color(r, g, b, a).endVertex();
        buffer.pos(mat, ix, iy, az).color(r, g, b, a).endVertex();

        buffer.pos(mat, ix, ay, iz).color(r, g, b, a).endVertex();
        buffer.pos(mat, ix, ay, az).color(r, g, b, a).endVertex();

        buffer.pos(mat, ax, iy, iz).color(r, g, b, a).endVertex();
        buffer.pos(mat, ax, iy, az).color(r, g, b, a).endVertex();

        buffer.pos(mat, ax, ay, iz).color(r, g, b, a).endVertex();
        buffer.pos(mat, ax, ay, az).color(r, g, b, a).endVertex();
    }

}
