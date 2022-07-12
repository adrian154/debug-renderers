package dev.bithole.debugrenderers.mixin;

import dev.bithole.debugrenderers.DebugRenderersClientMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Map;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/MinecraftClient;)V")
    public void init(MinecraftClient client, CallbackInfo info) {
        DebugRenderersClientMod.getInstance().initRenderers((DebugRenderer)(Object)this);
    }

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V")
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo info) {
        DebugRenderersClientMod.getInstance().getRenderers().render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
    }

}
