package Memory_Controller

import chisel3._
import chisel3.util._
import Memory_Controller.SPI_Flash_Commands._
import Utility.Delay

/*class SPI_Flash_Controller(count: Int) extends Module {
    require(count > 0, "count must be greater than 0")

    val io = IO(new Bundle {
        val read_enable = Input(Bool())
        val branch = Input(Bool())
        val address = Input(UInt(24.W))
        val data_valid = Output(Bool())
        val read_data = Output(UInt(32.W))

        val cs = Output(Bool())
        val mosi = Output(Bool())
        val miso = Input(Bool())
        val sck = Output(Bool())
    })

    val index = RegInit(0.U(32.W))
    val counter = RegInit(0.U(log2Ceil(count + 1).W))
    val sck = RegInit(false.B)
    val mosi = RegInit(false.B)
    val cs = RegInit(true.B)
    val data_reg = RegInit(0.U(32.W))
    
    val power_up :: idle :: transmit_cmd :: transmit_address :: receive_data :: Nil = Enum(5)
    val state = RegInit(power_up)

    counter := counter + 1.U

    io.cs := cs
    io.mosi := mosi
    io.sck := sck   
    io.read_data := data_reg 
    io.data_valid := false.B
    io.read_data := 0.U

    switch(state) {
        is{power_up} {
            cs := false.B
            when(counter === (count - 1).U) {
                counter := 0.U
                sck := ~sck   

                when(sck) {
                    index := index + 1.U
                    mosi := RELEASE_POWER_DOWN_CMD(7.U - index)

                    when(index === 8.U) {
                        index := 0.U
                        state := idle
                    }
                }             
            }       
        }
        is(idle) {
            io.data_valid := false.B
            io.read_data := data_reg

            cs := true.B
            mosi := false.B
            sck := true.B

            index := 0.U

            when(io.read_enable) {
                data_reg := 0.U
                cs := false.B
                state := transmit_cmd
            }
        }
        is(transmit_cmd) {
            when(counter === (count - 1).U) {
                counter := 0.U
                sck := ~sck
                
                when(sck) {
                    index := index + 1.U
                    mosi := READ_CMD(7.U - index)

                    when(index === 8.U) {
                        index := 0.U
                        state := transmit_address
                    }
                }   
            }
        }
        is(transmit_address) {
             when(counter === (count - 1).U) {
                counter := 0.U
                sck := ~sck 

                when(sck) {
                    index := index + 1.U                    
                    mosi := io.address(23.U - index)

                    when(index === 24.U) {
                        index := 0.U
                        state := receive_data
                    }
                }             
            }              
        }
        is(receive_data) {
            when(counter === (count - 1).U) {
                counter := 0.U
                sck := ~sck
  
                when(~sck) {
                    index := index + 1.U
                    data_reg := Cat(data_reg, io.miso.asUInt)

                    when(index === 32.U) {
                        io.data_valid := true.B
                        
                        when(io.branch) {
                            index := 0.U
                        }
                        . otherwise {
                            state := idle
                        }
                    } 
                }
            }
        }
    }
}*/

class SPI_Flash_Controller(count: Int) extends Module {
    require(count > 0, "count must be greater than 0")

    val io = IO(new Bundle {
        val read_enable = Input(Bool())
        val branch = Input(Bool())
        val address = Input(UInt(24.W))
        val data_valid = Output(Bool())
        val read_data = Output(UInt(32.W))

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
    val data_reg = RegInit(0.U(32.W))
    
    val power_up :: idle :: transmit_cmd :: transmit_address :: receive_data :: Nil = Enum(5)
    val state = RegInit(power_up)

    io.cs := cs
    io.mosi := mosi
    io.sck := sck   
    io.read_data := data_reg 
    io.data_valid := false.B
    io.read_data := 0.U

    val max = (count - 1).U
    counter := counter + 1.U
    when(counter === max) {
        counter := 0.U
        sck := ~sck
    }

    switch(state) {
        is{power_up} {
            /*when(sck && counter === max) {
                cs := false.B
                index := index + 1.U
                mosi := RELEASE_POWER_DOWN_CMD(7.U - index)
                when(index === 8.U) {
                    index := 0.U
                    cs := true.B
                    state := idle
                }
            }*/   
            spiTransmit(RELEASE_POWER_DOWN_CMD, 8.U) 
        }
        is(idle) {
            when(sck && counter === max) {
                io.data_valid := false.B
                io.read_data := data_reg

                mosi := false.B
                sck := true.B
                index := 0.U

                when(io.read_enable) {
                    data_reg := 0.U
                    state := transmit_cmd
                }
            }
        }
        is(transmit_cmd) {
            when(sck && counter === max) {
                cs := false.B
                index := index + 1.U
                mosi := READ_CMD(7.U - index)
                when(index === 8.U) {
                    index := 0.U
                    cs := true.B
                    state := transmit_address
                }                
            }
        }
        is(transmit_address) {
            when(sck && counter === max) {
                cs := false.B
                index := index + 1.U 
                mosi := io.address(23.U - index)

                when(index === 24.U) {
                    index := 0.U
                    state := receive_data
                }               
            }            
        }
        is(receive_data) {
            when(~sck && counter === max) {
                index := index + 1.U
                data_reg := Cat(data_reg, io.miso.asUInt)    

                when(index === 32.U) {
                    io.data_valid := true.B
                    when(io.branch) {
                        index := 0.U
                    }
                    . otherwise {
                        state := idle
                    }
                }        
            }
        }
    }

    def spiTransmit(data: UInt, length: UInt): Unit = {
        when(sck && counter === max) {
            cs := false.B
            index := index + 1.U
            mosi := data((length - 1.U) - index)
            when(index === length) {
                index := 0.U
                cs := true.B
                state := idle
            }
        }     
    }

}

object SPI_Flash_Controller extends App {
    emitVerilog(new SPI_Flash_Controller(count = 2), Array("--target-dir", "Generated"))
}