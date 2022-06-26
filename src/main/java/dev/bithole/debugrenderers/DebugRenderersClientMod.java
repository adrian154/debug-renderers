package dev.bithole.debugrenderers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.DebugRenderer;

import java.util.HashMap;
import java.util.Map;

public class DebugRenderersClientMod implements ClientModInitializer {

	public static final Map<String, DebugRenderer.Renderer> debugRenderers = new HashMap<>();

	@Override
	public void onInitializeClient() {

		DebugRenderer debugRenderer = MinecraftClient.getInstance().debugRenderer;
		debugRenderers.put("pathfinding", debugRenderer.pathfindingDebugRenderer);
		debugRenderers.put("water", debugRenderer.waterDebugRenderer);
		debugRenderers.put("chunkBorder", debugRenderer.chunkBorderDebugRenderer);
		debugRenderers.put("heightmap", debugRenderer.heightmapDebugRenderer);
		debugRenderers.put("collision", debugRenderer.collisionDebugRenderer);
		debugRenderers.put("neighborUpdate", debugRenderer.neighborUpdateDebugRenderer);
		debugRenderers.put("structure", debugRenderer.structureDebugRenderer);
		debugRenderers.put("skylight", debugRenderer.skyLightDebugRenderer);
		debugRenderers.put("genAttempt", debugRenderer.worldGenAttemptDebugRenderer);
		debugRenderers.put("blockOutline", debugRenderer.blockOutlineDebugRenderer);
		debugRenderers.put("chunkLoading", debugRenderer.chunkLoadingDebugRenderer);
		debugRenderers.put("village", debugRenderer.villageDebugRenderer);
		debugRenderers.put("villageSections", debugRenderer.villageSectionsDebugRenderer);
		debugRenderers.put("bee", debugRenderer.beeDebugRenderer);
		debugRenderers.put("raidCenter", debugRenderer.raidCenterDebugRenderer);
		debugRenderers.put("goalSelector", debugRenderer.goalSelectorDebugRenderer);
		debugRenderers.put("test", debugRenderer.gameTestDebugRenderer);
		debugRenderers.put("event", debugRenderer.gameEventDebugRenderer);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			DebugRenderersCommand.register(dispatcher);
		});

	}

}
