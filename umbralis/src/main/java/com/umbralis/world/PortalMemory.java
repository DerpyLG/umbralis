package com.umbralis.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

/** Recuerda desde qué portal entró cada jugador para devolverle al mismo sitio. */
public class PortalMemory extends PersistentState {
    public record Entry(RegistryKey<World> dimension, BlockPos pos) {
    }

    private final Map<UUID, NbtCompound> map = new HashMap<>();

    public static PortalMemory get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(PortalMemory::fromNbt, PortalMemory::new, "umbralis_portals");
    }

    public static PortalMemory fromNbt(NbtCompound nbt) {
        PortalMemory memory = new PortalMemory();
        NbtList list = nbt.getList("entries", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound c = list.getCompound(i);
            memory.map.put(c.getUuid("id"), c);
        }
        return memory;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        list.addAll(map.values());
        nbt.put("entries", list);
        return nbt;
    }

    public void remember(UUID player, RegistryKey<World> dimension, BlockPos pos) {
        NbtCompound c = new NbtCompound();
        c.putUuid("id", player);
        c.putString("dim", dimension.getValue().toString());
        c.putInt("x", pos.getX());
        c.putInt("y", pos.getY());
        c.putInt("z", pos.getZ());
        map.put(player, c);
        markDirty();
    }

    public Entry get(UUID player) {
        NbtCompound c = map.get(player);
        if (c == null) {
            return null;
        }
        RegistryKey<World> dim = RegistryKey.of(RegistryKeys.WORLD, new Identifier(c.getString("dim")));
        return new Entry(dim, new BlockPos(c.getInt("x"), c.getInt("y"), c.getInt("z")));
    }
}
