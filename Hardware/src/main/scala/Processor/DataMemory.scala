import chisel3._
import chisel3.util._
import utility.RisingEdgeFsm
import utility.BRAM

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
        memory(i.U).readAddress := 0.U
        memory(i.U).writeAddress := 0.U
        memory(i.U).writeData := 0.S
    }

    val address = io.address(12, 2)
    val nextAddress = address + 1.U
    val byteSelect = io.address(1,0)

    val addr = VecInit(0.U, 0.U, 0.U, 0.U)
    addr(0) := address
    addr(1) := Mux(byteSelect === 3.U, nextAddress, address)
    addr(2) := Mux(byteSelect(1), nextAddress, address)
    addr(3) := Mux(byteSelect(1) || byteSelect(0), nextAddress, address)
    
    val writeData = VecInit(
        io.writeData(7,0).asSInt,
        io.writeData(15,8).asSInt,
        io.writeData(23,16).asSInt,
        io.writeData(31,24).asSInt
    )

    val readData = WireDefault(0.S(32.W))
    io.readData := readData

    val RisingEdgeFsmW = Module(new RisingEdgeFsm(_hold = true, _delay = false))
    RisingEdgeFsmW.io.in := io.writeEnable
    val writeEnable = RisingEdgeFsmW.io.out 

    val RisingEdgeFsmR = Module(new RisingEdgeFsm(_hold = true, _delay = false))
    RisingEdgeFsmR.io.in := io.readEnable
    val readEnable = RisingEdgeFsmR.io.out  

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
    
    def write(_column: UInt, _address : UInt,  _data: SInt) { 
        memory(_column(1,0)).writeEnable := true.B
        memory(_column(1,0)).writeAddress := _address 
        memory(_column(1,0)).writeData := _data 
    }

    def read(_column: UInt, _address : UInt): SInt = {
        memory(_column(1,0)).readEnable := true.B
        memory(_column(1,0)).readAddress := _address

        memory(_column(1,0)).readData
    }

}

