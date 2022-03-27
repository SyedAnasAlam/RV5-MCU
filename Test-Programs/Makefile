cc = riscv64-linux-gnu-gcc
c_flags = -O3 -march=rv32i -mabi=ilp32

assembly_files = $(wildcard *.s) 
obj_files = $(assembly_files:.s=.o)
bin_files = $(assembly_files:.s=.bin)


all: $(bin_files) clear

$(bin_files): $(obj_files)
	riscv64-linux-gnu-objcopy -O binary $< $@

$(obj_files): $(assembly_files)
	$(cc) $(c_flags) -c $< -o $@

clear:
	rm -rf *.o

clean:
	rm -rf *.o *.bin
