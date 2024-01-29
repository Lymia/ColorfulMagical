package moe.lymia.cmom.compose

import com.github.tommyettinger.colorful.pure.oklab.ColorTools as OklabTools
import com.github.tommyettinger.colorful.pure.rgb.ColorTools as RgbTools

import java.awt.image.*
import java.awt.{Composite, CompositeContext, RenderingHints}

private object ColorBlend {
  private def lerp(a: Float, b: Float, f: Float): Float = a * (1.0f - f) + (b * f)
  private def lerpRgb(rgbAInt: Int, rgbB: Float, f: Float): Int = {
    val rgbA = RgbTools.fromRGBA8888(rgbAInt)
    val out = RgbTools.fromRGBA(
      lerp(RgbTools.red(rgbA), RgbTools.red(rgbB), f),
      lerp(RgbTools.green(rgbA), RgbTools.green(rgbB), f),
      lerp(RgbTools.blue(rgbA), RgbTools.blue(rgbB), f),
      RgbTools.alpha(rgbA)
    )
    RgbTools.toRGBA8888(out)
  }

  def oklabTransferHue(src: Int, dst: Int): Int = {
    val srcHsl = OklabTools.fromRGBA8888(src)
    val dstHsl = OklabTools.fromRGBA8888(src)
    val oklab = OklabTools.oklabByHCL(
      OklabTools.oklabHue(srcHsl),
      OklabTools.chroma(dstHsl),
      OklabTools.oklabLightness(dstHsl),
      OklabTools.alpha(dstHsl)
    )
    lerpRgb(src, OklabTools.toRGBA(oklab), OklabTools.alpha(dstHsl))
  }
  def oklabTransferChroma(src: Int, dst: Int): Int = {
    val srcHsl = OklabTools.fromRGBA8888(src)
    val dstHsl = OklabTools.fromRGBA8888(src)
    val oklab = OklabTools.oklabByHCL(
      OklabTools.oklabHue(dstHsl),
      lerp(OklabTools.chroma(dstHsl), OklabTools.chroma(srcHsl), OklabTools.alpha(srcHsl)),
      OklabTools.oklabLightness(dstHsl),
      OklabTools.alpha(dstHsl)
    )
    OklabTools.toRGBA8888(oklab)
  }
  def oklabTransferColor(src: Int, dst: Int): Int = {
    val srcHsl = OklabTools.fromRGBA8888(src)
    val dstHsl = OklabTools.fromRGBA8888(src)
    val oklab = OklabTools.oklabByHCL(
      OklabTools.oklabHue(srcHsl),
      OklabTools.chroma(srcHsl),
      OklabTools.oklabLightness(dstHsl),
      OklabTools.alpha(dstHsl)
    )
    lerpRgb(src, OklabTools.toRGBA(oklab), OklabTools.alpha(dstHsl))
  }
  def oklabTransferLightness(src: Int, dst: Int): Int = {
    val srcHsl = OklabTools.fromRGBA8888(src)
    val dstHsl = OklabTools.fromRGBA8888(src)
    val oklab = OklabTools.oklabByHCL(
      OklabTools.oklabHue(dstHsl),
      OklabTools.chroma(dstHsl),
      lerp(OklabTools.oklabLightness(dstHsl), OklabTools.oklabLightness(srcHsl), OklabTools.alpha(srcHsl)),
      OklabTools.alpha(dstHsl)
    )
    OklabTools.toRGBA8888(oklab)
  }
}

enum GimpBlendingMode {
  case OklabHue, OklabChroma, OklabColor, OklabLightness
}

case class GimpComposite(mode: GimpBlendingMode) extends Composite {
  override def createContext(
      src: ColorModel,
      dst: ColorModel,
      renderingHints: RenderingHints
  ): CompositeContext = {
    if (!isRgb(src) || !isRgb(dst)) {
      sys.error(f"Unknown color models: src = $src, dst = $dst")
    }
    new CompositeContext {
      override def compose(src: Raster, dstIn: Raster, dstOut: WritableRaster): Unit = {
        val width  = Math.min(src.getWidth, dstIn.getWidth)
        val height = Math.min(src.getHeight, dstIn.getHeight)

        val srcPixels = new Array[Int](width)
        val dstPixels = new Array[Int](width)

        for (y <- 0 until height) {
          src.getDataElements(0, y, width, 1, srcPixels)
          dstIn.getDataElements(0, y, width, 1, dstPixels)
          mode match {
            case GimpBlendingMode.OklabHue =>
              for (x <- 0 until width) dstPixels(x) = ColorBlend.oklabTransferHue(srcPixels(x), dstPixels(x))
            case GimpBlendingMode.OklabChroma =>
              for (x <- 0 until width) dstPixels(x) = ColorBlend.oklabTransferChroma(srcPixels(x), dstPixels(x))
            case GimpBlendingMode.OklabColor =>
              for (x <- 0 until width) dstPixels(x) = ColorBlend.oklabTransferColor(srcPixels(x), dstPixels(x))
            case GimpBlendingMode.OklabLightness =>
              for (x <- 0 until width) dstPixels(x) = ColorBlend.oklabTransferLightness(srcPixels(x), dstPixels(x))
          }
          dstOut.setDataElements(0, y, width, 1, dstPixels)
        }
      }

      override def dispose(): Unit = {}
    }
  }
  private def isRgb(cm: ColorModel): Boolean = cm match {
    case cm: DirectColorModel =>
      cm.getTransferType == DataBuffer.TYPE_INT &&
      cm.getRedMask == 0x00ff0000 &&
      cm.getGreenMask == 0x0000ff00 &&
      cm.getBlueMask == 0x000000ff &&
      ((cm.getNumComponents == 3) || (cm.getAlphaMask == 0xff000000))
    case _ => false
  }
}
