package memoryController

import chisel3._
import chisel3.util._
import spi._

/**
 * A test FSM intended for testing Flash Controller on FPGA
 * On rising edge of the input next it will perform a read on the flash 
 * It reads 32 bits on each read and output the upper 8 bit and lower 8 bit on read data
 * After each read transaction the address is incremented by 4
*/

class FlashControllerTestFSM(_count: Int) extends Module {
    require(_count > 0, "_count must be greater than 0")

    val io = IO(new Bundle {
        val readData = Output(UInt(16.W))
        val next = Input(Bool())
        val spi = new SPIMainPort()
    })

    val FlashController = Module(new FlashController(_count))
    io.spi <> FlashController.io.spi

    FlashController.io.readEnable := false.B
    FlashController.io.address := 0.U

    val dataBuffer = RegInit(0.U(32.W))
    val address = RegInit(0.U(24.W))
    val stepSync = RegNext(RegNext(io.next))

    io.readData := Cat(dataBuffer(31, 24), dataBuffer(7, 0))

    val idle :: intA :: read :: Nil = Enum(3)
    val state = RegInit(idle)
    
    val risingEdge = RegInit(false.B)
    switch(state) {
        is(idle) {
            FlashController.io.readEnable := false.B
            when(stepSync) {
                state := intA
            }
        }
        is(intA) {
            when(~stepSync) {
                state := read
            }
        }
        is(read) {
            FlashController.io.address := address
            FlashController.io.readEnable := true.B
            when(FlashController.io.dataValid) {
                dataBuffer := FlashController.io.readData
                address := address + 4.U
                state := idle
            }
        }
    }
}

object FlashControllerTestFSM extends App {
    emitVerilog(new FlashControllerTestFSM(_count = 8), Array("--target-dir", "Generated"))
}