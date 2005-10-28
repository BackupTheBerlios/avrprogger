
#include <stdlib.h>

struct {
  char signature[7];
  char * manufacturer;
  char * device;
  int memsize_flash;
  int memsize_eeprom;
  int memsize_ram;
  int pagesize_flash; /* 0 - no pagemode | -1 - don't know */
  int pagesize_eeprom;
} signatures [] = {
  { "1E9001", "Atmel", "AT90S1200",  1*1024,  64,    0,   0, 0},
  { "1E9101", "Atmel", "AT90S2313",  2*1024, 128,  128,   0, 0},
  { "1E9201", "Atmel", "AT90S4414",  4*1024, 256,  256,   0, 0},
  { "1E9203", "Atmel", "AT90S4433",  4*1024, 256,  128,   0, 0},
  { "1E9301", "Atmel", "AT90S8515",  8*1024, 512,  512,   0, 0},
  { "1E9307", "Atmel", "ATmega8",    8*1024, 512, 1024,  32, 4},
  { "1E9402", "Atmel", "ATmega163", 16*1024, 512, 1024, 128, 0},

  { "", NULL, NULL, 0, 0, 0, 0},
};


int
find_signature (const char * sig)
{
  char cmpsig[7];
  int i;
  sprintf (cmpsig, "%02X%02X%02X", sig[2], sig[1], sig[0]);

  for (i = 0; signatures[i].signature[0]; i++) {
    if (strcmp (signatures[i].signature, cmpsig) == 0)
      return i;
  }
  return -1;
}

void
get_pagesizes (int idx, int * pagesize_flash, int * pagesize_eeprom)
{
  if (idx < 0) return;
  pagesize_flash = signatures[idx].pagesize_flash;
  pagesize_eeprom = signatures[idx].pagesize_eeprom;
}
