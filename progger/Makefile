
LIBS=-lbfd
CFLAGS=-g

all: avrprog

avrprog: prg_main.o prg_serial.o prg_bfd.o prg_ihex.o
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $^ $(LIBS)

