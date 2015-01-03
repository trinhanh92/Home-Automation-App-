package anh.trinh.ble_demo.list_view;

import anh.trinh.ble_demo.data.DeviceTypeDef;

public class Device_c {
	
	private String name;
	private short	val;
	private int childID;
	
	public Device_c(String name, short val){
		this.name = name;
		this.val  = val;
	}
	
	public Device_c() {
		// TODO Auto-generated constructor stub
	}

	public String getName(){
		return name;
	}
	
	public short getVal(){
		return val;
	}
	
	public void setName(int devID){
		this.childID = devID;
		switch ((byte)devID) {
		case DeviceTypeDef.SWITCH:
			this.name = "Switch";
			break;
		case DeviceTypeDef.BUTTON:
			this.name = "Button";
			break;
		case DeviceTypeDef.DIMMER:
			this.name = "Dimmer";
			break;	
		case DeviceTypeDef.BUZZER:
			this.name = "Buzzer";
			break;
		case DeviceTypeDef.GAS_SENSOR:
			this.name = "Gas Sensor";
			break;
		case DeviceTypeDef.LEVEL_BULB:
			this.name = "Level Bulb";
			break;
		case DeviceTypeDef.LUMI_SENSOR:
			this.name = "Light Sensor";
			break;
		case DeviceTypeDef.TEMP_SENSOR:
			this.name = "Temp Sensor";
			break;
		case DeviceTypeDef.PIR_SENSOR:
			this.name = "PIR Sensor";
			break;
		case DeviceTypeDef.SERVO_SG90:
			this.name = "Servo SG90";
			break;
		case DeviceTypeDef.ON_OFF_BULB:
			this.name = "ON/OFF Bulb";
			break;
		case DeviceTypeDef.RGB_LED:
			this.name = "RGB Led";
			break;
		case DeviceTypeDef.SOIL_HUMI:
			this.name = "Soil Moiter";
			break;
		default:
			this.name = "Unknown Device";
			break;
		}
		this.name += " (" + Integer.toString(devID >> 24) + "-"
				+ Integer.toHexString((devID  & 0x00FFFFFF) >> 8) + ")";
	}
	
	public void setID(int devID){
		this.childID = devID;
	}
	
	public void setVal(short val){
		this.val = val;
	}
	
	public int getID(){
		return childID;
	}
}
