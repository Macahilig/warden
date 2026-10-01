package com.example.wardenautohit;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WardenAutoHitMod implements ModInitializer {
    public static final String MOD_ID = "warden-autohit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[WardenAutoHit] loaded");
    }
}
