package dev.bithole.debugrenderers;

import dev.bithole.debugrenderers.mixin.DebugInfoSenderMixin;
import dev.bithole.debugrenderers.mixin.DensityCapAccessor;
import dev.bithole.debugrenderers.mixin.SpawnDensityCapperAccessor;
import dev.bithole.debugrenderers.mixin.SpawnHelperInfoAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.SpawnHelper;

import java.util.Map;

public class SpawnInfoSender {

    public static final Identifier DEBUG_SPAWNING = new Identifier("debugrenderers", "spawning");

    public static void send(ServerWorld world, SpawnHelper.Info info) {

        PacketByteBuf buf = PacketByteBufs.create();

        NetworkHelper.sendToAll(world, buf, DEBUG_SPAWNING);

    }

}
