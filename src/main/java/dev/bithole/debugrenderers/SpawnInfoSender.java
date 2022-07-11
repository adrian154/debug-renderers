package dev.bithole.debugrenderers;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SpawnInfoSender {

    public static final Identifier DEBUG_SPAWNING = new Identifier("debugrenderers", "spawning");
    private static final List<SpawnAttempt> spawnAttempts = new ArrayList<>();

    public static void clear() {
        spawnAttempts.clear();
    }

    public static void addSpawnAttempt(SpawnGroup group, BlockPos pos) {
        spawnAttempts.add(new SpawnAttempt(pos, group));
    }

    public static void send(ServerWorld world) {

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeVarInt(spawnAttempts.size());
        for(SpawnAttempt attempt: spawnAttempts) {
            buf.writeString(attempt.group.asString());
            buf.writeBlockPos(attempt.pos);
        }

        NetworkHelper.sendToAll(world, buf, DEBUG_SPAWNING);

    }

    private static class SpawnAttempt {
        public final BlockPos pos;
        public final SpawnGroup group;
        public SpawnAttempt(BlockPos pos, SpawnGroup group) {
            this.pos = pos;
            this.group = group;
        }
    }

}
