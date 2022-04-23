import chisel3._
import chisel3.util._

class DataMemory extends Module {
    val WORDS = 128*16*16
    val io = IO(new Bundle {
        val address = Input(UInt(32.W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(32.W))
        val funct3 = Input(UInt(3.W))
        val readData = Output(SInt(32.W))
    })
    
    val memory = SyncReadMem(WORDS, SInt(32.W))
    val byteSelect = io.address(1,0)
    val rows = VecInit(io.address >> 2, (io.address >> 2) + 1.U)
    val byteEnable = WireDefault(0.U(4.W))
    val mask = WireDefault(0.U(4.U))
    val data = Wire(VecInit(
        io.writeData(7, 0).asSInt,
        io.writeData(15, 8).asSInt,
        io.writeData(23, 16).asSInt,
        io.writeData(31, 24).asSInt
    ))
    val writeData = Wire(VecInit(0.S, 0.S, 0.S, 0.S))

    switch(io.funct3) {
        is(0.U) { byteEnable := "b0001".U }
        is(1.U) { byteEnable := "b0011".U }
        is(2.U) { byteEnable := "b1111".U }
    }

    when(io.writeEnable) {
        switch(byteEnable) {
            is(0.U) {
                writeData := data
                mask := byteEnable
            }
            is(1.U) {
                write
            }
        }
    }
}


object DataMemory extends App {
    emitVerilog(new DataMemory(), Array("--target-dir", "Generated"))
}

