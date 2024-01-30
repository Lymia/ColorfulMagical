package moe.lymia.cmom

import moe.lymia.cmom.compose.{OverlayManifest, ResourceAssetSource}

import java.util.Scanner

trait CmomCliBase extends App {
  println("Hello, world!")

  val metaSource = new Scanner(
    classOf[CmomCliBase].getResourceAsStream("/assets/colorfulmagicaloremod/texture_compositor/overlays.json"),
    "UTF-8"
  ).useDelimiter("\\A").next
  val parsed = OverlayManifest.parse(metaSource)
  println(parsed)
  println(parsed.load(new ResourceAssetSource))
}
