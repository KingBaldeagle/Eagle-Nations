package com.baldeagle.eaglenations.politics;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class NationRank {
    public enum RankType {
        LEADER("Leader", 100, true, true, true, true, true),
        OFFICER("Officer", 75, true, true, true, true, false),
        CITIZEN("Citizen", 50, true, false, false, false, false),
        EXILED("Exiled", 0, false, false, false, false, false);

        private final String displayName;
        private final int weight;
        private final boolean canInvite;
        private final boolean canManageLand;
        private final boolean canManageDiplomacy;
        private final boolean canDeclareWar;
        private final boolean canSetLaws;

        RankType(String displayName, int weight, boolean canInvite, boolean canManageLand, 
                boolean canManageDiplomacy, boolean canDeclareWar, boolean canSetLaws) {
            this.displayName = displayName;
            this.weight = weight;
            this.canInvite = canInvite;
            this.canManageLand = canManageLand;
            this.canManageDiplomacy = canManageDiplomacy;
            this.canDeclareWar = canDeclareWar;
            this.canSetLaws = canSetLaws;
        }

        public String getDisplayName() { return displayName; }
        public int getWeight() { return weight; }
        public boolean canInvite() { return canInvite; }
        public boolean canManageLand() { return canManageLand; }
        public boolean canManageDiplomacy() { return canManageDiplomacy; }
        public boolean canDeclareWar() { return canDeclareWar; }
        public boolean canSetLaws() { return canSetLaws; }

        public static RankType fromString(String name) {
            for (RankType type : values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return CITIZEN;
        }
    }

    private final UUID playerId;
    private RankType rank;
    private long joinedAt;

    public NationRank(UUID playerId, RankType rank) {
        this.playerId = playerId;
        this.rank = rank;
        this.joinedAt = System.currentTimeMillis();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public RankType getRank() {
        return rank;
    }

    public void setRank(RankType rank) {
        this.rank = rank;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public boolean hasPermission(RankType required) {
        return rank.weight >= required.weight;
    }

    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("player_id", playerId);
        tag.putString("rank", rank.name());
        tag.putLong("joined_at", joinedAt);
        return tag;
    }

    public static NationRank deserializeNbt(CompoundTag tag) {
        UUID playerId = tag.getUUID("player_id");
        RankType rank = RankType.fromString(tag.getString("rank"));
        NationRank nationRank = new NationRank(playerId, rank);
        return nationRank;
    }
}