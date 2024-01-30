package moe.lymia.cmom

import moe.lymia.cmom.compose.{OverlayManifest, ResourceAssetSource}

import java.io.File
import java.util.Scanner
import javax.imageio.ImageIO

trait CmomCliBase extends App {
  println("Hello, world!")

  val metaSource = new Scanner(
    classOf[CmomCliBase].getResourceAsStream("/assets/colorfulmagicaloremod/texture_compositor/overlays.json"),
    "UTF-8"
  ).useDelimiter("\\A").next
  val parsed = OverlayManifest.parse(metaSource)
  println(parsed)
  val loaded = parsed.load(new ResourceAssetSource)
  println(loaded)

  val outImg = loaded.getOverlay("ore:iron").get.compose(Map("base_layer" -> "minecraft/textures/block/stone.png"))
  ImageIO.write(outImg, "png", new File("iron.png"))
}
