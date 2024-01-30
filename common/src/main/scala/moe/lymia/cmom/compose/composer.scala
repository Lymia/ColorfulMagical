package moe.lymia.cmom.compose

import com.jhlabs.composite.*
import moe.lymia.cmom.compose.TextureSource.SpriteSheetLocation

import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

final class TextureComposer(x: Int, y: Int) {
  private val image: BufferedImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB)

  private var i = 0
  def paste(src: TextureSource, mode: BlendingMode = BlendingMode.Alpha): Unit = {
    val gfx = this.image.createGraphics()
    gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
    gfx.setComposite(mode.compositor)
    src match {
      case TextureSource.TextureData(img) =>
        println((i, mode, gfx.getComposite, img))
        gfx.drawImage(img, 0, 0, image.getWidth, image.getHeight, null)
      case SpriteSheetLocation(img, x, y, x1, y1) =>
        println((i, x, y, x1, y1, mode, gfx.getComposite, img))
        gfx.drawImage(img, 0, 0, image.getWidth, image.getHeight, x, y, x1, y1, null)
    }
    ImageIO.write(image, "png", new File(f"layer_$i.png"))
    i += 1
  }

  def getImage: BufferedImage = image
}

enum TextureSource {
  case TextureData(data: BufferedImage)
  case SpriteSheetLocation(data: BufferedImage, x: Int, y: Int, x1: Int, y1: Int)
}

enum BlendingMode {
  case Alpha, Multiply, Screen, Difference, Addition, Subtract, DarkenOnly, LightenOnly
  case OklabHue, OklabChroma, OklabColor, OklabLightness
  case Dodge, Burn, HardLight, SoftLight, Overlay

  lazy val compositor: Composite = this match {
    case Alpha          => AlphaComposite.SrcOver
    case Multiply       => new MultiplyComposite(1)
    case Screen         => new ScreenComposite(1)
    case Difference     => new DifferenceComposite(1)
    case Addition       => new AddComposite(1)
    case Subtract       => new SubtractComposite(1)
    case DarkenOnly     => new DarkenComposite(1)
    case LightenOnly    => new LightenComposite(1)
    case OklabHue       => GimpComposite(GimpBlendingMode.OklabHue)
    case OklabChroma    => GimpComposite(GimpBlendingMode.OklabChroma)
    case OklabColor     => GimpComposite(GimpBlendingMode.OklabColor)
    case OklabLightness => GimpComposite(GimpBlendingMode.OklabLightness)
    case Dodge          => new DodgeComposite(1)
    case Burn           => new BurnComposite(1)
    case HardLight      => new HardLightComposite(1)
    case SoftLight      => new SoftLightComposite(1)
    case Overlay        => new OverlayComposite(1)
  }
}
object BlendingMode {
  private val GIMP_MODE_MAP = Map(
    "Normal (legacy)"             -> Some(BlendingMode.Alpha),
    "Dissolve (legacy)"           -> Some(BlendingMode.Alpha),          // just do alpha; wrong, but close enough
    "Behind (legacy)"             -> None,
    "Multiply (legacy)"           -> Some(BlendingMode.Multiply),
    "Screen (legacy)"             -> Some(BlendingMode.Screen),
    "Old broken Overlay"          -> Some(BlendingMode.Overlay),        // idc if this unbreaks it
    "Difference (legacy)"         -> Some(BlendingMode.Difference),
    "Addition (legacy)"           -> Some(BlendingMode.Addition),
    "Subtract (legacy)"           -> Some(BlendingMode.Subtract),
    "Darken only (legacy)"        -> Some(BlendingMode.LightenOnly),
    "Lighten only (legacy)"       -> Some(BlendingMode.DarkenOnly),
    "Hue (HSV) (legacy)"          -> Some(BlendingMode.OklabHue),       // TODO: HSV/HSL
    "Saturation (HSV) (legacy)"   -> Some(BlendingMode.OklabChroma),    // TODO: HSV/HSL
    "Color (HSL) (legacy)"        -> Some(BlendingMode.OklabColor),     // TODO: HSV/HSL
    "Value (HSV) (legacy)"        -> Some(BlendingMode.OklabLightness), // TODO: HSV/HSL
    "Divide (legacy)"             -> None,
    "Dodge (legacy)"              -> Some(BlendingMode.Dodge),
    "Burn (legacy)"               -> Some(BlendingMode.Burn),
    "Hard Light (legacy)"         -> Some(BlendingMode.HardLight),
    "Soft light (legacy)"         -> Some(BlendingMode.SoftLight),
    "Grain extract (legacy)"      -> None,
    "Grain merge (legacy)"        -> None,
    "Color erase (legacy)"        -> None,
    "Overlay"                     -> Some(BlendingMode.Overlay),
    "Hue (LCH)"                   -> Some(BlendingMode.OklabHue),
    "Chroma (LCH)"                -> Some(BlendingMode.OklabChroma),
    "Color (LCH)"                 -> Some(BlendingMode.OklabColor),
    "Lightness (LCH)"             -> Some(BlendingMode.OklabLightness),
    "Normal"                      -> Some(BlendingMode.Alpha),
    "Behind"                      -> None,
    "Multiply"                    -> Some(BlendingMode.Multiply),
    "Screen"                      -> Some(BlendingMode.Screen),
    "Difference"                  -> Some(BlendingMode.Difference),
    "Addition"                    -> Some(BlendingMode.Addition),
    "Substract"                   -> Some(BlendingMode.Subtract),
    "Darken only"                 -> Some(BlendingMode.DarkenOnly),
    "Lighten only"                -> Some(BlendingMode.LightenOnly),
    "Hue (HSV)"                   -> Some(BlendingMode.OklabHue),       // TODO: HSV/HSL
    "Saturation (HSV)"            -> Some(BlendingMode.OklabChroma),    // TODO: HSV/HSL
    "Color (HSL)"                 -> Some(BlendingMode.OklabColor),     // TODO: HSV/HSL
    "Value (HSV)"                 -> Some(BlendingMode.OklabLightness), // TODO: HSV/HSL
    "Divide"                      -> None,
    "Dodge"                       -> Some(BlendingMode.Dodge),
    "Burn"                        -> Some(BlendingMode.Burn),
    "Hard light"                  -> Some(BlendingMode.HardLight),
    "Soft light"                  -> Some(BlendingMode.SoftLight),
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
