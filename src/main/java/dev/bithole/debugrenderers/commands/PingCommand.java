package dev.bithole.debugrenderers.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PingCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("ping")
                .executes(ctx -> showPing(ctx.getSource()))
                .then(ClientCommandManager.argument("playerName", StringArgumentType.word())
                        .executes(ctx -> showPing(ctx.getSource(), StringArgumentType.getString(ctx, "playerName")))));
    }

    private static int showPing(FabricClientCommandSource source, String playerName) {
        return showPing(source, MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(playerName));
    }

    private static int showPing(FabricClientCommandSource source) {
        return showPing(source, MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(source.getPlayer().getUuid()));
    }

    private static int showPing(FabricClientCommandSource source, PlayerListEntry info) {
        if(info == null) {
            source.sendError(Text.translatable("commands.ping.noInfo"));
            return 1;
        }
        Formatting color;
        if(info.getLatency() < 100) {
            color = Formatting.GREEN;
        } else if(info.getLatency() < 200) {
            color = Formatting.YELLOW;
        } else {
            color = Formatting.RED;
        }
        source.sendFeedback(Text.translatable("commands.ping.result", info.getProfile().getName()).append(Text.literal(info.getLatency() + "ms").formatted(color)));
        return 0;
    }

}