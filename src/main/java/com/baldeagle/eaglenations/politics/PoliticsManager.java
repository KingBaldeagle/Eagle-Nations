package com.baldeagle.eaglenations.politics;

import com.baldeagle.eaglenations.EagleNations;
import com.baldeagle.eaglenations.nation.Nation;
import com.baldeagle.eaglenations.nation.NationManager;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PoliticsManager {
    private final Map<UUID, Map<UUID, NationRank>> nationMembers = new ConcurrentHashMap<>();
    private final Map<UUID, LawSystem> nationLaws = new ConcurrentHashMap<>();
    private final NationManager nationManager;
    private final MinecraftServer server;

    public PoliticsManager(NationManager nationManager, MinecraftServer server) {
        this.nationManager = nationManager;
        this.server = server;
    }

    public void onNationCreated(UUID nationId) {
        nationMembers.put(nationId, new ConcurrentHashMap<>());
        nationLaws.put(nationId, new LawSystem(nationId));
        EagleNations.LOGGER.info("Initialized politics for nation: {}", nationId);
    }

    public void onNationRemoved(UUID nationId) {
        nationMembers.remove(nationId);
        nationLaws.remove(nationId);
    }

    public void addMember(UUID nationId, UUID playerId) {
        Map<UUID, NationRank> members = nationMembers.get(nationId);
        if (members == null) {
            members = new ConcurrentHashMap<>();
            nationMembers.put(nationId, members);
        }
        
        NationRank.RankType rank = (members.isEmpty()) ? NationRank.RankType.LEADER : NationRank.RankType.CITIZEN;
        members.put(playerId, new NationRank(playerId, rank));
        
        EagleNations.LOGGER.info("Added player {} to nation {} as {}", playerId, nationId, rank);
    }

    public void removeMember(UUID nationId, UUID playerId) {
        Map<UUID, NationRank> members = nationMembers.get(nationId);
        if (members != null) {
            members.remove(playerId);
        }
    }

    public void setMemberRank(UUID nationId, UUID playerId, NationRank.RankType rank) {
        Map<UUID, NationRank> members = nationMembers.get(nationId);
        if (members != null) {
            NationRank nationRank = members.get(playerId);
            if (nationRank != null) {
                nationRank.setRank(rank);
            }
        }
    }

    public NationRank getMemberRank(UUID nationId, UUID playerId) {
        Map<UUID, NationRank> members = nationMembers.get(nationId);
        return members != null ? members.get(playerId) : null;
    }

    public Collection<NationRank> getMembers(UUID nationId) {
        Map<UUID, NationRank> members = nationMembers.get(nationId);
        return members != null ? members.values() : Collections.emptyList();
    }

    public boolean hasPermission(UUID nationId, UUID playerId, NationRank.RankType required) {
        NationRank rank = getMemberRank(nationId, playerId);
        return rank != null && rank.hasPermission(required);
    }

    public LawSystem getLawSystem(UUID nationId) {
        return nationLaws.get(nationId);
    }

    public boolean isLawAllowed(UUID nationId, Law.LawType type) {
        LawSystem laws = nationLaws.get(nationId);
        return laws != null ? laws.isAllowed(type) : true;
    }

    public boolean canManageLand(UUID nationId, UUID playerId) {
        return hasPermission(nationId, playerId, NationRank.RankType.OFFICER);
    }

    public boolean canManageDiplomacy(UUID nationId, UUID playerId) {
        return hasPermission(nationId, playerId, NationRank.RankType.OFFICER);
    }

    public boolean canDeclareWar(UUID nationId, UUID playerId) {
        return hasPermission(nationId, playerId, NationRank.RankType.LEADER);
    }

    public boolean canSetLaws(UUID nationId, UUID playerId) {
        return hasPermission(nationId, playerId, NationRank.RankType.LEADER);
    }

    public boolean canInvite(UUID nationId, UUID playerId) {
        return hasPermission(nationId, playerId, NationRank.RankType.CITIZEN);
    }
}