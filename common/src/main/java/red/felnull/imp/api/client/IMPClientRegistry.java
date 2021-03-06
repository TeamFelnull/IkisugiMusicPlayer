package red.felnull.imp.api.client;

import net.minecraft.resources.ResourceLocation;
import red.felnull.imp.api.IamMusicPlayerAPI;
import red.felnull.imp.api.register.IMPMusicPlayerRegister;
import red.felnull.imp.client.music.loader.IMusicLoader;
import red.felnull.otyacraftengine.api.register.OERegistries;

import java.util.List;

public class IMPClientRegistry {
    public static void init() {
        IMPMusicPlayerRegister musicPlayerRegister = new IMPMusicPlayerRegister();
        OERegistries.setRegistry(IMPMusicPlayerRegister.class, musicPlayerRegister);
        IamMusicPlayerAPI.getInstance().integrationConsumer(n -> {
            n.registrationMusicPlayerLoader(musicPlayerRegister);
        });

        OERegistries.getRegistry(IMPMusicPlayerRegister.class).getMap().values().forEach(IMusicLoader::init);
    }

    public static IMusicLoader getLoader(ResourceLocation location) {
        return OERegistries.getRegistry(IMPMusicPlayerRegister.class).getMap().get(location);
    }

    public static boolean isLoaderContains(ResourceLocation location) {
        return OERegistries.getRegistry(IMPMusicPlayerRegister.class).getMap().containsKey(location);
    }

    public static List<IMusicLoader> getLoaders() {
        return OERegistries.getRegistry(IMPMusicPlayerRegister.class).getMap().values().stream().toList();
    }

    public static List<ResourceLocation> getLoaderLocations() {
        return OERegistries.getRegistry(IMPMusicPlayerRegister.class).getMap().keySet().stream().toList();
    }
}
