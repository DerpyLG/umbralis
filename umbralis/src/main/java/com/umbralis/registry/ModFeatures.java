package com.umbralis.registry;

import com.umbralis.Umbralis;
import com.umbralis.world.feature.BossArenaFeature;
import com.umbralis.world.feature.CrystalTreeFeature;
import com.umbralis.world.feature.SpireFeature;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class ModFeatures {
    public static final Feature<DefaultFeatureConfig> SPIRE = Registry.register(Registries.FEATURE,
            Umbralis.id("spire"), new SpireFeature(DefaultFeatureConfig.CODEC));
    public static final Feature<DefaultFeatureConfig> CRYSTAL_TREE = Registry.register(Registries.FEATURE,
            Umbralis.id("crystal_tree"), new CrystalTreeFeature(DefaultFeatureConfig.CODEC));
    public static final Feature<DefaultFeatureConfig> BOSS_ARENA = Registry.register(Registries.FEATURE,
            Umbralis.id("boss_arena"), new BossArenaFeature(DefaultFeatureConfig.CODEC));

    public static void init() {
    }
}
