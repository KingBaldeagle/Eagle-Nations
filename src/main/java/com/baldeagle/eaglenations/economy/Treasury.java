package com.baldeagle.eaglenations.economy;

import com.baldeagle.eaglenations.Config;
import com.baldeagle.eaglenations.EagleNations;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

/**
 * Treasury manages a nation's economy and taxes.
 * 
 * Lightman's Currency coin denominations (base unit = copper):
 * - 10 Copper = 1 Iron
 * - 10 Iron = 1 Gold
 * - 10 Gold = 1 Emerald
 * - 10 Emerald = 1 Diamond
 * - 10 Diamond = 1 Netherite
 * 
 * Core value = total in lowest denomination (copper)
 * Example: 1 Gold = 10 Iron = 100 Copper = coreValue 100
 */
public class Treasury {
    private final UUID nationId;
    private final long nationIdLong;
    private IBankAccount teamBankAccount;
    private boolean teamAccountLinked = false;
    
    private double incomeTaxRate = 0.0;
    private double landTaxRate = 0.0;
    private double tradeTaxRate = 0.0;
    private boolean autoTaxCollect = false;

    public Treasury(UUID nationId) {
        this.nationId = nationId;
        this.nationIdLong = nationId.getMostSignificantBits();
    }

    public void initialize(MinecraftServer server) {
        if (!Config.ENABLE_TAXES.get()) return;
        
        try {
            linkToBankAccount();
            EagleNations.LOGGER.info("Treasury initialized for nation: {}", nationId);
        } catch (Exception e) {
            EagleNations.LOGGER.warn("Could not link bank account for nation {}: {}", nationId, e.getMessage());
        }
    }

    private void linkToBankAccount() {
        try {
            BankAPI api = BankAPI.getApi();
            List<BankReference> references = api.GetAllBankReferences(false);
            
            for (BankReference ref : references) {
                if (ref instanceof TeamBankReference teamRef) {
                    if (teamRef.teamID == nationIdLong) {
                        this.teamBankAccount = teamRef.get();
                        this.teamAccountLinked = true;
                        EagleNations.LOGGER.info("Linked to team bank account for nation: {}", nationId);
                        return;
                    }
                }
            }
            
            EagleNations.LOGGER.info("No team bank account found for nation {}. Tax treasury ready.", nationId);
            
        } catch (Exception e) {
            EagleNations.LOGGER.warn("Error linking bank account: {}", e.getMessage());
        }
    }

    public boolean hasTeamAccount() {
        return teamAccountLinked && teamBankAccount != null;
    }

    public IBankAccount getTeamAccount() {
        return teamBankAccount;
    }

    public boolean isActive() {
        return Config.ENABLE_TAXES.get();
    }

    public UUID getNationId() {
        return nationId;
    }

    public String getBalanceText() {
        if (!hasTeamAccount()) {
            return "No team account";
        }
        return teamBankAccount.getBalanceText().getString();
    }

    public long getTeamBalanceValue() {
        if (!hasTeamAccount()) {
            return 0L;
        }
        var values = teamBankAccount.getMoneyStorage().allValues();
        long total = 0;
        for (MoneyValue v : values) {
            total += v.getCoreValue();
        }
        return total;
    }

    public long getTotalMembersMoney(List<ServerPlayer> teamMembers) {
        long total = 0L;
        
        try {
            BankAPI api = BankAPI.getApi();
            List<BankReference> references = api.GetAllBankReferences(false);
            
            for (ServerPlayer player : teamMembers) {
                for (BankReference ref : references) {
                    if (ref instanceof PlayerBankReference playerRef) {
                        var playerRef2 = playerRef.getPlayer();
                        if (playerRef2 != null && playerRef2.id.equals(player.getUUID())) {
                            IBankAccount account = playerRef.get();
                            if (account != null) {
                                var values = account.getMoneyStorage().allValues();
                                for (MoneyValue v : values) {
                                    total += v.getCoreValue();
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            EagleNations.LOGGER.warn("Error getting member money: {}", e.getMessage());
        }
        
        return total;
    }

    public boolean canAfford(long value) {
        return hasTeamAccount() && getTeamBalanceValue() >= value;
    }

    public void setIncomeTaxRate(double rate) {
        this.incomeTaxRate = Math.max(0, Math.min(1.0, rate));
    }

    public void setLandTaxRate(double rate) {
        this.landTaxRate = Math.max(0, Math.min(1.0, rate));
    }

    public void setTradeTaxRate(double rate) {
        this.tradeTaxRate = Math.max(0, Math.min(1.0, rate));
    }

    public void setAutoTaxCollect(boolean auto) {
        this.autoTaxCollect = auto;
    }

    public double getIncomeTaxRate() { return incomeTaxRate; }
    public double getLandTaxRate() { return landTaxRate; }
    public double getTradeTaxRate() { return tradeTaxRate; }
    public boolean isAutoTaxCollect() { return autoTaxCollect; }

    public long calculateIncomeTax(long membersTotalValue) {
        return (long)(membersTotalValue * incomeTaxRate);
    }

    /**
     * Calculate land tax based on chunk count.
     * 
     * Pricing tiers (first 10 chunks are free):
     * - Chunks 1-10: 0 (free)
     * - Chunks 11-12: 1 copper each
     * - Chunks 13-14: 5 copper each (1 iron)
     * - Chunks 15-16: 10 copper each (1 iron, 5 copper)
     * - Chunks 17-18: 15 copper each
     * 
     * Price increases by 5 copper every 2 chunks after the first paid tier.
     * 
     * @param chunkCount Number of chunks claimed by the team
     * @return Total tax in copper
     */
    public long calculateLandTax(int chunkCount) {
        if (chunkCount <= 10) {
            return 0;
        }
        
        long totalTax = 0;
        int paidChunks = chunkCount - 10;  // Chunks 11 onwards
        
        for (int i = 0; i < paidChunks; i++) {
            int chunkNumber = i + 11;  // Starting from chunk 11
            int tier = (chunkNumber - 11) / 2;  // Tier increases every 2 chunks
            long pricePerChunk;
            
            if (tier == 0) {
                pricePerChunk = 1;  // Chunks 11-12: 1 copper
            } else {
                pricePerChunk = tier * 5;  // Chunks 13+: increases by 5 copper per tier
            }
            
            totalTax += pricePerChunk;
        }
        
        return totalTax;
    }
    
    /**
     * Get the price for a specific chunk number (1-indexed).
     * @param chunkNumber The chunk number (1-based)
     * @return Price in copper for that specific chunk
     */
    public long getChunkPrice(int chunkNumber) {
        if (chunkNumber <= 10) {
            return 0;
        }
        
        int tier = (chunkNumber - 11) / 2;
        
        if (tier == 0) {
            return 1;
        }
        
        return tier * 5;
    }

    public long calculateTradeTax(long tradeValue) {
        return (long)(tradeValue * tradeTaxRate);
    }

    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("income_tax_rate", incomeTaxRate);
        tag.putDouble("land_tax_rate", landTaxRate);
        tag.putDouble("trade_tax_rate", tradeTaxRate);
        tag.putBoolean("auto_tax_collect", autoTaxCollect);
        return tag;
    }

    public static Treasury deserializeNbt(CompoundTag tag) {
        UUID nationId = tag.getUUID("nation_id");
        Treasury treasury = new Treasury(nationId);
        treasury.incomeTaxRate = tag.getDouble("income_tax_rate");
        treasury.landTaxRate = tag.getDouble("land_tax_rate");
        treasury.tradeTaxRate = tag.getDouble("trade_tax_rate");
        treasury.autoTaxCollect = tag.getBoolean("auto_tax_collect");
        return treasury;
    }
}