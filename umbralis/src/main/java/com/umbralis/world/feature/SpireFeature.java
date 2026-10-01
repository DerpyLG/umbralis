package com.umbralis.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Aguja cónica de pilares de purpur salpicada de obsidiana. */
public class SpireFeature extends Feature<DefaultFeatureConfig> {
    public SpireFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();
        if (world.getBlockState(origin.down()).isAir()) {
            return false;
        }
        int height = 10 + random.nextInt(20);
        double baseRadius = 2.0 + random.nextInt(3);
        for (int y = -4; y <= height; y++) {
            double t = Math.max(0, y) / (double) height;
            double radius = Math.max(0.4, baseRadius * (1.0 - t));
            int r = (int) Math.ceil(radius);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > radius * radius + 0.5) continue;
                    BlockPos pos = origin.add(dx, y, dz);
                    if (world.isOutOfHeightLimit(pos)) continue;
                    world.setBlockState(pos, pick(random, y, height), Block.NOTIFY_LISTENERS);
                }
            }
        }
        return true;
    }

    private static BlockState pick(Random random, int y, int height) {
        float f = random.nextFloat();
        if (y > height - 3) {
            return f < 0.35f ? Blocks.CRYING_OBSIDIAN.getDefaultState() : Blocks.OBSIDIAN.getDefaultState();
        }
        if (f < 0.22f) return Blocks.OBSIDIAN.getDefaultState();
        if (f < 0.27f) return Blocks.CRYING_OBSIDIAN.getDefaultState();
        if (f < 0.6f) return Blocks.PURPUR_BLOCK.getDefaultState();
        return Blocks.PURPUR_PILLAR.getDefaultState();
    }
}
