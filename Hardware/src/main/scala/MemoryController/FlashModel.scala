package memoryController

import chisel3._
import chisel3.util._
import java.nio.file.{Files, Paths}
import memoryController.FlashCommands._
import spi._

/**
 * A hardware model af a SPI Flash
 * @param _count is number of system clock cycles per SPI clock cycles 
 * @param _app is path to a binary file and corresponds to the content of the flash
*/

class FlashModel(_count: Int, _app: String) extends Module {
    val io = IO(new Bundle {
        val spi = new SPISecondaryPort()
    })
    
    val program = Files.readAllBytes(Paths.get(_app))  
    val x = 26; // Sometimes the binary contains unneccessary header information, this is set to not simulate these bytes 
    val memory = Wire(Vec(program.length-x, SInt(8.W)))  
    var i, wordCount, byteCount = 0;
    for(i <- 0 until program.length-x) {
        if(i % 4 == 0) {
            wordCount += 1
            byteCount = 0
        }
        memory(i.U) := program(wordCount*4 - byteCount - 1).asSInt        
        byteCount += 1
    } 

    val counter = RegInit(0.U(log2Ceil(_count + 1).W))
    val max = (_count - 1).U
    counter := counter + 1.U
    when(counter === max) {
        counter := 0.U
    }

    val idle :: receiveInstruction :: commandLookup :: readCmd :: Nil = Enum(4)
    val state = RegInit(idle)
    val command = RegInit(0.U(8.W))
    val index = RegInit(0.U(24.W))
    val address = RegInit(1.U(24.W))
    val data = WireDefault(0.S(8.W))
    val subState = RegInit(0.U(2.W))
    val miso = RegInit(false.B)
    io.spi.miso := miso

    switch(state) {
        is(idle) {   
            index := 0.U
            miso := false.B
            subState := 0.U
            when(~io.spi.cs) {
                state := receiveInstruction
            }
        }
        is(receiveInstruction) {
            when(io.spi.cs) { state := idle }
            when(~io.spi.sck && counter === max) {
                index := index + 1.U
                command := Cat(command, io.spi.mosi.asUInt)

                when(index === 7.U) {
                    index := 0.U
                    state := commandLookup
                }
            }
        }
        is(commandLookup) {
            when(io.spi.cs) { state := idle }
            switch(command) {
                is(READ_CMD) { state := readCmd }
            }
        }
        is(readCmd) {
            when(io.spi.cs) { state := idle }
            switch(subState) {
                is(0.U) {
                    when(~io.spi.sck && counter === max) {
                        index := index + 1.U
                        address := Cat(address, io.spi.mosi.asUInt)
                        when(index === 23.U) {
                            index := 0.U                          
                            subState := 1.U
                        }
                    }
                }
                is(1.U) {
                    data := memory(address)
                    when(io.spi.sck && counter === max) {
                        index := index + 1.U
                        miso := data(7.U - index)
                        when(index === 7.U) {
                            index := 0.U
                            address := address + 1.U
                        }                         
                    }
                }
            }
        }
    }
}