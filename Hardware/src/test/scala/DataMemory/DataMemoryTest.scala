import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random
import scala.math.pow

class DataMemoryTest extends AnyFlatSpec with ChiselScalatestTester {
    "Data memory test" should "pass" in {
        test(new DataMemory()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
/*             val N = 10
            val max : Int = pow(2, 32).toInt
            var addresses = new Array[Int](N)
            val writeData = new Array[Int](N)
            var readData: Int = 0
            var expectedData: Int = 0
            var memory:Map[Int, Byte] = Map()

            var i = 0
            for(i <- 0 until N) {
                addresses(i) = Random.nextInt(128*16)
                writeData(i) = Random.nextInt()
            }

            dut.io.readEnable.poke(false.B)
            dut.io.writeEnable.poke(true.B)
            for(i <- 0 until N) {
                dut.io.address.poke(addresses(i).U)
                dut.io.writeData.poke(writeData(i).S)
                val funct3 = Random.nextInt(3)
                dut.io.funct3.poke(funct3.U)
                dut.clock.step()
                val dataToWrite = writeData(i)
                println("dataToWrite = " + writeData(i))
                if(funct3 == 0) {
                    val byte = (writeData(i) & 0xFF).toByte
                    memory += (addresses(i) -> byte)
                    memory += (addresses(i) + 1 -> 0)
                    memory += (addresses(i) + 2 -> 0)
                    memory += (addresses(i) + 3 -> 0)
                    println("Mem[" + addresses(i) + "] = " + byte)
                }
                else if(funct3 == 1) {
                    val byte1 = (writeData(i) & 0x00FF).toByte
                    val byte2 = ((writeData(i) & 0xFF00) >> 8).toByte
                    memory += (addresses(i) -> byte1)
                    memory += (addresses(i) + 1 -> byte2)
                    memory += (addresses(i) + 2 -> 0)
                    memory += (addresses(i) + 3 -> 0)
                    println("Mem[" + addresses(i) + "] = " + byte1)
                    println("Mem[" + (addresses(i) + 1) + "] = " + byte2)
                }
                else if(funct3 == 2) {
                    val byte1 =  (writeData(i) & 0x000000FF).toByte
                    val byte2 = ((writeData(i) & 0x0000FF00) >> 8).toByte
                    val byte3 = ((writeData(i) & 0x00FF0000) >> 16).toByte
                    val byte4 = ((writeData(i) & 0xFF000000) >> 24).toByte
                    memory += (addresses(i) -> byte1)
                    memory += (addresses(i) + 1 -> byte2)
                    memory += (addresses(i) + 2 -> byte3)
                    memory += (addresses(i) + 3 -> byte4)   
                    println("Mem[" + addresses(i) + "] = " + byte1)
                    println("Mem[" + (addresses(i) + 1) + "] = " + byte2)  
                    println("Mem[" + (addresses(i) + 2) +"] = " + byte3)
                    println("Mem[" + (addresses(i) + 3) + "] = " + byte4)               
                }
                println()
                println()
            }

            dut.io.writeEnable.poke(false.B)
            dut.clock.step()
            dut.clock.step()
        
            dut.io.readEnable.poke(true.B)
            for(i <- 0 until N) {
                dut.io.address.poke(addresses(i).U)
                dut.io.writeData.poke(writeData(i).S) 
                //val funct3 = Random.nextInt(3)
                val funct3 = 1
                dut.io.funct3.poke(funct3.U)
                dut.clock.step()
                readData = dut.io.readData.peek().litValue().toInt 
                println(memory(addresses(i) + 0))
                println(memory(addresses(i) + 1))
                println(memory(addresses(i) + 2))
                println(memory(addresses(i) + 3))
                println("funct3 = " + funct3)
                if(funct3 == 0) {
                    expectedData = memory(addresses(i))
                }
                else if(funct3 == 1) {
                    println(Console.MAGENTA + (memory(addresses(i) + 1) << 8))
                    println(Console.MAGENTA + memory(addresses(i)))
                    expectedData = (memory(addresses(i) + 1) << 8) | memory(addresses(i))
                }
                else if(funct3 == 2) {
                    expectedData =  (memory(addresses(i) + 3) << 24) | 
                                    (memory(addresses(i) + 2) << 16) | 
                                    (memory(addresses(i) + 1) << 8)  |
                                    memory(addresses(i))
                }
                dut.io.readData.expect(expectedData.S)
            } */



            dut.io.readEnable.poke(false.B)
            dut.io.writeEnable.poke(true.B)
            dut.io.address.poke(0.U)
            dut.io.writeData.poke(0x55AA55AA.S)
            dut.io.funct3.poke(0x02.U)
            dut.clock.step()

            dut.io.readEnable.poke(true.B)
            dut.io.writeEnable.poke(false.B)
            dut.io.address.poke(0.U)
            dut.io.funct3.poke(0x04.U)
            dut.clock.step()
            println(dut.io.readData.peek().litValue().toInt.toHexString)

            dut.io.readEnable.poke(true.B)
            dut.io.writeEnable.poke(false.B)
            dut.io.address.poke(1.U)
            dut.io.funct3.poke(0x04.U)
            dut.clock.step()
            println(dut.io.readData.peek().litValue().toInt.toHexString)

            dut.io.readEnable.poke(true.B)
            dut.io.writeEnable.poke(false.B)
            dut.io.address.poke(2.U)
            dut.io.funct3.poke(0x04.U)
            dut.clock.step()
            println(dut.io.readData.peek().litValue().toInt.toHexString)

            dut.io.readEnable.poke(true.B)
            dut.io.writeEnable.poke(false.B)
            dut.io.address.poke(3.U)
            dut.io.funct3.poke(0x04.U)
            dut.clock.step()
            println(dut.io.readData.peek().litValue().toInt.toHexString)

        }
    }
}

