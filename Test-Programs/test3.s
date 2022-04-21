# Test branches

addi x2, x0, -2
bgeu x2, x0, branch
addi x1, x0, -1
beq x0, x0, exit
branch:
    addi x1, x0, 1
	addi a0, x0, 10
	ecall
exit:    
    addi x0, x0, 0
	addi a0, x0, 10
	ecall
