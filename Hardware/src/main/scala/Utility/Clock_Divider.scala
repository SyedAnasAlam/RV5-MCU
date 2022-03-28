package Utility

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil

/**
    * @param count: Number of system clock cycles per output clock cycle
    * @param clock_out: Output clock with 
    * If @param count is 0, @param clock_out is equal to system clock
*/

/*class Clock_Divider(count: Int) extends Module {
    require(count >= 0, "Must be positive number of system clock cycles per output clock cycles")
    
    val io = IO(new Bundle {
        val clock_out = Output(Bool())
    })

    io.clock_out := clock.asBool

    if(count > 0) {
        val counter = RegInit(0.U(log2Ceil(count + 1).W))
        counter := counter + 1.U

        val clock_out = Wire(Bool())
        clock_out := false.B
        when(counter === (count - 1).asUInt) {
            clock_out := true.B
            counter := 0.U
        }

        io.clock_out := clock_out
    }
}*/

class Clock_Divider(count: Int) extends Module {
    require(count >= 0, "Must be positive number of system clock cycles per output clock cycles")
    
    val io = IO(new Bundle {
        val clock_out = Output(Bool())
    })

    io.clock_out := clock.asBool

    if(count > 0) {
        val counter = RegInit(0.U(log2Ceil(count + 1).W))
        counter := counter + 1.U

        val clock_out = RegInit(false.B)
        when(counter === (count - 1).asUInt) {
            clock_out := ~clock_out
            counter := 0.U
        }

        io.clock_out := clock_out
    }
}

