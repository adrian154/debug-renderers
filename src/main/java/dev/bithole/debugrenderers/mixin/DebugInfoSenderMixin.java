package dev.bithole.debugrenderers.mixin;

import dev.bithole.debugrenderers.DebugRenderersMod;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

// WARNING: flaky code ahead due to poor documentation
@Mixin(DebugInfoSender.class)
public class DebugInfoSenderMixin {

    private static Map<Path, Integer> pathIDs = new WeakHashMap<>();
    private static int nextPathID = 0;

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

        // this value appears to be inaccessible outside of Path, so we won't worry about its function too much
        // however, Path::toBuffer won't write anything if this set has zero members, so we have to pass a dummy element
        Set<TargetPathNode> targetNodes = Collections.singleton(new TargetPathNode(0, 0, 0));

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

    @Inject(at = @At("HEAD"), method="sendPathfindingData(Lnet/minecraft/world/World;Lnet/minecraft/entity/mob/MobEntity;Lnet/minecraft/entity/ai/pathing/Path;F)V")
    private static void sendPathfindingData(World world, MobEntity mob, @Nullable Path path, float nodeReachProximity, CallbackInfo info) {
        if(!world.isClient() && path != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            Integer pathID = pathIDs.get(path);
            if(pathID == null) {
                pathID = nextPathID++;
                pathIDs.put(path, pathID);
            }
            buf.writeInt(pathID);
            buf.writeFloat(nodeReachProximity);
            writePath(path, buf);
            sendToAll((ServerWorld)world, buf, CustomPayloadS2CPacket.DEBUG_PATH);
        }
    }

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
        List<String> goals = bee.getGoalSelector().getRunningGoals().map(Goal::toString).toList();
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

    @Inject(at = @At("HEAD"), method="sendBeehiveDebugData(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BeehiveBlockEntity;)V")
    private static void sendBeehiveDebugData(World world, BlockPos pos, BlockState state, BeehiveBlockEntity blockEntity, CallbackInfo info) {
        if(world.isClient()) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeString(blockEntity.getType().toString()); // TODO: is this the right type?
        buf.writeInt(blockEntity.getBeeCount());
        buf.writeInt(BeehiveBlockEntity.getHoneyLevel(state));
        buf.writeBoolean(blockEntity.isSmoked());
        sendToAll((ServerWorld)world, buf, CustomPayloadS2CPacket.DEBUG_HIVE);
    }

    private static void writeBlockBox(BlockBox box, PacketByteBuf buf) {
        buf.writeInt(box.getMinX());
        buf.writeInt(box.getMinY());
        buf.writeInt(box.getMinZ());
        buf.writeInt(box.getMaxX());
        buf.writeInt(box.getMaxY());
        buf.writeInt(box.getMaxZ());
    }

    @Inject(at = @At("HEAD"), method="sendStructureStart(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/structure/StructureStart;)V")
    private static void sendStructureStart(StructureWorldAccess world, StructureStart structureStart, CallbackInfo info) {
        if(world.isClient()) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(world.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).getId(world.getDimension()));
        writeBlockBox(structureStart.getBoundingBox(), buf);
        List<StructurePiece> children = structureStart.getChildren();
        buf.writeInt(children.size());
        for(StructurePiece piece: children) {
            writeBlockBox(piece.getBoundingBox(), buf);
            buf.writeBoolean(false); // TODO: what does this do? the code shows that it controls the color... what is the color supposed to indicate?
        }
        sendToAll(world.toServerWorld(), buf, CustomPayloadS2CPacket.DEBUG_STRUCTURES);
    }

}
