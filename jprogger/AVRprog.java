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
//	import javax.comm.*;

//	COM1 und nicht com1 !
//  /dev/ttyS0


/**
 * 
 */
public class AVRprog {

	static InputStream in = null;
	static OutputStream out = null;
	static byte[] outBuffer = new byte[4];
	static byte[] inBuffer = new byte[4];
	static byte[] fileBytes;
	static char[] line = new char[1000];
	static int lineLength;
	static int baudRate = 19200;
	static int blockSize = 64;

	static int pageSize = 0;
	static boolean blockMode = false;
	static String hexFile = null;
	static String eepFile = null;
	static boolean eraseOnly = false;  // TODO   
	static boolean writeFuses = false; 
	static boolean readFuses = false; 

	static int spiByte2 = 0;
	static int spiByte3 = 0;
	static int spiByte4 = 0;


	/**
	 * 
	 */
	static public void openRS232(String portName) throws Exception {
		RS232 port = null;
		try {
			port = RS232.open(portName, baudRate, 8, 0);
		} catch (Exception ex) {
			throw ex;		
		}
		//	RS232 port = RS232.open(portName);
//	System.out.println("AVRprog.openRS232() port: "+port) ;
		in = port.getInputStream();
		out = port.getOutputStream();
	}



//		/**
//		 * 
//		 */
//		static public void sunPort(String portName) throws Exception{
//			CommPortIdentifier cpi = null;
//			SerialPort port = null;
//			try {
//				cpi = CommPortIdentifier.getPortIdentifier(portName);
//				port = (SerialPort) cpi.open("AVRprog", 13);
//	      out = port.getOutputStream();
//	      in = port.getInputStream();
//	      port.setSerialPortParams(baudRate,
//	           SerialPort.DATABITS_8,
//	           SerialPort.STOPBITS_1,
//	           SerialPort.PARITY_NONE);
//			} catch (Exception ex) {
//				//	System.out.println("AVRprog.sunPort() ex: "+ex) ;
//				throw ex;
//			}
//	//	System.out.println("AVRprog.sunPort() port: "+port) ;
//		}

	

	/**
	 * 
	 */
	static public int transfer(int outCount, int inCount) {
		inBuffer[0] = 0;
		inBuffer[0] = 13;  // TODO   
		try {
			out.write(outBuffer, 0, outCount);
			if (inCount > 0) {
		 		//	in.read(inBuffer, 0, inCount);  //	seems not to work on LINUX
			   for (int i = 0; i < inCount;) {
			       int nread = in.read(inBuffer, i, inCount-i);
			       i += nread;
			   }
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
			char outChar = (char) outBuffer[0];
			char  inChar = (char) inBuffer[0];
			int inInt = (int) inBuffer[0];
			if (inInt<0 ) {
				inInt += 256;
			}
			System.out.println("AVRprog.cleanup()  !!! error with "+
			outChar+" --> "+ inChar+ "["+inInt+"]");
		}

		//	clear LED   
		outBuffer[0] = (byte) 'y';
		outBuffer[1] = 0;  // TODO  ? 
		transfer(2,1);
		if (inBuffer[0] != 13) {
			//	System.out.println("AVRprog.  !!! error at  "+(char) outBuffer[0]) ;
			//	return;
		}

		//	leave programming mode
		outBuffer[0] = (byte) 'L';
		transfer(1,1);
		if (inBuffer[0] != 13) {
			//	System.out.println("AVRprog.  !!! error at  "+(char) outBuffer[0]) ;
			//	return;
		}

		System.out.println(" OK ") ;

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
		System.out.println("   -> use serial port COM2 (standard is /dev/ttyS0)") ;
		System.out.println("java AVRprog -b  buttons") ;
		System.out.println("   -> transfer in block mode (64 bytes blocks and thus much faster)");
		System.out.println("java AVRprog -p128 -b buttons") ;
		System.out.println("   -> fastest transfer for ATMEGA163");
		System.out.println("java AVRprog -f000000") ;
		System.out.println("   -> write fuse bits: ");
		System.out.println("   -> specify in hex byte2, byte3 and byte4 for SPI transfer)");
		System.out.println("      (eg. for MEGA163 this is explained on page 150 of the datasheet)");
		System.out.println("java AVRprog -q") ;
		System.out.println("   -> read low fuse bits: ");
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

		boolean doVerify = false;

		//	String commPort = "COM1";
		String commPort = "/dev/ttyS0"; 
		hexFile = "buttons.hex";
		eepFile = "buttons.eep";
		pageSize = 0;
		blockMode = false;
		eraseOnly = false;

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
			} else if (param.startsWith("-b")) {  // block mode
				blockMode = true;
				outBuffer = new byte[4 + blockSize];
				inBuffer = new byte[4 + blockSize];
			//	} else if (param.startsWith("-f")) {  // fast mode
			//		baudRate = 38400;
			} else if (param.startsWith("-v")) {  // verify
				doVerify = true;
			} else if (param.startsWith("-e")) {  // erase
				eraseOnly = true;
			} else if (param.startsWith("-f")) {  // write fuse bits
				writeFuses = true;
				spiByte2 = hex2int(param.charAt(2)) * 16 + hex2int(param.charAt(3));
				spiByte3 = hex2int(param.charAt(4)) * 16 + hex2int(param.charAt(5));
				spiByte4 = hex2int(param.charAt(6)) * 16 + hex2int(param.charAt(7));
			} else if (param.startsWith("-q")) {  // read fuse bits
				readFuses = true;
				//	spiByte2 = hex2int(param.charAt(2)) * 16 + hex2int(param.charAt(3));
				//	spiByte3 = hex2int(param.charAt(4)) * 16 + hex2int(param.charAt(5));
				//	spiByte4 = hex2int(param.charAt(6)) * 16 + hex2int(param.charAt(7));
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
			//	hexFile = hexFile.toUpperCase();
			//	file = new File(hexFile);
			//	if ( ! file.exists()) {
			//		System.out.println("AVRprog.main() file does not exist: "+hexFile) ;
			//		return;
			//	}
			System.out.println("AVRprog.main() file does not exist: "+hexFile) ;
			return;
		}
		

		try {
			openRS232(commPort);     // try this lib first 
		} catch (Exception ex) {
			System.out.println("AVRprog.main() ex: "+ex) ;
			
			//	try {
			//		//	sunPort(commPort);       // if not found try Sun's implementation
			//	} catch (Exception ex1) {
			//		System.out.println("AVRprog.main() ") ;
			//		System.out.println("either problem with RS232 lib ") ;
			//		System.out.println("or try  -c parameter, for instance -cCOM1 ") ;
			//		return;
			//	}
		}

		//	//	sunPort(commPort);
		//	openRS232(commPort);

		startTime = System.currentTimeMillis();

		boolean OK = action(doVerify);

		cleanup(OK);

		//	OK = false;
		if (! OK) {
			return;
		}

		if (doVerify || writeFuses || readFuses) {
			//
		} else {
			doVerify = true;
			OK = action(doVerify);
			cleanup(OK);
		}
		
		if (OK) {
			endTime = System.currentTimeMillis();
			diffTime = endTime - startTime ;
			System.out.println("AVRprog.main() fertig (millis:  "+diffTime+")") ;
		}

	}


	/**
	 * 
	 */
	static public boolean action(boolean doVerify) {

	
		char ch = 0;
		int linePos = 0;
		String lineString = null;
		
		int count = 0;
		int address = 0;		
		int type = 0;
		int high = 0;
		int low = 0;
		int pos = 0;
		int pageAddress = 0;
		int byteAddress = 0;

		int fileLength = 0;

		int byteCount = 0;
		int wordCount = 0;
		int totalBytes = 0;
		int totalWords = 0;
		int percent = 0;
		int border = 10;

		int iBlock = 4;
		int checkHigh = 0;
		int checkLow = 0;
		int trial = 0;		
		int offset = 0;

		outBuffer[0] = 27;   // TODO  wake up call 
		transfer(1,0);


		// enter programming mode:	
		outBuffer[0] = (byte) 'P';
		transfer(1,1);
		if (inBuffer[0] != 13) {
			return false;
		}
//	System.out.println("AVRprog.java: P ") ;

//			//	set LED  // TODO   
//	//	System.out.println("AVRprog.java: x ") ;
//			outBuffer[0] = (byte) 'x';
//			outBuffer[1] = 11;  // TODO  ? 
//			transfer(2,1);
//			if (inBuffer[0] != 13) {
//				cleanup(false);
//				return;
//			}

		if (writeFuses) {
			outBuffer[0] = (byte) 'f';
			outBuffer[1] = (byte) spiByte2;
			outBuffer[2] = (byte) spiByte3;
			outBuffer[3] = (byte) spiByte4;
			transfer(4,1);
			if (inBuffer[0] != 13) {
				return false;
			}
			return true;			
		} else  if (readFuses) {
			outBuffer[0] = (byte) 'q';
			//	outBuffer[1] = (byte) spiByte2;
			//	outBuffer[2] = (byte) spiByte3;
			//	outBuffer[3] = (byte) spiByte4;
			//	transfer(4,1);
			transfer(1,1);
			String fuses = byte2hex(inBuffer[0]);
			System.out.println("AVRprog.action() fuse bits: "+fuses) ;
			return true;			
		} else {
			//	
		}
				
		if (!doVerify) {
			// read signature bytes:	
			outBuffer[0] = (byte) 's';
			transfer(1,3);
			//	chip erase
			outBuffer[0] = (byte) 'e';
			transfer(1,1);
			if (inBuffer[0] != 13) {
				return false;
			}
//	System.out.println("AVRprog.java: e ") ;

			if (eraseOnly) {
				return true;
			}

//				//	leave programming mode
//	System.out.println("AVRprog.java: l ") ;
//				outBuffer[0] = (byte) 'L';
//				transfer(1,1);
//				if (inBuffer[0] != 13) {
//					return false;
//				}
//	System.out.println("AVRprog.java: P ") ;
//				outBuffer[0] = (byte) 'P';
//				transfer(1,1);
//				if (inBuffer[0] != 13) {
//					return false;
//				}
		}
		

		if (pageSize > 0) {
			//	set page mode
//	System.out.println("AVRprog.java: M  pageSize:  "+pageSize) ;
			outBuffer[0] = (byte) 'M';
			transfer(1,1);
			if (inBuffer[0] != 13) {
				return false;
			}
		}

		//	report autoincrement address
//	System.out.println("AVRprog.java: a ") ;
		outBuffer[0] = (byte) 'a';
		transfer(1,1);
		if (inBuffer[0] != (byte) 'Y') {
			return false;
		}

		//	set address
		outBuffer[0] = (byte) 'A';
//	System.out.println("AVRprog.java: A ") ;
		outBuffer[1] = 0;
		outBuffer[2] = 0;
		transfer(3,1);
		if (inBuffer[0] != 13) {
			return false;
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

		if (doVerify) {
			System.out.print("AVRprog.action() bin am verifizieren  .") ;
		} else {
			System.out.print("AVRprog.action() bin am programmieren .") ;
		}
		
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

		if (doVerify && blockMode) {
			
			if (inBuffer.length < byteCount) {
				inBuffer = new byte[byteCount];
			}

			//	set address
			outBuffer[0] = (byte) 'A';
			outBuffer[1] = (byte) 0;
			outBuffer[2] = (byte) 0;
			transfer(3,1);
			if (inBuffer[0] != 13) {
				return false;
			}
			
			outBuffer[0] = (byte) 'g';
			outBuffer[1] = (byte) (totalWords  >> 8);
			outBuffer[2] = (byte) (totalWords  >> 0);
			outBuffer[3] = (byte) 'F';

			transfer(4, byteCount);

		}
		
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
				
//	lineString = new String(line, 0, linePos);
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
//	System.out.println("AVRprog.java: pos "+pos) ;

						pos += 4;
						byteCount += 2;

						if (doVerify) {

							if (blockMode) {
								checkLow  = inBuffer[offset++];
								if (checkLow < 0) {
									checkLow += 256;
								}
								checkHigh = inBuffer[offset++];
								if (checkHigh < 0) {
									checkHigh += 256;
								}

								if (low != checkLow) {
									System.out.println("Keine Uebereinstimmung  :-(   offset: "+(offset-1)) ;
//	System.out.println("AVRprog.java:  "+byte2hex(low)+" <> "+byte2hex(checkLow)) ;
									return true;
								}

								if (high != checkHigh) {
									System.out.println("Keine Uebereinstimmung  :-(  ") ;
//	System.out.println("AVRprog.java:  "+byte2hex(high)+" <> "+byte2hex(checkHigh)) ;
									return true;
								}

							} else {

								outBuffer[0] = (byte) 'R';
								transfer(1,2);
								checkHigh = inBuffer[0];
								if (checkHigh < 0) {
									checkHigh += 256;
								}
								checkLow  = inBuffer[1];
								if (checkLow < 0) {
									checkLow += 256;
								}
	
								if (checkHigh != high || checkLow != low) {
									System.out.println() ;
									System.out.println("AVRprog.java: error at: "+word2hex(wordCount)) ;
									System.out.println("expected: high: "+byte2hex(high)+"  low: "+byte2hex(low)) ;
									System.out.println("received: high: "+byte2hex(checkHigh)+"  low: "+byte2hex(checkLow)) ;
									return true;
								}
								
							}
							
						} else {  //  program, not verify

							if (blockMode) {

								outBuffer[iBlock++] = (byte) low;
								outBuffer[iBlock++] = (byte) high;

								if (iBlock - 4 == blockSize) {
//	System.out.println("AVRprog.java: iBlock: "+iBlock+"   iBlock-4   "+(iBlock-4)+"  byteCount: "+byteCount) ;
									outBuffer[0] = (byte) 'B';
									outBuffer[1] = 0;  // would be high byte in 16-bit block sizes
									outBuffer[2] = (byte) blockSize;
									outBuffer[3] = (byte) 'F';  // means F:Flash  E:EEprom
	
//	System.out.println("AVRprog.java: write Block of "+blockSize) ;
									transfer(blockSize + 4, 1);
	//	System.out.println("AVRprog.java: done !!! ") ;
									if (inBuffer[0] != 13) {
										return false;
									}
									iBlock = 4;
								}
	
//	System.out.println("AVRprog.java: adress/H/L  "+address+"/"+high+"/"+low) ;
								
							} else {

								//	write program memory: low byte  (!!! must be written first)
								outBuffer[0] = (byte) 'c';
								outBuffer[1] = (byte) low;
								transfer(2,1);
								if (inBuffer[0] != 13) {
									return false;
								}
	
								//	write program memory: high byte
								outBuffer[0] = (byte) 'C';
								outBuffer[1] = (byte) high;
								transfer(2,1);
								if (inBuffer[0] != 13) {
									return false;
								}
								
							}

							if (pageSize > 0 && (byteCount == pageSize)) {
	
//	System.out.println("AVRprog.java: write page at address: "+pageAddress+" at byteCount: "+byteCount) ;
	
								high = (pageAddress >> 8) & 0xFF;
								low  = (pageAddress >> 0) & 0xFF;
	
								//	set address for next page write
								outBuffer[0] = (byte) 'A';
								outBuffer[1] = (byte) high;
								outBuffer[2] = (byte) low;
								transfer(3,1);
								if (inBuffer[0] != 13) {
									return false;
								}
	
								//	issue page write
								outBuffer[0] = (byte) 'm';
								transfer(1,1);
								if (inBuffer[0] != 13) {
									return false;
								}
	
								//	set address for next page load (byte write)
								outBuffer[0] = (byte) 'A';
								outBuffer[1] = 0;
								outBuffer[2] = 0;
								transfer(3,1);
								if (inBuffer[0] != 13) {
									return false;
								}
	
								pageAddress += (pageSize / 2); 
								byteCount = 0;	
							}

							
						}
						



//	System.out.println("AVRprog.java: wordCount: "+wordCount) ;

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

		if (doVerify) {
			return true;
		}

		if (blockMode && iBlock > 4) {
			blockSize = iBlock - 4;
			if (blockSize > 0) {
				outBuffer[0] = (byte)'B';
				outBuffer[1] = 0;  ; // would be high byte in 16-bit block sizes
				outBuffer[2] = (byte) blockSize;
				outBuffer[3] = (byte) 'F';  // means F:Flash  E:EEprom
//	System.out.println("AVRprog.java: last block:  "+blockSize) ;
				transfer(blockSize + 4, 1);
//	System.out.println("AVRprog.java: last block done ! ") ;
				if (inBuffer[0] != 13) {
					return false;
				}
				iBlock = 4;
			}
		}

		if (pageSize > 0) {
//	System.out.println("AVRprog.java: last page at address: "+pageAddress+" at byteCount: "+byteCount) ;

			high = (pageAddress >> 8) & 0xFF;
			low  = (pageAddress >> 0) & 0xFF;

			//	set address for next page write
			outBuffer[0] = (byte) 'A';
			outBuffer[1] = (byte) high;
			outBuffer[2] = (byte) low;
			transfer(3,1);
			if (inBuffer[0] != 13) {
				return false;
			}

			//	issue page write
			outBuffer[0] = (byte) 'm';
			transfer(1,1);
			if (inBuffer[0] != 13) {
				return false;
			}
		}
		
		return true;
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


	/**
	 * 
	 */
	static public String word2hex(int b) {
		int upper = b / 256;
		int lower = b % 256;
		return ""+byte2hex(upper)+byte2hex(lower);		
	}


	/**
	 * 
	 */
	static public String byte2hex(int b) {
		if (b < 0) {
			b += 256;
		}
		
		int upper = b / 16;
		int lower = b % 16;
		return ""+nibble2hex(upper)+nibble2hex(lower);		
	}


	/**
	 * 
	 */
	static public char nibble2hex(int n) {
		switch (n) {
		case  0: return  '0';
		case  1: return  '1';
		case  2: return  '2';
		case  3: return  '3';
		case  4: return  '4';
		case  5: return  '5';
		case  6: return  '6';
		case  7: return  '7';
		case  8: return  '8';
		case  9: return  '9';
		case  10: return  'A';
		case  11: return  'B';
		case  12: return  'C';
		case  13: return  'D';
		case  14: return  'E';
		case  15: return  'F';
		default :
			System.out.println("AVRprog.nibble2hex() wrong input: "+n) ;
			return '?';
		}
	}

}






// setup:  cursor:350,0; frame:32,102,976,600; bookmarks:227,0,127,0,0,0,0,0;
