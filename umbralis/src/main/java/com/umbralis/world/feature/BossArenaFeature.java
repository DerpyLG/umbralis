package com.umbralis.world.feature;

import com.mojang.serialization.Codec;
import com.umbralis.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Plataforma circular con tres altares (uno por jefe) y pilares con cristal. */
public class BossArenaFeature extends Feature<DefaultFeatureConfig> {
    public BossArenaFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();
        int baseY = origin.getY() - 1;
        int radius = 11;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > radius) continue;
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                BlockState floor;
                float f = random.nextFloat();
                if (d <= 2.5) floor = Blocks.CRYING_OBSIDIAN.getDefaultState();
                else if (d <= 7) floor = f < 0.3f ? Blocks.PURPUR_PILLAR.getDefaultState() : Blocks.PURPUR_BLOCK.getDefaultState();
                else floor = f < 0.2f ? Blocks.CRYING_OBSIDIAN.getDefaultState() : Blocks.OBSIDIAN.getDefaultState();

                world.setBlockState(new BlockPos(x, baseY, z), floor, Block.NOTIFY_LISTENERS);
                for (int y = baseY - 1; y >= baseY - 6; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (world.getBlockState(p).isAir()) {
                        world.setBlockState(p, Blocks.OBSIDIAN.getDefaultState(), Block.NOTIFY_LISTENERS);
                    } else break;
                }
                for (int y = baseY + 1; y <= baseY + 14; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!world.getBlockState(p).isAir()) {
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }
                }
            }
        }

        Block[] altars = {ModBlocks.ALTAR_COLOSSUS, ModBlocks.ALTAR_WEAVER, ModBlocks.ALTAR_MONARCH};
        for (int k = 0; k < 3; k++) {
            double angle = Math.toRadians(90 + 120 * k);
            int ax = origin.getX() + (int) Math.round(Math.cos(angle) * 6);
            int az = origin.getZ() + (int) Math.round(Math.sin(angle) * 6);
            world.setBlockState(new BlockPos(ax, baseY + 1, az), altars[k].getDefaultState(), Block.NOTIFY_LISTENERS);
        }

        for (int i = 0; i < 4; i++) {
            double angle = Math.toRadians(45 + 90 * i);
            int px = origin.getX() + (int) Math.round(Math.cos(angle) * 9);
            int pz = origin.getZ() + (int) Math.round(Math.sin(angle) * 9);
            for (int y = 1; y <= 7; y++) {
                world.setBlockState(new BlockPos(px, baseY + y, pz), Blocks.PURPUR_PILLAR.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
            world.setBlockState(new BlockPos(px, baseY + 8, pz), ModBlocks.VOID_CRYSTAL.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
        return true;
    }
}
