package Utility

import chisel3._
import chisel3.util._
import Constants.System_Constants._

/**
    * @param delay_ns: Amount of delay in nano seconds
*/

class Delay(val delay_ns: Int) extends Module {
    val io = IO(new Bundle {
        val ready = Input(Bool())
        val done = Output(Bool())
    })

    val clock_period = 1000/SYSTEM_FREQUENCY_MHZ
    require(delay_ns >= clock_period, "Delay amount must be greater than system clock period")
    val wait_cycles = delay_ns/clock_period

    val idle :: delay :: finish :: Nil = Enum(3)

    val state = RegInit(idle)
    val done = RegInit(false.B)
    val ready = RegNext(io.ready)
    val counter = RegInit(0.U(log2Ceil(wait_cycles).W))

    switch(state) {
        is(idle) {
            done := false.B
            counter := 0.U
            when(ready) {
                state := delay
            }
        }
        is(delay) {
            done := false.B
            counter := counter + 1.U
            when(counter === (wait_cycles - 1).U) {
                state := finish
            }
        }
        is(finish) {
            done := true.B
            state := idle
        }
    }

    io.done := done
}

/*object Delay extends App {
    emitVerilog(new Delay(400000), Array("--target-dir", "Generated"))
}*/