import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random
import scala.math.pow

class RegisterFileTest extends AnyFlatSpec with ChiselScalatestTester {
    "Register file test" should "pass" in {
        test(new RegisterFile()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            
            write(0, 0, 1, 0xAA)
            write(0, 0, 2, 0X55)
            read(1, 2)


            println(dut.io.registerFile(0).peek().litValue.toInt.toHexString)


            def write(rs1:Int, rs2:Int, rw:Int, d: Int) {
                dut.io.regSource1.poke(rs1.U)
                dut.io.regSource2.poke(rs2.U)
                dut.io.regWrite.poke(rw.U)
                dut.io.writeEnable.poke(true.B)
                dut.io.writeData.poke(d.S)
                println("RF[" + rs1 + "] = " + dut.io.regData1.peek().litValue.toInt.toHexString)
                println("RF[" + rs2 + "] = " + dut.io.regData2.peek().litValue.toInt.toHexString)
                println("Write into RF[" + rw + "] = " + d.toHexString)
                dut.clock.step()
            }

            def read(rs1:Int, rs2:Int) = {
                dut.io.regSource1.poke(rs1.U)
                dut.io.regSource2.poke(rs2.U)
                dut.io.regWrite.poke(0.U)
                dut.io.writeEnable.poke(false.B)
                dut.io.writeData.poke(0.S)
                println("RF[" + rs1 + "] = " + dut.io.regData1.peek().litValue.toInt.toHexString)
                println("RF[" + rs2 + "] = " + dut.io.regData2.peek().litValue.toInt.toHexString)
                dut.clock.step()
            }
        }
    }
}



