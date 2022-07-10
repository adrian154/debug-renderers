package dev.bithole.debugrenderers.renderers;

import dev.bithole.debugrenderers.DebugRenderersClientMod;
import net.minecraft.client.MinecraftClient;

public class CustomRenderers {

    public final SpawnAttemptRenderer SPAWN_ATTEMPT_RENDERER;

    public CustomRenderers(MinecraftClient client) {
        SPAWN_ATTEMPT_RENDERER = new SpawnAttemptRenderer(client);
        DebugRenderersClientMod.addRenderer("spawnAttempt", SPAWN_ATTEMPT_RENDERER);
    }

}
