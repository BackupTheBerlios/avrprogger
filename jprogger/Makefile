
# $Id: Makefile,v 1.1 2003/03/20 19:32:52 hdietze Exp $

LD_SHARED = -shared
CPPFLAGS = -DJNIEXPORT= -DJNICALL=
CC = gcc-3.0
CJH = gcjh-3.0
CJ = gcj-3.0

AVRprog: RS232.class RS232InputStream.class RS232OutputStream.class AVRprog.class libRS232.so 
	$(CJ) -g --main=AVRprog -L . -o $@ $^

libRS232.so: RS232linux.o
	$(CC) $(LD_SHARED) $(LDFLAGS) -o $@ $<

RS232linux.o: RS232linux.c RS232.h RS232InputStream.h RS232OutputStream.h

%.h: %.class
	$(CJH) -jni $*

%.class: %.java
	$(CJ) -C $<
