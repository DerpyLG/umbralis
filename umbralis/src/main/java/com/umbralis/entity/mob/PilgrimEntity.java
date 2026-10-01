package com.umbralis.entity.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Peregrino Silente: pacífico. Con un fragmento de amatista bendice al jugador;
 * con la mano vacía susurra pistas sobre los puntos débiles de los jefes.
 */
public class PilgrimEntity extends PathAwareEntity {
    public PilgrimEntity(EntityType<? extends PilgrimEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new EscapeDangerGoal(this, 1.3));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!this.getWorld().isClient) {
            if (stack.isOf(Items.AMETHYST_SHARD)) {
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 3600, 0));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 40, 0));
                ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.HEART, getX(), getBodyY(1.0), getZ(), 6, 0.4, 0.4, 0.4, 0.02);
                player.sendMessage(Text.translatable("pilgrim.umbralis.blessing"), true);
            } else {
                player.sendMessage(Text.translatable("pilgrim.umbralis.lore." + (1 + this.random.nextInt(5))), false);
            }
        }
        return ActionResult.success(this.getWorld().isClient);
    }
}
