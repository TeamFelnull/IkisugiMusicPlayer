package red.felnull.imp.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.MutableComponent;
import red.felnull.imp.client.gui.IMPFonts;
import red.felnull.otyacraftengine.client.gui.components.IIkisugibleWidget;
import red.felnull.otyacraftengine.util.IKSGColorUtil;

public interface IMSDSmartRender extends IIkisugibleWidget {
    default void fillXGrayLine(PoseStack ps, int x, int y, int s) {
        GuiComponent.fill(ps, x, y, x + s, y + 1, IKSGColorUtil.toSRGB(14474460));
    }

    default void fillYGrayLine(PoseStack ps, int x, int y, int s) {
        GuiComponent.fill(ps, x, y, x + 1, y + s, IKSGColorUtil.toSRGB(14474460));
    }

    default void fillLightGray(PoseStack poseStack, int x, int y, int w, int h) {
        GuiComponent.fill(poseStack, x, y, x + w, y + h, IKSGColorUtil.toSRGB(16119543));
    }

    default void fillGreen(PoseStack poseStack, float x, float y, float w, float h) {
        gFill(poseStack, x, y, x + w, y + h, IKSGColorUtil.toSRGB(0x115d0e));
    }

    default void fillGray(PoseStack poseStack, int x, int y, int w, int h) {
        GuiComponent.fill(poseStack, x, y, x + w, y + h, IKSGColorUtil.toSRGB(14474460));
    }

    default void gFill(PoseStack poseStack, float i, float j, float k, float l, int m) {
        gInnerFill(poseStack.last().pose(), i, j, k, l, m);
    }

    default void gInnerFill(Matrix4f matrix4f, float i, float j, float k, float l, int m) {
        float o;
        if (i < k) {
            o = i;
            i = k;
            k = o;
        }

        if (j < l) {
            o = j;
            j = l;
            l = o;
        }

        float f = (float) (m >> 24 & 255) / 255.0F;
        float g = (float) (m >> 16 & 255) / 255.0F;
        float h = (float) (m >> 8 & 255) / 255.0F;
        float p = (float) (m & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, i, l, 0.0F).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, k, l, 0.0F).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, k, j, 0.0F).color(g, h, p, f).endVertex();
        bufferBuilder.vertex(matrix4f, i, j, 0.0F).color(g, h, p, f).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    default void fillMediumGray(PoseStack poseStack, int x, int y, int w, int h) {
        GuiComponent.fill(poseStack, x, y, x + w, y + h, IKSGColorUtil.toSRGB(0x656565));
    }

    default void fillBerryDarkGray(PoseStack poseStack, int x, int y, int w, int h) {
        GuiComponent.fill(poseStack, x, y, x + w, y + h, IKSGColorUtil.toSRGB(0x343434));
    }

    default void fillXDarkGrayLine(PoseStack ps, int x, int y, int s) {
        GuiComponent.fill(ps, x, y, x + s, y + 1, IKSGColorUtil.toSRGB(0x585858));
    }

    default void fillYDarkGrayLine(PoseStack ps, int x, int y, int s) {
        GuiComponent.fill(ps, x, y, x + 1, y + s, IKSGColorUtil.toSRGB(0x585858));
    }

    default void drawPrettyCenteredString(PoseStack poseStack, MutableComponent component, float i, float j, int k) {
        component = component.withStyle(IMPFonts.FLOPDE_SIGN_FONT);
        getFont().draw(poseStack, component, (float) (i - getFont().width(component) / 2), j, k);
    }

    default void drawPrettyString(PoseStack poseStack, MutableComponent component, float i, float j, int k) {
        component = component.withStyle(IMPFonts.FLOPDE_SIGN_FONT);
        getFont().draw(poseStack, component, i, j, k);
    }

    default void drawDarkBox(PoseStack poseStack, int x, int y, int w, int h) {
        fillXDarkGrayLine(poseStack, x, y, w);
        fillXDarkGrayLine(poseStack, x, y + h - 1, w);
        fillYDarkGrayLine(poseStack, x, y + 1, h - 1);
        fillYDarkGrayLine(poseStack, x + w - 1, y + 1, h - 1);
        fillBerryDarkGray(poseStack, x + 1, y + 1, w - 2, h - 2);
    }

    default void fillXLine(PoseStack ps, int x, int y, int s, int col) {
        GuiComponent.fill(ps, x, y, x + s, y + 1, col);
    }

    default void fillYLine(PoseStack ps, int x, int y, int s, int col) {
        GuiComponent.fill(ps, x, y, x + 1, y + s, col);
    }

    default void drawFill(PoseStack poseStack, int x, int y, int w, int h, int col) {
        GuiComponent.fill(poseStack, x, y, x + w, y + h, col);
    }

    default void drawSmartButtonBox(PoseStack poseStack, int x, int y, int w, int h, int st) {
        int c1 = st == 0 ? 0x383838 : st == 1 ? 0xa9a9a9 : 0x9ebdd1;
        int c2 = st == 0 ? 0x454545 : st == 1 ? 0xb9b9b9 : 0xb3d4e9;

        fillXLine(poseStack, x, y, w, IKSGColorUtil.toSRGB(c1));
        fillXLine(poseStack, x, y + h - 1, w, IKSGColorUtil.toSRGB(c1));
        fillYLine(poseStack, x, y + 1, h - 1, IKSGColorUtil.toSRGB(c1));
        fillYLine(poseStack, x + w - 1, y + 1, h - 1, IKSGColorUtil.toSRGB(c1));
        drawFill(poseStack, x + 1, y + 1, w - 2, h - 2, IKSGColorUtil.toSRGB(c2));
    }
}
