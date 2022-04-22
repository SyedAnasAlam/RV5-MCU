lui x1, 0xAA55A
lui x2, 0xA55
srli x2, x2, 12
or x3, x1, x2

addi x4, x0, 10

sw x3, 0(x4)

lbu x5, 0(x4)
lbu x6, 1(x4)
lbu x7, 2(x4)
lbu x8, 3(x4)
addi x9, x0, 421
lbu x11, 0(x9)

addi a0, x0, 10
ecall
