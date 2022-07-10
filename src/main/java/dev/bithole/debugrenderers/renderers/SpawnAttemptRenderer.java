package dev.bithole.debugrenderers.renderers;

import dev.bithole.debugrenderers.DebugRenderersMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

// View spawn attempt information
public class SpawnAttemptRenderer implements DebugRenderer.Renderer {

    private final MinecraftClient client;
    private SpawnInfo latestInfo;

    public SpawnAttemptRenderer(MinecraftClient client) {
        this.client = client;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getLines());
        for(BlockPos pos: latestInfo.spawnAttempts) {
            DebugRenderer.drawBox(pos, 0.05f, 1.0f, 1.0f, 1.0f, 0.3f);
        }
    }

    public void setSpawnInfo(SpawnInfo info) {
        this.latestInfo = info;
    }

    public SpawnInfo getSpawnInfo() {
        return latestInfo;
    }

    public static class SpawnInfo {

        public final List<BlockPos> spawnAttempts;

        public SpawnInfo(PacketByteBuf buf) {
            this.spawnAttempts = new ArrayList<>();
            int size = buf.readVarInt();
            System.out.println(size);
            for(int i = 0; i < size; i++) {
                spawnAttempts.add(buf.readBlockPos());
            }
        }

    }

}