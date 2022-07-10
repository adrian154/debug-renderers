package dev.bithole.debugrenderers.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.SpawnDensityCapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SpawnDensityCapper.class)
public interface SpawnDensityCapperAccessor {

    @Accessor("playersToDensityCap")
    public Map<ServerPlayerEntity, SpawnDensityCapper.DensityCap> getDensityCaps();

}
