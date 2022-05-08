package utility

import chisel3._
import chisel3.util._

/**
 * This description will instantiate dual-port block RAM on Lattice ICE40 FPGA
*/

class BRAM(size:Int, width:Int) extends Module {
    val io = IO(new Bundle {
        val writeAddress = Input(UInt(log2Up(size).W))
        val readAddress = Input(UInt(log2Up(size).W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(width.W))
        val readData = Output(SInt(width.W))
    })
    
    val BRAM = SyncReadMem(size, SInt(width.W))

    io.readData := 0.S
    when(io.readEnable) {
        io.readData := BRAM.read(io.readAddress)
    }

    when(io.writeEnable) {
        BRAM.write(io.writeAddress, io.writeData)
    }
}