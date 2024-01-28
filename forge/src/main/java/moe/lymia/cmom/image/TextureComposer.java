package moe.lymia.cmom.image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TextureComposer {
    private final BufferedImage image;
    private final Graphics2D gfx;

    public TextureComposer(int x, int y) {
        this.image = new BufferedImage(x, y, BufferedImage.TYPE_4BYTE_ABGR);
        this.gfx = this.image.createGraphics();
    }

    public void composeAlpha(BufferedImage src) {
        image.getGraphics().drawImage(src, 0, 0, image.getWidth(), image.getHeight(), null);
    }

    public void composeSoftLight(BufferedImage src) {

    }
}
