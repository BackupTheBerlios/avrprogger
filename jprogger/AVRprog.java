////////////////////////////////////////////////////////////////////////////////
// File    : AVRprog.java
// Purpose : AVR progger - transfers data to AVR microcontroller running prog.asm
// Copyright (c) 2002 GNOM SOFT GmbH, Dresden (Germany) All rights reserved.
// License : This file can be used and distributed under GNU General Public License.
// Author  : Gerhard Paulus (gp@gnomsoft.de)
// Version : 1.0
/////////////////////////////////////////////////////////////////////////////////


//	import org.storedobjects.db.*;
import java.io.*;
import gnu.io.*;
//import javax.comm.*;

//	COM1 und nicht com1 !
//  /dev/ttyS0


/**
 * 
 */
public class AVRprog {

	static InputStream in = null;
	static OutputStream out = null;
	static byte[] outBuffer = new byte[3];
	static byte[] inBuffer = new byte[3];
	static byte[] fileBytes;
	static char[] line = new char[1000];
	static int lineLength;


	/**
	 * 
	 */
//	static public void openRS232(String portName) {
//		RS232 port = RS232.open(portName, 19200, 8, 0);
//		//	RS232 port = RS232.open(portName);
////	System.out.println("AVRprog.openRS232() port: "+port) ;
//		in = port.getInputStream();
//		out = port.getOutputStream();
//	}
//


	/**
	 * 
	 */
	static public void sunPort(String portName) {
		CommPortIdentifier cpi = null;
		SerialPort port = null;
		try {
		    cpi = CommPortIdentifier.getPortIdentifier(portName);
		    port = (SerialPort) cpi.open("AVRprog", 13);
		    out = port.getOutputStream();
		    in = port.getInputStream();
		    port.setSerialPortParams(19200,
					     SerialPort.DATABITS_8,
					     SerialPort.STOPBITS_1,
					     SerialPort.PARITY_NONE);
		} catch (Exception ex) {
		    System.out.println("AVRprog.sunPort() ex: "+ex) ;
		}
//	System.out.println("AVRprog.sunPort() port: "+port) ;
	}

	

	/**
	 * 
	 */
	static public int transfer(int outCount, int inCount) {
		inBuffer[0] = 0;
		inBuffer[0] = 13;  // TODO   
		try {
	 	   out.write(outBuffer, 0, outCount);
		   for (int i = 0; i < inCount;) {
		       int nread = in.read(inBuffer, i, inCount-i);
		       i += nread;
		   }
		} catch (Exception ex) {
			System.out.println("AVRprog.transfer() ex: "+ex) ;
		}
		return 0;  // TODO   
	}



	/**
	 * 
	 */
	static public void cleanup(boolean OK) {
		if (! OK) {
			System.out.println("AVRprog.cleanup()  !!! error with "+(char) outBuffer[0]) ;
		}

		//	clear LED   
		outBuffer[0] = (byte) 'y';
		outBuffer[1] = 0;  // TODO  ? 
		transfer(2,1);
		if (inBuffer[0] != 13) {
		      	System.out.println("AVRprog.  !!! error at  "+(char) outBuffer[0]) ;
			//	return;
		}

		//	leave programming mode
		outBuffer[0] = (byte) 'L';
		transfer(1,1);
		if (inBuffer[0] != 13) {
		    System.out.println("AVRprog.  !!! error at  "+(char) outBuffer[0]) ;
		    //	return;
		}
	}


	/**
	 * 
	 */
	static public void help() {
		System.out.println("java AVRprog buttons") ;
		System.out.println("   -> this will transfer buttons.hex") ;
		System.out.println("java AVRprog -p128  buttons") ;
		System.out.println("   -> program memory is organized in pages with 128 bytes each") ;
		System.out.println("java AVRprog -cCOM2  buttons") ;
		System.out.println("   -> use serial port COM2 (standard is COM1)") ;
		System.out.println("java AVRprog -b  buttons") ;
		System.out.println("   -> transfer in block mode (much faster)");
	}	


	/**
	 * 
	 */
	static public void main(String[] params) {

//			int n = (int) Math.pow(2,16);
//	System.out.println("AVRprog.java:  n: "+n) ;
//			n = 512 * 256 / 2;
//	System.out.println("AVRprog.java:  n: "+n) ;
//			int t = 258;
//			n = (t >> 8) & 0xFF;
//	System.out.println("AVRprog.java:  high: "+n) ;
//			n = (t >> 0) & 0xFF;
//			//	n = t & 0xFF;
//	System.out.println("AVRprog.java:  low:  "+n) ;
//			byte b = (byte) t;
//	System.out.println("AVRprog.java: b: "+b) ;

		long startTime = 0;
		long endTime = 0;
		long diffTime = 0;
		
		String commPort = "/dev/ttyS2";
		String hexFile = "buttons.hex";
		String eepFile = "buttons.eep";
		char ch = 0;
		int linePos = 0;
		String lineString = null;
		
		int count = 0;
		int address = 0;		
		int type = 0;
		int high = 0;
		int low = 0;
		int pos = 0;
		int pageSize = 0;
		int pageAddress = 0;
		int byteAddress = 0;
		boolean blockMode = false;

		int fileLength = 0;

		int byteCount = 0;
		int wordCount = 0;
		int totalBytes = 0;
		int totalWords = 0;
		int percent = 0;
		int border = 10;


//			int test = 1;
//			test = test << 4;
//	System.out.println("AVRprog.java: test "+test) ;

		int nParams = params.length;
		String param = null;
		String str = null;
		if (nParams == 0) {
			System.out.println("AVRprog.main() I need at least the name of the hex file ...") ;
			help();
			return;
		}
		
		for (int i= 0; i < nParams; i++) {
			param = params[i];
			if (param.startsWith("-p")) {   // page size
				str = param.substring(2);
				pageSize = Integer.parseInt(str);
			} else if (param.startsWith("-?")) {
				help();
				return;
			} else if (param.startsWith("-c")) {  // comm port
				commPort = param.substring(2);
			} else {
				hexFile = param + ".hex";
				eepFile = param + ".hex";
			}
			
		}

//	System.out.println("AVRprog.java: pageSize: "+pageSize) ;
//	if (true) {
//		return;
//	}
//	System.out.println("AVRprog.main() "+hexFile) ;

		File file = new File(hexFile);
		if ( ! file.exists()) {
			System.out.println("AVRprog.main() file does not exist: "+hexFile) ;
			return;
		}
		
		System.out.println ("port: "+commPort);
		sunPort(commPort);
		//openRS232(commPort);
//	System.out.println("AVRprog.java: RS232 !!! ") ;

		startTime = System.currentTimeMillis();

		// enter programming mode:	
//	System.out.println("AVRprog.java: P ") ;
		outBuffer[0] = (byte) 'P';
		transfer(1,1);
		if (inBuffer[0] != 13) {
			cleanup(false);
			return;
		}

		//	set LED  // TODO   
//	System.out.println("AVRprog.java: x ") ;
		outBuffer[0] = (byte) 'x';
		outBuffer[1] = 0;  // TODO  ? 
		transfer(2,1);
		if (inBuffer[0] != 13) {
			cleanup(false);
			return;
		}


		// read signature bytes:	
//	System.out.println("AVRprog.java: s ") ;
		outBuffer[0] = (byte) 's';
		transfer(1,3);

	System.out.println("AVRprog.java: signature 1: "+inBuffer[0]) ;
	System.out.println("AVRprog.java: signature 2: "+inBuffer[1]) ;
	System.out.println("AVRprog.java: signature 3: "+inBuffer[2]) ;

//	cleanup(true);
//	if (true) {
//		return;
//	}


		if (pageSize > 0) {
			//	set page mode
System.out.println("AVRprog.java: M ") ;
			outBuffer[0] = (byte) 'M';
			transfer(1,1);
			if (inBuffer[0] != 13) {
				cleanup(false);
				return;
			}
		}


		//	report autoincrement address
//	System.out.println("AVRprog.java: a ") ;
		outBuffer[0] = (byte) 'a';
		transfer(1,1);
		if (inBuffer[0] != (byte) 'Y') {
			cleanup(false);
			return;
		}

		//	chip erase
//	System.out.println("AVRprog.java: e ") ;
		outBuffer[0] = (byte) 'e';
		transfer(1,1);
		if (inBuffer[0] != 13) {
			cleanup(false);
			return;
		}

		//	set address
		outBuffer[0] = (byte) 'A';
//	System.out.println("AVRprog.java: A ") ;
		outBuffer[1] = 0;
		outBuffer[2] = 0;
		transfer(3,1);
		if (inBuffer[0] != 13) {
			cleanup(false);
			return;
		}

//			//	read program memory
//			outBuffer[0] = (byte) 'R';
//			transfer(1,2);
//			high = inBuffer[0];
//			low  = inBuffer[1];
//	
//			//	write data memory
//			outBuffer[0] = (byte) 'D';
//			outBuffer[1] = 0;
//			transfer(1,2);
//			if (inBuffer[0] != 13) {
//				cleanup();
//				return;
//			}
//	
//			//	read data memory
//			outBuffer[0] = (byte) 'd';
//			transfer(1,1);
//			low = inBuffer[0];
//	

		System.out.print("AVRprog.main() bin am programmieren .") ;

		fileBytes = readFile(hexFile);
		fileLength = fileBytes.length;

		for (int i= 0; i < fileLength; i++) {
			ch = (char) fileBytes[i];
			switch (ch) {
			case  '\r' :
			case  '\n' :
				if (i+1 < fileLength) {
					if (fileBytes[i+1] == '\n') {
						i++;
					}
				}
				count = hex2int(line[1]) * 16 + hex2int(line[2]);
				byteCount += count;
				linePos = 0;
				break ;
			default :
				line[linePos++] = ch;
				break ;
			}
		}
		totalWords = byteCount / 2;
		byteCount = 0;

		linePos = 0;	
		for (int i= 0; i < fileLength; i++) {
			ch = (char) fileBytes[i];
			switch (ch) {
			case  '\r' :
			case  '\n' :
				if (i+1 < fileLength) {
					if (fileBytes[i+1] == '\n') {
						i++;
					}
				}
				
//					lineString = new String(line, 0, linePos);
//	System.out.println("AVRprog.java: lineString "+lineString) ;

				count = hex2int(line[1]) * 16 + hex2int(line[2]);
//	System.out.println("AVRprog.java: count: "+count) ;

				address = hex2int(line[5]) * 16 + hex2int(line[6]);
//	System.out.println("AVRprog.java: address: "+address) ;

				type = hex2int(line[8]);
//	System.out.println("AVRprog.java: type: "+type) ;

				switch (type) {
				case 1 :
//	System.out.println("AVRprog.java: end ! ") ;
					break ;
				case 0 :  // data
//	System.out.println("AVRprog.java: data ! ") ;
					pos = 9;
					count = count / 2; 
					for (int k= 0; k < count; k++) {
						low = hex2int(line[pos]) * 16 + hex2int(line[pos+1]);
						high = hex2int(line[pos+2]) * 16 + hex2int(line[pos+3]);
//	System.out.println("AVRprog.java: high/low: "+high+"/"+low) ;

//	System.out.println("AVRprog.java: low:  "+new String(line, pos, 2)) ;
						//	write program memory: low byte  (!!! must be first write)
						outBuffer[0] = (byte) 'c';
						outBuffer[1] = (byte) low;
						transfer(2,1);
						if (inBuffer[0] != 13) {
							cleanup(false);
							return;
						}
//	System.out.println("AVRprog.java: high: "+new String(line, pos+2, 2)) ;
						//	write program memory: high byte
						outBuffer[0] = (byte) 'C';
						outBuffer[1] = (byte) high;
						transfer(2,1);
						if (inBuffer[0] != 13) {
							cleanup(false);
							return;
						}
						pos += 4;

						byteCount += 2;
						if (pageSize > 0 && byteCount == pageSize) {

//	System.out.println("AVRprog.java: write page at address: "+pageAddress) ;

							high = (pageAddress >> 8) & 0xFF;
							low  = (pageAddress >> 0) & 0xFF;

							//	set address for next page write
							outBuffer[0] = (byte) 'A';
							outBuffer[1] = (byte) high;
							outBuffer[2] = (byte) low;
							transfer(3,1);
							if (inBuffer[0] != 13) {
								cleanup(false);
								return;
							}

							//	issue page write
							outBuffer[0] = (byte) 'm';
							transfer(1,1);
							if (inBuffer[0] != 13) {
								cleanup(false);
								return;
							}

							//	set address for next page load (byte write)
							outBuffer[0] = (byte) 'A';
							outBuffer[1] = 0;
							outBuffer[2] = 0;
							transfer(3,1);
							if (inBuffer[0] != 13) {
								cleanup(false);
								return;
							}

							pageAddress += (pageSize / 2); 
							byteCount = 0;	
						}

						wordCount++;
						percent = (int) (((double)wordCount / totalWords) * 100);
						if (percent > border) {
							System.out.print(".") ;
							border += 10;
						}

					}

					break ;
				default :
					break ;
				}
				
				linePos = 0;
				
				break ;
			default :
				line[linePos++] = ch;
				break ;
			}

		}

		System.out.println() ;

		if (pageSize > 0) {
			System.out.println("AVRprog.java: last write page at address: "+pageAddress) ;

			high = (pageAddress >> 8) & 0xFF;
			low  = (pageAddress >> 0) & 0xFF;

			//	set address for next page write
			outBuffer[0] = (byte) 'A';
			outBuffer[1] = (byte) high;
			outBuffer[2] = (byte) low;
			transfer(3,1);
			if (inBuffer[0] != 13) {
				cleanup(false);
				return;
			}

			//	issue page write
			outBuffer[0] = (byte) 'm';
			transfer(1,1);
			if (inBuffer[0] != 13) {
				cleanup(false);
				return;
			}
		}
		
		cleanup(true);

		endTime = System.currentTimeMillis();
		diffTime = endTime - startTime ;

		System.out.println("AVRprog.main() fertig (millis:  "+diffTime+")") ;
		System.exit (0);
	}


	/**
	 * 
	 */
	static public int hex2int(char hex) {
		switch (hex) {
		case  '0': return  0;
		case  '1': return  1;
		case  '2': return  2;
		case  '3': return  3;
		case  '4': return  4;
		case  '5': return  5;
		case  '6': return  6;
		case  '7': return  7;
		case  '8': return  8;
		case  '9': return  9;
		case  'A': return  10;
		case  'B': return  11;
		case  'C': return  12;
		case  'D': return  13;
		case  'E': return  14;
		case  'F': return  15;
		default :
			System.out.println("AVRprog.hex2int() wrong input: "+hex) ;
			return 0;
		}
		
	}


	/**
	 * Read file from disk into byte stream.
	 * Checks if user has enough memory to hold the byte stream.
	 */
	static public byte[] readFile(String fullPath) {
		byte[] buffer = null;

		if (fullPath == null) {
			System.out.println("AVRprog.java: fullPath == null ");

			return null;
		} 

		//	int len = fullPath.length();
		//		System.out.println("DB.java: ??? "+len) ;
		//	for (int i= 0; i < len ; i++) {
		//		System.out.println("DB.java: ? "+fullPath.charAt(i)) ;
		//	}
		

		File file = new File(fullPath);

		if (!file.exists()) {
			System.out.println("AVRprog.java: cannot find " + fullPath);
			return null;
		} 

		int size = (int) file.length();

		long freeMemory = Runtime.getRuntime().freeMemory();

		if (freeMemory < size) {
			System.out.println("AVRprog.java: free memory only " + freeMemory);

			return null;
		} 

		buffer = new byte[size];

		try {
			FileInputStream fis = new FileInputStream(fullPath);
			DataInputStream dis = new DataInputStream(fis);

			dis.read(buffer);
			dis.close();
			fis.close();
		} catch (Exception e) {
			System.out.println("AVRprog.java: " + e);
		} 

		return buffer;
	} 


}






// setup:  cursor:322,20; frame:100,50,900,600; bookmarks:385,29,107,0,0,0,0,0;
