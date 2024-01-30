package moe.lymia.cmom

import moe.lymia.cmom.ColorfulMagicalOreMod.LOGGER
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager

@Mod(ColorfulMagicalOreMod.MOD_ID)
class ColorfulMagicalOreMod {
  LOGGER.info(CmomCli.parsed.toString)
}
object ColorfulMagicalOreMod {
  final val MOD_ID = "colorfulmagicaloremod"
  final val LOGGER = LogManager.getLogger(MOD_ID)
}
