package com.baldeagle.eaglenations.economy;

import com.baldeagle.eaglenations.Config;
import com.baldeagle.eaglenations.EagleNations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public class Treasury {
    private final UUID nationId;
    private boolean isActive = false;
    
    private double incomeTaxRate = 0.0;
    private double landTaxRate = 0.0;
    private double tradeTaxRate = 0.0;
    private boolean autoTaxCollect = false;

    public Treasury(UUID nationId) {
        this.nationId = nationId;
        this.isActive = Config.ENABLE_TAXES.get();
    }

    public void initialize(MinecraftServer server) {
        if (!Config.ENABLE_TAXES.get()) return;
        EagleNations.LOGGER.info("Treasury initialized for nation: {}", nationId);
    }

    public boolean isActive() {
        return isActive && Config.ENABLE_TAXES.get();
    }

    public UUID getNationId() {
        return nationId;
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

    public double calculateIncomeTax(double playerIncome) {
        return playerIncome * incomeTaxRate;
    }

    public double calculateLandTax(int chunkCount) {
        return chunkCount * landTaxRate;
    }

    public double calculateTradeTax(double tradeValue) {
        return tradeValue * tradeTaxRate;
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