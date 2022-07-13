package dev.bithole.debugrenderers;

import dev.bithole.debugrenderers.commands.PingCommand;
import dev.bithole.debugrenderers.gui.InfoOverlay;
import dev.bithole.debugrenderers.renderers.Renderers;
import dev.bithole.debugrenderers.renderers.SpawnAttemptRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.DebugRenderer;

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

	public void initInfoOverlay(MinecraftClient client) { this.infoOverlay = new InfoOverlay(client); }
	public InfoOverlay getInfoOverlay() { return infoOverlay; }

	@Override
	public void onInitializeClient() {

		INSTANCE = this;

		ClientPlayConnectionEvents.INIT.register((handler, client) -> {

			ClientPlayNetworking.registerReceiver(SpawnInfoSender.DEBUG_SPAWNING, (client1, handler1, buf, responseSender) -> {
				if (renderers != null) {
					renderers.SPAWN_ATTEMPT_RENDERER.setSpawnInfo(new SpawnAttemptRenderer.SpawnInfo(buf));
				}
			});

		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			PingCommand.register(dispatcher);
		});

	}

}
