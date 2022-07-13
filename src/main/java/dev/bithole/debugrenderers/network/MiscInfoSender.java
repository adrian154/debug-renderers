package dev.bithole.debugrenderers.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class MiscInfoSender {

    public static final Identifier DEBUG_TICKTIME = new Identifier("debugrenderers:ticktime");

    public static void sendTickTime(MinecraftServer server, long tickTime) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeLong(tickTime);
        NetworkHelper.sendToAll(server, buf, DEBUG_TICKTIME);
    }

}
