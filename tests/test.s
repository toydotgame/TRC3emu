; TRC3 example operations
; Comment 2
.helloworld 255
.label2 2
#label3 256
NOP
HLT
; Comment 3
ADD r1 r2 r3
ADI 255 r1
ADI 255 r7
ADI 15 7
adi 12 1

subroutine:
SUB r1 r2 r3
XOR r1 r2 r3
XNO r1 r2 r3
IOR r1 r2 r3
NOR r1 r2 r3
AND r1 r2 r3
NAN r1 r2 r3
RSH r1 r2    ; RSH technically an ADD, then shift
RSH r1 r2 r3
RSH r1 r2
LDI 255 r1
JMP subroutine
BEQ 1023
BNE 1023
BGT 1023
BLT 1023
CAL 1023
RET
REA r1 7 r2
STO r1 7 r2
GPI 7 r1
GPO r1 7
BEL
PAS 7        ; Multiple ways to set page
PAS r1
PAS 7 r1
PAG r1

