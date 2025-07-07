; AUTHOR: toydotgame
; CREATED ON: 2025-07-07
; 16-bit calculator. Functions are + and -. TODO: * and /
; Works with operands from 0–9999. TODO: Negative operands
; Uses little-endian data storage, stores the following bytes:
; Operand 1 (2 bytes)
; Operand 2 (2 bytes)
; Operation (1 byte)
; Result    (4 bytes)
; Splash    (10 bytes)

#splash_size 10 ; bytes
.splash  "T"
.splash1 "o"
.splash2 "y"
.splash3 "C"
.splash4 "a"
.splash5 "l"
.splash6 "c"
.splash7 10  ; LF
.splash8 ">"
.splash9 "sp"

ldi splash r7       ; Get pointer
ldi splash_size r6  ; Get title size
add r7 r6 r7        ; End of pointer = pointer+size
ldi splash r6       ; Load pointer
splash_loop:
	rea r6 0 r5     ; M[ptr]→r5
	gpo r5 p0       ; Echo
	adi 1 r6        ; ptr++
	sub r6 r7 r0    ; i == end of pointer?
	bne splash_loop ; If not yet, loop

; GET OPERAND 1:
ldi 0 r5               ; Counter for digits input
operand_1_loop:
	jsr input          ; Get input
	add r7 r0 r1       ; Move input to r1
	ldi 10 r6
	sub r1 r6 r0       ; input >= 10?
	bgt save_operand_1 ; If so, not a number

	adi 1 r5           ; Increment digit count
	ldi 4 r6
	sub r5 r6 r0       ; digits = 4?
	beq save_operand_1 ; If so, finish

	jsr x10            ; Otherwise, x10 and get next
	jmp operand_1_loop
save_operand_1: ; TODO: input loop doesn't work for inputs >255

hlt

input:         ; Chucks input value into r7, echoes
	gpi p0 r7
	gpo r7 p0
	adi 208 r7 ; 2's comp. of "0" ASCII, effectively sub ASCII offset
	rts

x10:             ; r7 *= 10, also uses r6
	add r7 r7 r6 ; r1 = inBuff<<1
	add r7 r7 r7
	add r7 r7 r7
	add r7 r7 r7 ; inBuff <<= 3
	add r6 r7 r7 ; (inBuff<<1)+(inBuff<<3)→r7
	rts

