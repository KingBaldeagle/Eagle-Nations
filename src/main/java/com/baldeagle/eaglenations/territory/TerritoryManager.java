package com.baldeagle.eaglenations.territory;

import com.baldeagle.eaglenations.EagleNations;
import com.baldeagle.eaglenations.nation.NationManager;
import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.ClaimedChunkManager;
import dev.ftb.mods.ftbchunks.api.ClaimResult;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class TerritoryManager {
    private NationManager nationManager;
    private ServerLevel world;

    public TerritoryManager(NationManager nationManager, ServerLevel world) {
        this.nationManager = nationManager;
        this.world = world;
    }

    public boolean isChunkClaimedByNation(ChunkPos chunkPos, UUID nationTeamId) {
        ClaimedChunkManager manager = FTBChunksAPI.api().getManager();
        ClaimedChunk chunk = manager.getChunk(new ChunkDimPos(world.dimension(), chunkPos.x, chunkPos.z));
        if (chunk == null) {
            return false;
        }
        return chunk.getTeamData().getTeam().getId().equals(nationTeamId);
    }

    public Optional<ClaimedChunk> getChunkClaim(ChunkPos chunkPos) {
        ClaimedChunkManager manager = FTBChunksAPI.api().getManager();
        ClaimedChunk chunk = manager.getChunk(new ChunkDimPos(world.dimension(), chunkPos.x, chunkPos.z));
        return Optional.ofNullable(chunk);
    }

    public Collection<ClaimedChunk> getAllClaimedChunks() {
        ClaimedChunkManager manager = FTBChunksAPI.api().getManager();
        Collection<? extends ClaimedChunk> chunks = manager.getAllClaimedChunks();
        return chunks != null ? (Collection<ClaimedChunk>) chunks : Collections.emptyList();
    }

    public int getNationClaimCount(UUID nationTeamId) {
        return (int) getAllClaimedChunks().stream()
                .filter(c -> c.getTeamData().getTeam().getId().equals(nationTeamId))
                .count();
    }

    public boolean hasClaimedChunks(UUID nationTeamId) {
        return getNationClaimCount(nationTeamId) > 0;
    }

    public ClaimResult tryClaimChunk(ServerPlayer player, ChunkPos pos) {
        return FTBChunksAPI.api().claimAsPlayer(player, world.dimension(), pos, false);
    }

    public ClaimResult tryCheckClaim(ServerPlayer player, ChunkPos pos) {
        return FTBChunksAPI.api().claimAsPlayer(player, world.dimension(), pos, true);
    }
}