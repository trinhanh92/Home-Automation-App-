package anh.trinh.ble_demo.data;

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

	public DeviceInfo(int devIdx, int devID, short devVal) {
		this.devIdx = devIdx;
		this.devID = devID;
		this.devVal = devVal;
	}

	/**
	 * Set device index value
	 * 
	 * @param devIdx
	 */
	public void setDevIdx(int devIdx) {
		this.devIdx = devIdx;
	}

	/**
	 * Get device index value
	 * 
	 * @return devIdx
	 */
	public int getDevIdx() {
		return devIdx;
	}

	public void setDevID(int devID) {
		this.devID = devID;
	}

	public int getDevID() {
		return devID;
	}

	public String getName() {
		String devName;
		switch ((byte) devID) {

		case DeviceTypeDef.BUTTON:
			devName = "Button";
			break;
		case DeviceTypeDef.BUZZER:
			devName = "Buzzer";
			break;
		case DeviceTypeDef.DIMMER:
			devName = "Dimmer";
			break;
		case DeviceTypeDef.GAS_SENSOR:
			devName = "Gas Sensor";
			break;
		case DeviceTypeDef.LEVEL_BULB:
			devName = "Level Bulb";
			break;
		case DeviceTypeDef.LUMI_SENSOR:
			devName = "Light Sensor";
			break;
		case DeviceTypeDef.ON_OFF_BULB:
			devName = "On/Off Bulb";
			break;
		case DeviceTypeDef.PIR_SENSOR:
			devName = "PIR Sensor";
			break;
		case DeviceTypeDef.RGB_LED:
			devName = "RGB Led";
			break;
		case DeviceTypeDef.SERVO_SG90:
			devName = "Servo SG90";
			break;
		case DeviceTypeDef.SWITCH:
			devName = "Switch";
			break;
		case DeviceTypeDef.TEMP_SENSOR:
			devName = "Temp Sensor";
			break;
		case DeviceTypeDef.SOIL_HUMI:
			devName = "Soil Humi";
			break;

		default:
			devName = "Unknown Device";
			break;
		}
		devName = devName + " (" + Integer.toString(devID >> 24) + "-"
				+ Integer.toHexString((devID  & 0x00FFFFFF) >> 8) + ")";
		return devName;
	}

	public void setDevVal(short devVal) {
		this.devVal = devVal;
	}

	public short getDevVal() {
		return devVal;
	}

}
