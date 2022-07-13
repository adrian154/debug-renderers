package dev.bithole.debugrenderers.network;

import dev.bithole.debugrenderers.DebugRenderersClientMod;
import dev.bithole.debugrenderers.renderers.Renderers;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import java.util.ArrayDeque;
import java.util.Deque;

public class ClientPacketReceiver implements ClientPlayConnectionEvents.Init {

    private final DebugRenderersClientMod mod;
    private final Renderers renderers;

    // server-sent info
    private Deque<Long> tickTimes = new ArrayDeque<>();

    public ClientPacketReceiver(DebugRenderersClientMod mod) {
        this.mod = mod;
        this.renderers = mod.getRenderers();
    }

    public boolean tickTimesAvailable() {
        return tickTimes.size() > 0;
    }

    public float avgTickTime() {
        float avgUSPT = 0;
        for(long tickTime: tickTimes) {
            avgUSPT += tickTime;
        }
        return avgUSPT / 1e6f / tickTimes.size();
    }

    @Override
    public void onPlayInit(ClientPlayNetworkHandler handler, MinecraftClient client) {

        ClientPlayNetworking.registerReceiver(MiscInfoSender.DEBUG_TICKTIME, (_client, _handler, buf, responseSender) -> {
            tickTimes.add(buf.readLong());
            if(tickTimes.size() > 20) {
                tickTimes.remove();
            }
        });

    }

}
