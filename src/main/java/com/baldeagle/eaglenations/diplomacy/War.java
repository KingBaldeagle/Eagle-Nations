package com.baldeagle.eaglenations.diplomacy;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class War {
    public enum WarState {
        DECLARED("Declared", 0),
        ACTIVE("Active", 1),
        CAPTURING("Capturing", 2),
        PEACE_NEGOTIATED("Peace Negotiated", 3),
        SURRENDERED("Surrendered", 4);

        private final String displayName;
        private final int value;

        WarState(String displayName, int value) {
            this.displayName = displayName;
            this.value = value;
        }

        public String getDisplayName() { return displayName; }
        public int getValue() { return value; }
    }

    private final UUID warId;
    private final UUID aggressorId;
    private final UUID defenderId;
    private WarState state;
    private long declaredAt;
    private long startedAt;
    private long endedAt;
    private String warGoal;
    private int aggressiveScore;
    private int defensiveScore;
    private int winCondition; // chunks to capture to win

    public War(UUID aggressorId, UUID defenderId) {
        this.warId = UUID.randomUUID();
        this.aggressorId = aggressorId;
        this.defenderId = defenderId;
        this.state = WarState.DECLARED;
        this.declaredAt = System.currentTimeMillis();
        this.startedAt = 0;
        this.endedAt = 0;
        this.warGoal = "";
        this.aggressiveScore = 0;
        this.defensiveScore = 0;
        this.winCondition = 5;
    }

    public UUID getWarId() { return warId; }
    public UUID getAggressorId() { return aggressorId; }
    public UUID getDefenderId() { return defenderId; }
    public WarState getState() { return state; }
    public long getDeclaredAt() { return declaredAt; }
    public long getStartedAt() { return startedAt; }
    public long getEndedAt() { return endedAt; }
    public String getWarGoal() { return warGoal; }
    public int getAggressiveScore() { return aggressiveScore; }
    public int getDefensiveScore() { return defensiveScore; }
    public int getWinCondition() { return winCondition; }

    public void setState(WarState state) { this.state = state; }
    public void setWarGoal(String warGoal) { this.warGoal = warGoal; }
    public void setWinCondition(int winCondition) { this.winCondition = winCondition; }

    public void start() {
        this.state = WarState.ACTIVE;
        this.startedAt = System.currentTimeMillis();
    }

    public void addAggressorScore(int points) {
        this.aggressiveScore += points;
        checkWinCondition();
    }

    public void addDefensiveScore(int points) {
        this.defensiveScore += points;
    }

    public boolean isAggressorWinning() {
        return aggressiveScore >= winCondition;
    }

    public boolean isDefenderWinning() {
        return defensiveScore >= winCondition;
    }

    private void checkWinCondition() {
        if (aggressiveScore >= winCondition) {
            this.state = WarState.SURRENDERED;
            this.endedAt = System.currentTimeMillis();
        }
    }

    public boolean isActive() {
        return state == WarState.ACTIVE || state == WarState.CAPTURING;
    }

    public boolean involvesNation(UUID nationId) {
        return aggressorId.equals(nationId) || defenderId.equals(nationId);
    }

    public boolean isWarWinner(UUID nationId) {
        if (endedAt == 0) return false;
        if (aggressorId.equals(nationId)) return aggressiveScore >= winCondition;
        if (defenderId.equals(nationId)) return defensiveScore >= winCondition;
        return false;
    }

    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("war_id", warId);
        tag.putUUID("aggressor", aggressorId);
        tag.putUUID("defender", defenderId);
        tag.putString("state", state.name());
        tag.putLong("declared_at", declaredAt);
        tag.putLong("started_at", startedAt);
        tag.putLong("ended_at", endedAt);
        tag.putString("war_goal", warGoal);
        tag.putInt("aggressive_score", aggressiveScore);
        tag.putInt("defensive_score", defensiveScore);
        tag.putInt("win_condition", winCondition);
        return tag;
    }

    public static War deserializeNbt(CompoundTag tag) {
        UUID warId = tag.getUUID("war_id");
        UUID aggressorId = tag.getUUID("aggressor");
        UUID defenderId = tag.getUUID("defender");
        War war = new War(aggressorId, defenderId);
        war.setState(WarState.valueOf(tag.getString("state")));
        war.setWarGoal(tag.getString("war_goal"));
        if (tag.contains("started_at")) {
            war.startedAt = tag.getLong("started_at");
        }
        if (tag.contains("ended_at")) {
            war.endedAt = tag.getLong("ended_at");
        }
        if (tag.contains("aggressive_score")) {
            war.aggressiveScore = tag.getInt("aggressive_score");
        }
        if (tag.contains("defensive_score")) {
            war.defensiveScore = tag.getInt("defensive_score");
        }
        if (tag.contains("win_condition")) {
            war.winCondition = tag.getInt("win_condition");
        }
        return war;
    }
}