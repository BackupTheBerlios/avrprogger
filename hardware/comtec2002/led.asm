;; Ansteuerung einer 7-Segment-LED-Anzeige (4 Digits)
;; Copyright (C) 2002 Holger Dietze
     
;;   This program is free software; you can redistribute it and/or modify
;;   it under the terms of the GNU General Public License as published by
;;   the Free Software Foundation; either version 2 of the License, or
;;   (at your option) any later version.
;;   
;;   This program is distributed in the hope that it will be useful,
;;   but WITHOUT ANY WARRANTY; without even the implied warranty of
;;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;   GNU General Public License for more details.
;;   
;;   You should have received a copy of the GNU General Public License
;;   along with this program; if not, write to the Free Software
;;   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111, USA.

	.include "4433def.inc"

	;; Interrupt-Tabelle
	rjmp reset 		; Reset Handler
	reti			; IRQ0
	reti			; IRQ1
	reti			; Timer1 Capture
	reti			; Timer1 compare
	reti			; Timer1 Overflow
	rjmp next_digit		; Timer0 Overflow
	reti			; SPI transfer complete
	rjmp receive		; UART RX complete
	reti 			; UDR empty
	reti			; UART TX complete
	reti 			; ADC conversion complete
	reti 			; EEPROM ready
	reti			; Analog comparator

	;; PD7 - Segment a f
	;; PD6 -         b e
	;; PD5 -         c d
	;; PD4 -         d c
	;; PD3 -         e b
	;; PD2 -         f a
	;; PB1 -         g g
	;; PB0 -         . .
	;; WARNING! this is not the official naming
	;; of the segments
	;;
	;; PC2 - Digit 0
	;; PC3 -       1
	;; PC4 -       2
	;; PC5 -       3
	
	.equ DIGSTART = 0x60
	.equ DIGEND  = 0x63
	.equ ram_hextab = 0x70
	.def temp = r16
	.def port_B = r15
	.def dig_index = r20
	.def dig_bits = r21
	.def binL = r22
	.def binH = r23
	.def data = r17
	.equ quartz = 8000000
	.equ baud=9600
	
reset:
	ldi temp, RAMEND
	out SP, temp

	;; Timer 0 vorbereiten
	ldi temp, (1 << TOIE0)	; Timer0 Overflow enable
	out TIMSK, temp
	clr temp
	out TCNT0, temp
	ldi temp, 0b00000011
	out TCCR0, temp

	;; Ports vorbereiten
	ldi temp, 0xfc 		; Port D 6 hoechste Bits Ausgabe
	out DDRD, temp
	ldi temp, 0x0F 		; Port B 4 tiefste Bits Ausgabe
	out DDRB, temp
	ldi temp, 0x3c		; Port C Bits 2-5 Ausgabe
	out DDRC, temp

	;; Initialwert Port B
	ldi temp, 0x0f
	out PORTB, temp
	
	;; ADC nutzen
	ldi temp, 0		; ADC0 auswaehlen
	out ADMUX, temp
	ldi temp, 0b11100111	; ADEN, ADSC setzen, Vorteiler 128
	out ADCSR, temp

	;; UART einschalten
	sbi UCSRB, RXCIE
	sbi UCSRB, RXEN
	sbi UCSRB, TXEN
	ldi temp, quartz / (baud*16) - 1 
	out UBRR, temp
	
	;; Register fuer fortlaufende Ausgabe setzen
	ldi YL, DIGSTART 	; Start der Digits
	clr YH
	ldi dig_index, 0	; Zaehler
	ldi dig_bits, (1<<2)	; Schieberegister fuer Auswahl des Digits
	ser temp
	mov port_b, temp
	
	;; Digits alle setzen (Test)
	ldi r26, DIGSTART
	ldi temp, 0b01110001
	st X+, temp
	ldi temp, 0b11100110
	st X+, temp
	ldi temp, 0b00111111
	st X+, temp
	ldi temp, 0b00100111
	st X+, temp
	
	sei

	ldi temp, 32
loop:
	dec r2
	brne loop
	dec r3
	brne loop
	dec temp
	brne loop

	in binL, ADCL
	in binH, ADCH
	rcall convert
	ldi temp, 16
	rjmp loop

transmit:
	sbis UCSRA, UDRE
	rjmp transmit
	out UDR, temp
	ret
	
receive:
	push temp
	push data
	in data, UDR

	cpi data, 'e'
	brne recv1
	ldi temp, ~(1<<2)
	and port_b,temp
	rjmp recvecho

recv1:
	cpi data, 'a'
	brne recv2
	ldi temp, (1<<2)
	or port_b, temp
	rjmp recvecho

recv2:
	rcall transmit
	ldi temp, '?'
	
recvecho:	
	mov temp, data
	rcall transmit
recvdone:	
	ldi temp, 13
	rcall transmit
	ldi temp, 10
	rcall transmit
	pop data
	pop temp
	reti

next_digit:
	push temp
	push r18
	push r17
	;; kein Digit anwaehlen
	clr temp
	out PORTC, temp

	;; naechstes Digit holen
	ld temp, Y+
	mov r18, temp
	andi r18, 3
	mov r17, port_b
	andi r17, 0xfc
	or r18, r17
	mov port_b, r18
	out PORTB, port_b
	ori temp, 0x3
	out PORTD, temp

	;; Digit scharf schalten
	out PORTC, dig_bits

	lsl dig_bits
	inc dig_index
	cpi dig_index,4
	brlt done
	ldi r28, DIGSTART
	ldi dig_bits, (1<<2)
	clr dig_index
done:
	pop r17
	pop r18
	pop temp
	reti

convert:
	rcall convtemp
	;; Motor regeln
	;; Temperatur steht in binH,binL in dezimal
	ldi temp, (1<<2)
	sbrs port_b, 2
	rjmp convert1
	;; Motor ist aus -> Vergleich mit oberem Schwellwert
	cpi binH, 2
	brlt convert2
	brne convert_on
	cpi binL, 0x35
	brcs convert2
convert_on:
	com temp
	and port_b, temp
	rjmp convert2
convert1:
	;; Motor ist an -> Vergleich mit unterem Schwellwert
	cpi binH, 2
	breq convert_off1
	brge convert2
	rjmp convert_off
convert_off1:
	cpi binL, 0x30
	brcc convert2
convert_off:
	or port_b, temp

convert2:
	;; Warnton ausgeben
	ldi temp, (1<<3)
	cpi binH, 3
	brcs convert3
	com temp
	and port_b, temp
	rjmp convert4
convert3:
	or port_b, temp
convert4:
	
	;; auf LED ausgeben
	ldi r26,DIGSTART+1
	mov temp, binL
	andi temp, 0x0f
	clr data
	rcall convdig
	mov temp, binL
	swap temp
	andi temp, 0xf
	ser data
	rcall convdig
	clr data
	mov temp, binH
	andi temp, 0x0f
	rcall convdig

	ret

hextab1:	
	.db 0b00000011, 0b11100111		; 0 1
	.db 0b10010001, 0b11000001		; 2 3
	.db 0b01100101, 0b01001001		; 4 5
	.db 0b00001001, 0b11100011		; 6 7
	.db 0b00000001, 0b01100001		; 8 9
	.db 0b00100001, 0b00001101		; A b
	.db 0b00011011, 0b10000101		; C d
	.db 0b00011001, 0b00111001		; E F

convdig:
	ldi ZL, low(hextab1 << 1)
	ldi ZH, high(hextab1 << 1)
	clr r1
	add ZL, temp
	adc ZH, r1
	lpm

	sbrc data, 0
	dec r0
	
	st X+, r0
	ret

	;; Kalibrierung, bei anderem Heissleiter oder Widerstand
	;; wiederholen!!!
	;; 204  24
	;; 205  23
	;; 1ae  34
	;; 2d0  00?

;	T= T0+ (T1-T0)*(v-v0)/(v1-v0)
	;; T = 24/(-0xcc) * (v-0x204)
	.equ subtract = 0x2d0
	.equ multiply = 256 * 24 / (0x204 - 0x2d0)
convtemp:
	;; In:	 binL, binH ADC-Ergebnis
	;; Out:	 binL, binH Temperatur
	ldi r18, low(subtract)
	ldi r19, high(subtract)
	sub r18, binL
	sbc r19, binH
	
	ldi temp, low(multiply)
	mov r4, temp
	ldi temp, high(multiply)
	mov r5, temp

	clr binL
	clr binH
	ser temp
	
multloop:
	tst r4
	brne multloop1
	tst r5
	breq multloop2
multloop1:
	add binL, r18
	adc binH, r19
	
	sub r4, temp
	sbc r5, temp


	rjmp multloop

multloop2:
	;; fuer Ausgabe aufbereiten
	mov data, binH
	rcall hex2dec
	swap binL
	andi binL, 0x0f
	mov r1, binL
	lsr r1
	lsr r1
	add binL, r1
	lsr binL
	andi binL, 0x0f
	
	mov binH, r0
	swap r0
	swap binH
	mov temp, r0
	andi temp, 0xf0
	or binL,temp
	andi binH, 0x0f
	ret

hex2dec:
	clr r0
h2d1:	subi data, 10
	brlt h2d2
	inc r0
	rjmp h2d1
h2d2:
	swap r0
	subi data, -10
	or r0, data
	ret
	
