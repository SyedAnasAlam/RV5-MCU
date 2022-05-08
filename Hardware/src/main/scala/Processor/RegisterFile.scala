import chisel3._
import chisel3.util._
import utility.RisingEdgeFsm
import utility.BRAM
import utility.Constants._

class RegisterFile() extends Module {
  val io = IO(new Bundle {
    val regSource1 = Input(UInt(5.W))
    val regSource2 = Input(UInt(5.W))
    val regWrite = Input(UInt(5.W))
    val writeEnable = Input(Bool())
    val writeData = Input(SInt(32.W))
    val regData1 = Output(SInt(32.W))
    val regData2 = Output(SInt(32.W))
    val regOut = Output(UInt(16.W))
  })

  val regFile1 = Module(new BRAM(32, 32))
  val regFile2 = Module(new BRAM(32, 32))

  regFile1.io.readAddress := io.regSource1
  regFile1.io.writeAddress := io.regWrite
  regFile1.io.readEnable := true.B
  regFile1.io.writeEnable := false.B
  regFile1.io.writeData := io.writeData

  regFile2.io.readAddress := io.regSource2
  regFile2.io.writeAddress := io.regWrite
  regFile2.io.readEnable := true.B
  regFile2.io.writeEnable := false.B
  regFile2.io.writeData := io.writeData

  val RisingEdgeFsm = Module(new RisingEdgeFsm(_hold = false, _delay = true))
  RisingEdgeFsm.io.in := io.writeEnable
  val writeEnable = RisingEdgeFsm.io.out

  when(writeEnable) {
      regFile1.io.writeEnable := true.B     
      regFile2.io.writeEnable := true.B
  }  

  io.regData1 := regFile1.io.readData
  io.regData2 := regFile2.io.readData

  when(io.regSource1 === 0.U) {
    io.regData1 := 0.S
  }
  when(io.regSource2 === 0.U) {
    io.regData2 := 0.S
  }

  val regOut = RegInit(0.S(32.W))
  when(writeEnable) {
    when(io.regWrite === REG_OUT.U) {
      regOut := io.writeData
    }
  }
  io.regOut := regOut(15,0)
}


class RegisterFileSim() extends Module {
  val io = IO(new Bundle {
    val regSource1 = Input(UInt(5.W))
    val regSource2 = Input(UInt(5.W))
    val regWrite = Input(UInt(5.W))
    val writeEnable = Input(Bool())
    val writeData = Input(SInt(32.W))
    val regData1 = Output(SInt(32.W))
    val regData2 = Output(SInt(32.W))
    val registerFile = Output(Vec(32, SInt(32.W)))
  })

  val regFile1 = Module(new BRAM(32, 32))
  val regFile2 = Module(new BRAM(32, 32))

  regFile1.io.readAddress := io.regSource1
  regFile1.io.writeAddress := io.regWrite
  regFile1.io.readEnable := true.B
  regFile1.io.writeEnable := false.B
  regFile1.io.writeData := io.writeData

  regFile2.io.readAddress := io.regSource2
  regFile2.io.writeAddress := io.regWrite
  regFile2.io.readEnable := true.B
  regFile2.io.writeEnable := false.B
  regFile2.io.writeData := io.writeData

  val RisingEdgeFsm = Module(new RisingEdgeFsm(_hold = false, _delay = true))
  RisingEdgeFsm.io.in := io.writeEnable
  val writeEnable = RisingEdgeFsm.io.out

  when(writeEnable) {
      regFile1.io.writeEnable := true.B     
      regFile2.io.writeEnable := true.B
  }  

  io.regData1 := regFile1.io.readData
  io.regData2 := regFile2.io.readData

  when(io.regSource1 === 0.U) {
    io.regData1 := 0.S
  }
  when(io.regSource2 === 0.U) {
    io.regData2 := 0.S
  }

  // For simulation  
  val regFile = Reg(Vec(32, SInt(32.W)))
  when(writeEnable) {
      regFile(io.regWrite) := io.writeData
  }  
  regFile(0) := 0.S(32.W)
  io.registerFile := regFile  
  
}
