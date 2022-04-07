import chisel3._
import chisel3.util._
import MemoryController.{FlashController, FlashModel}

class Top(app: String) extends Module {
    val io = IO(new Bundle {
        
    })
    
    val FlashController = Module(new FlashController(count = 2))
    val FlashModel = Module(new FlashModel(count = 2, app))

}