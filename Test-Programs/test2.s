# Test I-format arithmetic instructions

addi x1, x0, 1241
addi x2, x0, -23

xori x3, x1, 42
ori x4, x1, -431
andi x5, x1, 663
slli x6, x1, 9
srli x7, x1, 14
srai x7, x1, 6
slti x8, x1, -2000
sltiu x9, x1, -128

addi a0, x0, 10
ecall

