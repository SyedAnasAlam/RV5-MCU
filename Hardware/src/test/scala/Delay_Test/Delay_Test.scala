import Utility.Delay

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


class Delay_Test extends AnyFlatSpec with ChiselScalatestTester {
    "Delay Test" should "pass" in {
        test(new Delay(100)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            
            dut.io.ready.poke(true.B)
            dut.clock.step()
            dut.io.ready.poke(false.B)

            for(i <- 0 until 100) {
                dut.clock.step()
            }
        }
    }
}