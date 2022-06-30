package dev.bithole.debugrenderers.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bithole.debugrenderers.DebugRenderersMod;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.PathfindingDebugRenderer;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathfindingDebugRenderer.class)
public class PathfindingDebugRendererMixin {

    @Invoker("getManhattanDistance")
    private static float getManhattanDistance(BlockPos pos, double x, double y, double z) {
        throw new AssertionError();
    }

    @Inject(at = @At("HEAD"), method="Lnet/minecraft/client/render/debug/PathfindingDebugRenderer;drawPathLines(Lnet/minecraft/entity/ai/pathing/Path;DDD)V", cancellable = true)
    private static void drawPathLines(Path path, double cameraX, double cameraY, double cameraZ, CallbackInfo info) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(6.0F);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        for(int i = 0; i < path.getLength(); ++i) {
            PathNode pathNode = path.getNode(i);
            if (!(getManhattanDistance(pathNode.getBlockPos(), cameraX, cameraY, cameraZ) > 80.0F)) {
                float f = (float)i / (float)path.getLength() * 0.33F;
                int j = i == 0 ? 0 : MathHelper.hsvToRgb(f, 0.9F, 0.9F);
                int k = j >> 16 & 255;
                int l = j >> 8 & 255;
                int m = j & 255;
                bufferBuilder.vertex((double)pathNode.x - cameraX + 0.5, (double)pathNode.y - cameraY + 0.5, (double)pathNode.z - cameraZ + 0.5).color(k, l, m, 255).next();
            }
        }

        tessellator.draw();
        info.cancel();

    }

}
