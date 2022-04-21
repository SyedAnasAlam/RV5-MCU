package MemoryController

import chisel3._
import chisel3.util._

class FlashSimulation(count: Int, app: String) extends Module {
    val io = IO(new Bundle {
        val sck = Output(Bool())
        val cs = Output(Bool())
        val mosi = Output(Bool())
        val miso = Output(Bool()) 
        val instruction = Output(UInt(32.W))
        val dataValid = Output(Bool())
    })

    val pc = RegInit(0.U(32.W))
    val instruction = WireDefault(0.U(32.W))

    val FlashController = Module(new FlashController(count = count))
    val FlashModel = Module(new FlashModel(count = count, app))
    FlashModel.io.spi <> FlashController.io.spi
    FlashController.io.branch := false.B
    FlashController.io.readEnable := true.B
    FlashController.io.address := pc(23, 0)

    when(FlashController.io.dataValid) {
        instruction := FlashController.io.readData
        pc := pc + 4.U
    }
    . otherwise {
        instruction := 0.U
    }
    
    io.sck := FlashController.io.spi.sck
    io.cs := FlashController.io.spi.cs
    io.mosi := FlashController.io.spi.mosi
    io.miso := FlashModel.io.spi.miso
    io.instruction := instruction
    io.dataValid := FlashController.io.dataValid
}