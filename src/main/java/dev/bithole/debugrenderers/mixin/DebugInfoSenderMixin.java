package dev.bithole.debugrenderers.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

// WARNING: flaky code ahead due to poor documentation
@Mixin(DebugInfoSender.class)
public class DebugInfoSenderMixin {

    private static int nextPathId = 0;

    @Invoker("sendToAll")
    public static void sendToAll(ServerWorld world, PacketByteBuf buf, Identifier channel) {
        throw new AssertionError();
    }

    /*
    // FIXME: Path is missing some crap
    @Inject(at = @At("HEAD"), method="sendPathfindingData(Lnet/minecraft/world/World;Lnet/minecraft/entity/mob/MobEntity;Lnet/minecraft/entity/ai/pathing/Path;F)V")
    private static void sendPathfindingData(World world, MobEntity mob, @Nullable Path path, float nodeReachProximity, CallbackInfo info) {
        if(!world.isClient() && path != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(nextPathId++);
            buf.writeFloat(nodeReachProximity);
            path.toBuffer(buf);
            sendToAll((ServerWorld)world, buf, CustomPayloadS2CPacket.DEBUG_PATH);
        }
    }
    */

    @Inject(at = @At("HEAD"), method="sendNeighborUpdate(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V")
    private static void sendNeighborUpdate(World world, BlockPos pos, CallbackInfo info) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarLong(world.getTime());
        buf.writeBlockPos(pos);
        sendToAll((ServerWorld)world, buf, CustomPayloadS2CPacket.DEBUG_NEIGHBORS_UPDATE);
    }

}
