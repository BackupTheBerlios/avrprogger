// //////////////////////////////////////////////////////////////////////////////
// 
// File    : RS232OutputStream.java
// Purpose : for reading from serial port
// 
// Copyright (c) 2002
// GNOM SOFT GmbH, Dresden (Germany) All rights reserved.
// License : This file can be used and distributed under
// GNU General Public License as specified in license.txt.
// 
// Author  : Gerhard Paulus
// Version : 1.0
// 
// ///////////////////////////////////////////////////////////////////////////////

import java.util.*;
import java.io.*;


/**
 * 
 */
public class RS232OutputStream extends OutputStream {
	static byte[] oneByteBuffer = new byte[1];
	RS232 port;

	
	/**
	 * 
	 */
	RS232OutputStream(RS232 port) {
		this.port = port;
	}	

//		/**
//		 * 
//		 */
//		public void write (int ch) throws IOException {
//			//
//		}

	protected void finalize() throws Throwable { port.close(); }
	public void close () throws IOException { port.close(); }
	public void write (int ch) throws IOException {
		oneByteBuffer[0] = (byte)ch;		
		write(port.index, oneByteBuffer, 0, 1);
	}
	public void write (byte[] buf) throws IOException {
		write(port.index, buf, 0, buf.length);
	}
	public void write (byte[] buf, int offset, int len)
		throws java.io.IOException {
		if (offset < 0 || len <= 0 || offset+len > buf.length)
			throw new IllegalArgumentException("bad write buffer range");
			write(port.index, buf, offset, len);
		}
	public void flush () { };
	
	private native int write (int index, byte[] buf, int offset, int len)
		throws IOException;

}


// setup:  cursor:48,0; frame:100,50,700,500; bookmarks:0,0,0,0,0,0,0,0;
