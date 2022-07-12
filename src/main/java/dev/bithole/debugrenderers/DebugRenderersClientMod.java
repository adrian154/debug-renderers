package dev.bithole.debugrenderers;

import dev.bithole.debugrenderers.commands.PingCommand;
import dev.bithole.debugrenderers.gui.InfoOverlay;
import dev.bithole.debugrenderers.renderers.Renderers;
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

public class DebugRenderersClientMod implements ClientModInitializer {

	private static DebugRenderersClientMod INSTANCE;

	private Renderers renderers;
	private InfoOverlay infoOverlay;

	public static DebugRenderersClientMod getInstance() {
		return INSTANCE;
	}

	public void initRenderers(DebugRenderer debugRenderer) {
		this.renderers = new Renderers(debugRenderer);
	}

	public Renderers getRenderers() {
		return renderers;
	}

	@Override
	public void onInitializeClient() {

		INSTANCE = this;

		MinecraftClient client = MinecraftClient.getInstance();
		infoOverlay = new InfoOverlay(client);

		ClientPlayConnectionEvents.INIT.register(new ClientPlayConnectionEvents.Init() {
			@Override
			public void onPlayInit(ClientPlayNetworkHandler handler, MinecraftClient client) {

				ClientPlayNetworking.registerReceiver(SpawnInfoSender.DEBUG_SPAWNING, new ClientPlayNetworking.PlayChannelHandler() {
					@Override
					public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
						if(renderers != null) {
							renderers.SPAWN_ATTEMPT_RENDERER.setSpawnInfo(new SpawnAttemptRenderer.SpawnInfo(buf));
						}
					}
				});

			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			PingCommand.register(dispatcher);
		});

	}

}
