import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import MemoryController._
class FlashControllerTest extends AnyFlatSpec with ChiselScalatestTester {
    "Flash Controller" should "pass" in {
        val ProgramFolder = "../Test-Programs/"
        val program = "test1.bin"
        test(new FlashSimulation(count = 2, app = ProgramFolder + program)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.clock.setTimeout(10001)
            for(i <- 0 until 10000) {
                dut.clock.step()
            }
        }
    }
}

