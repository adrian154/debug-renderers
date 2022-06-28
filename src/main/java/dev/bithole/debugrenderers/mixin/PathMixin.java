package dev.bithole.debugrenderers.mixin;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mixin(Path.class)
public abstract class PathMixin {

    @Accessor("debugNodes")
    public abstract void setOpenSet(PathNode[] openSet);

    @Accessor("debugSecondNodes")
    public abstract void setClosedSet(PathNode[] closedSet);

    @Accessor("debugTargetNodes")
    public abstract void setDebugTargetNodes(Set<TargetPathNode> targets);

    @Inject(at = @At("HEAD"), method = "toBuffer(Lnet/minecraft/network/PacketByteBuf;)V")
    public void toBuffer(CallbackInfo info) {

        // categorize nodes into open/closed based on the `closed` parameter
        // this *seems* right, but I have no idea whether it is...
        List<PathNode> openSet = new ArrayList<>(),
                       closedSet = new ArrayList<>();

        Path self = (Path)(Object)this;
        for (int i = 0; i < self.getLength(); i++) {
            PathNode node = self.getNode(i);
            if (node.visited)
                closedSet.add(node);
            else
                openSet.add(node);
        }

        setOpenSet(openSet.toArray(new PathNode[0]));
        setClosedSet(closedSet.toArray(new PathNode[0]));

        // this value appears to be inaccessible outside of Path, so we won't worry about its function too much
        // however, Path::toBuffer won't write anything if this set has zero members, so we have to pass a dummy element
        setDebugTargetNodes(Collections.singleton(new TargetPathNode(0, 0, 0)));

    }

}
