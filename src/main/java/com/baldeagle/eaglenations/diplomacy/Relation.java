package com.baldeagle.eaglenations.diplomacy;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class Relation {
    public enum RelationType {
        NEUTRAL("Neutral", 0),
        ALLY("Ally", 10),
        HOSTILE("Hostile", -10),
        WAR("War", -100),
        TRUCE("Truce", 5);

        private final String displayName;
        private final int value;

        RelationType(String displayName, int value) {
            this.displayName = displayName;
            this.value = value;
        }

        public String getDisplayName() { return displayName; }
        public int getValue() { return value; }

        public static RelationType fromString(String name) {
            for (RelationType type : values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return NEUTRAL;
        }
    }

    private final UUID nationId1;
    private final UUID nationId2;
    private RelationType type;
    private long establishedAt;
    private long expiresAt;
    private String treatyText;

    public Relation(UUID nationId1, UUID nationId2, RelationType type) {
        this.nationId1 = nationId1;
        this.nationId2 = nationId2;
        this.type = type;
        this.establishedAt = System.currentTimeMillis();
        this.expiresAt = 0;
        this.treatyText = "";
    }

    public UUID getNationId1() { return nationId1; }
    public UUID getNationId2() { return nationId2; }
    public RelationType getType() { return type; }
    public long getEstablishedAt() { return establishedAt; }
    public long getExpiresAt() { return expiresAt; }
    public String getTreatyText() { return treatyText; }

    public void setType(RelationType type) {
        this.type = type;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setTreatyText(String treatyText) {
        this.treatyText = treatyText;
    }

    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }

    public UUID getOtherNation(UUID nationId) {
        return nationId.equals(nationId1) ? nationId2 : nationId1;
    }

    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("nation_1", nationId1);
        tag.putUUID("nation_2", nationId2);
        tag.putString("type", type.name());
        tag.putLong("established_at", establishedAt);
        tag.putLong("expires_at", expiresAt);
        tag.putString("treaty_text", treatyText);
        return tag;
    }

    public static Relation deserializeNbt(CompoundTag tag) {
        UUID nationId1 = tag.getUUID("nation_1");
        UUID nationId2 = tag.getUUID("nation_2");
        RelationType type = RelationType.fromString(tag.getString("type"));
        Relation relation = new Relation(nationId1, nationId2, type);
        relation.setExpiresAt(tag.getLong("expires_at"));
        relation.setTreatyText(tag.getString("treaty_text"));
        return relation;
    }
}