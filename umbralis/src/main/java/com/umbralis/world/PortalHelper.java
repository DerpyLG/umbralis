package com.umbralis.world;

import com.umbralis.Umbralis;
import com.umbralis.block.UmbralPortalBlock;
import com.umbralis.registry.ModBlocks;
import net.minecraft.advancement.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public final class PortalHelper {
    private static final BlockPos ARRIVAL = new BlockPos(0, 90, 0);

    private PortalHelper() {
    }

    /** El marco puede mezclar obsidiana, purpur y obsidiana llorosa. */
    public static boolean isFrame(BlockState s) {
        return s.isOf(Blocks.OBSIDIAN) || s.isOf(Blocks.CRYING_OBSIDIAN)
                || s.isOf(Blocks.PURPUR_BLOCK) || s.isOf(Blocks.PURPUR_PILLAR);
    }

    public static boolean hasKilledDragon(PlayerEntity player) {
        if (player.isCreative()) {
            return true;
        }
        if (!(player instanceof ServerPlayerEntity sp) || sp.getServer() == null) {
            return false;
        }
        Advancement adv = sp.getServer().getAdvancementLoader().get(new Identifier("end/kill_dragon"));
        return adv != null && sp.getAdvancementTracker().getProgress(adv).isDone();
    }

    /** Busca un marco rectangular (mín. 2x3 interior, máx. 21x21) y lo rellena con portal. */
    public static boolean tryCreatePortal(World world, BlockPos start) {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            Direction right = Direction.from(axis, Direction.AxisDirection.POSITIVE);
            Direction left = right.getOpposite();
            BlockPos p = start;
            if (!world.getBlockState(p).isAir()) continue;

            int guard = 0;
            while (world.getBlockState(p.down()).isAir() && guard++ < 22) p = p.down();
            if (!isFrame(world.getBlockState(p.down()))) continue;

            guard = 0;
            while (world.getBlockState(p.offset(left)).isAir()
                    && isFrame(world.getBlockState(p.offset(left).down())) && guard++ < 22) {
                p = p.offset(left);
            }
            if (!isFrame(world.getBlockState(p.offset(left)))) continue;

            int width = 0;
            while (width < 22 && world.getBlockState(p.offset(right, width)).isAir()
                    && isFrame(world.getBlockState(p.offset(right, width).down()))) {
                width++;
            }
            if (width < 2 || width > 21 || !isFrame(world.getBlockState(p.offset(right, width)))) continue;

            int height = 0;
            while (height < 22 && rowClear(world, p.up(height), right, left, width)) height++;
            if (height < 3 || height > 21) continue;

            boolean topOk = true;
            for (int i = 0; i < width; i++) {
                if (!isFrame(world.getBlockState(p.offset(right, i).up(height)))) topOk = false;
            }
            if (!topOk) continue;

            BlockState portal = ModBlocks.UMBRAL_PORTAL.getDefaultState().with(UmbralPortalBlock.AXIS, axis);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    world.setBlockState(p.offset(right, i).up(j), portal, Block.NOTIFY_LISTENERS);
                }
            }
            return true;
        }
        return false;
    }

    private static boolean rowClear(World w, BlockPos base, Direction right, Direction left, int width) {
        for (int i = 0; i < width; i++) {
            if (!w.getBlockState(base.offset(right, i)).isAir()) return false;
        }
        return isFrame(w.getBlockState(base.offset(left))) && isFrame(w.getBlockState(base.offset(right, width)));
    }

    public static void teleport(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerWorld here = player.getServerWorld();
        player.resetPortalCooldown();
        PortalMemory memory = PortalMemory.get(server);

        if (here.getRegistryKey() == Umbralis.UMBRAL_WORLD) {
            ServerWorld dest = server.getOverworld();
            BlockPos target = null;
            PortalMemory.Entry entry = memory.get(player.getUuid());
            if (entry != null) {
                ServerWorld w = server.getWorld(entry.dimension());
                if (w != null) {
                    dest = w;
                    target = entry.pos();
                }
            }
            Vec3d spot;
            if (target != null) {
                spot = safeSpot(dest, target);
            } else {
                BlockPos sp = dest.getSpawnPos();
                int y = dest.getTopY(Heightmap.Type.MOTION_BLOCKING, sp.getX(), sp.getZ());
                spot = new Vec3d(sp.getX() + 0.5, y, sp.getZ() + 0.5);
            }
            player.teleport(dest, spot.x, spot.y, spot.z, player.getYaw(), player.getPitch());
        } else {
            ServerWorld umbral = server.getWorld(Umbralis.UMBRAL_WORLD);
            if (umbral == null) {
                player.sendMessage(Text.translatable("message.umbralis.no_dimension"), true);
                return;
            }
            memory.remember(player.getUuid(), here.getRegistryKey(), player.getBlockPos());
            buildArrival(umbral);
            player.teleport(umbral, 0.5, ARRIVAL.getY() + 1, 0.5, 0.0f, 0.0f);
        }
    }

    private static Vec3d safeSpot(ServerWorld w, BlockPos around) {
        for (BlockPos p : BlockPos.iterateOutwards(around, 3, 3, 3)) {
            BlockState s = w.getBlockState(p);
            if (s.isOf(ModBlocks.UMBRAL_PORTAL)) {
                Direction n = s.get(UmbralPortalBlock.AXIS) == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
                for (Direction d : new Direction[]{n, n.getOpposite()}) {
                    BlockPos c = p.offset(d, 2);
                    if (w.getBlockState(c).isAir() && w.getBlockState(c.up()).isAir() && !w.getBlockState(c.down()).isAir()) {
                        return Vec3d.ofBottomCenter(c);
                    }
                }
            }
        }
        return Vec3d.ofBottomCenter(around);
    }

    /** Plataforma de llegada de purpur y obsidiana con su portal de regreso. */
    private static void buildArrival(ServerWorld w) {
        BlockPos portalCheck = ARRIVAL.add(0, 1, 4);
        if (w.getBlockState(portalCheck).isOf(ModBlocks.UMBRAL_PORTAL)) return;

        net.minecraft.util.math.random.Random r = w.getRandom();
        int y = ARRIVAL.getY();
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 9; dz++) {
                for (int dy = 1; dy <= 9; dy++) {
                    w.setBlockState(new BlockPos(dx, y + dy, dz), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
                float f = r.nextFloat();
                BlockState floor = f < 0.45f ? Blocks.OBSIDIAN.getDefaultState()
                        : f < 0.9f ? Blocks.PURPUR_BLOCK.getDefaultState() : Blocks.CRYING_OBSIDIAN.getDefaultState();
                w.setBlockState(new BlockPos(dx, y, dz), floor, Block.NOTIFY_LISTENERS);
                for (int dy = 1; dy <= 3; dy++) {
                    BlockPos under = new BlockPos(dx, y - dy, dz);
                    if (w.getBlockState(under).isAir()) {
                        w.setBlockState(under, Blocks.OBSIDIAN.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }
                }
            }
        }
        // Marco: x -1..2, y 90..94 en z = 4
        for (int x = -1; x <= 2; x++) {
            for (int dy = 0; dy <= 4; dy++) {
                boolean edge = x == -1 || x == 2 || dy == 0 || dy == 4;
                BlockPos p = new BlockPos(x, y + dy, 4);
                if (edge) {
                    w.setBlockState(p, ((x + dy) % 2 == 0 ? Blocks.OBSIDIAN : Blocks.PURPUR_PILLAR).getDefaultState(), Block.NOTIFY_LISTENERS);
                } else {
                    w.setBlockState(p, ModBlocks.UMBRAL_PORTAL.getDefaultState().with(UmbralPortalBlock.AXIS, Direction.Axis.X), Block.NOTIFY_LISTENERS);
                }
            }
        }
        // Pilares con cristal en las esquinas
        int[][] corners = {{-5, -5}, {5, -5}, {-5, 8}, {5, 8}};
        for (int[] c : corners) {
            for (int dy = 1; dy <= 4; dy++) {
                w.setBlockState(new BlockPos(c[0], y + dy, c[1]), Blocks.PURPUR_PILLAR.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
            w.setBlockState(new BlockPos(c[0], y + 5, c[1]), ModBlocks.VOID_CRYSTAL.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }
}
