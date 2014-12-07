package anh.trinh.ble_demo.data;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import android.graphics.Color;
import anh.trinh.ble_demo.R;
import anh.trinh.ble_demo.list_view.Device_c;

/**
 * @version 1.0
 * @author Anh Trinh
 * @category Data Type
 *
 */


public class DataConversion {
	
	/**
	 * Convert byte to unsigned byte
	 * 
	 * @param number
	 * @return
	 */
	 public static int byte2Unsigned(byte number){
    	int result;
    	ByteBuffer mTempBuf = ByteBuffer.allocate(4);
    	if( (number & 0x80) != 0){
//    		result = number*0x000000ff;
    		result = mTempBuf.put(new byte[]{0x00, 0x00, 0x00, number}).getInt(0);
    		mTempBuf.clear();
    	}
    	else{
    		result = number;
    	}
    	return result;
	 }
	 
	 public static int short2Unsigned(short devVal){
//		int retVal;
//	    ByteBuffer mTempBuf = ByteBuffer.allocate(4);
//	    if((devVal & 0x8000) != 0){
//	    	revVal = devVal*0x0000ffff;
//	    	
//	    }
//	    else{
//	    	
//	    }
		return (int)devVal & 0x0000FFFF;
		}
	 
	 
	 /**
	 * Convert color 32 bit to 16 bit
	 * @param mColor
	 * @return
	 */
	public static short color32BitTo16Bit(int mColor){
		short retVal;
		int cRed, cGreen, cBlue;
		
		cRed = Color.red(mColor)*31/255;
		cGreen =  Color.green(mColor)*63/255;
		cBlue = Color.blue(mColor)*31/255;	
		retVal = (short) ((cRed<<11) | (cGreen<<5) | (cBlue));
		
		return retVal;
	}
	 
	/**
	 * Convert color 16 bit to 32 bit
	 * 
	 * @param mColor
	 * @return
	 */
	public static int color16BitTo32Bit(short mColor){
		int cRed, cGreen, cBlue;
		cRed = (mColor>>11)*255/31;
		cGreen = ((mColor>>5)&0x003F)*255/63;
		cBlue = ((mColor)&0x001F)*255/31;
		return Color.rgb(cRed, cGreen, cBlue);
	}
	
	 /**
	  * Convert integer to byte[] array
	  * 
	  * @param inNum
	  * @return
	  */
	 public static byte[] int2ByteArr(int inNum){
		 byte[] retBuf = new byte[4];
		 retBuf[0] = (byte)(inNum >> 24);
		 retBuf[1] = (byte)(inNum >> 16);
		 retBuf[2] = (byte)(inNum >> 8);
		 retBuf[3] = (byte)(inNum );
		 return retBuf;
		 
	 }
	 
	 /**
	  * Convert short to byte[] array
	  * 
	  * @param inNum
	  * @return
	  */
	 public static byte[] short2ByteArr(short inNum){
		 byte[] retBuf = new byte[2];
		 retBuf[0] = (byte)(inNum >> 8);
		 retBuf[1] = (byte)(inNum);
		 return retBuf;
	 }
	 
	 
	 /**
	  * Convert device info to byte[] array
	  * 
	  * @param devInfo
	  * @return
	  */
	 public static byte[] devInfo2ByteArr(int devID, short devVal){
		 ByteBuffer tempBuf = ByteBuffer.allocate(6);
		 tempBuf.put(int2ByteArr(devID));
		 tempBuf.put(short2ByteArr(devVal));
		return tempBuf.array(); 
	 }
	 
	 
	 /**
	  * Convert byte aray to integer number
	  * 
	  * @param dataBuf
	  * @return
	  */
	 public static int byteArr2Int(byte[] dataBuf){
		 ByteBuffer tempBuf = ByteBuffer.allocate(4);
		 tempBuf.put(dataBuf[0]);
		 tempBuf.put(dataBuf[1]);
		 tempBuf.put(dataBuf[2]);
		 tempBuf.put(dataBuf[3]);
//		 int retVal;
//		 retVal = (int) dataBuf[0] <<24 | (int) dataBuf[1] <<16 | (int) dataBuf[2] <<8  | (int) dataBuf[3] ;  
		 return tempBuf.getInt(0);
	 }
	 
	 /**
	  * 
	  * @param dataBuf
	  * @return
	  */
	 public static short byteArr2Short(byte[] dataBuf){
		 ByteBuffer tempBuf = ByteBuffer.allocate(2);
		 tempBuf.put(dataBuf[4]);
		 tempBuf.put(dataBuf[5]);
//		 short retVal;
//		 retVal = (short) (((short) (dataBuf[4] <<8) & 0xFF00)  | (short) dataBuf[5] & 0x00FF) ;  
		 return tempBuf.getShort(0);
	 }
	 
	 /**
	  * 
	  * Join 2 byte number to short number
	  * @param byteH
	  * @param byteL
	  * @return
	  */
	 public static short byteArr2Short(byte byteH, byte byteL){
		 ByteBuffer tempBuf = ByteBuffer.allocate(2);
		 tempBuf.put(byteH);
		 tempBuf.put(byteL);
		return tempBuf.getShort(0);
	 }

}
