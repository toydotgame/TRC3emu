; AUTHOR: toydotgame
; CREATED ON: 2025-07-06
; 8-bit binary to 3-digit BCD converter. Prints to GPIOout port 0.
; Reserve an 8-bit word for the actual number, plus 12 bits of shift
; space (3 bytes total)

.x 104            ; Number to convert

ldi x input       ; Load address of x into r1
rea input 0 input ; Read M[x+0]→r1

hlt               ; Halt

; REGISTER LOCATIONS:
#input r1         ; Byte holding the input value
#onesTens r2      ; Byte the ones digit is held in (lo half) and tens (hi half)
#hundreds r3      ; Byte the hundreds digit is held in (lo half)

check_ones:
	ldi 15 r7          ; Load 0xF into scratch
	and onesTens r7 r6 ; onesTens&0xF→r6 (ones BCD digit)
	ldi 5 r7
	sub r6 r7 r0       ; r6 < 5?
	blt check_ones_1   ; If so, finish up and return
	adi 3 r6           ; Else, r6 += 3
	
	ldi 240 r7               ; 0xF0 mask in scratch
	and onesTens r7 onesTens ; Mask out lo half
	ior onesTens r6 onesTens ; Add our new ones digit back
check_ones_1:
	jsr shift ; Shift entire scratch space
	rts

check_tens:
	rsh onesTens r6
	rsh r6 r6
	rsh r6 r6
	rsh r6 r6        ; onesTens >> 4
	ldi 5 r7
	sub r6 r7 r0     ; r6 < 5?
	blt check_tens_1 ; If so, finish up and return
	adi 3 r6         ; Else, r6 += 3

	add r6 r6 r6
	add r6 r6 r6
	add r6 r6 r6
	add r6 r6 r6             ; r6 <<= 4
	ldi 15 r7                ; 0xF mask
	and onesTens r7 onesTens ; Mask out hi half
	ior onesTens r6 onesTens ; Add new tens digit back
check_tens_1:
	jsr shift
	rts

; NOT DONE!
check_hundreds:
	ldi 15 r7          ; Load 0xF into scratch
	and hundreds r7 r6 ; hundreds&0xF→r6
	ldi 5 r7
	sub r6 r7 r0       ; r6 < 5?
	blt check_ones_1   ; If so, finish up and return
	adi 3 r6           ; Else, r6 += 3
	
	ldi 240 r7               ; 0xF0 mask in scratch
	and onesTens r7 onesTens ; Mask out lo half
	ior onesTens r6 onesTens ; Add our new ones digit back
check_ones_1:
	jsr shift ; Shift entire scratch space
	rts

shift:
	add input input input ; Shift input byte
	blt shift_onesTens    ; If !$C, continue
	adi 1 onesTens        ; Else, carry into next byte
shift_onesTens:           ; Case not handled when onesTens=255 and we inc above, but idc
	add onesTens onesTens onesTens
	blt shift_hundreds
	adi 1 hundreds
shift_hundreds:
	add hundreds hundreds hundreds
	rts
