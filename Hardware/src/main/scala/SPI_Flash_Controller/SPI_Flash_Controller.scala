package Memory_Controller

import chisel3._
import chisel3.util._

class SPI_Flash_Controller extends Module {
    val io = IO(new Bundle {
        val CMD = Input(UInt(8.W))
        val CE = Output(Bool())
        val MOSI = Output(Bool())
    })

    val CntReg = RegInit(0.U(8.W))
    io.MOSI := false.B
    io.CE := false.B

    io.MOSI := io.CMD(7.U - CntReg)
    CntReg := CntReg + 1.U
    when(CntReg === 8.U) {
        io.CE := true.B
        CntReg := 0.U
        io.MOSI := 0.U
    }

}
