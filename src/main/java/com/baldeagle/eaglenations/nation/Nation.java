package com.baldeagle.eaglenations.nation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.time.Instant;
import java.util.UUID;

public class Nation {
    private final UUID teamId;
    private final String displayName;
    private final int color;
    private final long createdAt;

    public Nation(UUID teamId, String displayName, int color) {
        this.teamId = teamId;
        this.displayName = displayName;
        this.color = color;
        this.createdAt = Instant.now().getEpochSecond();
    }

    private Nation(UUID teamId, String displayName, int color, long createdAt) {
        this.teamId = teamId;
        this.displayName = displayName;
        this.color = color;
        this.createdAt = createdAt;
    }

    public static Nation fromTeam(Object team) {
        return new Nation(
                UUID.randomUUID(),
                "Unnamed Nation",
                0xFFFFFF
        );
    }

    public UUID getTeamId() {
        return teamId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Component getDisplayNameComponent() {
        return Component.literal(displayName);
    }

    public int getColor() {
        return color;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Nation setColor(int color) {
        return new Nation(teamId, displayName, color, createdAt);
    }

    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("team_id", teamId);
        tag.putString("display_name", displayName);
        tag.putInt("color", color);
        tag.putLong("created_at", createdAt);
        return tag;
    }

    public static Nation deserializeNbt(CompoundTag tag) {
        UUID teamId = tag.getUUID("team_id");
        String displayName = tag.getString("display_name");
        int color = tag.getInt("color");
        long createdAt = tag.getLong("created_at");
        return new Nation(teamId, displayName, color, createdAt);
    }
}