import chisel3._
import chisel3.util._

class BRAM extends Module {
    val io = IO(new Bundle {
        val address = Input(UInt(11.W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(8.W))
        val readData = Output(SInt(8.W))
    })
    
    val BRAM = SyncReadMem(512*4, SInt(8.W))

    io.readData := 0.S
    when(io.readEnable) {
        io.readData := BRAM(io.address)
    }

    when(io.writeEnable) {
        BRAM.write(io.address, io.writeData)
    }
}

class DataMemory extends Module {
    val io = IO(new Bundle {
        val address = Input(UInt(13.W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(32.W))
        val funct3 = Input(UInt(3.W))
        val readData = Output(SInt(32.W))
    })

    val mem0 = Module(new BRAM())
    val mem1 = Module(new BRAM())
    val mem2 = Module(new BRAM())
    val mem3 = Module(new BRAM())
    val memory = VecInit(mem0.io, mem1.io, mem2.io, mem3.io)

    var i, j = 0
    for(i <- 0 until 4) {
        memory(i.U).writeEnable := false.B
        memory(i.U).readEnable := false.B
        memory(i.U).address := 0.U
        memory(i.U).writeData := 0.S
    }

    val address = io.address(12, 2)
    val nextAddress = address + 1.U
    val byteSelect = io.address(1,0)

    val writeData = VecInit(
        io.writeData(7,0).asSInt,
        io.writeData(15,8).asSInt,
        io.writeData(23,16).asSInt,
        io.writeData(31,24).asSInt
    )

    val readData = WireDefault(0.S(32.W))
    io.readData := readData

    when(io.writeEnable) {
        switch(io.funct3) {
            is(0.U) {
                write(byteSelect, address, writeData(0))
            }
            is(1.U) {
                write(byteSelect, address, writeData(0))
                write(byteSelect + 1.U, Mux(byteSelect === 3.U, nextAddress, address), writeData(1))
            }
            is(2.U) {
                write(byteSelect, address, writeData(0))
                write(byteSelect + 1.U, Mux(byteSelect === 3.U, nextAddress, address), writeData(1))
                write(byteSelect + 2.U, Mux(byteSelect(1), nextAddress, address), writeData(2))
                write(byteSelect + 3.U, Mux(byteSelect(1) || byteSelect(0), nextAddress, address), writeData(3))
            }
        }
    }

    
    when(io.readEnable) {
        switch(io.funct3) {
            is(0.U) {
                readData := read(byteSelect, address)
            }
            is(1.U) {
                readData := (read(byteSelect + 1.U, Mux(byteSelect === 3.U, nextAddress, address)) ##
                                read(byteSelect, address)).asSInt
            }
            is(2.U) {
                readData := ((read(byteSelect + 3.U, Mux(byteSelect(1) || byteSelect(0), nextAddress, address)) ## 
                                 read(byteSelect + 2.U, Mux(byteSelect(1), nextAddress, address)))     ##
                                (read(byteSelect + 1.U, Mux(byteSelect === 3.U, nextAddress, address)) ##
                                 read(byteSelect, address))).asSInt
            }
            is(4.U) {
                readData :=  (0.S(24.W) ## read(byteSelect, address)).asSInt
            }
            is(5.U) {
                readData :=  (0.U(16.W) ##
                                (read(byteSelect + 1.U, Mux(byteSelect === 3.U, nextAddress, address)) ##
                                read(byteSelect, address))).asSInt 
            }
        }
    }
    
    def write(column: UInt, address : UInt,  data: SInt) { 
        memory(column(1,0)).writeEnable := true.B
        memory(column(1,0)).address := address 
        memory(column(1,0)).writeData := data 
    }

    def read(column: UInt, address : UInt): SInt = {
        memory(column(1,0)).readEnable := true.B
        memory(column(1,0)).address := address

        memory(column(1,0)).readData
    }

}


object DataMemory extends App {
    emitVerilog(new DataMemory(), Array("--target-dir", "Generated"))
}

