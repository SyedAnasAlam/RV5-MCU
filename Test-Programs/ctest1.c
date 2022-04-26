// This is our minimal startup code (usually in _start)
asm("li sp, 0x1000"); 	// SP set to 4kB
asm("jal main");        // call main
asm("mv a1, a0");       // save return value in a1
asm("li a0, 10");       // prepare ecall exit
asm("ecall");           // now your simlator should stop

int sum(int a, int b)
{
	return a + b;
}

void main(void) 
{
	int len = 2;
	int arr[len];
    arr[0] = 1;
    arr[1] = 2;
	int _sum = sum(arr[0], arr[1]);
}
