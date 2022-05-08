import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.nio.file.{Files, Paths}
import utility.Constants._

 class TopTest extends AnyFlatSpec with ChiselScalatestTester {
    "Processor3 test" should "pass" in {
        val program = "loop.bin"
        test(new Top(PROGRAM_FOLDER + program)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.clock.setTimeout(0)
            var i = 0;
            for(i <- 0 until 50000) {
                dut.clock.step()
            }
            /* var i, j = 0
            
            //Ecall
            var exit = false  
            var id, arg = 0   
            while(!exit) {
                dut.clock.step(1)
                id = dut.io.systemCallId.peek().litValue().toInt
                arg = dut.io.systemCallArgument.peek().litValue().toInt
                id match {
                    case 1  => println(arg)
                    case 10 => exit = true
                    case _  =>  
                }
            }

            // Byte array to store registerfile
            var rfBytes = new Array[Byte](128)
            var mask, word = 0

            for(i <- 0 until 32) {
                word = dut.io.registerFile(i).peek().litValue().toInt 

                println("[" + i + ";" + word.toHexString + "]")
                if((i+1) % 8 == 0) print("\n")

                for(j <- 0 until 4) {
                    mask = 255 << j*8
                    rfBytes(i*4 + j) = ((word & mask) >> 8*j).toByte
                }
            }

            // Binary dump of register file
            Files.write(Paths.get(OUTPUT_FOLDER + program), rfBytes)  */
        }
    }
}

 