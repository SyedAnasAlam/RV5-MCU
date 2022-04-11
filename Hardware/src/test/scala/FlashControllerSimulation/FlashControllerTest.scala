import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import MemoryController._
// "../Test-Programs/t1.bin"
//"../../Documents/ftdiflash-master/file.bin"
class FlashControllerTest extends AnyFlatSpec with ChiselScalatestTester {
    "Flash Controller" should "pass" in {
        test(new FlashSimulation(count = 2, app = "../../Documents/ftdiflash-master/file.bin")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.readEnable.poke(true.B)
            dut.clock.step()
            dut.io.readEnable.poke(false.B)
            var i = 0
            for(i <- 0 until 300) {
                dut.clock.step()
            }
        }
    }
}

