package dev.bithole.debugrenderers.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

// View spawn attempt information
public class SpawnAttemptRenderer implements DebugRenderer.Renderer {

    private SpawnInfo latestInfo;

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        for(SpawnInfo.SpawnAttempt attempt: latestInfo.spawnAttempts) {
            float r, g, b;
            switch(attempt.group) {
                case "monster" ->  { r = 1; g = 0; b = 0; }
                case "creature" -> { r = 0; g = 1; b = 0; }
                default ->         { r = 1; g = 1; b = 1; }

            }
            DebugRenderer.drawBox(attempt.pos, 0.05f, r, g, b, 0.3f);
        }
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void setSpawnInfo(SpawnInfo info) {
        this.latestInfo = info;
    }

    public static class SpawnInfo {

        public final List<SpawnAttempt> spawnAttempts;

        public SpawnInfo(PacketByteBuf buf) {
            this.spawnAttempts = new ArrayList<>();
            int size = buf.readVarInt();
            for(int i = 0; i < size; i++) {
                String group = buf.readString();
                BlockPos pos = buf.readBlockPos();
                spawnAttempts.add(new SpawnAttempt(pos, group));
            }
        }

        static class SpawnAttempt {
            public final BlockPos pos;
            public final String group;
            public SpawnAttempt(BlockPos pos, String group) {
                this.pos = pos;
                this.group = group;
            }
        }

    }

}