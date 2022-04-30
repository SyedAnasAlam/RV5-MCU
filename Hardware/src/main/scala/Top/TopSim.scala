import chisel3._
import chisel3.util._
import MemoryController.{FlashController, FlashModel}
import SPI._
import ePCSrc._
import eRegSrc._

class TopSim(app: String) extends Module {
    val ECALL_ID_REG = 10   // ecall id in register a0 (x10)
    val ECALL_ARG_REG = 11  // ecall argument in register a1 (x11)
    
    val io = IO(new Bundle {
        //val registerFile = Output(Vec(32, SInt(32.W)))
        //val systemCallId = Output(SInt(32.W))
        //val systemCallArgument = Output(SInt(32.W))

        val aluOp = Output(eAluOp())
        val aluSrc1 = Output(Bool())
        val aluSrc2 = Output(Bool())
        val pcSrc = Output(ePCSrc())
        val regSrc = Output(eRegSrc())
        val memRead = Output(Bool())
        val memWrite = Output(Bool())
        val regWrite = Output(Bool())

        val aluControl = Output(eAluControl())

        val aluData1 = Output(SInt(32.W))
        val aluData2 = Output(SInt(32.W))
        val aluResult = Output(SInt(32.W))
        val aluResultBool = Output(Bool())
    })
    
    val flashClockCount = 2
    val FlashController = Module(new FlashController(count = flashClockCount))
    val Flash = Module(new FlashModel(count = flashClockCount, app))
    val RegisterFile = Module(new RegisterFile())
    val Control = Module(new Control())
    val ImmGenerator = Module(new ImmGenerator())
    val ALU = Module(new ALU())
    val AluControl = Module(new AluControl())
    val DataMemory = Module(new DataMemory())

    io.aluOp := Control.io.aluOp
    io.aluSrc1 := Control.io.aluSrc1
    io.aluSrc2 := Control.io.aluSrc2
    io.pcSrc := Control.io.pcSrc
    io.regSrc := Control.io.regSrc
    io.memRead := Control.io.memRead
    io.memWrite := Control.io.memWrite
    io.regWrite := Control.io.regWrite

    
    val pc = RegInit(0.U(32.W))
    val pcNew = RegInit(0.U(32.W))
    
    Flash.io.spi <> FlashController.io.spi

    FlashController.io.branch := false.B
    FlashController.io.readEnable := true.B
    FlashController.io.address := pcNew(23, 0)

    //val instruction = WireDefault(0.U(32.W))
    val counter = RegInit(0.U(12.W))
    val counter2 = RegInit(0.U(32.W))
    counter2 := counter2 + 1.U
    val instruction = RegInit(0.U(32.W))
    val regSource1 = instruction(19, 15)
    val regSource2 = instruction(24, 20)
    val regDest = instruction(11, 7) 
    when(Control.io.regWrite) {
        counter := counter + 1.U
    }


    val state = RegInit(0.U(2.W))
    switch(state) {
        is(0.U) {
            FlashController.io.address := 0.U
            when(FlashController.io.dataValid) {
                instruction := FlashController.io.readData
                counter := false.B
                state := 1.U
                counter2 := 0.U
            }
        }
        is(1.U) {
            FlashController.io.address := pcNew(23, 0)
             when(FlashController.io.dataValid) {
                instruction := FlashController.io.readData
                counter := false.B
                counter2 := 0.U
                state := 1.U
                pc := pcNew
            }         
        }
    }

/*     when(FlashController.io.dataValid) {
        instruction := FlashController.io.readData
        counter := 0.U
    }
    when(RegNext(RegNext(FlashController.io.dataValid))) {
        pc := pcNew
    } */

    Control.io.opcode := instruction(6, 0)
    val regWrite = Mux(counter > 0.U, false.B, Control.io.regWrite)

    RegisterFile.io.regSource1 := regSource1
    RegisterFile.io.regSource2 := regSource2
    RegisterFile.io.regWrite := regDest
    RegisterFile.io.writeEnable := RegNext(regWrite)

    ImmGenerator.io.instruction := instruction

    AluControl.io.aluOp := Control.io.aluOp
    AluControl.io.funct3 := instruction(14, 12)
    AluControl.io.funct7 := instruction(30)
    AluControl.io.isImmediate := Control.io.aluSrc2

    ALU.io.regData1 := Mux(Control.io.aluSrc1, (pc).asSInt, RegisterFile.io.regData1)
    ALU.io.regData2 := Mux(Control.io.aluSrc2, ImmGenerator.io.immediate, RegisterFile.io.regData2)
    ALU.io.aluControl := AluControl.io.aluControl

    DataMemory.io.address := ALU.io.result.asUInt
    DataMemory.io.readEnable := Control.io.memRead
    DataMemory.io.writeEnable := Control.io.memWrite
    DataMemory.io.writeData := RegisterFile.io.regData2
    DataMemory.io.funct3 := instruction(14, 12)

    io.aluControl := AluControl.io.aluControl
    io.aluData1 := Mux(Control.io.aluSrc1, pc.asSInt, RegisterFile.io.regData1)
    io.aluData2 := Mux(Control.io.aluSrc2, ImmGenerator.io.immediate, RegisterFile.io.regData2)
    io.aluResult := ALU.io.result
    io.aluResultBool := ALU.io.resultBool

    // TODO Clean this
    when(counter2 === 1.U) {
        switch(Control.io.pcSrc) {
            is(ePC_4) { 
                pcNew := pc + 4.U 
            }
            is(ePC_JUMP) { 
                pcNew := (pc.asSInt + (ImmGenerator.io.immediate << 1)).asUInt
            }
            is(ePC_JALR) { 
                pcNew := ALU.io.result.asUInt
            }
            is(ePC_BRANCH) {
                when(ALU.io.resultBool) {
                    pcNew := (pc.asSInt + (ImmGenerator.io.immediate << 1)).asUInt
                }
                . otherwise {
                    pcNew := pc + 4.U
                }
            }
        }
    }
    
    
    val writeData = WireDefault(0.S(32.W))
    switch(Control.io.regSrc) {
        is(eALU)   { writeData := ALU.io.result }
        is(eMEM)   { writeData := DataMemory.io.readData }
        is(eJUMP)  { writeData := (pc + 4.U).asSInt } //TODO double check this
    }
    RegisterFile.io.writeData := writeData

    // IO
    //io.systemCallId := Mux(instruction(6, 0) === "b1110011".U, RegisterFile.io.registerFile(ECALL_ID_REG), 0.S)
    //io.systemCallArgument := RegisterFile.io.registerFile(ECALL_ARG_REG)
    //io.registerFile := RegisterFile.io.registerFile
}