/* created by Gerhard Paulus
 * modifications:
 *   2003-03-19 Holger Dietze
 *        #included all RS232{,InputStream,OutputStream}.h instead of
 *        RS232Native.h
 */

/* $Id: RS232linux.c,v 1.1 2003/03/20 19:33:22 hdietze Exp $ */

#include "RS232.h"
#include "RS232InputStream.h"
#include "RS232OutputStream.h"

#include <sys/types.h> 
#include <sys/stat.h> 
#include <sys/time.h>
#include <fcntl.h> 
#include <unistd.h> 
#include <termios.h> 
#include <sys/ioctl.h> 


#define	INVALID_HANDLE_VALUE	-1
#define	MAX_COMPORTS	4
static struct serial { const char* name; jstring jname; int handle; } serial [MAX_COMPORTS];

static void initialize (JNIEnv* env) {
	//
}


/*
 * Class:     RS232
 * Method:    nativeConstructor
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_RS232_nativeOpen
  (JNIEnv* env, jclass clazz, jstring _port, 
  jint baudRate, jint byteSize, jint wait) {

  //	printf("Alles ist erreichbar \n");

	// release port later !
  const char* port = (*env)->GetStringUTFChars(env, _port, NULL);
  int i, n = MAX_COMPORTS;
	int releaseName = 1;
  for (i = 0; i < n; ++ i) {
    if (!serial[i].name) {
			releaseName = 0;
      serial[i].name =  port;
      serial[i].jname =  _port;
      serial[i].handle = INVALID_HANDLE_VALUE;
			break;
    }
    else if (strcmp(port, serial[i].name) == 0) {
      break;
    }  
  }

	if (i >= n) {
   (*env)->ThrowNew(env,
    (*env)->FindClass(env, "java/lang/IllegalArgumentException"), "bad portid");
    i = -1;
	} else {

		if (serial[i].handle == INVALID_HANDLE_VALUE) {
			int fd = INVALID_HANDLE_VALUE;

			//	// char devname[MAXLINESIZE]; 
			//	char lockname[256]; 
			//	int chars_read; 
			// TODO  create lock file 
	
			// don't wait for other device ready:
			fd = open ((const char *)port, O_RDWR|O_NOCTTY|O_NDELAY);  // non-blocking
			//	fd = open ((const char *)port, O_RDWR|O_NOCTTY);  // blocking
			serial[i].handle = fd;
		
			if (fd < 0) {
		        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/IOException"),
			    					"cannot open port");
			}
	
			// reset flags, results in non-blocking reads 
			fcntl(fd, F_SETFL, 0);
		
			// default is 9600 baud 8N1 
			int speed = 9600;
			int bits = 8;
			char parity = 'N';
			int stops = 1;
		
			struct termios termios;
		
			if ( tcgetattr(fd, &termios) != 0 ) {
		       (*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/IOException"),
		    					"cannot get flags ");
			}
		
			cfmakeraw(&termios);
			termios.c_cflag |= CLOCAL;		// disable modem status line check
			//no Flow control
			termios.c_cflag &= ~CRTSCTS;	// disable hardware flow control
			termios.c_iflag &= ~(IXON|IXOFF);	// disable software flow control
		
			//Set size of byte (number of data bits)
			termios.c_cflag &= ~CSIZE;
			if (bits == 7)
				termios.c_cflag |= CS7;
			else
				termios.c_cflag |= CS8;
		
			// one stop bit
			termios.c_cflag &= ~CSTOPB;
	
			// no parity bit
			termios.c_cflag &= ~PARENB;
		
			//Set speed
			switch (baudRate) {
			case 300: 		speed = B300;				break;
			case 600:			speed = B600;				break;
			case 1200:		speed = B1200;			break;
			case 2400:		speed = B2400;			break;
			case 4800:		speed = B4800;			break;
			case 9600:		speed = B9600;			break;
			case 19200:		speed = B19200;			break;
			case 38400:		speed = B38400;			break;
			case 57600:		speed = B57600;			break;
			case 115200:	speed = B115200;		break;
			case 230400:	speed = B230400;		break;
			default:	
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/IOException"),
   					"unsupported baud rate");
			}

			cfsetispeed(&termios,speed);  // input speed
			cfsetospeed(&termios,speed);  // output speed
		
			termios.c_cc[VMIN] = 1;
			// wait period in tenths of seconds:
			// 0 for wait will block forever when reading
			termios.c_cc[VTIME] = wait * 10;   
		
			// now set all attributs for this port
			if ( tcsetattr(fd, TCSANOW, &termios) != 0 ) {
		       (*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/IOException"),
		    					"cannot set flags");
			}
		}
	}

	if (releaseName == 1) {
		(*env)->ReleaseStringUTFChars(env, _port, port);
	}

	// return index of serial array:	
  return (jint)i;
}  


 /*
 * Class:     RS232
 * Method:    close
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_RS232_close
    (JNIEnv* env, jobject this, jint portHandle) {
  if (serial[portHandle].handle != INVALID_HANDLE_VALUE) {
		// TODO  release lock file 
		close(serial[portHandle].handle);
		//	printf("releasing port %s\n", serial[portHandle].name);
	  (*env)->ReleaseStringUTFChars(env, serial[portHandle].jname, serial[portHandle].name);
		serial[portHandle].handle = INVALID_HANDLE_VALUE;
	}

}


/*
 * Class:     RS232OutputStream
 * Method:    write
 * Signature: (I[BII)I
*/
JNIEXPORT jint JNICALL Java_RS232OutputStream_write
    (JNIEnv* env, jobject this, jint portHandle,
				jbyteArray _buf, jint offset, jint len) {
  jbyte* buf = (*env)->GetByteArrayElements(env, _buf, NULL); 
  jint result = -1;

	int fd = serial[portHandle].handle;
	if ( fd != INVALID_HANDLE_VALUE ) {
		long nleft, nwritten;
		jbyte *ptr;
		ptr = buf + offset;
		nleft = len;
		while (nleft > 0) {
			nwritten = write(fd, ptr, nleft);
			if (nwritten <= 0) {
				result = -1;
				break;
			}
			nleft -= nwritten;
			ptr   += nwritten;
		}
		result = len;
	}
	
  (*env)->ReleaseByteArrayElements(env, _buf, buf, JNI_ABORT);
  return result;
}


/*
 * Class:     RS232InputStream
 * Method:    read
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_RS232InputStream_read__I
    (JNIEnv* env, jobject this, jint portHandle) {
  unsigned char buf[1];
  jint result = -1;
	jbyte *ptr;
	ptr = buf;
	long nread;
	int fd = serial[portHandle].handle;
	if ( fd != INVALID_HANDLE_VALUE ) {
		nread = read(fd, ptr, 1);
	  result = nread <= 0 ? -1 : buf[0];
	}
  return result;
}


/*
 * Class:     RS232InputStream
 * Method:    read
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_RS232InputStream_read__I_3BII
    (JNIEnv* env, jobject this, jint portHandle,
				jbyteArray _buf, jint offset, jint len) {

  //	printf("RS232linux.c :   read starts ...\n");

  jbyte* buf = (*env)->GetByteArrayElements(env, _buf, NULL); 
  jint result = -1;
	long nread = 0;
	long nleft = len;
	jbyte *ptr;
	ptr = buf;

	int fd = serial[portHandle].handle;
	if ( fd != INVALID_HANDLE_VALUE ) {
		while (nleft > 0) {
			fd_set rfds;
			int rval;
			nread = read(fd, ptr, nleft);
  //	printf("RS232linux.c : nread/nleft: %d/%d\n", nread, nleft);
			if (nread < 0) {
					nleft = -1;
					break;	//Error
			}
			nleft -= nread;
			ptr   += nread;
		}
	}

	if (nleft >= 0) {
		result = (len - nleft);
	}
	
  (*env)->ReleaseByteArrayElements(env, _buf, buf, 0);

  //	printf("RS232linux.c : ... read ends\n");


  return result;
}




