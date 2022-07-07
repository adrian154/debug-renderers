package dev.bithole.debugrenderers.mixin;

import dev.bithole.debugrenderers.DebugRenderersMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

// Goal selectors aren't removed when they become inactive, leading to a lot of clutter
@Mixin(GoalSelectorDebugRenderer.class)
public abstract class GoalSelectorDebugRendererMixin {

    private static final Map<Integer, Long> lastSeen = new HashMap<>();

    @Accessor("goalSelectors")
    public abstract Map<Integer, List<GoalSelectorDebugRenderer.GoalSelector>> getGoalSelectors();

    @Inject(at = @At("TAIL"), method="setGoalSelectorList(ILjava/util/List;)V")
    public void setGoalSelectorList(int index, List<GoalSelectorDebugRenderer.GoalSelector> selectors, CallbackInfo info) {
        lastSeen.put(index, MinecraftClient.getInstance().world.getTime());
    }

    @Inject(at = @At("TAIL"), method="render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;DDD)V")
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo info) {

        Map<Integer, List<GoalSelectorDebugRenderer.GoalSelector>> goalSelectors = getGoalSelectors();
        Iterator<Map.Entry<Integer, Long>> it = lastSeen.entrySet().iterator();
        long time = MinecraftClient.getInstance().world.getTime();

        while(it.hasNext()) {
            Map.Entry<Integer, Long> entry = it.next();
            if(time - entry.getValue() > 20L) {
                it.remove();
                goalSelectors.remove(entry.getKey());
            }
        }

    }

}
