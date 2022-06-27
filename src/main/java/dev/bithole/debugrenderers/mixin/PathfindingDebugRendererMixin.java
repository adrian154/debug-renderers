package dev.bithole.debugrenderers.mixin;

import dev.bithole.debugrenderers.DebugRenderersMod;
import net.minecraft.client.render.debug.PathfindingDebugRenderer;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathfindingDebugRenderer.class)
public class PathfindingDebugRendererMixin {

    @Inject(at = @At("HEAD"), method="Lnet/minecraft/client/render/debug/PathfindingDebugRenderer;drawPathLines(Lnet/minecraft/entity/ai/pathing/Path;DDD)V")
    private static void drawPathLines(Path path, double cameraX, double cameraY, double cameraZ, CallbackInfo info) {
        // TODO: figure out why path lines won't show up
    }

}
