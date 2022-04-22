import chisel3._
import chisel3.util._

class DataMemory extends Module {
    val WORDS = 128*16
    val io = IO(new Bundle {
        val address = Input(UInt(32.W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(32.W))
        val funct3 = Input(UInt(3.W))
        val readData = Output(SInt(32.W))
    })
    // Default
    io.readData := 0.S

    val mem = SyncReadMem(WORDS, Vec(4, SInt(8.W)))

    val byteSel = io.address(1, 0)
    val rows = Wire(Vec(2, UInt(32.W)))
    rows(0) := io.address(31, 2)
    rows(1) := io.address(31, 2) + 1.U
    

    val byteEn = WireDefault(0.U(4.W))
    switch(io.funct3) {
      is("h00".U) { byteEn := "b0001".U }
      is("h01".U) { byteEn := "b0011".U }
      is("h02".U) { byteEn := "b1111".U }
    }
    val writeData = Wire(Vec(2, Vec(4, SInt(8.W))))
    writeData(0)(0) := 0.S
    writeData(0)(1) := 0.S
    writeData(0)(2) := 0.S
    writeData(0)(3) := 0.S
    writeData(1)(0) := 0.S
    writeData(1)(1) := 0.S
    writeData(1)(2) := 0.S
    writeData(1)(3) := 0.S

    val bytes = Wire(Vec(4, SInt(8.W)))
    bytes(0) := io.writeData(7, 0).asSInt 
    bytes(1) := io.writeData(15, 8).asSInt
    bytes(2) := io.writeData(23, 16).asSInt
    bytes(3) := io.writeData(31, 24).asSInt

    val mask = Wire(Vec(2, UInt(4.W)))
    mask(0) := 0.U
    mask(1) := 0.U

    switch(byteSel) {
        is(0.U) {
            writeData(0)(0) := bytes(0)
            writeData(0)(1) := bytes(1)
            writeData(0)(2) := bytes(2)
            writeData(0)(3) := bytes(3)
            mask(0) := byteEn
            mask(1) := 0.U
        }
        is(1.U) {
            writeData(0)(1) := bytes(0)
            writeData(0)(2) := bytes(1)
            writeData(0)(3) := bytes(2)
            writeData(1)(0) := bytes(3)
            mask(0) := "b1110".U & byteEn
            mask(1) := "b0001".U & byteEn
        }
        is(2.U) {
            writeData(0)(2) := bytes(0)
            writeData(0)(3) := bytes(1)
            writeData(1)(0) := bytes(2)
            writeData(1)(1) := bytes(3)
            mask(0) := "b1100".U & byteEn
            mask(1) := "b0011".U & byteEn
        }
        is(3.U) {
            writeData(0)(3) := bytes(0)
            writeData(1)(0) := bytes(1)
            writeData(1)(1) := bytes(2)
            writeData(1)(2) := bytes(3)
            mask(0) := "b1000".U & byteEn
            mask(1) := "b0111".U & byteEn
        }
    }

    val readData = Wire(Vec(2, Vec(4, SInt(8.W))))
    readData(0)(0) := 0.S
    readData(0)(1) := 0.S
    readData(0)(2) := 0.S
    readData(0)(3) := 0.S
    readData(1)(0) := 0.S
    readData(1)(1) := 0.S
    readData(1)(2) := 0.S
    readData(1)(3) := 0.S

    val readWord = Wire(Vec(4, SInt(8.W)))
    readWord(0) := 0.S
    readWord(1) := 0.S
    readWord(2) := 0.S
    readWord(3) := 0.S
    readData(0) := mem.read(rows(0))
    readData(1) := mem.read(rows(1))
    switch(byteSel) {
        is(0.U) {
            readWord := readData(0)
        }
        is(1.U) {
            readWord(0) := readData(0)(1)
            readWord(1) := readData(0)(2)
            readWord(2) := readData(0)(3)
            readWord(3) := readData(1)(0)
        }
        is(2.U) {
            readWord(0) := readData(0)(2)
            readWord(1) := readData(0)(3)
            readWord(2) := readData(1)(0)
            readWord(3) := readData(1)(1)
        }
        is(3.U) {
            readWord(0) := readData(0)(3)
            readWord(1) := readData(1)(1)
            readWord(2) := readData(1)(2)
            readWord(3) := readData(1)(3)
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
            io.readData := ( 0.U(16.W) ## (readWord(1) ## readWord(0)) ).asSInt
        }
    }
    

    val idle :: intA :: write0 :: write1 :: Nil = Enum(4)
    val state = RegInit(idle)
    switch(state) {
        is(idle) {
            when(~io.writeEnable) {
                state := intA
            }
        }
        is(intA) {
            when(io.writeEnable) {
                state := write0
            }
        }
        is(write0) {
            mem.write(rows(0), writeData(0), mask(0).asBools) 
            state := write1
        }
        is(write1) {
            mem.write(rows(1), writeData(1), mask(1).asBools)
            state := idle
        }
    }
}


object DataMemory extends App {
    emitVerilog(new DataMemory(), Array("--target-dir", "Generated"))
}
