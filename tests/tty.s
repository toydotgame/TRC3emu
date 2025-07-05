; AUTHOR: toydotgame
; CREATED ON: 2025-07-05
; Test to check that every single GPIO port works well in the emulator.
; First echoes the terminal number to each GPIO output port, then goes
; into an indefinite loop for each terminal (that mirrors gpio.s). If
; the character "%" is input, the machine halts

ldi "0" r1       ; ADIs henceforth will be offsets of this ASCII value

gpo r1 p0
adi 1 r1
gpo r1 p1        ; GPIO ports are immediate-addressed, so we gotta do this manually
adi 1 r1
gpo r1 p2
adi 1 r1
gpo r1 p3
adi 1 r1
gpo r1 p4
adi 1 r1
gpo r1 p5
adi 1 r1
gpo r1 p6
adi 1 r1
gpo r1 p7

ldi "%" r2       ; Halt on % input
echo_loop:
	jsr echo_tty0
	jsr echo_tty1
	jsr echo_tty2
	jsr echo_tty3
	jsr echo_tty4
	jsr echo_tty5
	jsr echo_tty6
	jsr echo_tty7
	jmp echo_loop

halt:
	hlt

echo_tty0:
	gpi p0 r1    ; p0→r1
	gpo r1 p0    ; Echo
	sub r1 r2 r0 ; r1="%"?
	beq halt     ; If so, halt
	rts          ; Else,  return from subroutine

echo_tty1:
	gpi p1 r1    ; p0→r1
	gpo r1 p1    ; Echo
	sub r1 r2 r0 ; r1="%"?
	beq halt     ; If so, halt
	rts          ; Else,  return from subroutine

echo_tty2:
	gpi p2 r1    ; p0→r1
	gpo r1 p2    ; Echo
	sub r1 r2 r0 ; r1="%"?
	beq halt     ; If so, halt
	rts          ; Else,  return from subroutine

echo_tty3:
	gpi p3 r1    ; p0→r1
	gpo r1 p3    ; Echo
	sub r1 r2 r0 ; r1="%"?
	beq halt     ; If so, halt
	rts          ; Else,  return from subroutine

echo_tty4:
	gpi p4 r1    ; p0→r1
	gpo r1 p4    ; Echo
	sub r1 r2 r0 ; r1="%"?
	beq halt     ; If so, halt
	rts          ; Else,  return from subroutine

echo_tty5:
	gpi p5 r1    ; p0→r1
	gpo r1 p5    ; Echo
	sub r1 r2 r0 ; r1="%"?
	beq halt     ; If so, halt
	rts          ; Else,  return from subroutine

echo_tty6:
	gpi p6 r1    ; p0→r1
	gpo r1 p6    ; Echo
	sub r1 r2 r0 ; r1="%"?
	beq halt     ; If so, halt
	rts          ; Else,  return from subroutine

echo_tty7:
	gpi p7 r1    ; p0→r1
	gpo r1 p7    ; Echo
	sub r1 r2 r0 ; r1="%"?
	beq halt     ; If so, halt
	rts          ; Else,  return from subroutine
