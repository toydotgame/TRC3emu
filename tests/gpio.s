LDI "%" r2

loop:
GPI p0 r1    ; p0→r1
GPO r1 p0    ; Echo
SUB r1 r2 r0 ; r1="%" ASCII?
BNE loop     ; If so, loop
; Else, halt:
HLT

