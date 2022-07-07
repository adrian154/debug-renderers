package dev.bithole.debugrenderers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;

public class DebugRenderersCommand {

    private static final RendererSuggestionProvider rendererSuggestionProvider = new RendererSuggestionProvider();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("dr")
                .then(argument("renderer", word())
                        .suggests(rendererSuggestionProvider)
                        .executes(ctx -> toggleRenderer(ctx.getSource(), getString(ctx, "renderer"))))
                .executes(ctx -> listRenderers(ctx.getSource())));
    }

    private static int listRenderers(FabricClientCommandSource source) {

        List<String> enabled = new ArrayList<>(), disabled = new ArrayList<>();
        for(Map.Entry<String, DebugRenderer.Renderer> entry: DebugRenderersClientMod.debugRenderers.entrySet()) {
            if(DebugRenderersClientMod.rendererStatus.get(entry.getValue())) {
                enabled.add(entry.getKey());
            } else {
                disabled.add(entry.getKey());
            }
        }

        source.sendFeedback(Text.translatable("commands.dr.enabledHeader")
                .styled(style -> style.withBold(true).withColor(Formatting.GREEN))
                .append((enabled.size() > 0 ? Text.literal(String.join(" ", enabled)) : Text.translatable("commands.dr.none")).styled(style -> style.withBold(false))));

        source.sendFeedback(Text.translatable("commands.dr.disabledHeader")
                .styled(style -> style.withBold(true).withColor(Formatting.GRAY))
                .append((disabled.size() > 0 ? Text.literal(String.join(" ", disabled)) : Text.translatable("commands.dr.none")).styled(style->style.withBold(false))));

        return 0;
    }

    private static int toggleRenderer(FabricClientCommandSource source, String rendererName) {
        DebugRenderer.Renderer debugRenderer = DebugRenderersClientMod.debugRenderers.get(rendererName);
        if(debugRenderer != null) {
            Boolean status = DebugRenderersClientMod.rendererStatus.get(debugRenderer);
            if(DebugRenderersClientMod.rendererStatus.put(debugRenderer, !status)) {
                source.sendFeedback(Text.translatable("commands.dr.onDisabled", rendererName));
            } else {
                source.sendFeedback(Text.translatable("commands.dr.onEnabled", rendererName));
            }
        } else {
            source.sendError(Text.translatable("commands.dr.noSuchRenderer", rendererName));
        }

        return 0;
    }

    private static class RendererSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
            for(String rendererName: DebugRenderersClientMod.debugRenderers.keySet()) {
                builder.suggest(rendererName);
            }
            return builder.buildFuture();
        }

    }

}
