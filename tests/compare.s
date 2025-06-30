;LDI 1 r1
LDI 2 r1
LDI 1 r2
;LDI 2 r2

SUB r1 r2 r0 ; Compare r1 and r2
;BEQ halt     ; r1=r2? goto halt
;BNE halt     ; r1!=r2?
;BGT halt     ; r1>=r2?
BLT halt     ; r1<r2

loop:
	ADI 1 r7
	JMP loop ; Busy loop to indicate branch fail

halt:
	HLT      ; Halt if branch succeeds

