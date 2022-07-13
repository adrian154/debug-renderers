package dev.bithole.debugrenderers.renderers;

import dev.bithole.debugrenderers.DebugRenderersClientMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Renderers {

    private final Map<DebugRenderer.Renderer, Text> rendererNames = new HashMap<>();
    private final Map<DebugRenderer.Renderer, Boolean> rendererStatus = new HashMap<>();

    public void addRenderer(DebugRenderer.Renderer renderer, Text name) {
        rendererNames.put(renderer, name);
        rendererStatus.put(renderer, false);
    }

    /* Toggles a renderer; returns the updated status. */
    public boolean toggleRenderer(DebugRenderer.Renderer renderer) {
        Boolean status = rendererStatus.get(renderer);
        if(status == null) {
            throw new AssertionError();
        }
        rendererStatus.put(renderer, !status);
        return !status;
    }

    public Set<DebugRenderer.Renderer> getRenderers() {
        return rendererNames.keySet();
    }

    public Text getName(DebugRenderer.Renderer renderer) {
        return rendererNames.get(renderer);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumerProvider, double cameraX, double cameraY, double cameraZ) {
        for(Map.Entry<DebugRenderer.Renderer, Boolean> entry: rendererStatus.entrySet()) {
            if(entry.getValue()) {
                entry.getKey().render(matrices, vertexConsumerProvider, cameraX, cameraY, cameraZ);
            }
        }
    }

    public Renderers(DebugRenderer debugRenderer) {

        // initialize custom renderers
        // ...

        // add Minecraft renderers
        this.addRenderer(debugRenderer.pathfindingDebugRenderer, Text.translatable("renderer.pathfinding"));
        this.addRenderer(debugRenderer.waterDebugRenderer, Text.translatable("renderer.water"));
        this.addRenderer(debugRenderer.heightmapDebugRenderer, Text.translatable("renderer.heightmap"));
        this.addRenderer(debugRenderer.collisionDebugRenderer, Text.translatable("renderer.collision"));
        this.addRenderer(debugRenderer.neighborUpdateDebugRenderer, Text.translatable("renderer.neighborUpdate"));
        this.addRenderer(debugRenderer.structureDebugRenderer, Text.translatable("renderer.structure"));
        this.addRenderer(debugRenderer.skyLightDebugRenderer, Text.translatable("renderer.skyLight"));
        this.addRenderer(debugRenderer.blockOutlineDebugRenderer, Text.translatable("renderer.blockOutline"));
        this.addRenderer(debugRenderer.chunkLoadingDebugRenderer, Text.translatable("renderer.chunkLoading"));
        this.addRenderer(debugRenderer.beeDebugRenderer, Text.translatable("renderer.bee"));
        this.addRenderer(debugRenderer.gameEventDebugRenderer, Text.translatable("renderer.gameEvent"));
        this.addRenderer(debugRenderer.raidCenterDebugRenderer, Text.translatable("renderer.raidCenter"));
        this.addRenderer(debugRenderer.goalSelectorDebugRenderer, Text.translatable("renderer.goalSelector"));
        this.addRenderer(debugRenderer.villageDebugRenderer, Text.translatable("renderer.brain"));

    }

}
