ASM	= tavrasm
AFLAGS	= -I ../AVRASM_inc

all: progger.hex

%.hex: %.asm
	$(ASM) $(AFLAGS) $<
