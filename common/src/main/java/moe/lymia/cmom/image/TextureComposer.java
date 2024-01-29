package moe.lymia.cmom.image;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class TextureComposer {
    private final BufferedImage image;
    private final Graphics2D gfx;

    public TextureComposer(int x, int y) {
        this.image = new BufferedImage(x, y, BufferedImage.TYPE_4BYTE_ABGR);
        this.gfx = this.image.createGraphics();

        gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    public void paste(BufferedImage src) {
        gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        gfx.drawImage(src, 0, 0, image.getWidth(), image.getHeight(), null);
    }

    public void pasteBlend(BufferedImage src, Composite effect) {
        gfx.setComposite(effect);
        gfx.drawImage(src, 0, 0, image.getWidth(), image.getHeight(), null);
    }
}
