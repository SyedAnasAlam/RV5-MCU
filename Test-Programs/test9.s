jal x1, main

func:
    addi x2, x0, 5
    jr	x1

main:
    addi x9, x0, 1
    addi x9, x9, 1
    addi x9, x9, 1
    jal x1, func
    addi a0, x2, 0
    li a0, 10
	ecall
