import MemoryController.FlashControllerTestFSM

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


class FlashControllerFSMTest extends AnyFlatSpec with ChiselScalatestTester {
    "Flash controller test FSM" should "pass" in {
        test(new FlashControllerTestFSM(2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            for(i <- 0 until 80) {
                dut.clock.step()
            }
            dut.io.step.poke(true.B)
            dut.io.miso.poke(false.B)
            dut.clock.step()
            dut.io.step.poke(false.B)

            for(i <- 0 until 400) {
                dut.clock.step()
            }

            dut.io.step.poke(true.B)
            dut.io.miso.poke(false.B)
            dut.clock.step()
            dut.io.step.poke(false.B)

            for(i <- 0 until 400) {
                dut.clock.step()
            }

        }
    }
}