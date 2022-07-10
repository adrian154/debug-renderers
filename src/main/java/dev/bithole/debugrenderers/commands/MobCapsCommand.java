package dev.bithole.debugrenderers.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.bithole.debugrenderers.mixin.DensityCapAccessor;
import dev.bithole.debugrenderers.mixin.SpawnDensityCapperAccessor;
import dev.bithole.debugrenderers.mixin.SpawnHelperInfoAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.SpawnHelper;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;

public class MobCapsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mc")
                .then(argument("player", player())
                        .executes(ctx -> showMobCaps(ctx.getSource(), getPlayer(ctx, "player"))))
                .executes(ctx -> showMobCaps(ctx.getSource())));
    }

    private static void writeList(ServerCommandSource source, Object2IntMap<SpawnGroup> list) {
        for(Object2IntMap.Entry<SpawnGroup> entry: list.object2IntEntrySet()) {
            source.sendFeedback(Text.literal(String.format(" - %s: %d", entry.getKey().asString(), entry.getIntValue())), false);
        }
    }

    private static int showMobCaps(ServerCommandSource source) {
        SpawnHelper.Info info = source.getWorld().getChunkManager().getSpawnInfo();
        source.sendFeedback(Text.literal("Global Mobcaps").styled(style -> style.withBold(true)), false);
        writeList(source, info.getGroupToCount());
        return 0;
    }

    private static int showMobCaps(ServerCommandSource source, ServerPlayerEntity player) {
        SpawnHelper.Info info = source.getWorld().getChunkManager().getSpawnInfo();
        SpawnDensityCapper capper = ((SpawnHelperInfoAccessor)info).getDensityCapper();
        SpawnDensityCapper.DensityCap cap = ((SpawnDensityCapperAccessor)capper).getDensityCaps().get(player);
        source.sendFeedback(Text.literal("Mobcaps for ").append(player.getName()).styled(style -> style.withBold(true)), false);
        writeList(source, ((DensityCapAccessor)cap).getSpawnGroups());
        return 0;
    }

}
