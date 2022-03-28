import Memory_Controller.SPI_Flash_Controller

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


class SPI_Flash_Controller_Test extends AnyFlatSpec with ChiselScalatestTester {
    "Waveform" should "pass" in {
        test(new SPI_Flash_Controller(1)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

            dut.io.addr.poke(0.U)
            dut.io.read_enable.poke(false.B)
            dut.io.miso.poke(false.B)

            dut.clock.step()
            dut.clock.step()

            dut.io.addr.poke(5.U)
            dut.io.read_enable.poke(true.B)
            dut.io.miso.poke(false.B)

            
            while(dut.io.debug_transmit_complete.peek().litValue() == 0) {
                dut.clock.step()
            }

            val data = "b10101010".U(8.W)

            var i, j = 0
            for(i <- 0 until 100) {
                dut.clock.step()
                if(dut.io.sck.peek().litValue() == 1) {
                    j = j + 1
                    dut.io.miso.poke(data(j))
                }
            }
        }
    }
}