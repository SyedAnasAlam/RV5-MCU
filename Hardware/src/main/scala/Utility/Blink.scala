import chisel3._
import chisel3.util._

class Blink extends Module {
    val io = IO(new Bundle {
        val out = Output(Bool())
    })
    
    val counter = RegInit(0.U(64.W))
    val out = RegInit(false.B)

    counter := counter + 1.U
    when(counter === 2500000.U) {
        counter := 0.U
        out := ~out
    }

    io.out := out 
}


object Blink extends App {
    emitVerilog(new Blink(), Array("--target-dir", "Blink"))
}


