package Utility

import chisel3._
import chisel3.util._
import Constants.System_Constants._

class Clock_Divider(frequency: Int, max: Int) extends Module {
    require(frequency <= SYSTEM_FREQUENCY_MHZ, "Target frequency must be smaller than system frequency")
    
    val io = IO(new Bundle {
        val clock_out = Output(Bool())
        val clock_neg_out = Output(Bool())
    })

    io.clock_out := false.B

    val counter = RegInit(0.U(32.W))
    counter := counter + 1.U
    when(counter === max.asUInt) {
        io.clock_out := true.B
        counter := 0.U
    }

    io.clock_neg_out := ~io.clock_out
}
