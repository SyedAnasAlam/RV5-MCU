import chisel3._
import chisel3.util._

class RAM(size:Int, width:Int) extends Module {
    val io = IO(new Bundle {
        val writeAddress = Input(UInt(log2Up(size).W))
        val readAddress = Input(UInt(log2Up(size).W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(width.W))
        val readData = Output(SInt(width.W))
    })
    
    val BRAM = SyncReadMem(size, SInt(width.W))

    io.readData := 0.S
    when(io.readEnable) {
        io.readData := BRAM.read(io.readAddress)
    }

    when(io.writeEnable) {
        BRAM.write(io.writeAddress, io.writeData)
    }
}

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

  val regFile1 = Module(new RAM(32, 32))
  val regFile2 = Module(new RAM(32, 32))

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

  when(io.writeEnable) {
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
  
  // SIMULATION
   val regFile = Reg(Vec(32, SInt(32.W)))
  when(io.writeEnable) {
      regFile(io.regWrite) := io.writeData
  }  
  regFile(0) := 0.S(32.W)
  io.registerFile := regFile 
}

 
/* import chisel3._
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
 */
