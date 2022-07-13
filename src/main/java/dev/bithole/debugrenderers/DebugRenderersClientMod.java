package dev.bithole.debugrenderers;

import dev.bithole.debugrenderers.commands.PingCommand;
import dev.bithole.debugrenderers.gui.InfoOverlay;
import dev.bithole.debugrenderers.network.ClientPacketReceiver;
import dev.bithole.debugrenderers.renderers.Renderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.DebugRenderer;

public class DebugRenderersClientMod implements ClientModInitializer {

	// singleton stuff
	private static DebugRenderersClientMod INSTANCE;
	public static DebugRenderersClientMod getInstance() {
		return INSTANCE;
	}

	private Renderers renderers;
	private InfoOverlay infoOverlay;
	private ClientPacketReceiver packetReceiver;

	public void initRenderers(DebugRenderer debugRenderer) {
		this.renderers = new Renderers(debugRenderer);
	}
	public Renderers getRenderers() {
		return renderers;
	}

	public void initInfoOverlay(MinecraftClient client) { this.infoOverlay = new InfoOverlay(client, this); }
	public InfoOverlay getInfoOverlay() { return infoOverlay; }

	public ClientPacketReceiver getPacketReceiver() { return packetReceiver; }

	@Override
	public void onInitializeClient() {

		INSTANCE = this;

		packetReceiver = new ClientPacketReceiver(this);
		ClientPlayConnectionEvents.INIT.register(packetReceiver);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			PingCommand.register(dispatcher);
		});

	}

}
