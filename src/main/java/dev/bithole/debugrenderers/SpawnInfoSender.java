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

        // num spawning chunks
        buf.writeInt(info.getSpawningChunkCount());

        // global mobcap
        Object2IntMap<SpawnGroup> mobCounts = info.getGroupToCount();
        buf.writeVarInt(mobCounts.size());
        for(Object2IntMap.Entry<SpawnGroup> entry: mobCounts.object2IntEntrySet()) {
            buf.writeString(entry.getKey().asString());
            buf.writeInt(entry.getIntValue());
        }

        // per player mobcaps
        SpawnDensityCapper capper = ((SpawnHelperInfoAccessor)info).getDensityCapper();
        Map<ServerPlayerEntity, SpawnDensityCapper.DensityCap> perPlayerMobcaps = ((SpawnDensityCapperAccessor) capper).getDensityCaps();
        buf.writeVarInt(perPlayerMobcaps.size());

        for(Map.Entry<ServerPlayerEntity, SpawnDensityCapper.DensityCap> entry: perPlayerMobcaps.entrySet()) {
            buf.writeUuid(entry.getKey().getUuid());
            Object2IntMap<SpawnGroup> localMobCounts = ((DensityCapAccessor)entry.getValue()).getSpawnGroups();
            buf.writeVarInt(localMobCounts.size());
            for(Object2IntMap.Entry<SpawnGroup> countEntry: localMobCounts.object2IntEntrySet()) {
                buf.writeString(countEntry.getKey().asString());
                buf.writeInt(countEntry.getIntValue());
            }
        }

        NetworkHelper.sendToAll(world, buf, DEBUG_SPAWNING);

    }

}
