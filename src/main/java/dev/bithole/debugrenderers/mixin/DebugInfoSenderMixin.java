package dev.bithole.debugrenderers.mixin;

import dev.bithole.debugrenderers.DebugRenderersMod;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// WARNING: flaky code ahead due to poor documentation
@Mixin(DebugInfoSender.class)
public class DebugInfoSenderMixin {

    private static int nextPathId = 0;

    // this method is super sucky since i can't really tell what all the fields are supposed to mean
    private static void writePath(Path path, PacketByteBuf buf) {

        // categorize nodes into open/closed based on the `closed` parameter
        // this *seems* right, but I have no idea whether it is...
        List<PathNode> openSet = new ArrayList<>(),
                       closedSet = new ArrayList<>();
        for(int i = 0; i < path.getLength(); i++) {
            PathNode node = path.getNode(i);
            if(node.visited)
                closedSet.add(node);
            else
                openSet.add(node);
        }

        Set<TargetPathNode> targetNodes = new HashSet<>();
        BlockPos target = path.getTarget();
        targetNodes.add(new TargetPathNode(target.getX(), target.getY(), target.getZ()));

        try {
            Method method = path.getClass().getDeclaredMethod("setDebugInfo", PathNode[].class, PathNode[].class, Set.class);
            method.setAccessible(true);
            method.invoke(path, openSet.toArray(new PathNode[0]), closedSet.toArray(new PathNode[0]), targetNodes);
            path.toBuffer(buf);
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            DebugRenderersMod.LOGGER.error("This should never happen.");
        }

    }

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

    @Inject(at = @At("HEAD"), method="sendBeeDebugData(Lnet/minecraft/entity/passive/BeeEntity;)V")
    private static void sendBeeDebugData(BeeEntity bee, CallbackInfo info) {

        if(bee.getWorld().isClient) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(bee.getX());
        buf.writeDouble(bee.getY());
        buf.writeDouble(bee.getZ());
        buf.writeUuid(bee.getUuid());
        buf.writeInt(bee.getId());
        buf.writeNullable(bee.getHivePos(), PacketByteBuf::writeBlockPos);
        buf.writeNullable(bee.getFlowerPos(), PacketByteBuf::writeBlockPos);
        buf.writeInt(bee.getMoveGoalTicks());

        // TODO: figure out if this is the right Path, there could be several!
        Path path = bee.getNavigation().getCurrentPath();
        if(path != null) {
            buf.writeBoolean(true);
            writePath(path, buf);
        } else {
            buf.writeBoolean(false);
        }

        // TODO: figure out if these are the right goals
        List<String> goals = bee.getGoalSelector().getRunningGoals().map(goal -> goal.toString()).collect(Collectors.toList());
        buf.writeVarInt(goals.size());
        for(String goal: goals) {
            buf.writeString(goal);
        }

        // looks like Yarn's mapping for this is wrong but oh well... i succumbed to the sweet elixir that are the Mojang's mappings and now i can never do anything to fix it
        List<BlockPos> blacklistedHives = bee.getPossibleHives();
        buf.writeVarInt(blacklistedHives.size());
        for(BlockPos pos: blacklistedHives) {
            buf.writeBlockPos(pos);
        }

        sendToAll((ServerWorld)bee.getWorld(), buf, CustomPayloadS2CPacket.DEBUG_BEE);

    }

}
