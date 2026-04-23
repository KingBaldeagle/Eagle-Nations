package com.baldeagle.eaglenations;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Whether Eagle Nations is enabled")
            .define("enabled", true);

    public static final ModConfigSpec.BooleanValue AUTO_CREATE_NATIONS = BUILDER
            .comment("Automatically create a nation when a new FTB Team is created")
            .define("autoCreateNations", true);

    public static final ModConfigSpec.BooleanValue ENABLE_WAR_SYSTEM = BUILDER
            .comment("Enable war declarations and PvP rules between nations")
            .define("enableWarSystem", true);

    public static final ModConfigSpec.BooleanValue ENABLE_TERRITORY_SYSTEM = BUILDER
            .comment("Enable chunk claiming and territory control")
            .define("enableTerritorySystem", true);

    public static final ModConfigSpec.IntValue MAX_TERRITORY_DISTANCE = BUILDER
            .comment("Maximum distance from capital for territory claims")
            .defineInRange("maxTerritoryDistance", 10, 1, 100);

    public static final ModConfigSpec.IntValue MIN_TEAM_SIZE_FOR_NATION = BUILDER
            .comment("Minimum team size required to form a nation")
            .defineInRange("minTeamSizeForNation", 2, 1, 100);

    public static final ModConfigSpec.BooleanValue ENABLE_TAXES = BUILDER
            .comment("Enable nation taxes and treasury")
            .define("enableTaxes", true);

    public static final ModConfigSpec.BooleanValue ENABLE_RANKS = BUILDER
            .comment("Enable custom nation ranks and permissions")
            .define("enableRanks", true);

    public static final ModConfigSpec.BooleanValue LOG_EVENTS = BUILDER
            .comment("Log nation events for debugging")
            .define("logEvents", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}