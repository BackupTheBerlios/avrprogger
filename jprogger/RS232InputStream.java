// //////////////////////////////////////////////////////////////////////////////
// 
// File    : RS232InputStream.java
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
public class RS232InputStream extends InputStream {
	RS232 port;
	
	/**
	 * 
	 */
	RS232InputStream(RS232 port) {
		this.port = port;
	}	


//		/**
//		 * 
//		 */
//		public int read () throws IOException { 
//			//
//			return 0;
//		}	

	protected void finalize() throws Throwable { port.close(); }
	public void close () throws IOException { port.close(); }
	public int read () throws IOException { return read(port.index); }
	public int read (byte[] buf) throws IOException {
		return read(port.index, buf, 0, buf.length);
	}
	public int read (byte[] buf, int offset, int len) throws IOException {
		if (offset < 0 || len <= 0 || offset+len > buf.length)
			throw new IllegalArgumentException("bad read buffer range");
			return read(port.index, buf, offset, len);
		}
	public long skip (long len) { return 0; }

	private native int read (int index) throws IOException;
	private native int read (int index, byte[] buf, int offset, int len) throws IOException;

}






// setup:  cursor:0,0; frame:100,50,788,500; bookmarks:0,0,0,0,0,0,0,0;
