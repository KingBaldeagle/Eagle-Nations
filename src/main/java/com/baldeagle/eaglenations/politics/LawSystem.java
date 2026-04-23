package com.baldeagle.eaglenations.politics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LawSystem {
    private UUID ownerNationId;
    private List<Law> laws;

    public LawSystem(UUID ownerNationId) {
        this.ownerNationId = ownerNationId;
        this.laws = new ArrayList<>();
        initDefaultLaws();
    }

    private void initDefaultLaws() {
        laws.add(new Law(Law.LawType.BUILD, true));
        laws.add(new Law(Law.LawType.INTERACT, true));
        laws.add(new Law(Law.LawType.CONTAINER, false));
        laws.add(new Law(Law.LawType.PVP, false));
        laws.add(new Law(Law.LawType.EXPLOSION, false));
        laws.add(new Law(Law.LawType.MOB_GRIEFING, false));
        laws.add(new Law(Law.LawType.ENTRY, true));
    }

    public UUID getOwnerNationId() {
        return ownerNationId;
    }

    public List<Law> getLaws() {
        return laws;
    }

    public Law getLaw(Law.LawType type) {
        for (Law law : laws) {
            if (law.getType() == type) {
                return law;
            }
        }
        return null;
    }

    public boolean isAllowed(Law.LawType type) {
        Law law = getLaw(type);
        return law != null ? law.isAllowed() : true;
    }

    public void setLaw(Law law) {
        Law existing = getLaw(law.getType());
        if (existing != null) {
            laws.remove(existing);
        }
        laws.add(law);
    }

    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("owner_nation_id", ownerNationId);
        
        ListTag lawsList = new ListTag();
        for (Law law : laws) {
            lawsList.add(law.serializeNbt());
        }
        tag.put("laws", lawsList);
        
        return tag;
    }

    public static LawSystem deserializeNbt(CompoundTag tag) {
        UUID ownerNationId = tag.getUUID("owner_nation_id");
        LawSystem system = new LawSystem(ownerNationId);
        
        system.laws.clear();
        ListTag lawsList = tag.getList("laws", Tag.TAG_COMPOUND);
        for (int i = 0; i < lawsList.size(); i++) {
            Law law = Law.deserializeNbt(lawsList.getCompound(i));
            system.laws.add(law);
        }
        
        return system;
    }
}