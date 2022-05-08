package spi

import chisel3._

/**
 * SPI signal bundles
 * Following naming is used for SPI signals:
 * MOSI: Main-Out-Secondary-In
 * MISO: Main-In-Secondary-Out
*/


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
