
package anh.trinh.ble_demo.data;

import anh.trinh.ble_demo.R;

/**
 * @version 1.0
 * @author Anh Trinh
 * 
 *
 */

public class DeviceInfo {
	
	private int devIdx;
	private int devID;
	private short devVal;
	
	public DeviceInfo() {
		// TODO Auto-generated constructor stub
	}
	
	public DeviceInfo(int devIdx, int devID, short devVal){
		this.devIdx = devIdx;
		this.devID 	= devID;
		this.devVal = devVal;
	}
	
	/**
	 * Set device index value
	 * 
	 * @param devIdx
	 */
	public void setDevIdx(int devIdx){
		this.devIdx = devIdx;
	}
	
	/**
	 * Get device index value
	 * 
	 * @return devIdx
	 */
	public int getDevIdx(){
		return devIdx;
	}
	
	public void setDevID(int devID){
		this.devID = devID;
	}
	
	public int getDevID(){
		return devID;
	}
	
	public String getName(){
		String devName;
		switch ((byte)devID) {
		
		case DeviceTypeDef.BUTTON:
			devName = "BUTTON";
			break;
		case DeviceTypeDef.BUZZER:
			devName = "BUZZER";
			break;
		case DeviceTypeDef.DIMMER:
			devName = "DIMMER";
			break;
		case DeviceTypeDef.GAS_SENSOR:
			devName = "GAS SENSOR";
			break;
		case DeviceTypeDef.LEVEL_BULB:
			devName = "LEVEL BULB";
			break;
		case DeviceTypeDef.LUMI_SENSOR:
			devName = "LUMI SENSOR";
			break;
		case DeviceTypeDef.ON_OFF_BULB:
			devName = "ON OFF BULB";
			break;
		case DeviceTypeDef.PIR_SENSOR:
			devName = "PIR SENSOR";
			break;
		case DeviceTypeDef.RGB_LED:
			devName = "RGB LED";
			break;
		case DeviceTypeDef.SERVO_SG90:
			devName = "SERVO SG90";
			break;
		case DeviceTypeDef.SWITCH:
			devName = "SWICH";
			break;
		case DeviceTypeDef.TEMP_SENSOR:
			devName = "TEMP SENSOR";
			break;

		default:
			devName = "UNKNOWN DEVICE";
			break;
		}
		return devName;
	}
	
	public void setDevVal(short devVal){
		this.devVal = devVal;
	}
	
	public short getDevVal(){
		return devVal;
	}
	

}
