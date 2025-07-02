PAS 7 r0    ; Set page to 7
LDI 255 r1  ; r1←255
STO r0 7 r1 ; Store r1 into addr. 7
REA r0 7 r7 ; Read addr. 7 into r7
PAS 2 r0    ; Set page to 2
BEL         ; Ring the bell
PAG r2      ; Store page into r2
HLT         ; Halt

; SHOULD YIELD:
; r1=255, r2=2, r7=255, page=2
