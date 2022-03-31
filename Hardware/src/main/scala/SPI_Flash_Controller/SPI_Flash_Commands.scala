package Memory_Controller

import chisel3._

object SPI_Flash_Commands {
    val READ_CMD = "h03".U(8.W)
    val RELEASE_POWER_DOWN_CMD = 171.U(8.W)
}