package dev.bithole.debugrenderers.mixin;

import dev.bithole.debugrenderers.DebugRenderersClientMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.render.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// We use a mixin to initialize the list of renderers because the ClientModInitializer is called before DebugRenderer is instantiated
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/RunArgs;)V")
    public void init(RunArgs args, CallbackInfo info) {

        DebugRenderer debugRenderer = MinecraftClient.getInstance().debugRenderer;

        // working
        DebugRenderersClientMod.addRenderer("pathfinding", debugRenderer.pathfindingDebugRenderer);
        DebugRenderersClientMod.addRenderer("water", debugRenderer.waterDebugRenderer);
        DebugRenderersClientMod.addRenderer("chunkBorder", debugRenderer.chunkBorderDebugRenderer);
        DebugRenderersClientMod.addRenderer("heightmap", debugRenderer.heightmapDebugRenderer);
        DebugRenderersClientMod.addRenderer("collision", debugRenderer.collisionDebugRenderer);
        DebugRenderersClientMod.addRenderer("neighborUpdate", debugRenderer.neighborUpdateDebugRenderer);
        DebugRenderersClientMod.addRenderer("structure", debugRenderer.structureDebugRenderer);
        DebugRenderersClientMod.addRenderer("skyLight", debugRenderer.skyLightDebugRenderer);
        DebugRenderersClientMod.addRenderer("blockOutline", debugRenderer.blockOutlineDebugRenderer);
        DebugRenderersClientMod.addRenderer("chunkLoading", debugRenderer.chunkLoadingDebugRenderer);
        DebugRenderersClientMod.addRenderer("bee", debugRenderer.beeDebugRenderer);
        DebugRenderersClientMod.addRenderer("gameTest", debugRenderer.gameTestDebugRenderer);
        DebugRenderersClientMod.addRenderer("sculk", debugRenderer.gameEventDebugRenderer);
        DebugRenderersClientMod.addRenderer("raidCenter", debugRenderer.raidCenterDebugRenderer);
        DebugRenderersClientMod.addRenderer("goalSelector", debugRenderer.goalSelectorDebugRenderer);

        // WIP
        DebugRenderersClientMod.addRenderer("village", debugRenderer.villageDebugRenderer);
        DebugRenderersClientMod.addRenderer("villageSections", debugRenderer.villageSectionsDebugRenderer);
        
        // broken
        DebugRenderersClientMod.addRenderer("worldGenAttempt", debugRenderer.worldGenAttemptDebugRenderer);

    }

}
