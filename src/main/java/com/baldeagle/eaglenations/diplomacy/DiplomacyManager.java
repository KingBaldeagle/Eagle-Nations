package com.baldeagle.eaglenations.diplomacy;

import com.baldeagle.eaglenations.EagleNations;
import com.baldeagle.eaglenations.Config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DiplomacyManager {
    private final Map<String, Relation> relations = new ConcurrentHashMap<>();
    private final Map<UUID, List<War>> nationWars = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> warDeclarations = new ConcurrentHashMap<>();
    private final Map<UUID, List<War>> warsAgainstUs = new ConcurrentHashMap<>();
    private long warCooldownMs = 24 * 60 * 60 * 1000L; // 24 hours

    public void onNationRemoved(UUID nationId) {
        List<War> wars = nationWars.get(nationId);
        if (wars != null) {
            for (War war : wars) {
                endWar(war, nationId);
            }
        }
        nationWars.remove(nationId);
        
        List<War> defendingWars = warsAgainstUs.get(nationId);
        if (defendingWars != null) {
            for (War war : defendingWars) {
                endWar(war, nationId);
            }
        }
        warsAgainstUs.remove(nationId);
        
        warDeclarations.remove(nationId);
        
        relations.entrySet().removeIf(entry -> entry.getKey().contains(nationId.toString()));
    }

    public boolean canDeclareWar(UUID nationId, UUID targetId) {
        if (!Config.ENABLE_WAR_SYSTEM.get()) {
            return false;
        }
        
        Set<UUID> recentDeclarations = warDeclarations.get(nationId);
        if (recentDeclarations != null) {
            for (UUID target : recentDeclarations) {
                if (target.equals(targetId)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    public boolean declareWar(UUID aggressorId, UUID defenderId, String warGoal) {
        if (!canDeclareWar(aggressorId, defenderId)) {
            return false;
        }

        if (!isRelationAllowed(aggressorId, defenderId, Relation.RelationType.WAR)) {
            return false;
        }

        War war = new War(aggressorId, defenderId);
        war.setWarGoal(warGoal);
        
        UUID key = UUID.randomUUID();
        List<War> wars = nationWars.computeIfAbsent(aggressorId, k -> new ArrayList<>());
        wars.add(war);
        
        List<War> defensiveWars = warsAgainstUs.computeIfAbsent(defenderId, k -> new ArrayList<>());
        defensiveWars.add(war);
        
        Set<UUID> targets = warDeclarations.computeIfAbsent(aggressorId, k -> new HashSet<>());
        targets.add(defenderId);
        
        setRelation(aggressorId, defenderId, Relation.RelationType.WAR);
        
        EagleNations.LOGGER.info("War declared: {} vs {}", aggressorId, defenderId);
        return true;
    }

    public void startWar(War war) {
        war.start();
        EagleNations.LOGGER.info("War {} started", war.getWarId());
    }

    public void endWar(War war, UUID winnerId) {
        war.setState(War.WarState.SURRENDERED);
        
        UUID aggressor = war.getAggressorId();
        UUID defender = war.getDefenderId();
        
        setRelation(aggressor, defender, Relation.RelationType.NEUTRAL);
        
        Set<UUID> decls = warDeclarations.get(aggressor);
        if (decls != null) {
            decls.remove(defender);
        }
        
        EagleNations.LOGGER.info("War {} ended. Winner: {}", war.getWarId(), winnerId);
    }

    public boolean proposeAlliance(UUID proposerId, UUID targetId) {
        if (!isRelationAllowed(proposerId, targetId, Relation.RelationType.ALLY)) {
            return false;
        }
        
        setRelation(proposerId, targetId, Relation.RelationType.ALLY);
        
        EagleNations.LOGGER.info("Alliance formed: {} <-> {}", proposerId, targetId);
        return true;
    }

    public boolean setRelation(UUID nationId1, UUID nationId2, Relation.RelationType type) {
        String key = makeKey(nationId1, nationId2);
        Relation relation = relations.get(key);
        
        if (relation != null) {
            relation.setType(type);
        } else {
            relation = new Relation(nationId1, nationId2, type);
            relations.put(key, relation);
        }
        
        return true;
    }

    public Relation getRelation(UUID nationId1, UUID nationId2) {
        String key = makeKey(nationId1, nationId2);
        return relations.get(key);
    }

    public Relation.RelationType getRelationType(UUID nationId1, UUID nationId2) {
        Relation relation = getRelation(nationId1, nationId2);
        return relation != null ? relation.getType() : Relation.RelationType.NEUTRAL;
    }

    public boolean areAllied(UUID nationId1, UUID nationId2) {
        return getRelationType(nationId1, nationId2) == Relation.RelationType.ALLY;
    }

    public boolean areAtWar(UUID nationId1, UUID nationId2) {
        return getRelationType(nationId1, nationId2) == Relation.RelationType.WAR;
    }

    public boolean isHostile(UUID nationId1, UUID nationId2) {
        Relation.RelationType type = getRelationType(nationId1, nationId2);
        return type == Relation.RelationType.HOSTILE || type == Relation.RelationType.WAR;
    }

    private boolean isRelationAllowed(UUID nationId1, UUID nationId2, Relation.RelationType type) {
        Relation.RelationType current = getRelationType(nationId1, nationId2);
        
        if (type == Relation.RelationType.ALLY) {
            return current == Relation.RelationType.NEUTRAL || current == Relation.RelationType.TRUCE;
        }
        if (type == Relation.RelationType.WAR) {
            return current != Relation.RelationType.ALLY;
        }
        
        return true;
    }

    public List<War> getActiveWars(UUID nationId) {
        List<War> wars = nationWars.get(nationId);
        if (wars == null) {
            return Collections.emptyList();
        }
        
        List<War> active = new ArrayList<>();
        for (War war : wars) {
            if (war.isActive()) {
                active.add(war);
            }
        }
        return active;
    }

    public War getWar(UUID nationId1, UUID nationId2) {
        List<War> wars = nationWars.get(nationId1);
        if (wars != null) {
            for (War war : wars) {
                if (war.involvesNation(nationId2) && war.isActive()) {
                    return war;
                }
            }
        }
        
        List<War> defensiveWars = warsAgainstUs.get(nationId1);
        if (defensiveWars != null) {
            for (War war : defensiveWars) {
                if (war.involvesNation(nationId2) && war.isActive()) {
                    return war;
                }
            }
        }
        
        return null;
    }

    public boolean isPvPEnabled(UUID nationId1, UUID nationId2) {
        if (!Config.ENABLE_WAR_SYSTEM.get()) {
            return false;
        }
        
        return areAtWar(nationId1, nationId2);
    }

    private String makeKey(UUID id1, UUID id2) {
        String s1 = id1.toString();
        String s2 = id2.toString();
        if (s1.compareTo(s2) < 0) {
            return s1 + ":" + s2;
        }
        return s2 + ":" + s1;
    }

    private String makeWarKey(UUID id1, UUID id2) {
        return id1.toString() + "_vs_" + id2.toString();
    }
}