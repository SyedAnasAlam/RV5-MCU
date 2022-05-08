package utility

import chisel3._
import chisel3.util._

class RisingEdgeFsm(_hold : Boolean, _delay : Boolean) extends Module {
  val io = IO(new Bundle {
      val in = Input(Bool())
      val out = Output(Bool())
  })

    val out = RegInit(false.B)

    val a :: b :: hold :: delay :: Nil = Enum(4)
    val state = RegInit(a)

    switch(state) {
        is(a) {
            out := false.B
            when(~io.in) {
                state := b
            }
        }
        is(b) {
            when(io.in) {
                if(_hold) {
                    out := true.B
                    state := hold
                }
                else if(_delay) {
                    state := delay
                }
                else {
                    out := true.B
                    state := a
                }
            }
        }
        is(hold) {
            state := RegNext(a)
        }
        is(delay) {
            out := true.B
            state := a
        }
    }
    io.out := out
}
