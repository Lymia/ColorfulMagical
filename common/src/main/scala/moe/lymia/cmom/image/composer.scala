package moe.lymia.cmom.image

import moe.lymia.cmom.image.TextureSource.SpriteSheetLocation
import org.jdesktop.swingx.graphics.BlendComposite

import java.awt.*
import java.awt.image.BufferedImage

final class TextureComposer(x: Int, y: Int) {
  private val image: BufferedImage = new BufferedImage(x, y, BufferedImage.TYPE_4BYTE_ABGR)
  private val gfx: Graphics2D      = this.image.createGraphics()

  gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)

  def paste(src: TextureSource, mode: BlendingMode = BlendingMode.Alpha): Unit = {
    gfx.setComposite(mode.compositor)
    src match {
      case TextureSource.TextureData(img) =>
        gfx.drawImage(img, 0, 0, image.getWidth, image.getHeight, null)
      case SpriteSheetLocation(img, x, y, w, h) =>
        gfx.drawImage(img, 0, 0, img.getWidth, img.getHeight, x, y, x + w, y + h, null)
    }
  }
}

enum TextureSource {
  case TextureData(data: BufferedImage)
  case SpriteSheetLocation(data: BufferedImage, x: Int, y: Int, w: Int, h: Int)
}

enum BlendingMode {
  case Alpha, Multiply, Screen, Difference, Addition, Subtract, DarkenOnly, LightenOnly

  lazy val compositor: Composite = this match {
    case Alpha       => AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
    case Multiply    => BlendComposite.Multiply
    case Screen      => BlendComposite.Screen
    case Difference  => BlendComposite.Difference
    case Addition    => BlendComposite.Add
    case Subtract    => BlendComposite.Subtract
    case DarkenOnly  => BlendComposite.Darken
    case LightenOnly => BlendComposite.Lighten
  }
}
object BlendingMode {
  private val GIMP_MODE_MAP = Map(
    "Normal (legacy)"             -> Some(BlendingMode.Alpha),
    "Dissolve (legacy)"           -> None,
    "Behind (legacy)"             -> None,
    "Multiply (legacy)"           -> Some(BlendingMode.Multiply),
    "Screen (legacy)"             -> Some(BlendingMode.Screen),
    "Old broken Overlay"          -> None,
    "Difference (legacy)"         -> Some(BlendingMode.Difference),
    "Addition (legacy)"           -> Some(BlendingMode.Addition),
    "Subtract (legacy)"           -> Some(BlendingMode.Subtract),
    "Darken only (legacy)"        -> Some(BlendingMode.LightenOnly),
    "Lighten only (legacy)"       -> Some(BlendingMode.DarkenOnly),
    "Hue (HSV) (legacy)"          -> None,
    "Saturation (HSV) (legacy)"   -> None,
    "Color (HSL) (legacy)"        -> None,
    "Value (HSV) (legacy)"        -> None,
    "Divide (legacy)"             -> None,
    "Dodge (legacy)"              -> None,
    "Burn (legacy)"               -> None,
    "Hard Light (legacy)"         -> None,
    "Soft light (legacy)"         -> None,
    "Grain extract (legacy)"      -> None,
    "Grain merge (legacy)"        -> None,
    "Color erase (legacy)"        -> None,
    "Overlay"                     -> None,
    "Hue (LCH)"                   -> None,
    "Chroma (LCH)"                -> None,
    "Color (LCH)"                 -> None,
    "Lightness (LCH)"             -> None,
    "Normal"                      -> Some(BlendingMode.Alpha),
    "Behind"                      -> None,
    "Multiply"                    -> Some(BlendingMode.Multiply),
    "Screen"                      -> Some(BlendingMode.Screen),
    "Difference"                  -> Some(BlendingMode.Difference),
    "Addition"                    -> Some(BlendingMode.Addition),
    "Substract"                   -> Some(BlendingMode.Subtract),
    "Darken only"                 -> Some(BlendingMode.DarkenOnly),
    "Lighten only"                -> Some(BlendingMode.LightenOnly),
    "Hue (HSV)"                   -> None,
    "Saturation (HSV)"            -> None,
    "Color (HSL)"                 -> None,
    "Value (HSV)"                 -> None,
    "Divide"                      -> None,
    "Dodge"                       -> None,
    "Burn"                        -> None,
    "Hard light"                  -> None,
    "Soft light"                  -> None,
    "Grain extract"               -> None,
    "Grain merge"                 -> None,
    "Vivid light"                 -> None,
    "Pin light"                   -> None,
    "Linear light"                -> None,
    "Hard mix"                    -> None,
    "Exclusion"                   -> None,
    "Linear burn"                 -> None,
    "Luma/Luminance darken only"  -> None,
    "Luma/Luminance lighten only" -> None,
    "Luminance"                   -> None,
    "Color erase"                 -> None,
    "Erase"                       -> None,
    "Merge"                       -> None,
    "Split"                       -> None,
    "Pass through"                -> None
  )

  def forGimpMode(name: String): BlendingMode =
    GIMP_MODE_MAP.get(name).flatten.getOrElse(sys.error(s"unknown gimp blending mode: $name"))
}
