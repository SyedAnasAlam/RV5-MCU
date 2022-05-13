package memoryController

import chisel3._
import chisel3.util._
import memoryController.FlashCommands._
import spi._

/**
 * SPI Flash Controller 
 * @param _count is number of system clock cycles per output SPI clock cycles
 * SCK is high when idle
 * Data is shifted out on the falling edge of SCK such that it is valid on the rising edge of SCK
 * The controller samples data from the flash on the rising edge of SCK
*/

object FlashCommands {
    val READ_CMD = "h03".U(8.W)
    val RELEASE_POWER_DOWN_CMD = "hAB".U(8.W)
}

class FlashController(_count: Int) extends Module {
    require(_count > 0, "count must be greater than 0")

    val io = IO(new Bundle {
        val readEnable = Input(Bool())
        val address = Input(UInt(24.W))
        val dataValid = Output(Bool())
        val readData = Output(UInt(32.W))
        
        val spi = new SPIMainPort()
    })

    val index = RegInit(0.U(32.W))
    val counter = RegInit(0.U(log2Ceil(_count + 1).W))
    val sck = RegInit(true.B)
    val mosi = RegInit(false.B)
    val cs = RegInit(true.B)
    val dataBuffer = RegInit(0.U(32.W))
    val dataValid = WireDefault(false.B)
    
    val powerUp :: idle :: transmitCMD :: transmitAddress :: receiveData :: Nil = Enum(5)
    val state = RegInit(powerUp)

    io.spi.cs := cs
    io.spi.mosi := mosi
    io.spi.sck := sck   
    io.readData := dataBuffer 
    io.dataValid := dataValid

    val max = (_count - 1).U
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
            io.readData := dataBuffer
            when(sck && counter === max) {
                dataValid := false.B

                mosi := false.B
                sck := true.B
                cs := true.B
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
                state := receiveData
            }         
        }
        is(receiveData) {
            when(~sck && counter === max) {
                index := index + 1.U
                dataBuffer := Cat(dataBuffer, io.spi.miso.asUInt)    

                when(index === 32.U) {
                    dataValid:= true.B
                    state := idle
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
