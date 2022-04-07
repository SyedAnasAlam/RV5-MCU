package MemoryController

import chisel3._
import chisel3.util._

class FlashSimulation(count: Int, app: String) extends Module {
    val io = IO(new Bundle {
        val readEnable = Input(Bool())
        val address = Input(UInt(24.W))
        val dataValid = Output(Bool())
        val readData = Output(UInt(32.W))   

        val cs = Output(Bool())
        val mosi = Output(Bool())
        val miso = Output(Bool()) 
        val sck = Output(Bool())

        val debug_counter = Output(UInt(log2Ceil(count + 1).W))
        val debug_state = Output(UInt(2.W))
        val debug_index = Output(UInt(24.W))
        val debug_command = Output(UInt(8.W))
        val debug_address = Output(UInt(24.W))
        val debug_subState = Output(UInt(2.W))
        val debug_data = Output(UInt(32.W))
    })
    
    val FlashController = Module(new FlashController(count = count))
    val FlashModel = Module(new FlashModel(count = count, app))
    FlashController.io.branch := false.B
    FlashController.io.readEnable := io.readEnable
    FlashController.io.address := io.address
    io.dataValid := FlashController.io.dataValid
    io.readData := FlashController.io.readData


    // TODO: Use bundle connection here
    FlashModel.io.cs := FlashController.io.cs
    FlashModel.io.mosi := FlashController.io.mosi
    FlashController.io.miso := FlashModel.io.miso
    FlashModel.io.sck := FlashController.io.sck   

    io.cs := FlashController.io.cs
    io.mosi := FlashController.io.mosi
    io.miso := FlashModel.io.miso
    io.sck := FlashController.io.sck

    io.debug_counter := FlashModel.io.debug_counter
    io.debug_state := FlashModel.io.debug_state
    io.debug_index := FlashModel.io.debug_index
    io.debug_command := FlashModel.io.debug_command
    io.debug_address := FlashModel.io.debug_address
    io.debug_subState := FlashModel.io.debug_subState
    io.debug_data := FlashModel.io.debug_data

}