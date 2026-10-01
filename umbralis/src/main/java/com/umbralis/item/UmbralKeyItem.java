package com.umbralis.item;

import com.umbralis.Umbralis;
import com.umbralis.world.PortalHelper;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UmbralKeyItem extends Item {
    public UmbralKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World world = ctx.getWorld();
        PlayerEntity player = ctx.getPlayer();
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (world.getRegistryKey() == Umbralis.UMBRAL_WORLD) {
            if (player != null) player.sendMessage(Text.translatable("message.umbralis.portal_in_umbral"), true);
            return ActionResult.FAIL;
        }
        if (player == null || !PortalHelper.hasKilledDragon(player)) {
            if (player != null) player.sendMessage(Text.translatable("message.umbralis.dragon_required"), true);
            return ActionResult.FAIL;
        }
        BlockPos start = ctx.getBlockPos().offset(ctx.getSide());
        if (PortalHelper.tryCreatePortal(world, start)) {
            world.playSound(null, start, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 1.0f, 0.7f);
            ctx.getStack().damage(1, player, p -> p.sendToolBreakStatus(ctx.getHand()));
            return ActionResult.CONSUME;
        }
        player.sendMessage(Text.translatable("message.umbralis.portal_failed"), true);
        return ActionResult.FAIL;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.umbralis.umbral_key.tooltip").formatted(Formatting.GRAY));
    }
}
