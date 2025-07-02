;LDI "H" r1
;GPO r1 p0
;LDI "I" r1
;GPO r1 r0
;LDI "!" r1
;GPO r1 r0

LDI 32 r1  ; Start char
LDI 126 r2 ; End char
loop:
	GPO r1 p0
	ADI 1 r1
	SUB r2 r1 r0 ; r1>126?
	BGT loop

HLT

