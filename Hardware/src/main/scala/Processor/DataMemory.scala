import chisel3._
import chisel3.util._

class DataMemory extends Module {
    val io = IO(new Bundle {
        val address = Input(UInt(13.W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val writeData = Input(SInt(32.W))
        val funct3 = Input(UInt(3.W))
        val readData = Output(SInt(32.W))
    })

    val mem0 = Module(new BRAM(512*4, 8))
    val mem1 = Module(new BRAM(512*4, 8))
    val mem2 = Module(new BRAM(512*4, 8))
    val mem3 = Module(new BRAM(512*4, 8))
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

    val writeEnable = RegInit(false.B)
    val readEnable = RegInit(false.B)

    val a :: b :: hold :: Nil = Enum(3)
    val risingEdgeFsm = RegInit(a)
    val risingEdgeFsm2 = RegInit(a)
    switch(risingEdgeFsm) {
        is(a) {
            writeEnable := false.B
            when(!io.writeEnable) {
                risingEdgeFsm := b
            }
        }
        is(b) {
            when(io.writeEnable) {
                risingEdgeFsm := hold
                writeEnable := true.B
            }
        }
        is(hold) {
            val counter = RegInit(0.U(2.W))
            counter := counter + 1.U
            when(counter === 1.U) {
                //risingEdgeFsm := a
            }
            risingEdgeFsm := RegNext(a)
        }
    }
    switch(risingEdgeFsm2) {
        is(a) {
            readEnable := false.B
            when(!io.readEnable) {
                risingEdgeFsm2 := b
            }
        }
        is(b) {
            when(io.readEnable) {
                risingEdgeFsm2 := hold
                readEnable := true.B
            }
        }
        is(hold) {
            val counter = RegInit(0.U(2.W))
            counter := counter + 1.U
            when(counter === 1.U) {
                //risingEdgeFsm2 := a
            }
            risingEdgeFsm2 := RegNext(a)
        }
    }

    when(writeEnable) {
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

    
    when(readEnable) {
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

