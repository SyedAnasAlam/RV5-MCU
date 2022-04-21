import chisel3._
import chisel3.util._
import MemoryController._
import SPI._

class Test extends Module {
    val io = IO(new Bundle {
        val spiExternal = new SPISecondaryPort()
        val spi = new SPIMainPort()        
    })
    
    io.spiExternal.miso := io.spi.miso

    val FlashController = Module(new FlashController(count = 2))
    FlashController.io.readEnable := true.B
    FlashController.io.branch := false.B
    FlashController.io.address := 0.U
    
    io.spi <> FlashController.io.spi
    when(~io.spiExternal.cs) {
        io.spi <> io.spiExternal
    }
}


object Test extends App {
    emitVerilog(new Test(), Array("--target-dir", "Generated"))
}


