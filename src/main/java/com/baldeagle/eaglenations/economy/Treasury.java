package com.baldeagle.eaglenations.economy;

import com.baldeagle.eaglenations.Config;
import com.baldeagle.eaglenations.EagleNations;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.UUID;

public class Treasury {
    private final UUID nationId;
    private final long nationIdLong;
    private IBankAccount bankAccount;
    private boolean accountLinked = false;
    
    private double incomeTaxRate = 0.0;
    private double landTaxRate = 0.0;
    private long landTaxPerChunkCents = 100L;
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
                        this.bankAccount = teamRef.get();
                        this.accountLinked = true;
                        EagleNations.LOGGER.info("Linked to bank account for nation: {}", nationId);
                        return;
                    }
                }
            }
            
            EagleNations.LOGGER.info("No bank account found for nation {}. Tax treasury ready.", nationId);
            
        } catch (Exception e) {
            EagleNations.LOGGER.warn("Error linking bank account: {}", e.getMessage());
        }
    }

    public boolean hasBankAccount() {
        return accountLinked && bankAccount != null;
    }

    public boolean isActive() {
        return Config.ENABLE_TAXES.get();
    }

    public UUID getNationId() {
        return nationId;
    }

    public String getBalanceText() {
        if (!hasBankAccount()) {
            return "No account";
        }
        return bankAccount.getBalanceText().getString();
    }

    public IBankAccount getBankAccount() {
        return bankAccount;
    }

    public BankAPI getBankAPI() {
        return BankAPI.getApi();
    }

    public void setIncomeTaxRate(double rate) {
        this.incomeTaxRate = Math.max(0, Math.min(1.0, rate));
    }

    public void setLandTaxRate(double rate) {
        this.landTaxRate = Math.max(0, Math.min(1.0, rate));
    }

    public void setLandTaxPerChunk(long cents) {
        this.landTaxPerChunkCents = cents;
    }

    public void setTradeTaxRate(double rate) {
        this.tradeTaxRate = Math.max(0, Math.min(1.0, rate));
    }

    public void setAutoTaxCollect(boolean auto) {
        this.autoTaxCollect = auto;
    }

    public double getIncomeTaxRate() { return incomeTaxRate; }
    public double getLandTaxRate() { return landTaxRate; }
    public long getLandTaxPerChunk() { return landTaxPerChunkCents; }
    public double getTradeTaxRate() { return tradeTaxRate; }
    public boolean isAutoTaxCollect() { return autoTaxCollect; }

    public long calculateIncomeTax(long playerBalanceCents) {
        return (long)(playerBalanceCents * incomeTaxRate);
    }

    public long calculateLandTax(int chunkCount) {
        return chunkCount * landTaxPerChunkCents;
    }

    public long calculateTradeTax(long tradeValueCents) {
        return (long)(tradeValueCents * tradeTaxRate);
    }

    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("income_tax_rate", incomeTaxRate);
        tag.putDouble("land_tax_rate", landTaxRate);
        tag.putLong("land_tax_per_chunk", landTaxPerChunkCents);
        tag.putDouble("trade_tax_rate", tradeTaxRate);
        tag.putBoolean("auto_tax_collect", autoTaxCollect);
        return tag;
    }

    public static Treasury deserializeNbt(CompoundTag tag) {
        UUID nationId = tag.getUUID("nation_id");
        Treasury treasury = new Treasury(nationId);
        treasury.incomeTaxRate = tag.getDouble("income_tax_rate");
        treasury.landTaxRate = tag.getDouble("land_tax_rate");
        treasury.landTaxPerChunkCents = tag.getLong("land_tax_per_chunk");
        treasury.tradeTaxRate = tag.getDouble("trade_tax_rate");
        treasury.autoTaxCollect = tag.getBoolean("auto_tax_collect");
        return treasury;
    }
}