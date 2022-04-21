# Test R-format arithmetic instructions

addi x1, x0, 1803
addi x2, x0, -431

add x3, x1, x2
and x4, x1, x3
or x5, x2, x4
addi x4, x0, 3
sll x6, x5, x4
slt x7, x4, x6
sltu x7, x4, x6
sra x8, x6, x4
sub x9, x1, x2
xor x11, x1, x2

addi a0, x0, 10
ecall
