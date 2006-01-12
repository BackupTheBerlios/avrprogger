;------------------------------------------------------------------------------
; progger.asm:  programmer for programming AVR flash and EEPROM
; 
; author:    Gerhard Paulus
; company:   GNOM SOFT GmbH, Dresden
; date:      August 2002
;
; $Id: progger.asm,v 1.3 2006/01/12 23:14:23 hdietze Exp $
; $Changelog$
;
; This software implements the programming protocoll specified in 
; Atmels application note avr910.asm dated May 2000.
; It is coded for the AT90S4433/2333 and uses hardware UART and SPI. 
; Currently nothing specific is coded in the main routine.
; 
; Flash programming usually writes byte by byte directly to flash.
; If any of the Megas is selected the faster Page Write Mode is used
; (bytes are written first to a page which is flushed later in one go).
; All things related to fuses and locks are not implemented.
; 
; The idea is to have the programming AVR (P) and the target AVR (T) 
; on one breadboard. If both are AT90S4433 then the schema looks like this:
; 
;         P:                      T:
;     ........               .......... 
; 
;     PB5   19 ------------- 19    SCK      (Serial Clock, Taktgeber)
;     PB4   18 ------------- 18    MISO     (Master In Slave Out)
;     PB3   17 ------------- 17    MOSI     (Master Out Slave In)
;                                                   
;     PB0   14 -------------  1    RESET
; 
; P and T share supply voltage, ground and quartz input.
; 
; As client software you can use AVRProg.exe of Atmels AVRStudio. 
; 
; 
; If you change something in the code please take care of 'quartz' and 'burncounter' 
;------------------------------------------------------------------------------

; // TODO  add watchdog ! 

.include "4433def.inc"
.def temp1 = r16
.def temp2 = r17
.def temp3 = r18
.def data  = r19
.def lowAddress  = r20  ; address low
.def highAddress = r21  ; address high
.def memory   = r22  ; memory byte
.def device   = r23  ; device code
.def pagemode   = r24  ; write page mode
.def readmode   = r25  ; 
.def command   = r26  ; 
; .equ quartz = 3686400 ; for Standard STK500 
.equ quartz = 8000000 ; for 8 MHz 
.equ burnCounter = 40 ; 
.equ baud = 19200 ; Baudrate

.equ	tn12	= 0x55
.equ	tn15	= 0x56
.equ	S1200D	= 0x13
.equ	S2313A	= 0x20
.equ	S4414A	= 0x28
.equ	S8515A	= 0x38
.equ	S2323A	= 0x48
.equ	S2343A	= 0x4C
.equ	S2333A	= 0x34
.equ	S4433A	= 0x30
.equ	S4434	= 0x6C
.equ	S8535	= 0x68
.equ	m83	= 0x65
.equ	m161	= 0x60
.equ	m163	= 0x64
.equ	m603	= 0x42
.equ	m103	= 0x41


;**** Revision Codes ****

.equ	SW_MAJOR = '2'		; Major SW revision number
.equ	SW_MINOR = '2'		; Minor SW revision number
.equ	HW_MAJOR = '1'		; Major HW revision number
.equ	HW_MINOR = '0'		; Minor HW revision number


rjmp reset           ; Reset Handler
reti                 ; IRQ0 Handler
reti								 ; IRQ1 Handler
reti                 ; Timer1 Capture Handler
reti                 ; Timer1 compare Handler
reti                 ; Timer1 Overflow Handler
reti                 ; Timer0 Overflow Handler
reti                 ; SPI Transfer Complete Handler
rjmp receive         ; UART RX Complete Handler : RXCIE
reti                 ; UDR Empty Handler
reti                 ; UART TX Complete Handler
reti                 ; ADC Conversion Complete Interrupt Handler
reti                 ; EEPROM Ready Handler
reti                 ; Analog Comparator Handler

reset:               ; main program starts here
	ldi temp1, RAMEND   
	out SP, temp1	; set stack pointer

	sbi UCSRB, RXCIE  ; enable receive completed interrupt 
	sbi UCSRB, TXEN   ; enable transmit
	sbi UCSRB, RXEN   ; enable receive
	ldi temp1, quartz / (baud * 16) - 1
	out UBRR, temp1    ; BAUD Rate 9600 

	ldi pagemode, 0x00 ; no page write mode (default is byte write mode)
	sei ; interrupts generell aktivieren
  
main:

	loop:
		rjmp loop 



;--------------------------------------------------
receive:

	in data, UDR  ; this automatically clears the RXC flag
	mov command, data

	cpi data, 0x1B   ; Esc
	brne command1
	reti

	;* | Enter programming mode            | 'P' |       |      | 13d |   1  |
	command1:   
		cpi data, 'P'
		brne command1a

		ldi temp1, 0xFF
		out DDRB, temp1
		cbi DDRB, 4  ; MISO 
		; sbi DDRB, 0  ;  PB0 output
		sbi PORTB, 0 ;  target RESET low -> start programming
		ldi temp1, 0xFF
		rcall delay
		cbi PORTB, 0 ;  target RESET low -> start programming
		ldi temp1, 0xFF
		rcall delay
		sbi SPCR, MSTR  ; dieser MC ist jetzt SPI Master

		sbi SPCR, SPR0  ; slow speed
		sbi SPCR, SPR1  ; slow speed
										;  (SPCR = SPI Control Register)
										;  SPR1    SPR0
										;   0       0   Quartz / 4
										;   0       1   Quartz / 16
										;   1       0   Quartz / 64
										;   1       1   Quartz / 128

		sbi SPCR, SPE  ;  aktiviere SPI

		; reset address:
		ldi lowAddress, 0
		ldi highAddress, 0

		ldi data, 0xAC  
		rcall transfer  ; #1
		ldi data, 0x53  
		rcall transfer  ; #2
		rcall transfer  ; #3
		rcall transfer  ; #4  
		ldi data, 13  ; CR
		rcall transmit
		reti

	;* | Leave programming mode            | 'L' |       |      | 13d |   5  |
	command1a:
		cpi data, 'L'
		brne command1b

		sbi PORTB, 0 ;  target RESET high  -> end programming
		sbi PORTB, 1 ;  LED on pin 1 off
		cbi DDRB, 0  ;  PB0 input
		; ldi temp1, 0x00
		; out DDRB, temp1 ; make all pins input // TODO  ? 
		; cbi DDRB, 1  ;  PB1 input
		ldi pagemode, 0x00 ; no page write mode (default is byte write mode)
		ldi data, 13  ; CR
		rcall transmit
		reti


	;* | Report autoincrement address      | 'a' |       | 	    | 'Y' |      |
	command1b: 
		cpi	data,'a'	
		brne	command2

		ldi	data,'Y'	
		rcall transmit
		reti

	;* | Set page mode            | 'M' |      |      | 13d |    |
	command2:
		cpi data, 'M'
		brne command2a

		ldi pagemode, 0x01 ; page write mode
		ldi data, 13  ; CR
		rcall transmit
		reti

		
	;* | Set address                       | 'A' | ah al |      | 13d |   2  |
	command2a:
		cpi data, 'A'
		brne command3

		rcall receiveMore
		mov highAddress, data
		rcall receiveMore
		mov lowAddress, data
		ldi data, 13  ; CR
		rcall transmit
		reti

	
	;* | Write program memory, low byte    | 'c' |    dd |      | 13d |   3  |
	command3:
		cpi data, 'c'
		brne command4

		ldi readmode, 0x20  ; read low byte 
		rcall receiveMore
		mov memory, data

		ldi data, 0x40  
		rcall transfer  ; #1
		mov data, highAddress  
		rcall transfer  ; #2
		mov data, lowAddress  
		rcall transfer  ; #3
		mov data, memory  
		rcall transfer  ; #4  

		rjmp waitFlash

	;* | Write program memory, high byte   | 'C' |    dd |      | 13d |   3  |
	command4:
		cpi data, 'C'
		brne command4a

		ldi readmode, 0x28  ; read high byte  
		rcall receiveMore
		mov memory, data

		ldi data, 0x48  
		rcall transfer  ; #1
		mov data, highAddress  
		rcall transfer  ; #2
		mov data, lowAddress  
		rcall transfer  ; #3
		mov data, memory  
		rcall transfer  ; #4  

		waitFlash:

		cpi pagemode, 0x01		
		breq nowait
		cpi device, m83		
		breq nowait
		cpi device, m161		
		breq nowait
		cpi device, m163		
		breq nowait
		cpi device, m603		
		breq nowait
		cpi device, m103		
		breq nowait

		cpi memory, $FF  ;  polling does not work for FF
		breq pause
		rcall waitFF  ; poll for not FF
		rjmp nowait
		pause:
		ldi temp1, burnCounter ; wait until burning has completed
		rcall delay
		rjmp nowait

		nowait:

		cpi command, 'C'  ; if high byte written then increment address
		brne endwrite
		; autoincrement address
		ldi temp1, 1
		clr temp2
		add lowAddress, temp1
		adc highAddress, temp2

		endwrite:
		; for faster comm ports maybe better at end of function
		ldi data, 13  ; CR  
		rcall transmit
		reti


	;* | Issue Page Write                  | 'm' |       |      | 13d |      |
	command4a:
		cpi data, 'm'
		brne command5

		ldi	data,0x4c	
		rcall transfer  ; #1
		mov data, highAddress  
		rcall transfer  ; #2
		mov data, lowAddress  
		rcall transfer  ; #3
		ldi data, 0  
		rcall transfer  ; #4  

		ldi readmode, 0x28  ; read high byte  
		rcall waitFF

		; rcall pagedelay

		; ldi temp1, 0xFF
		; rcall delay

		ldi data, 13  ; CR
		rcall transmit
		reti


	;* | Read program memory               | 'R' |       |dd(dd)|     |   4  |
	command5:
		cpi data, 'R'
		brne command6

		ldi data, 0x28  ; read high byte  
		rcall transfer  ; #1
		mov data, highAddress  
		rcall transfer  ; #2
		mov data, lowAddress  
		rcall transfer  ; #3
		ldi data, 0x00  
		rcall transfer  ; #4  
		rcall transmit

		ldi data, 0x20  ; read low byte 
		rcall transfer  ; #1
		mov data, highAddress  
		rcall transfer  ; #2
		mov data, lowAddress  
		rcall transfer  ; #3
		ldi data, 0x00  
		rcall transfer  ; #4  
		rcall transmit

		; autoincrement address
		ldi temp1, 1
		clr temp2
		add lowAddress, temp1
		adc highAddress, temp2

		reti

	;* | Write data memory                 | 'D' |    dd |      | 13d |      |
	command6:
		cpi data, 'D'
		brne command7

		mov memory, data
		ldi data, 0xC0  
		rcall transfer  ; #1
		mov data, highAddress  
		rcall transfer  ; #2
		mov data, lowAddress  
		rcall transfer  ; #3
		mov data, memory  
		rcall transfer  ; #4  
		ldi data, 13  ; CR
		rcall transmit
		ldi temp1, burnCounter ; wait until burnCountering has completed
		rcall delay
		ldi temp1, burnCounter ; wait until burnCountering has completed
		rcall delay
		; autoincrement address
		ldi temp1, 1
		clr temp2
		add lowAddress, temp1
		adc highAddress, temp2
		reti


	;* | Read data memory                  | 'd' |       |   dd |     |      |
	command7:
		cpi data, 'd'
		brne command8

		ldi data, 0xA0  
		rcall transfer  ; #1
		mov data, highAddress  
		rcall transfer  ; #2
		mov data, lowAddress  
		rcall transfer  ; #3
		ldi data, 0
		rcall transfer  ; #4  
		; EEPROM byte is already in data
		rcall transmit
		; autoincrement address
		ldi temp1, 1
		clr temp2
		add lowAddress, temp1
		adc highAddress, temp2
		reti


	;* | Chip erase                        | 'e' |       |      | 13d |      |
	command8:
		cpi data, 'e'
		brne command9

		ldi data, 0xAC  
		rcall transfer  ; #1
		ldi data, 0x80  
		rcall transfer  ; #2
		rcall transfer  ; #3
		rcall transfer  ; #4  
		ldi temp1, burnCounter ; wait until erasing has completed
		rcall delay
		ldi temp1, burnCounter ; wait until erasing has completed
		rcall delay
		ldi temp1, burnCounter ; wait until erasing has completed
		rcall delay
		ldi data, 13  ; CR
		rcall transmit
		reti
		


	;* | Write lock bits                   | 'l' |    dd |      | 13d |      |
	command9:
		cpi data, 'l'
		brne command10

		rcall receiveMore
		mov memory, data
		ldi data, 0xAC
		rcall transfer
		ldi data, 0xE0
		rcall transfer
		rcall transfer
		mov data, memory
		rcall transfer
		ldi data, 13
		rcall transmit
		reti

	;* | Write fuse bits                   | 'f' |    dd |      | 13d |  11  |
	command10:
		cpi data, 'f'
		brne command11

		rcall receiveMore
		mov memory, data
		ldi data, 0xAC
		rcall transfer
		ldi data, 0xA0
		rcall transfer
		rcall transfer
		mov data, memory
		rcall transfer
		ldi data, 13
		rcall transmit
		reti

	;* | Read fuse and lock bits           | 'F' |       |   dd |     |  11  |
	command11:
		cpi data, 'F'
		brne command13

		; TODO
		ldi data, '?'  ;  
		rcall transmit
		reti


	; command12 no longer used

	;* | Select device type                | 'T' |    dd |      | 13d |   6  |
	command13:
		cpi data, 'T'
		brne command14

		rcall receiveMore
		mov device, data
		ldi data, 13  ; CR
		rcall transmit
		reti
		

	;* | Read signature bytes              | 's' |       | 3*dd |     |      |
	command14:
		cpi data, 's'
		brne command15

		ldi data, 0x30  
		rcall transfer  ; #1
		ldi data, 0x00  
		rcall transfer  ; #2
		ldi data, 0x02        ; processor type
		rcall transfer  ; #3
		rcall transfer  ; #4
		rcall transmit   

		ldi data, 0x30  
		rcall transfer  ; #1
		ldi data, 0x00  
		rcall transfer  ; #2
		ldi data, 0x01        ; memory
		rcall transfer  ; #3
		rcall transfer  ; #4
		rcall transmit   

		ldi data, 0x30  
		rcall transfer  ; #1
		ldi data, 0x00  
		rcall transfer  ; #2
		ldi data, 0x00        ; manufacturer (should be $1E for Atmel)
		rcall transfer  ; #3
		rcall transfer  ; #4
		rcall transmit   
		reti


	;* | Return supported device codes     | 't' |       | n*dd | 00d |   7  |
	command15:
		cpi data, 't'
		brne command16

		ldi	data,tn12	
		rcall transmit   
		ldi	data,tn15	
		rcall transmit   
		ldi	data,S1200D	
		rcall transmit   
		ldi	data,S2313A	
		rcall transmit   
		ldi	data,S4414A	
		rcall transmit   
		ldi	data,S8515A	
		rcall transmit   
		ldi	data,S2323A	
		rcall transmit   
		ldi	data,S2343A	
		rcall transmit   
		ldi	data,S2333A	
		rcall transmit   
		ldi	data,S4433A	
		rcall transmit   
		ldi	data,S4434	
		rcall transmit   
		ldi	data,S8535	
		rcall transmit   
		ldi	data,m83	
		rcall transmit   
		ldi	data,m161	
		rcall transmit   
		ldi	data,m163	
		rcall transmit   
		ldi	data,m103	
		rcall transmit   
		ldi	data,m603	
		rcall transmit   
		ldi data, $00  ; end of list
		rcall transmit   
		reti


	;* | Return software identifier        | 'S' |       | s[7] |     |   8  |
	command16:
		cpi data, 'S'
		brne command17

		; always return 7 chars:
		ldi data, 'A'
		rcall transmit   
		ldi data, 'V'
		rcall transmit   
		ldi data, 'R'
		rcall transmit   
		ldi data, ' '
		rcall transmit   
		ldi data, 'I'
		rcall transmit   
		ldi data, 'S'
		rcall transmit   
		ldi data, 'P'
		rcall transmit   
		reti
		

	;* | Return sofware version            | 'V' |       |dd dd |     |   9  |
	command17:
		cpi data, 'V'
		brne command18

		ldi	data,SW_MAJOR
		rcall transmit   
		ldi	data,SW_MINOR
		rcall transmit   
		reti


	;* | Return hardware version           | 'v' |       |dd dd |     |   9  |
	command18:
		cpi data, 'v'
		brne command19

		ldi	data,0x30+HW_MAJOR 
		rcall transmit   
		ldi	data,0x30+HW_MINOR 
		rcall transmit   
		reti

	;* | Return programmer type            | 'p' |       |   dd |     |  10  |
	command19:
		cpi data, 'p'
		brne command20

		ldi data, 'S'
		rcall transmit   
		reti


	;* | Set LED                           | 'x' |    dd |      | 13d |  12  |
	command20:
		cpi data, 'x'
		brne command21

		rcall receiveMore
		cbi PORTB, 1 ;  LED on pin 1 on
		ldi data, 13
		rcall transmit   
		reti


	;* | Clear LED                         | 'y' |    dd |      | 13d |  12  |
	command21:
		cpi data, 'y'
		brne command22

		rcall receiveMore
		sbi PORTB, 1 ;  LED on pin 1 off
		ldi data, 13
		rcall transmit   
		reti


	;* | Universial command                | ':' |  3*dd |   dd | 13d |      |
	command22:
		cpi data, ':'
		brne command23

		rcall receiveMore
		rcall transfer  ; # 1
		rcall receiveMore
		rcall transfer  ; # 2
		rcall receiveMore
		rcall transfer  ; # 3
		ldi data, 0
		rcall transfer  ; # 4
		rcall transmit   
		ldi temp1, 0xFF
		rcall delay
		ldi data, 13
		rcall transmit   
		reti

	;* | New universal command	       | '.' |  4*dd |   dd | 13d |      |
	command23:
		cpi data, '.'
		brne command24

		rcall receiveMore
		rcall transfer  ; # 1
		rcall receiveMore
		rcall transfer  ; # 2
		rcall receiveMore
		rcall transfer  ; # 3
		rcall receiveMore
		rcall transfer  ; # 4
		rcall transmit   
		ldi temp1, 0xFF
		rcall delay
		ldi data, 13
		rcall transmit   
		reti

	;* | Special test command	       | 'Z' |  2*dd |   dd |     |      |
	command24:
		cpi data, 'Z'
		brne command25

		rcall receiveMore
		; ignore
		rcall receiveMore
		; ignore
		rcall transmit   
		reti

	command25:
		ldi data, '?'
		rcall transmit   

reti



;--------------------------------------------------
receiveMore:      ; receive one more byte from UART
	sbis UCSRA, RXC
	rjmp receiveMore
	in data, UDR  ; this automatically clears the RXC flag
ret



;--------------------------------------------------
transmit:    ; via UART 
  sbis UCSRA,UDRE   ; Warten, bis UDR bereit ist
	rjmp transmit
	out UDR, data
ret



;--------------------------------------------------
transfer:   ; via SPI
	cbi SPSR, SPIF ; SPI flag löschen	
	out SPDR, data ; Übertragung starten
	wait1:
		sbis SPSR, SPIF
		rjmp wait1
	cbi SPSR, SPIF ; SPI flag löschen	
	in data, SPDR 
	ldi temp1, 0x30
	rcall delay
ret

;--------------------------------------------------
delay:   ; small delay
	ldi temp2, 0xFF
	delay1: 
		dec temp2
		brne delay1
		dec temp1
		brne delay1
ret		

;--------------------------------------------------
pagedelay:   ; longer delay
	sbi PORTB, 1 ;  LED on pin 1 off
	ldi temp1, 0x03
	ldi temp2, 0xFF
	ldi temp3, 0xFF
	delay2: 
		dec temp3
		brne delay2
		dec temp2
		brne delay2
		dec temp1
		brne delay2
		cbi PORTB, 1 ;  LED on pin 1 on
ret		


;--------------------------------------------------
waitFF:   ; wait until burning flash has finished 
					; (until something other than FF is read)

		mov data, readmode  ; read high byte or low byte
		rcall transfer  ; #1
		mov data, highAddress  
		rcall transfer  ; #2
		mov data, lowAddress  
		rcall transfer  ; #3
		ldi data, 0x00  
		rcall transfer  ; #4  
		cpi data, 0xFF  ; is still FF there in program memory ?
		breq waitFF
ret



