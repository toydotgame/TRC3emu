LDI 37 r2 ; "%" ASCII value
loop:
GPI 0 r1
GPO r1 0
SUB r1 r2 r0
BNE loop
; Else, halt
HLT

