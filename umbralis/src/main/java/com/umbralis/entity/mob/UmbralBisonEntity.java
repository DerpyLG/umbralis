package com.umbralis.entity.mob;

import com.umbralis.registry.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/** Bisonte Etéreo: una vaca luminosa que se puede ordeñar y criar con trigo. */
public class UmbralBisonEntity extends CowEntity {
    public UmbralBisonEntity(EntityType<? extends UmbralBisonEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return CowEntity.createCowAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0);
    }

    @Override
    public UmbralBisonEntity createChild(ServerWorld world, PassiveEntity mate) {
        return ModEntities.BISON.create(world);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld().isClient && this.random.nextInt(25) == 0) {
            this.getWorld().addParticle(ParticleTypes.END_ROD, getParticleX(0.5), getRandomBodyY(), getParticleZ(0.5), 0.0, 0.02, 0.0);
        }
    }
}
