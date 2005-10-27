
#include <stdlib.h>
#include <bfd.h>
#include <stdio.h>

void
prg_bfd_init (void)
{
  bfd_init ();
}

bfd *
prg_input_open (const char * filename, void * file)
{
  bfd * b = bfd_fdopenr (filename, "ihex", fileno(file));
  return b;
}

void
prg_input_close (bfd * abfd)
{
  bfd_close (abfd);
}

void
prg_write_section (bfd * abfd, asection * sect, void *obj)
{
  int fd = *((int*)obj);
  fprintf (stderr, "considering section %s\n", sect->name);
  if (sect->flags & SEC_LOAD) {
    fprintf (stderr, "writing section %s @%xd size %xd\n",
	    sect->name, sect->lma, bfd_get_section_size_after_reloc (sect));
  }
}

void
prg_write (int portfd, bfd * abfd)
{
  
  bfd_map_over_sections (abfd, prg_write_section, &portfd);

}
