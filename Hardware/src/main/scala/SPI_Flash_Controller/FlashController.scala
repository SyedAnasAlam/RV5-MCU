package MemoryController

import chisel3._
import chisel3.util._
import MemoryController.FlashCommands._

object FlashCommands {
    val READ_CMD = "h03".U(8.W)
    val RELEASE_POWER_DOWN_CMD = "hAB".U(8.W)
}

class FlashController(count: Int) extends Module {
    require(count > 0, "count must be greater than 0")

    val io = IO(new Bundle {
        val readEnable = Input(Bool())
        val branch = Input(Bool())
        val address = Input(UInt(24.W))
        val dataValid = Output(Bool())
        val readData = Output(UInt(32.W))

        val cs = Output(Bool())
        val mosi = Output(Bool())
        val miso = Input(Bool())
        val sck = Output(Bool())
    })

    val index = RegInit(0.U(32.W))
    val counter = RegInit(0.U(log2Ceil(count + 1).W))
    val sck = RegInit(true.B)
    val mosi = RegInit(false.B)
    val cs = RegInit(true.B)
    val dataBuffer = RegInit(0.U(32.W))
    
    val powerUp :: idle :: transmitCMD :: transmitAddress :: receiveData :: Nil = Enum(5)
    val state = RegInit(powerUp)

    io.cs := cs
    io.mosi := mosi
    io.sck := sck   
    io.readData := dataBuffer 
    io.dataValid := false.B

    val max = (count - 1).U
    counter := counter + 1.U
    when(counter === max) {
        counter := 0.U
        sck := ~sck
    }

    switch(state) {
        is{powerUp} {  
            when(spiTransmit(data = RELEASE_POWER_DOWN_CMD, length = 8.U)) {
                cs := true.B
                state := idle
            }
        }
        is(idle) {
            when(sck && counter === max) {
                io.dataValid := false.B
                io.readData := dataBuffer

                mosi := false.B
                sck := true.B
                index := 0.U

                when(io.readEnable) {
                    dataBuffer := 0.U
                    state := transmitCMD
                }
            }
        }
        is(transmitCMD) {
            when(spiTransmit(data = READ_CMD, length = 8.U)) {
                sck := true.B
                state := transmitAddress
            }
        }
        is(transmitAddress) {
            when(spiTransmit(data = io.address, length = 24.U)) {
                sck := true.B
                state := receiveData
            }         
        }
        is(receiveData) {
            when(~sck && counter === max) {
                index := index + 1.U
                dataBuffer := Cat(dataBuffer, io.miso.asUInt)    

                when(index === 32.U) {
                    io.dataValid := true.B
                    when(io.branch) {
                        index := 0.U
                    }
                    . otherwise {
                        cs := true.B
                        state := idle
                    }
                }        
            }
        }
    }

    def spiTransmit(data: UInt, length: UInt): Bool = {
        val ret = WireDefault(false.B)
        when(sck && counter === max) {
            cs := false.B
            index := index + 1.U
            mosi := data((length - 1.U) - index)
            when(index === length) {
                index := 0.U
                ret := true.B
            }
        }
        ret     
    }

}

object FlashController extends App {
    emitVerilog(new FlashController(count = 2), Array("--target-dir", "Generated"))
}