import chisel3._
import chisel3.util._
import eAluOp._
import ePCSrc._
import eRegSrc._

class Control() extends Module {
    val io = IO(new Bundle {
        val opcode = Input(UInt(7.W))   
        val aluOp = Output(eAluOp())
        val aluSrc1 = Output(Bool())
        val aluSrc2 = Output(Bool())
        val pcSrc = Output(ePCSrc())
        val regSrc = Output(eRegSrc())
        val memRead = Output(Bool())
        val memWrite = Output(Bool())
        val regWrite = Output(Bool())
    })
    
    io.aluOp    := DontCare
    io.aluSrc1  := false.B
    io.aluSrc2  := false.B
    io.pcSrc    := ePC_4
    io.regSrc   := DontCare
    io.memRead  := false.B
    io.memWrite := false.B
    io.regWrite := false.B

    val isArithmetic = (io.opcode === "b0110011".U || io.opcode === "b0010011".U)
    val isBranch     = (io.opcode === "b1100011".U)
    val isLoadStore  = (io.opcode === "b0000011".U || io.opcode === "b0100011".U)
    val isLuiAuipc   = (io.opcode === "b0110111".U || io.opcode === "b0010111".U)
    val isJump       = (io.opcode === "b1100111".U || io.opcode === "b1101111".U)

    when(isArithmetic) {
        io.aluOp    := eARITHMETIC
        io.aluSrc1  := false.B
        io.aluSrc2  := ~io.opcode(5)
        io.pcSrc    := ePC_4
        io.regSrc   := eALU
        io.memRead  := false.B
        io.memWrite := false.B
        io.regWrite := true.B
    }
    . elsewhen(isBranch) {
        io.aluOp    := eBRANCH
        io.aluSrc1  := false.B
        io.aluSrc2  := false.B
        io.pcSrc    := ePC_BRANCH
        io.regSrc   := DontCare
        io.memRead  := false.B
        io.memWrite := false.B
        io.regWrite := false.B
    }
    . elsewhen(isLoadStore) {
        io.aluOp    := eLoadStoreJump
        io.aluSrc1  := false.B
        io.aluSrc2  := true.B
        io.pcSrc    := ePC_4
        io.regSrc   := eMEM
        io.memRead  := ~io.opcode(5)
        io.memWrite := io.opcode(5)
        io.regWrite := ~io.opcode(5)
    }
    . elsewhen(isLuiAuipc) {
        io.aluOp    := Mux(io.opcode(5), eLUIOP, eAUIPCOP)
        io.aluSrc1  := true.B
        io.aluSrc2  := true.B
        io.pcSrc    := ePC_4
        io.regSrc   := eALU
        io.memRead  := false.B
        io.memWrite := false.B
        io.regWrite := true.B
    }
    . elsewhen(isJump) {
        io.aluOp    := eLoadStoreJump
        io.aluSrc1  := false.B
        io.aluSrc2  := true.B
        io.pcSrc    := Mux(io.opcode(3), ePC_JUMP, ePC_JALR)
        io.regSrc   := eJUMP
        io.memRead  := false.B
        io.memWrite := false.B
        io.regWrite := true.B
    }
}