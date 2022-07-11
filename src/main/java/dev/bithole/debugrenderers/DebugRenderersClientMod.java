package dev.bithole.debugrenderers;

import dev.bithole.debugrenderers.renderers.CustomRenderers;
import dev.bithole.debugrenderers.renderers.SpawnAttemptRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;

public class DebugRenderersClientMod implements ClientModInitializer {

	public static final Map<String, DebugRenderer.Renderer> debugRenderers = new HashMap<>();
	public static final Map<DebugRenderer.Renderer, Boolean> rendererStatus = new HashMap<>();

	public static CustomRenderers customRenderers;

	public static void addRenderer(String name, DebugRenderer.Renderer renderer) {
		debugRenderers.put(name, renderer);
		rendererStatus.put(renderer, false);
	}

	@Override
	public void onInitializeClient() {

		ClientPlayConnectionEvents.INIT.register(new ClientPlayConnectionEvents.Init() {
			@Override
			public void onPlayInit(ClientPlayNetworkHandler handler, MinecraftClient client) {

				ClientPlayNetworking.registerReceiver(SpawnInfoSender.DEBUG_SPAWNING, new ClientPlayNetworking.PlayChannelHandler() {
					@Override
					public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
						customRenderers.SPAWN_ATTEMPT_RENDERER.setSpawnInfo(new SpawnAttemptRenderer.SpawnInfo(buf));
					}
				});

			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			NetworkHelper.DebugRenderersCommand.register(dispatcher);
		});

	}

}
