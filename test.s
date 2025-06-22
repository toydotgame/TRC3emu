; TRC3 example operations
NOP
HLT
ADD r1 r2 r3
ADI 255 r1
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
JMP 1023
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

