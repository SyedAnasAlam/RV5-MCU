import chisel3._
import chisel3.util._

class RegisterFile extends Module {
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

  val regFile = Reg(Vec(32, SInt(32.W)))

  when(io.writeEnable) {
      regFile(io.regWrite) := io.writeData
  }  
  io.regData1 := regFile(io.regSource1)
  io.regData2 := regFile(io.regSource2)

  regFile(0) := 0.S(32.W)
  
  io.registerFile := regFile
}


