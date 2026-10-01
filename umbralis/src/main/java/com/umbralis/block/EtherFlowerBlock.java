package com.umbralis.block;

import com.umbralis.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class EtherFlowerBlock extends FlowerBlock {
    public EtherFlowerBlock(StatusEffect effect, int duration, Settings settings) {
        super(effect, duration, settings);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(ModBlocks.ETHER_MOSS) || floor.isOf(ModBlocks.UMBRAL_STONE)
                || floor.isOf(Blocks.PURPUR_BLOCK) || floor.isOf(Blocks.OBSIDIAN)
                || floor.isOf(Blocks.CRYING_OBSIDIAN);
    }
}
