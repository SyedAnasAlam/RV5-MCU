	li sp, 0x1000
	jal main
	mv a1, a0
	li a0, 10
	ecall
sum:
	addi	sp,sp,-32
	sw	s0,28(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lw	a4,-20(s0)
	lw	a5,-24(s0)
	add	a5,a4,a5
	mv	a0,a5
	lw	s0,28(sp)
	addi	sp,sp,32
	jr	ra
main:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	sw	s1,20(sp)
	addi	s0,sp,32
	mv	t1,sp
	mv	s1,t1
	li	t1,2
	sw	t1,-20(s0)
	lw	t1,-20(s0)
	addi	t3,t1,-1
	sw	t3,-24(s0)
	mv	t3,t1
	mv	a6,t3
	li	a7,0
	srli	t3,a6,27
	slli	a3,a7,5
	or	a3,t3,a3
	slli	a2,a6,5
	mv	a3,t1
	mv	a0,a3
	li	a1,0
	srli	a3,a0,27
	slli	a5,a1,5
	or	a5,a3,a5
	slli	a4,a0,5
	mv	a5,t1
	slli	a5,a5,2
	addi	a5,a5,15
	srli	a5,a5,4
	slli	a5,a5,4
	sub	sp,sp,a5
	mv	a5,sp
	addi	a5,a5,3
	srli	a5,a5,2
	slli	a5,a5,2
	sw	a5,-28(s0)
	lw	a5,-28(s0)
	li	a4,1
	sw	a4,0(a5)
	lw	a5,-28(s0)
	li	a4,2
	sw	a4,4(a5)
	lw	a5,-28(s0)
	lw	a4,0(a5)
	lw	a5,-28(s0)
	lw	a5,4(a5)
	mv	a1,a5
	mv	a0,a4
	call	sum
	sw	a0,-32(s0)
	mv	sp,s1
	nop
	addi	sp,s0,-32
	lw	ra,28(sp)
	lw	s0,24(sp)
	lw	s1,20(sp)
	addi	sp,sp,32
	jr	ra
