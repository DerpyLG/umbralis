package com.umbralis.entity.boss;

import com.umbralis.entity.mob.VoidHatchlingEntity;
import com.umbralis.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * JEFE 2 — Tejedora del Vacío.
 *  - ESCUDO DE CRÍAS: mientras viva alguna cría es INVULNERABLE y se cura con ellas.
 *  - Al morir la última cría queda EXPUESTA 10 s (x2 de daño) y luego vuelve a invocarlas
 *    (más en las fases siguientes).
 *  - Dispara Redes del Vacío que ralentizan y llenan la arena de telarañas.
 */
public class VoidWeaverEntity extends AbstractUmbralBoss {
    private final List<UUID> minions = new ArrayList<>();
    private int exposedTicks;
    private int webCooldown = 60;
    private boolean warnedShield;

    public VoidWeaverEntity(EntityType<? extends VoidWeaverEntity> type, World world) {
        super(type, world, BossBar.Color.GREEN);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return baseAttributes(380.0, 11.0, 0.34);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new BossMeleeGoal(this, 1.05, true));
        this.goalSelector.add(2, new PounceAtTargetGoal(this, 0.6f));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 24.0f));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 120, 1), this);
        }
        return hit;
    }

    @Override
    protected void bossTick() {
        ServerWorld sw = (ServerWorld) this.getWorld();
        LivingEntity target = this.getTarget();

        boolean hadMinions = !minions.isEmpty();
        minions.removeIf(id -> {
            Entity e = sw.getEntity(id);
            return e == null || !e.isAlive();
        });
        if (hadMinions && minions.isEmpty()) {
            exposedTicks = 200;
            warnedShield = false;
            sw.playSound(null, getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.HOSTILE, 3.0f, 0.5f);
            burst(ParticleTypes.EXPLOSION, 3, 1.5);
            tell(Text.translatable("boss.umbralis.weaver_exposed"), 40);
        }

        if (!minions.isEmpty() && this.age % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(minions.size());
        }

        if (exposedTicks > 0) {
            exposedTicks--;
            if (this.age % 5 == 0) {
                burst(ParticleTypes.ENCHANT, 6, 1.2);
            }
        } else if (minions.isEmpty() && target != null) {
            summonMinions(sw, target);
        }

        if (target != null) {
            if (webCooldown > 0) {
                webCooldown--;
            } else {
                shootWeb(target);
                webCooldown = phase >= 3 ? 40 : phase == 2 ? 60 : 80;
            }
        }
    }

    private void summonMinions(ServerWorld sw, LivingEntity target) {
        int count = phase >= 2 ? 6 : 4;
        for (int i = 0; i < count; i++) {
            VoidHatchlingEntity h = ModEntities.HATCHLING.create(sw);
            if (h == null) continue;
            double a = i * Math.PI * 2 / count;
            h.refreshPositionAndAngles(getX() + Math.cos(a) * 4.5, getY(), getZ() + Math.sin(a) * 4.5, this.random.nextFloat() * 360.0f, 0.0f);
            h.setTarget(target);
            sw.spawnEntity(h);
            minions.add(h.getUuid());
        }
        sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.HOSTILE, 3.0f, 0.5f);
    }

    private void shootWeb(LivingEntity target) {
        WebShotEntity web = new WebShotEntity(this.getWorld(), this);
        web.setPosition(getX(), getEyeY() - 0.3, getZ());
        double dx = target.getX() - web.getX();
        double dy = target.getBodyY(0.33) - web.getY();
        double dz = target.getZ() - web.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        web.setVelocity(dx, dy + horizontal * 0.2, dz, 1.4f, 4.0f);
        this.getWorld().spawnEntity(web);
        this.playSound(SoundEvents.ENTITY_SPIDER_AMBIENT, 2.0f, 1.4f);
    }

    @Override
    protected float modifyIncomingDamage(DamageSource source, float amount) {
        if (!minions.isEmpty()) {
            if (!warnedShield && source.getAttacker() instanceof PlayerEntity p) {
                p.sendMessage(Text.translatable("boss.umbralis.weaver_shielded"), true);
                warnedShield = true;
            }
            return 0.0f;
        }
        return exposedTicks > 0 ? amount * 2.0f : amount;
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (this.getWorld() instanceof ServerWorld sw) {
            for (UUID id : minions) {
                Entity e = sw.getEntity(id);
                if (e != null) e.discard();
            }
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        NbtList list = new NbtList();
        for (UUID id : minions) list.add(NbtHelper.fromUuid(id));
        nbt.put("Minions", list);
        nbt.putInt("Exposed", exposedTicks);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        minions.clear();
        NbtList list = nbt.getList("Minions", NbtElement.INT_ARRAY_TYPE);
        for (int i = 0; i < list.size(); i++) minions.add(NbtHelper.toUuid(list.get(i)));
        exposedTicks = nbt.getInt("Exposed");
    }
}
