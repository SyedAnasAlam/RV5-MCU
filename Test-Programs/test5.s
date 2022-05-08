# Test jump

nop
nop
jal x1, jump

addi x2, x0, 5

beq x0, x0, end

jump:
	addi x3, x0, 3
    nop
    jalr x4, 0(x1)
    
end:
	addi a0, x0, 10
    ecall