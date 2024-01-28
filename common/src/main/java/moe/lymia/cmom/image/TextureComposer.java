package moe.lymia.cmom.image;

import moe.lymia.contrib.swingx.BlendComposite;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class TextureComposer {
    private final BufferedImage image;
    private final Graphics2D gfx;

    public TextureComposer(int x, int y) {
        this.image = new BufferedImage(x, y, BufferedImage.TYPE_4BYTE_ABGR);
        this.gfx = this.image.createGraphics();
    }

    public void composeAlpha(BufferedImage src) {
        gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        gfx.drawImage(src, 0, 0, image.getWidth(), image.getHeight(), null);
    }

    private static BufferedImage layerBlend(BufferedImage src, float alpha) {
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D gfx = dst.createGraphics();
        gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        gfx.drawImage(src, 0, 0, null);
        return dst;
    }

    public void composeEffect(BufferedImage src, float alpha, BlendComposite effect) {
        BufferedImage layer = layerBlend(src, alpha);
        gfx.setComposite(effect);
        gfx.drawImage(layer, 0, 0, image.getWidth(), image.getHeight(), null);
    }
}
