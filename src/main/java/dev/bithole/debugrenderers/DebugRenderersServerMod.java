package dev.bithole.debugrenderers;

import net.fabricmc.api.DedicatedServerModInitializer;

public class DebugRenderersServerMod implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        DebugRenderersMod.LOGGER.warn("DebugRenderers doesn't do anything when installed on a server. You should get rid of this mod.");
    }

}
