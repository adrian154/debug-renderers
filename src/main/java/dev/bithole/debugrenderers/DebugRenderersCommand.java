package dev.bithole.debugrenderers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;

public class DebugRenderersCommand {

    private static final RendererSuggestionProvider rendererSuggestionProvider = new RendererSuggestionProvider();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("dbr")
                .then(argument("renderer", word())
                        .suggests(rendererSuggestionProvider)
                        .executes(ctx -> toggleRenderer(ctx.getSource(), getString(ctx, "renderer")))));
    }

    private static int toggleRenderer(FabricClientCommandSource source, String rendererName) {
        DebugRenderer.Renderer debugRenderer = DebugRenderersClientMod.debugRenderers.get(rendererName);
        if(debugRenderer != null) {
            Boolean status = DebugRenderersClientMod.rendererStatus.get(debugRenderer);
            if(DebugRenderersClientMod.rendererStatus.put(debugRenderer, !status)) {
                source.sendFeedback(Text.translatable("commands.dbr.disabled", rendererName));
            } else {
                source.sendFeedback(Text.translatable("commands.dbr.enabled", rendererName));
            }
        } else {
            source.sendError(Text.translatable("commands.dbr.noSuchRenderer", rendererName));
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
