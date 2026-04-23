package com.baldeagle.eaglenations.economy;

import com.baldeagle.eaglenations.Config;
import com.baldeagle.eaglenations.EagleNations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TreasuryManager {
    private final java.util.Map<UUID, Treasury> treasuries = new ConcurrentHashMap<>();
    private MinecraftServer server;

    public TreasuryManager(MinecraftServer server) {
        this.server = server;
    }

    public void onNationCreated(UUID nationId) {
        if (!Config.ENABLE_TAXES.get()) return;
        
        Treasury treasury = new Treasury(nationId);
        treasury.initialize(server);
        treasuries.put(nationId, treasury);
        EagleNations.LOGGER.info("Treasury created for nation: {}", nationId);
    }

    public void onNationRemoved(UUID nationId) {
        treasuries.remove(nationId);
    }

    public Treasury getTreasury(UUID nationId) {
        return treasuries.get(nationId);
    }

    public Treasury getOrCreate(UUID nationId) {
        return treasuries.computeIfAbsent(nationId, id -> {
            Treasury t = new Treasury(id);
            t.initialize(server);
            return t;
        });
    }

    public boolean isTaxEnabled() {
        return Config.ENABLE_TAXES.get();
    }

    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        
        ListTag list = new ListTag();
        for (java.util.Map.Entry<UUID, Treasury> e : treasuries.entrySet()) {
            CompoundTag t = e.getValue().serializeNbt();
            t.putUUID("nation_id", e.getKey());
            list.add(t);
        }
        tag.put("treasuries", list);
        
        return tag;
    }

    public void deserializeNbt(CompoundTag tag) {
        if (!Config.ENABLE_TAXES.get()) return;
        
        ListTag list = tag.getList("treasuries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            Treasury treasury = Treasury.deserializeNbt(list.getCompound(i));
            treasuries.put(treasury.getNationId(), treasury);
        }
        
        EagleNations.LOGGER.info("Loaded {} treasuries", treasuries.size());
    }
}