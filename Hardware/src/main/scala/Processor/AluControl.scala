import chisel3._
import chisel3.util._
import eAluControl._
import eAluOp._

class AluControl extends Module {
  val io = IO(new Bundle {
    val aluOp = Input(eAluOp())
    val funct3 = Input(UInt(3.W))
    val funct7 = Input(Bool())
    val isImmediate = Input(Bool())
    val aluControl = Output(eAluControl())
})

    //Default
    io.aluControl := eADD
    
    switch(io.aluOp) {
       is(eARITHMETIC) {
        switch(io.funct3) {
            is("h00".U) {
                io.aluControl := eADD
                when(~io.isImmediate) {
                    io.aluControl := Mux(io.funct7, eSUB, eADD)
                }
            }
            is("h04".U) { io.aluControl := eXOR }
            is("h06".U) { io.aluControl := eOR  }
            is("h07".U) { io.aluControl := eAND }
            is("h01".U) { io.aluControl := eSLL }
            is("h05".U) {
                //imm[5:11] = 0x20 -> Same logic for I eAND R
                when(io.funct7) { io.aluControl := eSRA }
                .     otherwise { io.aluControl := eSRL }
            }
            is("h02".U) { io.aluControl := eSLT }
            is("h03".U) { io.aluControl := eSLTU}
        }
       }
       
       is(eBRANCH) {
            switch(io.funct3) {
                is("h00".U) { io.aluControl := eEQU }
                is("h01".U) { io.aluControl := eNEQ }
                is("h04".U) { io.aluControl := eLT  }
                is("h05".U) { io.aluControl := eGE  }
                is("h06".U) { io.aluControl := eLTU }
                is("h07".U) { io.aluControl := eGEU }
                
            }
       }
       is(eLoadStoreJump) { io.aluControl := eADD  }
       is(eLUIOP)         { io.aluControl := eLUI  }
       is(eAUIPCOP)       { io.aluControl := eAUIPC}
    }
}

object AluControl extends App {
    emitVerilog(new AluControl(), Array("--target-dir", "Generated"))
}