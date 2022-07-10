package dev.bithole.debugrenderers.renderer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        public final int numSpawningChunks;
        public final Object2IntMap<String> globalMobCounts;
        public final Map<UUID, Object2IntMap<String>> perPlayerMobCounts;

        public SpawnInfo(PacketByteBuf buf) {

            this.numSpawningChunks = buf.readInt();

            this.globalMobCounts = new Object2IntOpenHashMap<>();
            for(int i = 0; i < buf.readVarInt(); i++) {
                this.globalMobCounts.put(buf.readString(), buf.readInt());
            }

            this.perPlayerMobCounts = new HashMap<>();
            for(int i = 0; i < buf.readVarInt(); i++) {
                Object2IntMap<String> map = new Object2IntOpenHashMap<>();
                perPlayerMobCounts.put(buf.readUuid(), map);
                for(int j = 0; j < buf.readVarInt(); j++) {
                    map.put(buf.readString(), buf.readInt());
                }
            }

        }

    }

}