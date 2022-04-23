import chisel3._
import chisel3.util._



class DataMemory extends Module {
    val max = 512*20 - 1 // 
    val io = IO(new Bundle {
        val address = Input(UInt(11.W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(32.W))
        val funct3 = Input(UInt(3.W))
        val readData = Output(SInt(32.W))
    })

    val mem = VecInit(Seq.fill(4) { Reg( Vec(4, Vec(512, SInt(8.W)) ) ) } )
    val memRowIndex = io.address >> 9
    val memRowA = mem(memRowIndex)
    val memRowB = mem(memRowIndex + 1.U)

    // localAddress = map(io.address, 0, 2048, 0, 512)
    // localAddress = (io.address * 512) / (2048)
    // localAddress = (io.address << 9) >> 11
    // localAddress = io.address >> 2
    val localAddress = io.address >> 2
    val address = localAddress(8, 2)
    val byteSel = localAddress(1, 0)

    wData = VecInit(
        io.writeData(7, 0).asSInt,
        io.writeData(15, 8).asSInt,
        io.writeData(23, 16).asSInt,
        io.writeData(31, 24).asSInt
    )

    val rData = WireDefault(0.S(32.W))




    io.readData := 0.S
}


object DataMemory extends App {
    emitVerilog(new DataMemory(), Array("--target-dir", "Generated"))
}

