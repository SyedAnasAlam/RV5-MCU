import Utility.Clock_Divider

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class Clock_Divider_Test extends AnyFlatSpec with ChiselScalatestTester {
    "Clock Divider" should "pass" in {
        test(new Clock_Divider(2)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        
            var i = 0
            for(i <- 0 until 99) {
                dut.clock.step()
            }

        }
    }
}