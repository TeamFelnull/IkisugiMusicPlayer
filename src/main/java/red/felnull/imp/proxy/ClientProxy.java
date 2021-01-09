package red.felnull.imp.proxy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import red.felnull.imp.client.data.IMPClientRegistration;
import red.felnull.imp.client.data.MusicDownloader;
import red.felnull.imp.client.data.MusicUploader;
import red.felnull.imp.client.data.YoutubeData;
import red.felnull.imp.client.gui.IMPScrennContainerRegister;
import red.felnull.imp.client.gui.toasts.FFmpegLoadToast;
import red.felnull.imp.client.handler.ClientMusicHandler;
import red.felnull.imp.client.handler.MusicRingerHandler;
import red.felnull.imp.client.handler.MusicUploadHandler;
import red.felnull.imp.client.handler.RenderHandler;
import red.felnull.imp.client.music.MusicThread;
import red.felnull.imp.client.music.ClientWorldMusicManager;
import red.felnull.imp.client.renderer.tileentity.IMPTileEntityRenderers;
import red.felnull.imp.ffmpeg.FFmpegDownloader;
import red.felnull.imp.ffmpeg.FFmpegManeger;

public class ClientProxy extends CommonProxy {
    public static void clientInit() {
        IMPTileEntityRenderers.registerTileEntityRenderer();
        IMPScrennContainerRegister.registerFactories();
        YoutubeData.init();
    }

    @Override
    public void preInit() {
        super.preInit();
        MusicUploader.init();
        MusicDownloader.init();
        IMPClientRegistration.init();
    }

    @Override
    public void init() {
        super.init();
        MinecraftForge.EVENT_BUS.register(RenderHandler.class);
        MinecraftForge.EVENT_BUS.register(MusicUploadHandler.class);
        MinecraftForge.EVENT_BUS.register(ClientMusicHandler.class);
        MinecraftForge.EVENT_BUS.register(MusicRingerHandler.class);
        ClientWorldMusicManager.init();
    }

    @Override
    public void posInit() {
        super.posInit();
        MusicThread.startThread();
    }

    @Override
    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    @Override
    public void addFFmpegLoadToast() {
        FFmpegManeger maneger = FFmpegManeger.instance();
        if ((maneger.getState() == FFmpegManeger.FFmpegState.DOWNLOADING || maneger.getState() == FFmpegManeger.FFmpegState.EXTRACTING || maneger.getState() == FFmpegManeger.FFmpegState.PREPARATION) && !FFmpegLoadToast.isAlreadyExists()) {
            Minecraft.getInstance().getToastGui().add(new FFmpegLoadToast());
        }
    }
}
