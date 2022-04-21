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
    val mem = SyncReadMem(WORDS, Vec(4, SInt(8.W)))
    val readData = Wire(Vec(4, SInt(8.W)))
    val readData2 = Wire(Vec(4, SInt(8.W)))
    val readWord = Wire(Vec(4, SInt(8.W)))
    // TODO Clean this
    readData(0) := 0.S
    readData(1) := 0.S
    readData(2) := 0.S
    readData(3) := 0.S

    readData2(0) := 0.S
    readData2(1) := 0.S
    readData2(2) := 0.S
    readData2(3) := 0.S

    readWord(0) := 0.S
    readWord(1) := 0.S
    readWord(2) := 0.S
    readWord(3) := 0.S

    io.readData := 0.S
    when(io.readEnable) {
        val addr1 = io.address >> 2.U
        val addr2 = addr1 + 1.U
        readData := mem.read(addr1)
        readData2 := mem.read(addr2)
        switch(io.address(1, 0)) {
          is(0.U) {
              readWord := readData
          }
          is(1.U) {
            readWord(0) := readData(1)
            readWord(1) := readData(2)
            readWord(2) := readData(3)
            readWord(3) := readData2(0)
          }
          is(2.U) {
            readWord(0) := readData(2)
            readWord(1) := readData(3)
            readWord(2) := readData2(0)
            readWord(3) := readData2(1)    
          }
          is(3.U) {
            readWord(0) := readData(3)
            readWord(1) := readData2(1)
            readWord(2) := readData2(2)
            readWord(3) := readData2(3)
          }
        }
        switch(io.funct3) {
          is("h00".U) {
            io.readData := readWord(0)
          }
          is("h01".U) {
            io.readData := (readWord(1) ## readWord(0)).asSInt
          }
          is("h02".U) {
            io.readData := ( (readWord(3) ## readWord(2)) ## (readWord(1) ## readWord(0)) ).asSInt
          }
          is("h04".U) {
            io.readData := ( 0.S(24.W) ## (readWord(0)) ).asSInt
          }
          is("h05".U) {
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
        mem.write(io.address >> 2, element, mask.asBools)
    }
}


object DataMemory extends App {
    emitVerilog(new DataMemory(), Array("--target-dir", "Generated"))
}


