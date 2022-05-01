import chisel3._
import chisel3.util._

class BRAM(size:Int, width:Int) extends Module {
    val io = IO(new Bundle {
        val address = Input(UInt(log2Up(size).W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(width.W))
        val readData = Output(SInt(width.W))
    })
    
    val BRAM = SyncReadMem(size, SInt(width.W))

    io.readData := 0.S
    when(io.readEnable) {
        io.readData := BRAM(io.address)
    }

    when(io.writeEnable) {
        BRAM.write(io.address, io.writeData)
    }
}