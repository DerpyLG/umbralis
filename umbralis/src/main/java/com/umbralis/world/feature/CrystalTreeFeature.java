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

/** Árbol de cristal: tronco de purpur y copa luminosa de cristal del vacío y amatista. */
public class CrystalTreeFeature extends Feature<DefaultFeatureConfig> {
    public CrystalTreeFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();
        if (world.getBlockState(origin.down()).isAir() || !world.getBlockState(origin).isAir()) {
            return false;
        }
        int height = 6 + random.nextInt(6);
        for (int y = 0; y < height; y++) {
            BlockPos p = origin.up(y);
            if (!world.getBlockState(p).isAir()) return y > 3;
            world.setBlockState(p, Blocks.PURPUR_PILLAR.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
        crown(world, origin.up(height), 2, random);
        int branches = 2 + random.nextInt(2);
        for (int b = 0; b < branches; b++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int len = 3 + random.nextInt(3);
            BlockPos start = origin.up(height - 2 - random.nextInt(2));
            BlockPos end = start;
            for (int i = 1; i <= len; i++) {
                end = start.add((int) Math.round(Math.cos(angle) * i), i / 2, (int) Math.round(Math.sin(angle) * i));
                if (world.getBlockState(end).isAir()) {
                    world.setBlockState(end, Blocks.PURPUR_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            }
            crown(world, end, 1, random);
        }
        return true;
    }

    private static void crown(StructureWorldAccess world, BlockPos center, int radius, Random random) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy * 1.5 + dz * dz > radius * radius + 1.5) continue;
                    BlockPos p = center.add(dx, dy, dz);
                    if (!world.getBlockState(p).isAir()) continue;
                    float f = random.nextFloat();
                    BlockState s = f < 0.35f ? ModBlocks.VOID_CRYSTAL.getDefaultState()
                            : f < 0.8f ? Blocks.AMETHYST_BLOCK.getDefaultState() : null;
                    if (s != null) world.setBlockState(p, s, Block.NOTIFY_LISTENERS);
                }
            }
        }
    }
}
