
#include <stdlib.h>
#include <fcntl.h>
#include <stdio.h>
#include <errno.h>
#include <termios.h>

static void
prg_serial_setup (int fd, int speed)
{
  struct termios t;
  speed_t sp;

  switch (speed) {
  case 0: /* keep speed */
    sp = B0;
    break;
  case 50:
    sp = B50;
    break;
  case 75:
    sp = B75;
    break;
  case 110:
    sp = B110;
    break;
  case 134:
    sp = B134;
    break;
  case 150:
    sp = B150;
    break;
  case 200:
    sp = B200;
    break;
  case 300:
    sp = B300;
    break;
  case 600:
    sp = B600;
    break;
  case 1200:
    sp = B1200;
    break;
  case 1800:
    sp = B1800;
    break;
  case 2400:
    sp = B2400;
    break;
  case 4800:
    sp = B4800;
    break;
  case 9600:
    sp = B9600;
    break;
  case 19200:
    sp = B19200;
    break;
  case 38400:
    sp = B38400;
    break;
  case 57600:
    sp = B57600;
    break;
  case 115200:
    sp = B115200;
    break;
  case 230400:
    sp = B230400;
    break;
  case 460800:
    sp = B460800;
    break;
  default:
    fprintf (stderr, "unknown speed %d\n", speed);
    exit (EXIT_FAILURE);
  }

  if (tcgetattr (fd, &t)) {
    fprintf (stderr, "tcgetattr failed: %s", strerror (errno));
    exit (EXIT_FAILURE);
  }

  t.c_cflag &= ~CSIZE;
  t.c_cflag |= CS8;
  t.c_cflag |= CLOCAL;
  t.c_lflag &= ~ICANON;

  if (cfsetspeed (&t, sp)) {
    fprintf (stderr, "cfsetspeed failed: %s", strerror (errno));
  }

  t.c_cc[VMIN] = 1;
  t.c_cc[VTIME] = 10 /* 1 second */;

  if (tcsetattr (fd, TCSAFLUSH, &t)) {
    fprintf (stderr, "tcsetattr failed: %s", strerror (errno));
    exit (EXIT_FAILURE);
  }
}

int
prg_serial_open (const char *port, int speed)
{
  int fd = -1;

  fd = open (port, O_RDWR | O_NOCTTY);

  if (fd == -1) {
    fprintf (stderr, "open of %s failed: %s\n", port, strerror (errno));
    exit (EXIT_FAILURE);
  }

  if (!isatty (fd)) {
    fprintf (stderr, "open: %s is not a terminal\n", port);
    exit (EXIT_FAILURE);
  }

  prg_serial_setup (fd, speed);

  return fd;
}

int cur_addr = -1;
int last_page = -1;
int use_autoincr = -1;
char signature[3];
int avr_index = -1;

int pagesize_flash = -1;
int pagesize_eeprom = -1;

void
parse_signature (char * sig)
{
  avr_index = find_signature (sig);
  get_pagesizes (avr_index, &pagesize_flash, &pagesize_eeprom);
}

static int
prg_serial_put (int fd, const char * str, const char * expect)
{
  int len = strlen (str);
  int wr = write (fd, str, len);
  if (wr == len) {
    char * buf;
    int rd;
    int cmp;
    if (expect == NULL || expect[0] == '\0') return wr;
    buf = alloca (strlen(expect)+1);
    rd = read (fd, buf, strlen(expect));
    buf[rd] = '\0';
    cmp = strcmp (buf, expect);
    return cmp ? -1 : wr;
  } else return -1;
}

void
prg_serial_start (int fd)
{
  char autoincr;
  prg_serial_put (fd, "P", "\r");
  cur_addr = 0;
  prg_serial_put (fd, "a", NULL);
  read (fd, &autoincr, 1);
  use_autoincr = autoincr == 'Y' ? 1 : 0;
  prg_serial_put (fd, "s", NULL);
  read (fd, signature, 3);
  parse_signature (signature);
}

void
prg_serial_end (int fd)
{
  prg_serial_put (fd, "L", "\r");
}

void
prg_serial_page_write (int fd)
{
  prg_serial_put (fd, "m", "\r");
  last_page = cur_addr;
}

void
prg_serial_write (int fd, int addr, const char * buf, int len)
{
  char tmp [256];
  int i;

  if (addr != cur_addr) {
    if (pagesize_flash > 0 && last_page >= 0)
      prg_serial_page_write (fd);
    tmp[0] = 'A';
    tmp[1] = (addr >> 8) & 0xff;
    tmp[2] = (addr & 0xff);
    tmp[3] = '\0';
    prg_serial_put (fd, tmp, "\r");
    cur_addr = addr;
  }

  for (i=0; i < len; i+=2) {
    char lo = buf[i], hi = buf[i+1];
    tmp[0] = 'c';
    tmp[1] = lo;
    tmp[2] = '\0';
    prg_serial_put (fd, tmp, "\r");
    tmp[0] = 'C';
    tmp[1] = hi;
    tmp[2] = '\0';
    prg_serial_put (fd, tmp, "\r");
    cur_addr += 2;
    if (pagesize_flash > 0 && last_page >= 0
	&& cur_addr >= last_page + pagesize_flash)
      prg_serial_page_write (fd);
  }
}
