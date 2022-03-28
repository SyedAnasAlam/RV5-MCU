package Memory_Controller

import chisel3._

object SPI_Flash_Commands {
    val READ_CMD = "h03".U(8.W)
}