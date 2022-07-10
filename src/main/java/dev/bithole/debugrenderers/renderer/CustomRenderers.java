package dev.bithole.debugrenderers.renderer;

import dev.bithole.debugrenderers.DebugRenderersClientMod;
import dev.bithole.debugrenderers.DebugRenderersMod;
import net.minecraft.client.MinecraftClient;

public class CustomRenderers {

    public final SpawnAttemptRenderer SPAWN_ATTEMPT_RENDERER;

    public CustomRenderers(MinecraftClient client) {
        SPAWN_ATTEMPT_RENDERER = new SpawnAttemptRenderer(client);
        DebugRenderersClientMod.addRenderer("spawnAttempt", SPAWN_ATTEMPT_RENDERER);
    }

}
