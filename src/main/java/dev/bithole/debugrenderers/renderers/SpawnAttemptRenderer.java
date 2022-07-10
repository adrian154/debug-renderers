package dev.bithole.debugrenderers.renderers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;

// View spawn attempt information
public class SpawnAttemptRenderer implements DebugRenderer.Renderer {

    private final MinecraftClient client;
    private SpawnInfo latestInfo;

    public SpawnAttemptRenderer(MinecraftClient client) {
        this.client = client;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {

    }

    public void setSpawnInfo(SpawnInfo info) {
        this.latestInfo = info;
    }

    public SpawnInfo getSpawnInfo() {
        return latestInfo;
    }

    public static class SpawnInfo {

        public SpawnInfo(PacketByteBuf buf) {

        }

    }

}