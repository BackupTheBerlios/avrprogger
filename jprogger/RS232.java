// //////////////////////////////////////////////////////////////////////////////
// 
// File    : RS232.java
// Purpose : for reading/writing bytes from/to serial port
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
public class RS232 {
	static RS232[] ports = new RS232[4];
	RS232OutputStream out;
	RS232InputStream  in;
	int index;	// usually 0 or 1 (can also be 2 or 3)
	String name;
	
	static boolean libraryLoaded;

	static {
System.out.println("RS232.java: neue Version !!! ") ;
		try {
			System.loadLibrary("RS232");
			libraryLoaded = true;
		} catch(Error err) {
			System.out.println("installing libraries ...") ;

			String libName = null;
			byte[] buffer = null;
			File file = null;
			int size = 0;
			RandomAccessFile srcFile = null;
			RandomAccessFile destFile = null;
			String libPath = System.getProperty("java.library.path");
			int pos = libPath.indexOf(File.pathSeparator);
			libPath = libPath.substring(0, pos);									

			// TODO  make this an install method 
			try {
				libName = "libRS232.so";		
				file = new File(libName);
				if (file.exists()) {
					size = (int) file.length();
					buffer = new byte[size];
					srcFile = new RandomAccessFile(file, "r");
					libName = libPath + File.separator + libName;
					destFile = new RandomAccessFile(libName, "rws");
					srcFile.read(buffer);
					destFile.write(buffer);
					srcFile.close();
					destFile.close();
					System.out.println("installed: "+libName) ;
				} 

				libName = "RS232.dll";		
				file = new File(libName);
				if (file.exists()) {
					size = (int) file.length();
					buffer = new byte[size];
					srcFile = new RandomAccessFile(file, "r");
					libName = libPath + File.separator + libName;
					destFile = new RandomAccessFile(libName, "rws");
					srcFile.read(buffer);
					destFile.write(buffer);
					srcFile.close();
					destFile.close();
					System.out.println("installed: "+libName) ;
				} 

				//	try again:
				System.loadLibrary("RS232");
				libraryLoaded = true;
			} catch(Exception ex) {
				System.out.println("RS232 ex: "+ex) ;
			}

		}


//	System.out.println("RS232.java: OK !") ;
//	if (true) {
//		System.exit(0);
//	}

		
	}



	/**
	 * 
	 */
	static public RS232 open(String portName, int baudRate, int byteSize, int wait) {
		RS232 port = null;
		if (!libraryLoaded) {
			return null;
		}

		int index = -1;
		try {
			index = nativeOpen(portName, baudRate, byteSize, wait);
		} catch (Exception ex) {
			System.out.println("RS232.open(): ex: "+ex) ;
		}

System.out.println("RS232.open() index: "+index) ;

		if (index == -1) {
			System.out.println("RS232.open()  port not opened : "+portName) ;
		} else {
			port = ports[index];
			if (port == null) {
				port = new RS232(portName, index);
				ports[index] = port;
			}
		}
		return port;		

	}



	/**
	 * 
	 */
	private RS232() {
		//
	}	

	/**
	 * 
	 */
	public RS232(String name, int index) {
		this.name = name;
		this.index = index;
	}	

	/**
	 * 
	 */
	public RS232OutputStream getOutputStream() {
		if (out == null) {
			out = new RS232OutputStream(this);
		}
		return out;		
	}

	/**
	 * 
	 */
	public RS232InputStream getInputStream() {
		if (in == null) {
			in = new RS232InputStream(this);
		}
		return in;		
	}


	/**
	 * 
	 */
	static public void main(String[] params) {
		
		
	}

	protected void finalize() throws Throwable { close(); }
	public void close ()  { 
		try {
			close(index); 
		} catch (Exception ex) {
			//	
		}
	}

	private static native int nativeOpen (String portName, int baudRate, int byteSize, int wait) throws IOException;
	private native void close (int index) throws IOException;

}







// setup:  cursor:49,0; frame:0,50,1024,500; bookmarks:0,0,0,0,0,0,0,0;
