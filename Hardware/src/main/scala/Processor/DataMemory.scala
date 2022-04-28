import chisel3._
import chisel3.util._

class BRAM extends Module {
    val io = IO(new Bundle {
        val address = Input(UInt(32.W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(8.W))
        val readData = Output(SInt(8.W))
    })
    
    val BRAM = SyncReadMem(512*20, SInt(8.W))

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
        val address = Input(UInt(32.W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(32.W))
        val funct3 = Input(UInt(3.W))
        val readData = Output(SInt(32.W))
        val dataValid = Output(Bool())
    })

    val mem = Module(new BRAM())
    mem.io.address := 0.U
    mem.io.readEnable := false.B
    mem.io.writeEnable := false.B
    mem.io.writeData := 0.S
    
    val writeData = VecInit(
        io.writeData(7,0).asSInt,
        io.writeData(15,8).asSInt,
        io.writeData(23,16).asSInt,
        io.writeData(31,24).asSInt
    )


    val readData = Wire(Vec(4, SInt(8.W)))
    readData(0) := 0.S
    readData(1) := 0.S
    readData(2) := 0.S
    readData(3) := 0.S
    
    val idle :: write :: read :: Nil = Enum(3)
    val state = RegInit(idle)
    val counter = RegInit(0.U(2.W))
    val nBytes = WireDefault(0.U)

    io.readData := 0.S
    io.dataValid := false.B
    switch(state) {
        is(idle) {
            io.dataValid := false.B

            mem.io.address := 0.U
            mem.io.readEnable := false.B
            mem.io.writeEnable := false.B
            mem.io.writeData := 0.S      
            
            counter := 0.U
       
            switch(io.funct3) {
                is(0.U) { nBytes := 1.U }
                is(1.U) { nBytes := 2.U }
                is(2.U) { nBytes := 4.U }
            }

            when(io.writeEnable) {
                state := write
            }
            when(io.readEnable) {
                state := read
            }
        }
        is(write) {
            counter := counter + 1.U

            mem.io.writeEnable := true.B
            mem.io.address := io.address + counter 
            mem.io.writeData := io.writeData(7, 0).asSInt
            
            when(counter === nBytes) {
                state := idle
            }
            
        }
        is(read) {
            counter := counter + 1.U

            mem.io.readEnable := true.B
            mem.io.address := io.address + counter
            readData(counter) := mem.io.readData

            when(counter === 4.U) {
                io.dataValid := true.B
                state := idle
            }

            switch(io.funct3) {
                is(0.U) { io.readData := readData(0) }
                is(1.U) { io.readData := (readData(1) ## readData(0)).asSInt }
                is(2.U) { io.readData := ((readData(3) ## readData(2)) ## (readData(1) ## readData(0))).asSInt }
                is(4.U) { io.readData := (0.S(24.W) ## readData(0)).asSInt }
                is(5.U) { io.readData := (0.U(16.W) ## (readData(1) ## readData(0))).asSInt }
            }
        }
    }
}


object Memory extends App {
    emitVerilog(new DataMemory(), Array("--target-dir", "Generated"))
}

