import chisel3._
import chisel3.util._

class DataMemory extends Module {
    val WORDS = 128*16
    val io = IO(new Bundle {
        val address = Input(UInt(32.W))
        val readData = Output(SInt(32.W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(32.W))
        val funct3 = Input(UInt(3.W))
    })
    val mem = SyncReadMem(WORDS, Vec(4, SInt(8.W)))
    val readData = Wire(Vec(4, SInt(8.W)))
    readData(0) := 0.S
    readData(1) := 0.S
    readData(2) := 0.S
    readData(3) := 0.S
    io.readData := 0.S
    when(io.readEnable) {
        readData := mem.read(io.address)
        switch(io.funct3) {
          is("h00".U) {
            io.readData := readData(0)
          }
          is("h01".U) {
            io.readData := (readData(1) ## readData(0)).asSInt
          }
          is("h02".U) {
            io.readData := ( (readData(3) ## readData(2)) ## (readData(1) ## readData(0)) ).asSInt
          }
          is("h03".U) {
            io.readData := ( 0.S(24.W) ## (readData(0)) ).asSInt
          }
          is("h04".U) {
            io.readData := ( 0.U(16.W) ## (readData(1) ## readData(0)) ).asSInt
          }
        }
    }
    
    val element = Wire(Vec(4, SInt(8.W)))
    element(0) := io.writeData(7, 0).asSInt
    element(1) := io.writeData(15, 8).asSInt
    element(2) := io.writeData(23, 16).asSInt
    element(3) := io.writeData(31, 24).asSInt
    val mask = WireDefault(0.U(4.W))
    switch(io.funct3) {
      is("h00".U) { mask := "b0001".U }
      is("h01".U) { mask := "b0011".U }
      is("h02".U) { mask := "b1111".U }
    }

    when(io.writeEnable) {
        mem.write(io.address, element, mask.asBools)
    }
}


object DataMemory extends App {
    emitVerilog(new DataMemory(), Array("--target-dir", "Generated"))
}


