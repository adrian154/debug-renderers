package dev.bithole.debugrenderers.gui;

import dev.bithole.debugrenderers.commands.PingCommand;
import dev.bithole.debugrenderers.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class InfoOverlay extends DrawableHelper {

    private final MinecraftClient client;
    private final TextRenderer textRenderer;
    private final InfoOverlaySettings settings;

    private int lastKnownLatency;

    public InfoOverlay(MinecraftClient client) {
        this.client = client;
        this.textRenderer = client.textRenderer;
        this.settings = new InfoOverlaySettings(true, true, true, true, true);
    }

    public void updateLatency(int latency) {
        this.lastKnownLatency = latency;
    }

    public void render(MatrixStack matrices) {
        this.client.getProfiler().push("infoOverlay");
        renderLeftText(matrices);
        this.client.getProfiler().pop();
    }

    private void renderLeftText(MatrixStack matrices) {
        List<String> lines = getLeftText();
        for(int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int y = i * textRenderer.fontHeight + 2;
            DebugHud.fill(matrices, 1, y - 1, textRenderer.getWidth(line) + 3, y + textRenderer.fontHeight - 1, 0x90505050);
            textRenderer.draw(matrices, line, 2, y, 0xe0e0e0);
        }
    }

    private List<String> getLeftText() {

        List<String> lines = new ArrayList<>();
        Entity cameraEntity = this.client.getCameraEntity();


        if(settings.FPS) {
            lines.add(String.format("FPS: %d", MinecraftClientAccessor.getFPS()));
        }

        if(settings.coordinates) {
            lines.add(String.format("XYZ: %.1f / %.1f / %.1f", cameraEntity.getX(), cameraEntity.getY(), cameraEntity.getZ()));
        }

        if(settings.facing) {
            String facing;
            switch(cameraEntity.getHorizontalFacing()) {
                case NORTH -> facing = "north (-Z)";
                case SOUTH -> facing = "south (+Z)";
                case WEST -> facing = "west (-X)";
                case EAST -> facing = "east (+X)";
                default -> facing = "<invalid>";
            }
            lines.add(String.format("Facing: %s", facing));
        }

        if(settings.biome) {
            RegistryEntry<Biome> entry = client.world.getBiome(cameraEntity.getBlockPos());
            String name = entry.getKeyOrValue().map(key -> {
                Identifier id = key.getValue();
                return Text.translatable("biome." + id.getNamespace() + "." + id.getPath()).getString();
            }, value -> "<unknown>");
            lines.add(String.format("Biome: %s", name));
        }

        if(settings.ping) {
            String ping;
            if(lastKnownLatency == 0) {
                ping = "--";
            } else {
                ping = PingCommand.getPingColor(lastKnownLatency).toString() + lastKnownLatency + "ms";
            }
            lines.add(String.format("Ping: %s", ping));
        }

        return lines;

    }

    record InfoOverlaySettings(
        boolean FPS,
        boolean coordinates,
        boolean facing,
        boolean biome,
        boolean ping
    ) {

    }

}
