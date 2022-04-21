import chisel3._
import chisel3.experimental.Analog
import chisel3.util.HasBlackBoxInline

class Memory() extends BlackBox with HasBlackBoxInline {
    val io = IO(new Bundle{
        val clk = Input(Clock)
        val wen = Input(UInt(4.W))
        val addr = Input(UInt(32.W))
        val wdata = Input(SInt(32.W))
        val readEnable = Input(Bool())
        val writeEnable = Input(Bool())
        val rdata = Output(SInt(32.W))
    })

    setInline("BlockRAM.v", 
    s"""
        | module BRAM #(parameter integer WORDS = 128*16)
        | (
        |   input clk,
        |   input [3:0] wen,
        |   input [31:0] addr,
        |   input [31:0] wdata,
        |   input readEnable,
        |   input writeEnable,
        |   output reg [31:0] rdata
        | );
        |   reg [31:0] mem [0:WORDS-1]
        |   always @(posedge clk) begin
        |       if(readEnable)  rdata <= mem[addr];
        |       if(writeEnable) begin
        |           if (wen[0]) mem[addr][ 7: 0] <= wdata[ 7: 0];
        |           if (wen[1]) mem[addr][15: 8] <= wdata[15: 8];
        |           if (wen[2]) mem[addr][23:16] <= wdata[23:16];
        |           if (wen[3]) mem[addr][31:24] <= wdata[31:24];
        |       end   
        |   end
        | endmodule  
        |""".stripMargin)
}