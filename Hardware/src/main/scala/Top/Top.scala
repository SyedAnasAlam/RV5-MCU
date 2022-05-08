import chisel3._
import chisel3.util._
import java.nio.file.{Files, Paths}
import memoryController.{FlashController, FlashModel}
import utility.Constants._
import spi._
import ePCSrc._
import eRegSrc._

class Top(_app: String) extends Module {   
    val io = IO(new Bundle {
        val regOut = Output(UInt(16.W))
    })

    val program = Files.readAllBytes(Paths.get(PROGRAM_FOLDER + _app))    
    val imem = VecInit(program.map(_.S(8.W)))

    val RegisterFile = Module(new RegisterFile())
    val Control = Module(new Control())
    val ImmGenerator = Module(new ImmGenerator())
    val ALU = Module(new ALU())
    val AluControl = Module(new AluControl())
    val DataMemory = Module(new DataMemory())
    
    val pc = RegInit(0.U(32.W))
    val pcNew = RegInit(0.U(32.W))

    val instruction = RegInit(0.U(32.W))
    val regSource1 = instruction(19, 15)
    val regSource2 = instruction(24, 20)
    val regDest = instruction(11, 7) 
    val updatePC = WireDefault(false.B)
    
    val startup :: fetch :: hold :: exit :: Nil = Enum(4)
    val fetchFsm = RegInit(startup)
    switch(fetchFsm) {
        is(startup) { 
            instruction := ((imem(3.U) ## imem(2.U)) ## (imem(1.U) ## imem(0.U)))
            fetchFsm := hold
        }
        is(fetch) {
            instruction := 0.U
            val counter = RegInit(0.U(6.W))
            counter := counter + 1.U
            pc := pcNew
            when(counter === 2.U) {
                instruction := ((imem(pc + 3.U) ## imem(pc + 2.U)) ## (imem(pc + 1.U) ## imem(pc)))
                fetchFsm := hold
            } 
        }
        is(hold) {
            val counter = RegInit(0.U(2.W))
            counter := counter + 1.U
            when(counter === 2.U) {
                updatePC := true.B
                fetchFsm := fetch
            }
            when(instruction(6, 0) === "b1110011".U) {
                fetchFsm := exit
            }
        }
        is(exit) {
            instruction := 0x00000013.U
        }
    }

    Control.io.opcode := instruction(6, 0)

    RegisterFile.io.regSource1 := regSource1
    RegisterFile.io.regSource2 := regSource2
    RegisterFile.io.regWrite := regDest
    RegisterFile.io.writeEnable := Control.io.regWrite

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

    when(updatePC) {
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
        is(eALU)   { writeData := ALU.io.result          }
        is(eMEM)   { writeData := DataMemory.io.readData }
        is(eJUMP)  { writeData := (pc + 4.U).asSInt      } 
    }
    RegisterFile.io.writeData := writeData

    io.regOut := RegisterFile.io.regOut
}  

object Top extends App {
    emitVerilog(new Top(_app = "loop.bin"), Array("--target-dir", "Generated"))
}