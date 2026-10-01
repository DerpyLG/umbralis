package com.umbralis.block;

import com.umbralis.entity.boss.AbstractUmbralBoss;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

/** Altar que invoca a un jefe. Los altares 2 y 3 consumen el trofeo del jefe anterior. */
public class BossAltarBlock extends Block {
    private final Supplier<EntityType<? extends AbstractUmbralBoss>> boss;
    private final Supplier<Item> requiredItem;

    public BossAltarBlock(Settings settings, Supplier<EntityType<? extends AbstractUmbralBoss>> boss, Supplier<Item> requiredItem) {
        super(settings);
        this.boss = boss;
        this.requiredItem = requiredItem;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (world.getDifficulty() == Difficulty.PEACEFUL) {
            player.sendMessage(Text.translatable("message.umbralis.altar_peaceful"), true);
            return ActionResult.CONSUME;
        }
        if (!world.getEntitiesByClass(AbstractUmbralBoss.class, new Box(pos).expand(64), Entity::isAlive).isEmpty()) {
            player.sendMessage(Text.translatable("message.umbralis.altar_busy"), true);
            return ActionResult.CONSUME;
        }
        Item need = requiredItem == null ? null : requiredItem.get();
        if (need != null) {
            ItemStack held = player.getStackInHand(hand);
            if (!held.isOf(need)) {
                player.sendMessage(Text.translatable("message.umbralis.altar_needs", need.getName()), true);
                return ActionResult.CONSUME;
            }
            if (!player.getAbilities().creativeMode) {
                held.decrement(1);
            }
        }
        AbstractUmbralBoss entity = boss.get().create(world);
        if (entity == null) {
            return ActionResult.FAIL;
        }
        entity.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0f, 0.0f);
        ((ServerWorld) world).spawnEntity(entity);
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos.up()));
            bolt.setCosmetic(true);
            world.spawnEntity(bolt);
        }
        return ActionResult.CONSUME;
    }
}
