package SPI

import chisel3._

class SPIMainPort() extends Bundle() {
    val sck = Output(Bool())
    val cs = Output(Bool())
    val mosi = Output(Bool())
    val miso = Input(Bool())
}

class SPISecondaryPort() extends Bundle() {
    val sck = Input(Bool())
    val cs = Input(Bool())
    val mosi = Input(Bool())
    val miso = Output(Bool())
}
