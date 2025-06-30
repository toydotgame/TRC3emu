LDI 10 r2

init:
ADI 255 r2   ; Decrement counter
CAL foo

SUB r2 r0 r0 ; 10=0?
BEQ halt     ; If so, halt
JMP init     ; Else, loop

foo:
	ADI 1 r1
	RET

halt:
	HLT

