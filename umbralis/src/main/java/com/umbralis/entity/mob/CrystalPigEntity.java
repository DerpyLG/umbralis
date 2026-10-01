package com.umbralis.entity.mob;

import com.umbralis.registry.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/** Cerdo de Cristal: manso, brillante y fácil de criar. */
public class CrystalPigEntity extends PigEntity {
    public CrystalPigEntity(EntityType<? extends CrystalPigEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PigEntity.createPigAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 14.0);
    }

    @Override
    public CrystalPigEntity createChild(ServerWorld world, PassiveEntity mate) {
        return ModEntities.CRYSTAL_PIG.create(world);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld().isClient && this.random.nextInt(30) == 0) {
            this.getWorld().addParticle(ParticleTypes.GLOW, getParticleX(0.5), getRandomBodyY(), getParticleZ(0.5), 0.0, 0.01, 0.0);
        }
    }
}
