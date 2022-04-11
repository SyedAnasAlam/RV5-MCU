import chisel3._
import chisel3.util._
import eAluControl._

class ALU extends Module {
  val io = IO(new Bundle {
    val aluControl = Input(eAluControl())
    val regData1 = Input(SInt(32.W))
    val regData2 = Input(SInt(32.W))
    val result = Output(SInt(32.W))
    val resultBool = Output(Bool())
})
    io.result := 0.S
    io.resultBool := false.B
    
    switch(io.aluControl) {
        is(eADD)   { io.result := io.regData1 + io.regData2 }
        is(eSUB)   { io.result := io.regData1 - io.regData2 }
        is(eXOR)   { io.result := io.regData1 ^ io.regData2 }
        is(eOR)    { io.result := io.regData1 | io.regData2 }
        is(eAND)   { io.result := io.regData1 & io.regData2 }
        is(eSLL)   { io.result := io.regData1 << io.regData2(18, 0)} // Width of shift amount cannot be larger than 20 bits (or equal)
        is(eSRL)   { io.result := (io.regData1.asUInt >> io.regData2.asUInt).asSInt }
        is(eSRA)   { io.result := io.regData1 >> io.regData2.asUInt }
        is(eSLT)   { io.result := Mux(io.regData1 < io.regData2, 1.S(32.W), 0.S(32.W)) }
        is(eSLTU)  { io.result := Mux(io.regData1.asUInt < io.regData2.asUInt, 1.S(32.W), 0.S(32.W)) }
        is(eLUI)   { io.result := io.regData2 << 12 }
        is(eAUIPC) { io.result := io.regData1 + (io.regData2 << 12)}
    }
    switch(io.aluControl) {
        is(eEQU) { io.resultBool := io.regData1 === io.regData2 }
        is(eNEQ) { io.resultBool := io.regData1 =/= io.regData2 }
        is(eLT)  { io.resultBool := io.regData1 < io.regData2   }
        is(eGE)  { io.resultBool := io.regData1 >= io.regData2  }
        is(eLTU) { io.resultBool := io.regData1.asUInt < io.regData2.asUInt  }
        is(eGEU) { io.resultBool := io.regData1.asUInt >= io.regData2.asUInt }
    }

}

