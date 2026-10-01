package com.umbralis;

import com.umbralis.registry.ModBlocks;
import com.umbralis.registry.ModEntities;
import com.umbralis.registry.ModFeatures;
import com.umbralis.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Umbralis implements ModInitializer {
    public static final String MOD_ID = "umbralis";
    public static final RegistryKey<World> UMBRAL_WORLD = RegistryKey.of(RegistryKeys.WORLD, id("umbral"));

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModEntities.init();
        ModItems.init();
        ModFeatures.init();
    }
}
