package MemoryController

import chisel3._
import chisel3.util._
import java.nio.file.{Files, Paths}
import MemoryController.FlashCommands._

class FlashModel(count: Int, app: String) extends Module {
    val io = IO(new Bundle {
        val cs = Input(Bool())
        val mosi = Input(Bool())
        val miso = Output(Bool())
        val sck = Input(Bool())

/*         val debug_counter = Output(UInt((log2Ceil(count + 1).W)))
        val debug_state = Output(UInt(2.W))
        val debug_index = Output(UInt(24.W))
        val debug_command = Output(UInt(8.W))
        val debug_address = Output(UInt(24.W))
        val debug_subState = Output(UInt(2.W))
        val debug_data = Output(UInt(32.W)) */
    })
    
    val program = Files.readAllBytes(Paths.get(app))    
    val memory = VecInit(program.map(_.S(8.W)))

    val counter = RegInit(0.U(log2Ceil(count + 1).W))
    val max = (count - 1).U
    counter := counter + 1.U
    when(counter === max) {
        counter := 0.U
    }


    val idle :: receiveInstruction :: commandLookup :: readCmd :: Nil = Enum(4)
    val state = RegInit(idle)
    val command = RegInit(0.U(8.W))
    val index = RegInit(0.U(24.W))
    val address = RegInit(1.U(24.W))
    val data = WireDefault(0.S(8.W))
    val subState = RegInit(0.U(2.W))
    val miso = RegInit(false.B)
    io.miso := miso

    switch(state) {
        is(idle) {   
            index := 0.U
            miso := false.B
            when(~io.cs) {
                state := receiveInstruction
            }
        }
        is(receiveInstruction) {
            when(io.cs) { state := idle }
            when(~io.sck && counter === max) {
                index := index + 1.U
                command := Cat(command, io.mosi.asUInt)

                when(index === 7.U) {
                    index := 0.U
                    state := commandLookup
                }
            }
        }
        is(commandLookup) {
            when(io.cs) { state := idle }
            switch(command) {
                is(READ_CMD) { state := readCmd }
            }
        }
        is(readCmd) {
            when(io.cs) { state := idle }
            switch(subState) {
                is(0.U) {
                    when(~io.sck && counter === max) {
                        index := index + 1.U
                        address := Cat(address, io.mosi.asUInt)
                        when(index === 23.U) {
                            index := 0.U                          
                            subState := 1.U
                        }
                    }
                }
                is(1.U) {
                    data := memory(address)
                    when(io.sck && counter === max) {
                        index := index + 1.U
                        miso := data(7.U - index)
                        when(index === 7.U) {
                            index := 0.U
                            address := address + 1.U
                        }                         
                    }
                }
            }
        }
    }
/*     io.debug_command := command
    io.debug_counter := counter
    io.debug_index := index
    io.debug_state := state.asUInt
    io.debug_address := address
    io.debug_subState := subState
    io.debug_data := data.asUInt */
}