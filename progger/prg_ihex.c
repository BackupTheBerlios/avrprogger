
#include <stdio.h>
#include <stdlib.h>

#define MAX_HEX_LEN (16)

static inline int
hexdig2int (char digit)
{
  return (digit >= '0' && digit <= '9') ? (digit - '0')
    : (digit >= 'A' && digit <= 'F') ? (digit - 'A' + 10)
    : (digit >= 'a' && digit <= 'f') ? (digit - 'a' + 10)
    : -1;
}

static int
hex2int (const char * buf, int numdigits)
{
  int retval = 0;
  char c;
  for (; ((c = *buf)) && numdigits; buf++, numdigits--) {
    int digit = hexdig2int (c);
    if (digit == -1) {
      fprintf (stderr, "bad hex digit %c\n", c);
      exit (EXIT_FAILURE);
    }
    retval *= 16;
    retval += digit;
  }
  return retval;
}

void
prg_ihex_init (void)
{
}

void
prg_ihex_write (int port, FILE * file)
{
  char buf[256];
  char bytes[MAX_HEX_LEN];

  prg_serial_start (port);

  while (fgets (buf, sizeof(buf), file)) {
    int linelen;
    int addr;
    int flag;
    int i;

    if (buf[0] != ':') {
      fprintf (stderr, "wrong input file format\n");
      exit (EXIT_FAILURE);
    }
    linelen = hex2int (buf+1, 2);
    addr = hex2int (buf+3, 4);
    flag = hex2int (buf+7, 2);

    if (linelen > MAX_HEX_LEN) {
      fprintf (stderr, "line too long\n");
      linelen = MAX_HEX_LEN;
    }
    for (i=0; i < linelen; i++) {
      bytes[i] = hex2int (buf+9+2*i, 2);
    }

    prg_serial_write (addr, bytes, linelen);
  }

  prg_serial_end (port);
}
