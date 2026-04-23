package com.baldeagle.eaglenations.politics;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class Law {
    public enum LawType {
        BUILD("Build", "Allow building in territory"),
        INTERACT("Interact", "Allow block interaction"),
        CONTAINER("Container", "Allow container access"),
        PVP("PvP", "Allow PvP in territory"),
        EXPLOSION("Explosion", "Allow explosions"),
        MOB_GRIEFING("MobGriefing", "Allow mob griefing"),
        ENTRY("Entry", "Allow non-members to enter");

        private final String name;
        private final String description;

        LawType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    private final UUID lawId;
    private final LawType type;
    private final boolean allowed;
    private final boolean whitelist;
    private String description;

    public Law(LawType type, boolean allowed) {
        this(UUID.randomUUID(), type, allowed);
    }

    public Law(UUID lawId, LawType type, boolean allowed) {
        this.lawId = lawId;
        this.type = type;
        this.allowed = allowed;
        this.whitelist = false;
        this.description = type.getDescription();
    }

    public UUID getLawId() {
        return lawId;
    }

    public LawType getType() {
        return type;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean applies(boolean defaultValue) {
        if (whitelist) {
            return !allowed;
        }
        return allowed;
    }

    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("law_id", lawId);
        tag.putString("type", type.name());
        tag.putBoolean("allowed", allowed);
        tag.putBoolean("whitelist", whitelist);
        tag.putString("description", description);
        return tag;
    }

    public static Law deserializeNbt(CompoundTag tag) {
        UUID lawId = tag.getUUID("law_id");
        LawType type = Law.LawType.valueOf(tag.getString("type"));
        boolean allowed = tag.getBoolean("allowed");
        Law law = new Law(lawId, type, allowed);
        law.setDescription(tag.getString("description"));
        return law;
    }
}