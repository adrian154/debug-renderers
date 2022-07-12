package dev.bithole.debugrenderers.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public class InfoOverlay extends DrawableHelper {

    private final MinecraftClient client;
    private final TextRenderer textRenderer;

    public InfoOverlay(MinecraftClient client) {
        this.client = client;
        this.textRenderer = client.textRenderer;
    }

    public void render(MatrixStack matrices) {
        this.client.getProfiler().push("infoOverlay");
        renderLeftText(matrices);
        this.client.getProfiler().pop();
    }

    private void renderLeftText(MatrixStack matrices) {
        textRenderer.draw(matrices, "Hello, World.", 2, 2, 0xe0e0e0);
    }

    public static class InfoOverlayOptions {
        
    }

}
