package com.umbralis.registry;

import com.umbralis.Umbralis;
import com.umbralis.block.BossAltarBlock;
import com.umbralis.block.EtherFlowerBlock;
import com.umbralis.block.UmbralPortalBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public class ModBlocks {
    public static final Block UMBRAL_STONE = block("umbral_stone", new Block(
            FabricBlockSettings.copyOf(Blocks.PURPUR_BLOCK).mapColor(MapColor.PURPLE).strength(12.0f, 600.0f).requiresTool()), true);

    public static final Block ETHER_MOSS = block("ether_moss", new Block(
            FabricBlockSettings.copyOf(Blocks.PURPUR_BLOCK).mapColor(MapColor.CYAN).sounds(BlockSoundGroup.MOSS_BLOCK)
                    .strength(3.0f, 20.0f).luminance(s -> 5)), true);

    public static final Block VOID_CRYSTAL = block("void_crystal", new Block(
            FabricBlockSettings.copyOf(Blocks.AMETHYST_BLOCK).mapColor(MapColor.MAGENTA).luminance(s -> 13)), true);

    public static final Block ETHER_FLOWER = block("ether_flower", new EtherFlowerBlock(StatusEffects.NIGHT_VISION, 8,
            FabricBlockSettings.copyOf(Blocks.ALLIUM).luminance(s -> 7)), true);

    public static final Block UMBRAL_PORTAL = block("umbral_portal", new UmbralPortalBlock(
            FabricBlockSettings.create().noCollision().strength(-1.0f).luminance(s -> 11)
                    .sounds(BlockSoundGroup.GLASS).dropsNothing()), false);

    public static final Block ALTAR_COLOSSUS = block("altar_colossus", new BossAltarBlock(
            FabricBlockSettings.copyOf(Blocks.OBSIDIAN).luminance(s -> 9),
            () -> ModEntities.OBSIDIAN_COLOSSUS, null), true);

    public static final Block ALTAR_WEAVER = block("altar_weaver", new BossAltarBlock(
            FabricBlockSettings.copyOf(Blocks.OBSIDIAN).luminance(s -> 9),
            () -> ModEntities.VOID_WEAVER, () -> ModItems.OBSIDIAN_HEART), true);

    public static final Block ALTAR_MONARCH = block("altar_monarch", new BossAltarBlock(
            FabricBlockSettings.copyOf(Blocks.OBSIDIAN).luminance(s -> 9),
            () -> ModEntities.ECLIPSE_MONARCH, () -> ModItems.WEAVER_EYE), true);

    private static Block block(String name, Block block, boolean withItem) {
        Registry.register(Registries.BLOCK, Umbralis.id(name), block);
        if (withItem) {
            Registry.register(Registries.ITEM, Umbralis.id(name), new BlockItem(block, new FabricItemSettings()));
        }
        return block;
    }

    public static void init() {
    }
}
