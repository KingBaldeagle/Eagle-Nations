package com.baldeagle.eaglenations.nation;

import com.baldeagle.eaglenations.EagleNations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NationManager {
    private static final String DATA_FILE = "eaglenations_nations.dat";
    private static final LevelResource DATA_RESOURCE = new LevelResource(DATA_FILE);

    private static final int DATA_VERSION = 1;

    private final Map<UUID, Nation> nations = new ConcurrentHashMap<>();
    private final MinecraftServer server;

    public NationManager(MinecraftServer server) {
        this.server = server;
    }

    public Optional<Nation> getNation(UUID teamId) {
        return Optional.ofNullable(nations.get(teamId));
    }

    public Optional<Nation> getNationByTeamId(UUID teamId) {
        return nations.values().stream()
                .filter(nation -> nation.getTeamId().equals(teamId))
                .findFirst();
    }

    public Collection<Nation> getAllNations() {
        return Collections.unmodifiableCollection(nations.values());
    }

    public void createNation(Nation nation) {
        nations.put(nation.getTeamId(), nation);
        EagleNations.LOGGER.info("Created nation: {} for team: {}", nation.getDisplayName(), nation.getTeamId());
        markDirty();
    }

    public void removeNation(UUID teamId) {
        Nation removed = nations.remove(teamId);
        if (removed != null) {
            EagleNations.LOGGER.info("Removed nation: {}", removed.getDisplayName());
            markDirty();
        }
    }

    public void updateNation(Nation nation) {
        nations.put(nation.getTeamId(), nation);
        markDirty();
    }

    public void load() {
        Path dataPath = server.getWorldPath(DATA_RESOURCE);

        if (!Files.exists(dataPath)) {
            EagleNations.LOGGER.info("No nation data found, starting fresh");
            return;
        }

        try (DataInputStream dis = new DataInputStream(Files.newInputStream(dataPath))) {
            CompoundTag rootTag = NbtIo.read(dis);
            if (rootTag == null) {
                return;
            }

            int dataVersion = rootTag.contains("version") ? rootTag.getInt("version") : 0;
            if (dataVersion < DATA_VERSION) {
                EagleNations.LOGGER.warn("Old data version {} migrating to {}", dataVersion, DATA_VERSION);
            }

            ListTag nationsList = rootTag.getList("nations", Tag.TAG_COMPOUND);
            for (int i = 0; i < nationsList.size(); i++) {
                CompoundTag nationTag = nationsList.getCompound(i);
                Nation nation = Nation.deserializeNbt(nationTag);
                nations.put(nation.getTeamId(), nation);
            }

            EagleNations.LOGGER.info("Loaded {} nations", nations.size());
        } catch (Exception e) {
            EagleNations.LOGGER.error("Failed to load nation data", e);
        }
    }

    public void save() {
        Path dataPath = server.getWorldPath(DATA_RESOURCE);

        ListTag nationsList = new ListTag();
        for (Nation nation : nations.values()) {
            nationsList.add(nation.serializeNbt());
        }

        CompoundTag rootTag = new CompoundTag();
        rootTag.putInt("version", DATA_VERSION);
        rootTag.put("nations", nationsList);

        try {
            Files.createDirectories(dataPath.getParent());
            try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(dataPath))) {
                NbtIo.write(rootTag, dos);
            }
        } catch (IOException e) {
            EagleNations.LOGGER.error("Failed to save nation data", e);
        }
    }

    private void markDirty() {
    }
}