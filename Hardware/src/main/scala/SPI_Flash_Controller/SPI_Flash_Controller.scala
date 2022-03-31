package Memory_Controller

import chisel3._
import chisel3.util._
import Memory_Controller.SPI_Flash_Commands._
import Utility.Delay

/*class SPI_Flash_Controller(count: Int) extends Module {
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
    val data_reg = RegInit(0.U(8.W))

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
            io.read_data := data_reg

            cs := true.B
            mosi := false.B
            sck := true.B

            index := 0.U

            when(io.read_enable) {
                data_reg := 0.U
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
}*/

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
    val cs = RegInit(true.B)
    val address = Cat(0.U(16.W), io.addr)
    val data_reg = RegInit(0.U(12.W))

    io.wp := true.B
    io.hold := true.B
    
    io.data_valid := false.B
    io.read_data := false.B
    io.debug_transmit_complete := false.B

    val idle :: power_up :: wait_power_up :: transmit :: wait_receive_data :: receive_data :: exit :: Nil = Enum(7)
    val state = RegInit(idle)

    switch(state) {
        is(idle) {
            io.data_valid := false.B
            io.read_data := data_reg

            cs := true.B
            mosi := false.B
            sck := true.B

            index := 0.U

            when(io.read_enable) {
                data_reg := 0.U
                counter := counter + 1.U
                cs := false.B
                state := power_up
            }
        }
        is{power_up} {
            counter := counter + 1.U
            when(counter === (count - 1).U) {
                counter := 0.U
                sck := ~sck     
                when(sck) {
                    index := index + 1.U
                    
                    val transmit_data = Cat(RELEASE_POWER_DOWN_CMD)
                    mosi := transmit_data(7.U - index)

                    when(index === 8.U) {
                        index := 0.U
                        cs := true.B
                        state := wait_power_up
                    }
                }             
            }       
        }
        is(wait_power_up) {
            counter := counter + 1.U
            when(counter === (count - 1).U) {
                counter := 0.U
                sck := ~sck
                when(sck) {
                    sck := true.B
                    mosi := false.B
                    cs := true.B
                }
            }  

            val Delay = Module(new Delay(3000))
            Delay.io.ready := true.B
            when(Delay.io.done) {
                Delay.io.ready := false.B
                counter := counter + 1.U
                cs := false.B
                state := transmit
             }
        }
        is(transmit) {
            counter := counter + 1.U
            when(counter === (count - 1).U) {
                counter := 0.U
                sck := ~sck
                
                when(sck) {
                    cs := false.B
                    index := index + 1.U
                    val transmit_data = Cat(READ_CMD, address)
                    mosi := transmit_data(31.U - index)

                    when(index === 32.U) {
                        index := 0.U
                        sck := true.B
                        mosi := false.B
                        cs := false.B
                        state := wait_receive_data
                    }
                }   
            }
        }
        is(wait_receive_data) {
            val Delay = Module(new Delay(1000000))
            Delay.io.ready := true.B
            when(Delay.io.done) {
                Delay.io.ready := false.B
                state := receive_data
            }
        }
        is(receive_data) {
            counter := counter + 1.U
            when(counter === (count - 1).U) {
                counter := 0.U
                sck := ~sck
  
                when(~sck) {
                    index := index + 1.U
                    data_reg := Cat(data_reg, io.miso.asUInt)
                }

                when(index === 96.U) {
                    state := exit
                } 
            }
        }
        is(exit) {
            io.data_valid := true.B
            io.read_data := data_reg
            sck := true.B
            cs := true.B
        }
    }

    io.cs := cs
    io.mosi := mosi
    io.sck := sck    
}

object SPI_Flash_Controller extends App {
    emitVerilog(new SPI_Flash_Controller(count = 8), Array("--target-dir", "Generated"))
}