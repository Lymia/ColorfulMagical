package moe.lymia.cmom.image

import java.awt._
import java.awt.image.BufferedImage

final class TextureComposer(x: Int, y: Int) {
  private val image: BufferedImage = new BufferedImage(x, y, BufferedImage.TYPE_4BYTE_ABGR)
  private val gfx: Graphics2D      = this.image.createGraphics()

  gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)

  def paste(src: BufferedImage): Unit = {
    gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER))
    gfx.drawImage(src, 0, 0, image.getWidth, image.getHeight, null)
  }

  def pasteBlend(src: BufferedImage, effect: Composite): Unit = {
    gfx.setComposite(effect)
    gfx.drawImage(src, 0, 0, image.getWidth, image.getHeight, null)
  }
}
