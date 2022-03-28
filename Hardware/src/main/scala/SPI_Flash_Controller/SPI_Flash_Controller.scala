package Memory_Controller

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil
import chisel3.stage.ChiselStage
import Memory_Controller.SPI_Flash_Commands._

class SPI_Flash_Controller(count: Int) extends Module {
    require(count > 0, "count must be greater than 0")

    val io = IO(new Bundle {
        val read_enable = Input(Bool())
        val addr = Input(UInt(8.W))
        val data_valid = Output(Bool())
        val read_data = Output(UInt(8.W))

        val cs = Output(Bool())
        val mosi = Output(Bool())
        val miso = Input(Bool())
        val sck = Output(Bool())
        val hold = Output(Bool())
        val wp = Output(Bool())

        val debug_transmit_complete = Output(Bool())
    })

    val index = RegInit(0.U(32.W))
    val counter = RegInit(0.U(log2Ceil(count + 1).W))
    val sck = RegInit(false.B)
    val mosi = RegInit(false.B)
    val cs = RegInit(false.B)
    val address = Cat(0.U(16.W), io.addr)

    io.wp := true.B
    io.hold := true.B
    
    io.data_valid := false.B
    io.read_data := false.B
    io.debug_transmit_complete := false.B

    val idle :: transmit :: receive_data :: Nil = Enum(3)
    val state = RegInit(idle)

    switch(state) {
        is(idle) {
            io.data_valid := false.B
            io.read_data := false.B

            cs := true.B
            mosi := false.B
            sck := false.B

            index := 0.U

            when(io.read_enable) {
                state := transmit
            }
        }
        is(transmit) {
            cs := false.B

            counter := counter + 1.U
            when(counter === (count - 1).U) {
                counter := 0.U
                sck := ~sck
                val transmit_data = Cat(READ_CMD, address)
                mosi := transmit_data(31.U - index)
                
                when(sck) {
                    index := index + 1.U
                }

                when(index === 32.U) {
                    index := 0.U
                    io.debug_transmit_complete := true.B
                    state := receive_data
                } 
            }
        }
        is(receive_data) {
            counter := counter + 1.U
            when(counter === (count - 1).U) {
                counter := 0.U
                sck := ~sck

                val data_reg = RegInit(0.U(8.W))
                
                when(~sck) {
                    index := index + 1.U
                    data_reg := Cat(data_reg, io.miso.asUInt)
                }

                when(index === 8.U) {
                    io.data_valid := true.B
                    io.read_data := data_reg
                    state := idle
                } 
            }
        }
    }

    io.cs := cs
    io.mosi := mosi
    io.sck := ~sck    
}

object SPI_Flash_Controller extends App {
    emitVerilog(new SPI_Flash_Controller(count = 4), Array("--target-dir", "Generated"))
}