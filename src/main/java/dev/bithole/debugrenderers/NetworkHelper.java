package dev.bithole.debugrenderers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class NetworkHelper {

    // For use with non-vanilla plugin channels, since there may be clients that don't support them which we want to avoid communicating with
    public static void sendToAll(ServerWorld world, PacketByteBuf buf, Identifier channel) {
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(channel, buf);
        for (PlayerEntity playerEntity : world.getPlayers()) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) playerEntity;
            if(ServerPlayNetworking.canSend(serverPlayer, channel)) {
                serverPlayer.networkHandler.sendPacket(packet);
            }
        }
    }

}
