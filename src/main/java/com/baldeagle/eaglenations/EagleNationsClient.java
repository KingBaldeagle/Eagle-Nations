package com.baldeagle.eaglenations;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = EagleNations.MODID, dist = Dist.CLIENT)
public class EagleNationsClient {
    public EagleNationsClient() {
        EagleNations.LOGGER.info("Eagle Nations client loaded");
    }
}