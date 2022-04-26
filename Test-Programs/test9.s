jal main
mv a1, a0
li a0, 10
ecall

func:
    addi x2, x0, 5
    jr	ra

main:
    addi x1, x0, 1
    addi x1, x1, 1
    addi x1, x1, 1
    call func
    addi a0, x2, 0
