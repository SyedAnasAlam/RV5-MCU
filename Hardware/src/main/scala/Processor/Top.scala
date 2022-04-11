import chisel3._
import chisel3.util._
import MemoryController.{FlashController, FlashModel}

class Top(app: String) extends Module {
    val io = IO(new Bundle {
        
    })
    
    val pc = RegInit(0.U(32.W))

    val flashClockCount = 2
    val FlashController = Module(new FlashController(count = flashClockCount))
    val FlashModel = Module(new FlashModel(count = flashClockCount, app))
    
    FlashModel.io.sck := FlashController.io.sck
    FlashModel.io.cs := FlashController.io.cs
    FlashModel.io.mosi := FlashController.io.mosi
    FlashController.io.miso := FlashModel.io.miso

    FlashController.io.branch := false.B
    FlashController.io.readEnable := true.B
    FlashController.io.address := pc(23, 0)
    
    

}