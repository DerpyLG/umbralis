package com.umbralis.registry;

import com.umbralis.Umbralis;
import com.umbralis.item.UmbralKeyItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item UMBRAL_KEY = item("umbral_key",
            new UmbralKeyItem(new FabricItemSettings().maxDamage(4).rarity(Rarity.EPIC)));
    public static final Item OBSIDIAN_HEART = item("obsidian_heart", new Item(new FabricItemSettings().rarity(Rarity.RARE)));
    public static final Item WEAVER_EYE = item("weaver_eye", new Item(new FabricItemSettings().rarity(Rarity.RARE)));
    public static final Item ECLIPSE_SHARD = item("eclipse_shard", new Item(new FabricItemSettings().rarity(Rarity.EPIC)) {
        @Override
        public boolean hasGlint(ItemStack stack) {
            return true;
        }
    });

    public static final Item STALKER_EGG = egg("umbral_stalker", ModEntities.STALKER, 0x1c1228, 0xa050e0);
    public static final Item WRAITH_EGG = egg("umbral_wraith", ModEntities.WRAITH, 0x3c286e, 0x60e0ff);
    public static final Item CRAWLER_EGG = egg("purpur_crawler", ModEntities.CRAWLER, 0x1e122d, 0xc83cff);
    public static final Item BISON_EGG = egg("umbral_bison", ModEntities.BISON, 0x46326e, 0xd2c8ff);
    public static final Item PIG_EGG = egg("crystal_pig", ModEntities.CRYSTAL_PIG, 0xaa78c8, 0xf0c8ff);
    public static final Item PILGRIM_EGG = egg("pilgrim", ModEntities.PILGRIM, 0xaa96c8, 0x78c8e6);
    public static final Item COLOSSUS_EGG = egg("obsidian_colossus", ModEntities.OBSIDIAN_COLOSSUS, 0x120c1a, 0xff6e1e);
    public static final Item WEAVER_EGG = egg("void_weaver", ModEntities.VOID_WEAVER, 0x1e122d, 0x5aff78);
    public static final Item MONARCH_EGG = egg("eclipse_monarch", ModEntities.ECLIPSE_MONARCH, 0x0a0814, 0xffe68c);

    public static final ItemGroup GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(UMBRAL_KEY))
            .displayName(Text.translatable("itemGroup.umbralis.main"))
            .entries((ctx, e) -> {
                e.add(UMBRAL_KEY);
                e.add(ModBlocks.UMBRAL_STONE);
                e.add(ModBlocks.ETHER_MOSS);
                e.add(ModBlocks.VOID_CRYSTAL);
                e.add(ModBlocks.ETHER_FLOWER);
                e.add(ModBlocks.ALTAR_COLOSSUS);
                e.add(ModBlocks.ALTAR_WEAVER);
                e.add(ModBlocks.ALTAR_MONARCH);
                e.add(OBSIDIAN_HEART);
                e.add(WEAVER_EYE);
                e.add(ECLIPSE_SHARD);
                e.add(STALKER_EGG);
                e.add(WRAITH_EGG);
                e.add(CRAWLER_EGG);
                e.add(BISON_EGG);
                e.add(PIG_EGG);
                e.add(PILGRIM_EGG);
                e.add(COLOSSUS_EGG);
                e.add(WEAVER_EGG);
                e.add(MONARCH_EGG);
            }).build();

    private static Item item(String name, Item item) {
        return Registry.register(Registries.ITEM, Umbralis.id(name), item);
    }

    private static Item egg(String name, EntityType<? extends MobEntity> type, int primary, int secondary) {
        return item(name + "_spawn_egg", new SpawnEggItem(type, primary, secondary, new FabricItemSettings()));
    }

    public static void init() {
        Registry.register(Registries.ITEM_GROUP, Umbralis.id("main"), GROUP);
    }
}
