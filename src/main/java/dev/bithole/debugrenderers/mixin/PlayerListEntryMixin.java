package dev.bithole.debugrenderers.mixin;

import dev.bithole.debugrenderers.DebugRenderersClientMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {

    @Inject(at = @At("TAIL"), method = "setLatency(I)V")
    protected void setLatency(int latency, CallbackInfo info) {
        if(((PlayerListEntry)(Object)this).getProfile().getId().equals(MinecraftClient.getInstance().player.getUuid())) {
            DebugRenderersClientMod.getInstance().getInfoOverlay().updateLatency(latency);
        }
    }

}