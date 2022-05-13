import chisel3._
import chisel3.util._
import java.nio.file.{Files, Paths}
import memoryController.{FlashController, FlashModel}
import utility.Constants._
import spi._
import ePCSrc._
import eRegSrc._

class TopFlash() extends Module {   
    val io = IO(new Bundle {
        val spi = new SPIMainPort()
        val spiExternal = new SPISecondaryPort()
        val regOut = Output(UInt(16.W))
    })

    val FlashController = Module(new FlashController(_count = 2))
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

    FlashController.io.readEnable := true.B
    FlashController.io.address := 0.U
    
    io.spiExternal.miso := io.spi.miso
    io.spi <> FlashController.io.spi

    when(~io.spiExternal.cs) {
        io.spi <> io.spiExternal
    }

    val startup :: fetch :: hold :: Nil = Enum(3)
    val fetchFsm = RegInit(startup)
    switch(fetchFsm) {
        is(startup) {
            FlashController.io.address := 0.U
            when(FlashController.io.dataValid) {
                instruction := FlashController.io.readData
                fetchFsm := hold
            }          
        }
        is(fetch) {
            instruction := 0.U
            FlashController.io.address := pcNew(23, 0)
            when(FlashController.io.dataValid) {
                instruction := FlashController.io.readData
                pc := pcNew
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

object TopFlash extends App {
    emitVerilog(new TopFlash(), Array("--target-dir", "Generated"))
}