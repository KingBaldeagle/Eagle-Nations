package com.baldeagle.eaglenations.ftb;

import com.baldeagle.eaglenations.Config;
import com.baldeagle.eaglenations.EagleNations;
import com.baldeagle.eaglenations.nation.Nation;
import com.baldeagle.eaglenations.nation.NationManager;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLeftPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.api.event.TeamManagerEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class FTBTeamBridge {
    private NationManager nationManager;
    private MinecraftServer server;

    public FTBTeamBridge(NationManager nationManager, MinecraftServer server) {
        this.nationManager = nationManager;
        this.server = server;
    }

    public static void register(IEventBus eventBus) {
        eventBus.register(new FTBTeamBridge(null, null));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTeamCreated(TeamCreatedEvent event) {
        Team team = event.getTeam();
        EagleNations.LOGGER.info("Team created: {} ({})", team.getName(), team.getId());

        if (nationManager != null && Config.AUTO_CREATE_NATIONS.get()) {
            Nation nation = Nation.fromTeam(team);
            nationManager.createNation(nation);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTeamManagerLoaded(TeamManagerEvent event) {
        TeamManager manager = event.getManager();
        EagleNations.LOGGER.info("FTB Teams manager loaded with {} teams", manager.getTeams().size());

        if (server != null && nationManager != null) {
            for (Team team : manager.getTeams()) {
                Optional<Nation> existing = nationManager.getNation(team.getId());
                if (existing.isEmpty()) {
                    Nation nation = Nation.fromTeam(team);
                    nationManager.createNation(nation);
                    EagleNations.LOGGER.info("Synced team {} as nation", team.getName());
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerJoinedTeam(PlayerJoinedPartyTeamEvent event) {
        Team team = event.getTeam();
        ServerPlayer player = event.getPlayer();
        String playerName = player.getName().getString();
        EagleNations.LOGGER.info("Player {} joined team {}", playerName, team.getName());

        if (nationManager != null) {
            nationManager.getNation(team.getId()).ifPresent(nation -> {
                EagleNations.LOGGER.info("Player {} is now member of nation {}", playerName, nation.getDisplayName());
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerLeftTeam(PlayerLeftPartyTeamEvent event) {
        Team team = event.getTeam();
        ServerPlayer player = event.getPlayer();
        String playerName = player.getName().getString();
        EagleNations.LOGGER.info("Player {} left team {}", playerName, team.getName());
    }

    public TeamManager getTeamManager() {
        return FTBTeamsAPI.api().getManager();
    }

    public Optional<Team> getTeam(UUID teamId) {
        TeamManager manager = getTeamManager();
        Collection<Team> teams = manager.getTeams();
        return teams.stream()
                .filter(team -> team.getId().equals(teamId))
                .findFirst();
    }
}