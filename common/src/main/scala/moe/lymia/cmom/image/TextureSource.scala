package moe.lymia.cmom.image

import java.awt.image.BufferedImage

enum TextureSource {
  case TextureData(data: BufferedImage)
  case SpriteSheetLocation(data: BufferedImage, x: Int, y: Int, w: Int, h: Int)


}