package dev.bithole.debugrenderers.mixin;

import dev.bithole.debugrenderers.DebugRenderersMod;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.PositionSourceType;
import net.minecraft.world.event.listener.GameEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.*;

// WARNING: flaky code ahead due to poor documentation
@Mixin(DebugInfoSender.class)
public class DebugInfoSenderMixin {

    private static class IDMapper<T> {

        private final Map<T, Integer> ids = new WeakHashMap<>();
        private int nextID = 0;

        public int getID(T object) {
            Integer id = ids.get(object);
            if(id == null) {
                id = nextID++;
                ids.put(object, id);
            }
            return id;
        }

    }

    private static final IDMapper<Path> pathIDMapper = new IDMapper<>();
    private static final IDMapper<GoalSelector> goalSelectorIDMapper = new IDMapper<>();

    @Invoker("sendToAll")
    private static void sendToAll(ServerWorld world, PacketByteBuf buf, Identifier channel) {
        throw new AssertionError();
    }

    @Inject(at = @At("HEAD"), method = "sendPathfindingData(Lnet/minecraft/world/World;Lnet/minecraft/entity/mob/MobEntity;Lnet/minecraft/entity/ai/pathing/Path;F)V")
    private static void sendPathfindingData(World world, MobEntity mob, @Nullable Path path, float nodeReachProximity, CallbackInfo info) {
        if (!world.isClient() && path != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(pathIDMapper.getID(path));
            buf.writeFloat(nodeReachProximity);
            path.toBuffer(buf);
            sendToAll((ServerWorld) world, buf, CustomPayloadS2CPacket.DEBUG_PATH);
        }
    }

    @Inject(at = @At("HEAD"), method = "sendNeighborUpdate(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V")
    private static void sendNeighborUpdate(World world, BlockPos pos, CallbackInfo info) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarLong(world.getTime());
        buf.writeBlockPos(pos);
        sendToAll((ServerWorld) world, buf, CustomPayloadS2CPacket.DEBUG_NEIGHBORS_UPDATE);
    }

    @Inject(at = @At("HEAD"), method = "sendBeeDebugData(Lnet/minecraft/entity/passive/BeeEntity;)V")
    private static void sendBeeDebugData(BeeEntity bee, CallbackInfo info) {

        if (bee.getWorld().isClient) return;

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
        if (path != null) {
            buf.writeBoolean(true);
            path.toBuffer(buf);
        } else {
            buf.writeBoolean(false);
        }

        // TODO: figure out if these are the right goals
        List<String> goals = bee.getGoalSelector().getRunningGoals().map(goal -> goal.getGoal().toString()).toList();
        buf.writeVarInt(goals.size());
        for (String goal : goals) {
            buf.writeString(DebugRenderersMod.remap(goal));
        }

        // looks like Yarn's mapping for this is wrong but oh well... i succumbed to the sweet elixir that are the Mojang's mappings and now i can never do anything to fix it
        List<BlockPos> blacklistedHives = bee.getPossibleHives();
        buf.writeVarInt(blacklistedHives.size());
        for (BlockPos pos : blacklistedHives) {
            buf.writeBlockPos(pos);
        }

        sendToAll((ServerWorld) bee.getWorld(), buf, CustomPayloadS2CPacket.DEBUG_BEE);

    }

    @Inject(at = @At("HEAD"), method = "sendBeehiveDebugData(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BeehiveBlockEntity;)V")
    private static void sendBeehiveDebugData(World world, BlockPos pos, BlockState state, BeehiveBlockEntity blockEntity, CallbackInfo info) {
        if (world.isClient()) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeString(""); // TODO: figure out what this field ("type") could possibly mean
        buf.writeInt(blockEntity.getBeeCount());
        buf.writeInt(BeehiveBlockEntity.getHoneyLevel(state));
        buf.writeBoolean(blockEntity.isSmoked());
        sendToAll((ServerWorld) world, buf, CustomPayloadS2CPacket.DEBUG_HIVE);
    }

    private static void writeBlockBox(BlockBox box, PacketByteBuf buf) {
        buf.writeInt(box.getMinX());
        buf.writeInt(box.getMinY());
        buf.writeInt(box.getMinZ());
        buf.writeInt(box.getMaxX());
        buf.writeInt(box.getMaxY());
        buf.writeInt(box.getMaxZ());
    }

    @Inject(at = @At("HEAD"), method = "sendStructureStart(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/structure/StructureStart;)V")
    private static void sendStructureStart(StructureWorldAccess world, StructureStart structureStart, CallbackInfo info) {
        if (world.isClient()) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(world.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).getId(world.getDimension()));
        writeBlockBox(structureStart.getBoundingBox(), buf);
        List<StructurePiece> children = structureStart.getChildren();
        buf.writeInt(children.size());
        for (StructurePiece piece : children) {
            writeBlockBox(piece.getBoundingBox(), buf);
            buf.writeBoolean(false); // TODO: what does this do? the code shows that it controls the color... what is the color supposed to indicate?
        }
        sendToAll(world.toServerWorld(), buf, CustomPayloadS2CPacket.DEBUG_STRUCTURES);
    }

    @Inject(at = @At("HEAD"), method = "sendGameEvent(Lnet/minecraft/world/World;Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/util/math/Vec3d;)V")
    private static void sendGameEvent(World world, GameEvent event, Vec3d pos, CallbackInfo info) {
        if (world.isClient()) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(event.getId());
        buf.writeDouble(pos.getX());
        buf.writeDouble(pos.getY());
        buf.writeDouble(pos.getZ());
        sendToAll((ServerWorld) world, buf, CustomPayloadS2CPacket.DEBUG_GAME_EVENT);
    }

    @Inject(at = @At("HEAD"), method = "sendGameEventListener(Lnet/minecraft/world/World;Lnet/minecraft/world/event/listener/GameEventListener;)V")
    private static void sendGameEventListener(World world, GameEventListener eventListener, CallbackInfo info) {
        if (world.isClient()) return;
        PacketByteBuf buf = PacketByteBufs.create();
        PositionSource posSource = eventListener.getPositionSource();
        PositionSourceType posSourceType = posSource.getType();
        buf.writeIdentifier(world.getRegistryManager().get(Registry.POSITION_SOURCE_TYPE_KEY).getId(posSourceType));
        posSourceType.writeToBuf(buf, posSource);
        buf.writeVarInt(eventListener.getRange());
        sendToAll((ServerWorld) world, buf, CustomPayloadS2CPacket.DEBUG_GAME_EVENT_LISTENERS);
    }

    @Inject(at = @At("HEAD"), method = "sendRaids(Lnet/minecraft/server/world/ServerWorld;Ljava/util/Collection;)V")
    private static void sendRaids(ServerWorld world, Collection<Raid> raids, CallbackInfo info) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(raids.size());
        for (Raid raid : raids) {
            buf.writeBlockPos(raid.getCenter());
        }
        sendToAll(world, buf, CustomPayloadS2CPacket.DEBUG_RAIDS);
    }

    @Inject(at = @At("HEAD"), method = "sendGoalSelector(Lnet/minecraft/world/World;Lnet/minecraft/entity/mob/MobEntity;Lnet/minecraft/entity/ai/goal/GoalSelector;)V")
    private static void sendGoalSelector(World world, MobEntity mob, GoalSelector goalSelector, CallbackInfo info) {
        if(world.isClient()) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(mob.getBlockPos());
        buf.writeInt(goalSelectorIDMapper.getID(goalSelector));
        Set<PrioritizedGoal> goals = goalSelector.getGoals();
        buf.writeInt(goals.size());
        for(PrioritizedGoal goal: goals) {
            buf.writeInt(goal.getPriority());
            buf.writeBoolean(goal.isRunning());
            buf.writeString(DebugRenderersMod.remap(goal.getGoal().toString()), 255);
        }
        sendToAll((ServerWorld)world, buf, CustomPayloadS2CPacket.DEBUG_GOAL_SELECTOR);
    }

    @Invoker("writeBrain")
    private static void writeBrain(LivingEntity entity, PacketByteBuf buf) {
        throw new AssertionError();
    }

    @Inject(at = @At("HEAD"), method = "sendBrainDebugData(Lnet/minecraft/entity/LivingEntity;)V")
    private static void sendBrainDebugData(LivingEntity entity, CallbackInfo info) {
        if(entity.getWorld().isClient()) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(entity.getX());
        buf.writeDouble(entity.getY());
        buf.writeDouble(entity.getZ());
        buf.writeUuid(entity.getUuid());
        buf.writeInt(entity.getId());
        buf.writeString(entity.getEntityName()); // TODO: which name is this?
        if(entity instanceof VillagerEntity) {
            VillagerEntity villager = (VillagerEntity)entity;
            buf.writeString(villager.getVillagerData().getProfession().toString());
            buf.writeInt(villager.getExperience());
        } else {
            buf.writeString("");
            buf.writeInt(0);
        }
        buf.writeFloat(entity.getHealth());
        buf.writeFloat(entity.getMaxHealth());
        writeBrain(entity, buf);
        sendToAll((ServerWorld)entity.getWorld(), buf, CustomPayloadS2CPacket.DEBUG_BRAIN);
    }

    /*
    @Inject(at = @At("HEAD"), method = "(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)V")
    private static void sendPoiAddition(ServerWorld world, BlockPos pos, CallbackInfo info) {

    }
    */

}
