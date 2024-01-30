package moe.lymia.cmom.compose

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}

import java.awt.image.BufferedImage
import java.io.InputStream
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

trait AssetSource {

  private val imageCache: LoadingCache[String, BufferedImage] =
    CacheBuilder
      .newBuilder()
      .maximumSize(1024)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build((key => ImageIO.read(loadAsset(key))) : CacheLoader[String, BufferedImage])

  def loadAsset(path: String): InputStream
  final def loadImage(path: String): BufferedImage = imageCache.get(path)
  final def loadRequest(request: Map[String, String]): Map[String, BufferedImage] =
    request.map((k, v) => (k, loadImage(v)))
}

class ResourceAssetSource extends AssetSource {
  override def loadAsset(path: String): InputStream =
    classOf[AssetSource].getResourceAsStream(f"/assets/$path")
}
