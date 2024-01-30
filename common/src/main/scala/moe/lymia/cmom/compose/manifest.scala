package moe.lymia.cmom.compose

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, readFromString}
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

import java.awt.image.BufferedImage

private case class JsonManifest(
    out_name: String,
    image_size: (Int, Int),
    texture_size: (Int, Int),
    layers: Map[String, List[JsonLayer]]
)
private case class JsonLayer(
    mode: String,

    // normal
    name: Option[String],
    bound: Option[(Int, Int, Int, Int)],
    blend_mode: Option[String],

    // external
    layer: Option[String]
)

given JsonValueCodec[JsonManifest] = JsonCodecMaker.make
given JsonValueCodec[JsonLayer]    = JsonCodecMaker.make

private object JsonParser {
  def parse(data: String): OverlayManifest = {
    val jsManifest = readFromString[JsonManifest](data)
    OverlayManifest(
      imageName = jsManifest.out_name,
      imageSize = jsManifest.image_size,
      textureSize = jsManifest.texture_size,
      overlays = jsManifest.layers.map { (x, y) =>
        (
          x,
          OverlayInfo(y.map { elem =>
            elem.mode match {
              case "Layer" =>
                OverlayElement.Layer(elem.name.get, elem.bound.get, BlendingMode.forGimpMode(elem.blend_mode.get))
              case "External" =>
                OverlayElement.External(elem.layer.get)
            }
          })
        )
      }
    )
  }
}

case class OverlayManifest(
    imageName: String,
    imageSize: (Int, Int),
    textureSize: (Int, Int),
    overlays: Map[String, OverlayInfo]
) {
  def load(ctx: AssetSource): LoadedManifest = new LoadedManifest(ctx, this)
}
object OverlayManifest {
  def parse(data: String): OverlayManifest = JsonParser.parse(data)
}

case class OverlayInfo(elements: List[OverlayElement])

enum OverlayElement {
  case Layer(name: String, bound: (Int, Int, Int, Int), blendingMode: BlendingMode)
  case External(name: String)
}

class LoadedManifest(ctx: AssetSource, manifest: OverlayManifest) {
  private val spriteSheetImage = ctx.loadImage(f"colorfulmagicaloremod/texture_compositor/${manifest.imageName}")

  private def adjustX(x: Int) = if (spriteSheetImage.getWidth == manifest.imageSize._1) {
    x
  } else {
    val cx = Math.round(x.toFloat * (spriteSheetImage.getWidth.toFloat / manifest.imageSize._1.toFloat))
    Math.max(Math.min(cx, spriteSheetImage.getWidth()), 0)
  }

  private def adjustY(y: Int) = if (spriteSheetImage.getHeight() == manifest.imageSize._2) {
    y
  } else {
    val cy = Math.round(y.toFloat * (spriteSheetImage.getHeight().toFloat / manifest.imageSize._2.toFloat))
    Math.max(Math.min(cy, spriteSheetImage.getHeight()), 0)
  }

  private def adjustBound(t: (Int, Int, Int, Int)): (Int, Int, Int, Int) =
    (adjustX(t._1), adjustY(t._2), adjustX(t._3), adjustY(t._4))

  private val overlays: Map[String, LoadedOverlay] =
    manifest.overlays.map((k, v) =>
      (
        k,
        new LoadedOverlay(
          ctx,
          v.elements.map {
            case OverlayElement.Layer(name, bound, blendingMode) =>
              val adjusted = adjustBound(bound)
              LoadedElement.Layer(
                TextureSource.SpriteSheetLocation(spriteSheetImage, bound._1, bound._2, bound._3, bound._4),
                blendingMode
              )
            case OverlayElement.External(name) =>
              LoadedElement.External(name)
          },
          manifest.textureSize
        )
      )
    )

  def getOverlay(name: String): Option[LoadedOverlay] = overlays.get(name)
  lazy val overlayList: List[String]                  = overlays.keys.toList

  override def toString =
    s"LoadedManifest(ctx=$ctx, spriteSheetImage=$spriteSheetImage, overlays=$overlays, overlayList=$overlayList)"
}

private enum LoadedElement {
  case Layer(src: TextureSource, mode: BlendingMode)
  case External(name: String)
}

class LoadedOverlay(ctx: AssetSource, layers: List[LoadedElement], textureSize: (Int, Int)) {
  def compose(params: Map[String, String]): BufferedImage = {
    val composer = new TextureComposer(textureSize._1, textureSize._2)
    for (layer <- layers) layer match {
      case LoadedElement.Layer(src, mode) => composer.paste(src, mode)
      case LoadedElement.External(name) =>
        val texture = ctx.loadImage(params.getOrElse(name, sys.error(f"No such parameter: $name")))
        composer.paste(TextureSource.TextureData(texture))
    }
    composer.getImage
  }

  override def toString = s"LoadedOverlay(layers=$layers, textureSize=$textureSize)"
}
