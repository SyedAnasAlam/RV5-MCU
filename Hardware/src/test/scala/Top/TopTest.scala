import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.nio.file.{Files, Paths}

 class TopTest extends AnyFlatSpec with ChiselScalatestTester {
    "Processor test" should "pass" in {
        val ProgramFolder = "../Test-Programs/"
        val OutputFolder = "../Test-Programs/Result/"
        val program = "loop.bin"
        test(new TopSim(ProgramFolder + program)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.clock.setTimeout(200000)
            var i, j = 0
            
            //For ecall
            var exit = false  
            var id, arg = 0   

            while(!exit) {
                dut.clock.step(1)
                id = dut.io.systemCallId.peek().litValue().toInt
                arg = dut.io.systemCallArgument.peek().litValue().toInt
                id match {
                    // TODO Support print ecall
                    case 1  => println("%d\n", arg)
                    case 10 => exit = true
                    case 11 => println("%c\n", arg)
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
            Files.write(Paths.get(OutputFolder + program), rfBytes)
        }
    }
}

