package dev.bithole.debugrenderers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class DebugRenderersMod implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("debugrenderers");
    private static Properties classnameMap;

    public static String remap(String classname) {
        if(classnameMap != null) {
            return (String)classnameMap.getOrDefault(classname, classname);
        }
        return "<mappings unavailable>";
    }

    @Override
    public void onInitialize() {

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("debugrenderers", "resources");
            }

            @Override
            public void reload(ResourceManager manager) {
                Map<Identifier, Resource> resources = manager.findResources("mappings", path -> true);
                LOGGER.info("# of resources: " + resources.size());
                for(Resource resource: resources.values()) {
                    if(resource != null) {
                        try(InputStream stream = resource.getInputStream()) {
                            classnameMap = new Properties();
                            classnameMap.load(stream);
                            LOGGER.info("Mappings loaded!");
                        } catch(Exception e) {
                            LOGGER.error("Failed to load mappings");
                        }
                    } else {
                        LOGGER.error("Mappings not found");
                    }
                }
            }

        });

    }

}
