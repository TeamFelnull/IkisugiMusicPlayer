package red.felnull.imp.client.gui.screen.monitor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import red.felnull.imp.blockentity.MusicSharingDeviceBlockEntity;
import red.felnull.imp.client.gui.IMPFonts;
import red.felnull.imp.client.gui.components.MSDSmartEditBox;
import red.felnull.imp.client.gui.screen.MusicSharingDeviceScreen;
import red.felnull.imp.client.renderer.PlayImageRenderer;
import red.felnull.imp.data.resource.ImageInfo;
import red.felnull.otyacraftengine.client.util.IKSGRenderUtil;
import red.felnull.otyacraftengine.util.IKSGImageUtil;
import red.felnull.otyacraftengine.util.IKSGURLUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ImageNameMonitor extends MSDBaseMonitor {
    private static final Gson GSON = new Gson();
    private static final Map<String, ImageOPURLData> optimizations = new HashMap<>();
    private MSDSmartEditBox imageURLTextBox;
    protected MSDSmartEditBox nameTextBox;
    protected ImageInfo imageInfo = ImageInfo.EMPTY;
    private ImageOptimizationThread optimizationThread;
    private ImageLoadState loadState = ImageLoadState.NONE;
    private String errorMessage;
    protected boolean canChangeImage = true;
    private ImageButton fileCooseImageButton;
    private ImageButton faseImageButton;
    private ImageButton clearImageButton;


    public ImageNameMonitor(Component component, MusicSharingDeviceBlockEntity.Screen msdScreen, MusicSharingDeviceScreen parentScreen, int x, int y, int width, int height) {
        super(component, msdScreen, parentScreen, x, y, width, height);
    }


    @Override
    public void init() {
        super.init();
        this.imageURLTextBox = addCreateSmartTextEditBox(new TranslatableComponent("imp.msdTextBox.imageURL"), x + 45, y + 23, 53, n -> {
            if (n.isEmpty())
                return;

            setImage(n);
        });

        this.nameTextBox = addCreateSmartTextEditBox(new TranslatableComponent("imp.msdTextBox.name"), x + 101, y + 23, 95, n -> {
            if (imageInfo.getImageType() == ImageInfo.ImageType.STRING) {
                setImageInfo(new ImageInfo(ImageInfo.ImageType.STRING, n));
            }
        });

        this.nameTextBox.active = this.nameTextBox.visible = canChangeImage;
        this.imageURLTextBox.active = this.imageURLTextBox.visible = canChangeImage;

        boolean canFileCoose = false;

        this.fileCooseImageButton = this.addRenderableWidget(new ImageButton(x + 45, y + 54, 8, 8, 0, 107, 8, MSD_WIDGETS, n -> {
        }));
        this.fileCooseImageButton.active = this.fileCooseImageButton.visible = canFileCoose;

        this.faseImageButton = this.addRenderableWidget(new ImageButton(x + 53 - (canFileCoose ? 0 : 8), y + 54, 8, 8, 8, 107, 8, MSD_WIDGETS, n -> {
            setImageInfo(new ImageInfo(ImageInfo.ImageType.PLAYER_FACE, getMinecraft().player.getGameProfile().getName()));
        }));

        this.clearImageButton = this.addRenderableWidget(new ImageButton(x + 61 - (canFileCoose ? 0 : 8), y + 54, 8, 8, 16, 107, 8, MSD_WIDGETS, n -> {
            setImageInfo(new ImageInfo(ImageInfo.ImageType.STRING, nameTextBox.getValue()));
        }));
    }


    @Override
    public void disable() {
        super.disable();
        if (optimizationThread != null) {
            optimizationThread.stopped();
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        super.render(poseStack, i, j, f);
        fillXDarkGrayLine(poseStack, x + 3, y + 23, 39);
        fillXDarkGrayLine(poseStack, x + 3, y + 61, 39);
        fillYDarkGrayLine(poseStack, x + 3, y + 24, 37);
        fillYDarkGrayLine(poseStack, x + 41, y + 24, 37);
        fillLightGray(poseStack, x + 4, y + 24, 37, 37);

        PlayImageRenderer.getInstance().render(imageInfo, poseStack, x + 4, y + 24, 37, false);
        if (canChangeImage) {
            MutableComponent component;
            if (loadState != ImageLoadState.NONE) {
                component = loadState.getComponent().copy();
                if (loadState == ImageLoadState.ERROR) {
                    if (errorMessage != null) {
                        component.append(": " + errorMessage);
                    }
                    component.withStyle(ChatFormatting.RED);
                } else {
                    component.append("...");
                }
            } else {
                component = new TranslatableComponent("imp.msdText.setImageInfo");
            }
            poseStack.pushPose();
            component = component.withStyle(IMPFonts.FLOPDE_SIGN_FONT);
            float scale = Math.min(1f, 150f / getFont().width(component));
            IKSGRenderUtil.poseScaleAll(poseStack, scale);
            drawPrettyString(poseStack, component, (x + 44f) / scale, (y + 43f) / scale, 0);
            poseStack.popPose();
        }
    }


    public void setImageInfo(ImageInfo info) {
        if (optimizationThread != null) {
            optimizationThread.stopped();
            optimizationThread = null;
        }

        if (info.getImageType() != ImageInfo.ImageType.URL) {
            imageURLTextBox.setValue("");
        }

        this.imageInfo = info;
    }

    @Override
    public void onFilesDrop(List<Path> list) {
        if (!canChangeImage)
            return;
        if (list.size() == 1) {
            File droppedFile = list.get(0).toFile();
            if (droppedFile.exists()) {
                setImage(droppedFile);
            }
        }
    }

    protected void setImage(String url) {

        if (optimizations.containsValue(url)) {
            imageURLTextBox.setValue(optimizations.get(url).url());
            return;
        }

        AtomicBoolean enf = new AtomicBoolean(false);
        optimizations.values().stream().filter(n -> n.url().equals(url)).findFirst().ifPresent(n -> {
            setImageInfo(new ImageInfo(ImageInfo.ImageType.URL, n.url(), n.w(), n.h()));
            enf.set(true);
        });
        if (enf.get())
            return;

        setImage(null, url);
    }

    protected void setImage(File file) {
        setImage(file, null);
    }

    private void setImage(File file, String url) {
        if (optimizationThread != null) {
            optimizationThread.stopped();
            optimizationThread = null;
        }

        if (url != null)
            optimizationThread = new ImageOptimizationThread(url);
        else
            optimizationThread = new ImageOptimizationThread(file);

        optimizationThread.start();
    }

    private class ImageOptimizationThread extends Thread {
        private boolean stopped;
        private final String url;
        private final File file;

        private ImageOptimizationThread(File file) {
            this.url = null;
            this.file = file;
        }

        private ImageOptimizationThread(String url) {
            this.url = url;
            this.file = null;
        }

        @Override
        public void run() {

            try {
                loadState = ImageLoadState.LOADING;
                ImageData opImage;

                if (url != null) {
                    URL imurl = new URL(url);
                    opImage = imageOptimization(IKSGURLUtil.getStream(imurl));
                } else {
                    opImage = imageOptimization(new FileInputStream(file));
                }

                if (stopped)
                    return;

                loadState = ImageLoadState.UPLOADING;

                HttpClient hc = HttpClient.newHttpClient();
                HttpRequest hr = HttpRequest.newBuilder(URI.create("https://api.imgur.com/3/image"))
                        .POST(HttpRequest.BodyPublishers.ofByteArray(opImage.imageData()))
                        .header("Authorization", "Client-ID d33f23f7c189083")
                        .build();

                HttpResponse<String> res = hc.send(hr, HttpResponse.BodyHandlers.ofString());
                JsonObject upData = GSON.fromJson(res.body(), JsonObject.class);

                if (stopped)
                    return;

                String upLoadedURL = upData.getAsJsonObject("data").get("link").getAsString();
                optimizations.put(url, new ImageOPURLData(upLoadedURL, opImage.w, opImage.h));

                if (stopped)
                    return;

                imageInfo = new ImageInfo(ImageInfo.ImageType.URL, upLoadedURL, opImage.w, opImage.h);

                imageURLTextBox.setValue(upLoadedURL);


                loadState = ImageLoadState.NONE;

            } catch (Exception ex) {
                loadState = ImageLoadState.ERROR;
                errorMessage = ex.getLocalizedMessage();
            }
        }

        public void stopped() {
            this.stopped = true;
            loadState = ImageLoadState.NONE;
        }
    }


    private ImageData imageOptimization(InputStream stream) throws IOException {

        byte[] data = stream.readAllBytes();

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));

        int wi = image.getWidth();
        int hi = image.getHeight();

        float w = wi >= hi ? 1 : (float) wi / hi;
        float h = hi >= wi ? 1 : (float) hi / wi;

        if (w > h) {
            h *= 1f / w;
            w = 1;
        }
        if (h > w) {
            w *= 1f / h;
            h = 1;
        }

        int ws = (int) (256 * w);
        int hs = (int) (256 * h);

        String formatName = IKSGImageUtil.getFormatName(data);
        long maxL = 1024L * 1024;
        if ("gif".equalsIgnoreCase(formatName)) {
            byte[] gd = IKSGImageUtil.resizeGif(data, ws, hs);
            for (int i = 0; i < 3; i++) {
                if (gd.length > maxL) {
                    int fc = IKSGImageUtil.gifDivide(gd).length;
                    int reframe = (int) (((double) maxL / (double) gd.length) * (double) fc);
                    gd = IKSGImageUtil.reframeGif(gd, reframe);
                }
            }
            for (int i = 0; i < 3; i++) {
                if (gd.length > maxL) {
                    int fc = IKSGImageUtil.gifDivide(gd).length;
                    int reframe = (int) ((double) fc / 2d);
                    gd = IKSGImageUtil.reframeGif(gd, reframe);
                }
            }
            if (gd.length > maxL) {
                throw new IOException("Size over");
            }
            return new ImageData(gd, w, h);
        }

        BufferedImage outImage = IKSGImageUtil.resize(image, ws, hs);
        byte[] datab = IKSGImageUtil.toByte(outImage, formatName);

        if (datab.length > maxL) {
            throw new IOException("Size over");
        }

        return new ImageData(datab, w, h);
    }

    @Override
    public void tick() {
        super.tick();
        this.faseImageButton.visible = this.faseImageButton.active = this.clearImageButton.visible = this.clearImageButton.active = this.fileCooseImageButton.visible = this.fileCooseImageButton.active = canChangeImage;
    }

    private static enum ImageLoadState {
        NONE(new TextComponent("none")),
        LOADING(new TranslatableComponent("imp.imageLoadState.loading")),
        UPLOADING(new TranslatableComponent("imp.imageLoadState.uploading")),
        ERROR(new TranslatableComponent("imp.imageLoadState.error"));
        private final MutableComponent component;

        private ImageLoadState(MutableComponent component) {
            this.component = component;
        }

        public MutableComponent getComponent() {
            return component;
        }
    }


    private static record ImageOPURLData(String url, float w, float h) {

    }

    private static record ImageData(byte[] imageData, float w, float h) {

    }
}
