import chisel3._
import chisel3.util._

class ImmGenerator extends Module {
  val io = IO(new Bundle {
      val instruction = Input(UInt(32.W))
      val immediate = Output(SInt(32.W))
})

    io.immediate := 0.S
    
    switch(io.instruction(6, 0)) {
        //I format
        is("b0010011".U) {
            io.immediate := io.instruction(31, 20).asSInt
            when(io.instruction(14, 12) === "h05".U || io.instruction(14, 12) === "h01".U) {
                io.immediate := io.instruction(24, 20).asSInt
            }
        }
        is{"b1100111".U} { io.immediate := io.instruction(31, 20).asSInt }
        is("b0000011".U) { io.immediate := io.instruction(31, 20).asSInt }
        //S format
        is("b0100011".U) { io.immediate := (io.instruction(31, 25) ## io.instruction(11, 7)).asSInt }
        //B format
        is("b1100011".U) { io.immediate := (io.instruction(31) ## io.instruction(7) ## io.instruction(30, 25) ## io.instruction(11, 8)).asSInt }
        //J format
        is("b1101111".U) { io.immediate := (io.instruction(31) ## io.instruction(19, 12) ## io.instruction(20) ## io.instruction(30, 21)).asSInt }
        //U format
        is("b0110111".U) { io.immediate := io.instruction(31, 12).asSInt }
        is("b0010111".U) { io.immediate := io.instruction(31, 12).asSInt }        
    }
}



