
#include <stdlib.h>
#include <stdio.h>

#define _GNU_SOURCE
#include <getopt.h>

int
main (int argc, char *argv[])
{
  int c;
  char * portname = NULL;
  int serial_speed = 9600;

  int blocksize = -1;
  char * infilename = NULL;
  FILE * infile = NULL;
  
  int portfd = -1;

  struct bfd * in_bfd = NULL;

  static  int is_ihexfile = 0;

  static struct option long_options [] =
    {
      {"port", required_argument, 0, 'p'},
      {"blocksize", required_argument, 0, 'b'},
      {"speed", required_argument, 0, 's'},
      {"ihex", no_argument, &is_ihexfile, 'h'},
      {0,0,0,0}
    };

  while (1) {
    int option_index = 0;
    c = getopt_long( argc, argv, "hp:b:s:",
		     long_options, &option_index);
    if (c == -1) break;

    switch (c) {
    case 'p':
      portname = optarg;
      break;
    case 'b':
      blocksize = atoi (optarg);
      break;
    case 's':
      serial_speed = atoi (optarg);
      break;
    case 'h':
      break; /* already handled */
    case 0:
      break;

    default:
      abort ();
    }
  }

  if (optind < argc) {
    infilename = argv[optind];
  }

  if (infilename) {
    infile = fopen (infilename, "r");
  } else {
    fprintf (stderr, "please give filename\n");
    exit (EXIT_FAILURE);
  }

  if (is_ihexfile) {
    prg_ihex_init ();
    portfd = prg_serial_open (portname, serial_speed);
    prg_ihex_write (portfd, infile);
    close (portfd);
    fclose (infile);
  } else {
    prg_bfd_init ();
    
    in_bfd = prg_input_open (infilename, infile);
    
    portfd = prg_serial_open (portname, serial_speed);
    prg_write (portfd, in_bfd);
    close (portfd);

    prg_input_close (in_bfd);
  }

  return EXIT_SUCCESS;
}
