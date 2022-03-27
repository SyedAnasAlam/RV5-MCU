import Memory_Controller.SPI_Flash_Controller

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WaveformTest extends AnyFlatSpec with ChiselScalatestTester {
    "Waveform" should "pass" in {
        test(new SPI_Flash_Controller).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            
            dut.io.CMD.poke(1.U)
            
            var i = 0
            for(i <- 0 until 10) {
                dut.clock.step()
            }

        }
    }
}