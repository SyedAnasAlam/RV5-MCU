import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random
import scala.math.pow

class DataMemoryTest extends AnyFlatSpec with ChiselScalatestTester {
    "Data memory test" should "pass" in {
        test(new DataMemory()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
          
          write(10, 0xAA55AA55, 2);

          dut.clock.step(1);
          dut.io.readEnable.poke(false.B)
          dut.io.writeEnable.poke(false.B)
          dut.io.address.poke(0.U)
          dut.io.writeData.poke(0.S)
          dut.io.funct3.poke(0.U)
          dut.clock.step()

          read(10, 4);
          read(11, 4);
          read(12, 4);
          read(13, 4);
          
          /* dut.clock.step(10);
          println("");

          write(2, 0x88, 0);
          read(0, 4);
          read(1, 4);
          read(2, 4);
          read(3, 4);
          read(4, 4);

          println("")
          write(512, -1245, 2);
          read(512, 2);

          println("");
          write(7, 0x44332211, 1);
          read(7, 1);

          println("");
          write(8000, -1, 2);
          read(8000, 5); */

          def write(address: Int, writeData: Int, funct3:Int) {
            dut.io.readEnable.poke(false.B)
            dut.io.writeEnable.poke(true.B)
            dut.io.address.poke(address.U)
            dut.io.writeData.poke(writeData.S)
            dut.io.funct3.poke(funct3.U)
            dut.clock.step()
          }

          def read(address:Int, funct3:Int) = {
            dut.io.readEnable.poke(true.B)
            dut.io.writeEnable.poke(false.B)
            dut.io.address.poke(address.U)
            dut.io.funct3.poke(funct3.U)
            dut.clock.step()
            println(dut.io.readData.peek().litValue().toInt.toHexString)
          }
        }
    }
}



