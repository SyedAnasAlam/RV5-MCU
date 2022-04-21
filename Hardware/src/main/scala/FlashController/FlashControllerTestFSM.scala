package MemoryController

import chisel3._
import chisel3.util._
import SPI._

class FlashControllerTestFSM(count: Int) extends Module {
    require(count > 0, "count must be greater than 0")

    val io = IO(new Bundle {
        val readData = Output(UInt(16.W))
        val step = Input(Bool())
        val spi = new SPIMainPort()
        val debug_dataValid = Output(Bool())
    })

    val FlashController = Module(new FlashController(count))
    io.spi <> FlashController.io.spi

    FlashController.io.readEnable := false.B
    FlashController.io.branch := false.B
    FlashController.io.address := 0.U

    val dataBuffer = RegInit(0.U(32.W))
    val address = RegInit(0.U(24.W))
    val stepSync = RegNext(RegNext(io.step))

    io.readData := Cat(dataBuffer(31, 24), dataBuffer(7, 0))

    val idle :: intA :: read :: Nil = Enum(3)
    val state = RegInit(idle)
    
    val risingEdge = RegInit(false.B)
    
    switch(state) {
        is(idle) {
            FlashController.io.readEnable := false.B
            when(~stepSync) {
                state := intA
            }
        }
        is(intA) {
            when(stepSync) {
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
    io.debug_dataValid := FlashController.io.dataValid
}

object FlashControllerTestFSM extends App {
    emitVerilog(new FlashControllerTestFSM(count = 8), Array("--target-dir", "Generated"))
}