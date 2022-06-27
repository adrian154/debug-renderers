package dev.bithole.debugrenderers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.DebugRenderer;

import java.util.HashMap;
import java.util.Map;

public class DebugRenderersClientMod implements ClientModInitializer {

	public static final Map<String, DebugRenderer.Renderer> debugRenderers = new HashMap<>();
	public static final Map<DebugRenderer.Renderer, Boolean> rendererStatus = new HashMap<>();

	public static void addRenderer(String name, DebugRenderer.Renderer renderer) {
		debugRenderers.put(name, renderer);
		rendererStatus.put(renderer, false);
	}

	@Override
	public void onInitializeClient() {

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			DebugRenderersCommand.register(dispatcher);
		});

	}

}
