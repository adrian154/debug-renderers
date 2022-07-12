package dev.bithole.debugrenderers.mixin;

import dev.bithole.debugrenderers.DebugRenderersClientMod;
import dev.bithole.debugrenderers.renderers.Renderers;
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

    }

}
