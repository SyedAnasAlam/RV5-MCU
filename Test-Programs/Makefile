
test1:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c test1.s -o test1.o
	riscv64-linux-gnu-objcopy -O binary test1.o test1.bin
	rm test1.o

test2:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c test2.s -o test2.o
	riscv64-linux-gnu-objcopy -O binary test2.o test2.bin
	rm test2.o

test3:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c test3.s -o test3.o
	riscv64-linux-gnu-objcopy -O binary test3.o test3.bin
	rm test3.o

test4:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c test4.s -o test4.o
	riscv64-linux-gnu-objcopy -O binary test4.o test4.bin
	rm test4.o

test5:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c test5.s -o test5.o
	riscv64-linux-gnu-objcopy -O binary test5.o test5.bin
	rm test5.o

test6:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c test6.s -o test6.o
	riscv64-linux-gnu-objcopy -O binary test6.o test6.bin
	rm test6.o


test7:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c test7.s -o test7.o
	riscv64-linux-gnu-objcopy -O binary test7.o test7.bin
	rm test7.o

test8:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c test8.s -o test8.o
	riscv64-linux-gnu-objcopy -O binary test8.o test8.bin
	rm test8.o

test9:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c test9.s -o test9.o
	riscv64-unknown-elf-objcopy -O binary test9.o test9.bin
	rm test9.o

test10:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c test10.s -o test10.o
	riscv64-unknown-elf-objcopy -O binary test10.o test10.bin
	rm test10.o

ctest1:
	riscv64-unknown-elf-gcc -nostartfiles -nostdlib -march=rv32i -mabi=ilp32 -T ~/linker.ld ctest1.c -o ctest1.out
	riscv64-unknown-elf-objcopy -O binary ctest1.out ctest1.bin
	rm ctest1.out

loop:
	riscv64-unknown-elf-gcc -nostartfiles -nostdlib -march=rv32i -mabi=ilp32 -T ~/linker.ld loop.c -o loop.out
	riscv64-unknown-elf-gcc -nostartfiles -nostdlib -march=rv32i -mabi=ilp32 -S loop.c -o loop.s
	riscv64-unknown-elf-objcopy -O binary loop.out loop.bin
	rm loop.out

