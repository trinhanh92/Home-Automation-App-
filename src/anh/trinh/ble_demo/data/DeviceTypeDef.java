package anh.trinh.ble_demo.data;

import anh.trinh.ble_demo.R;

public final class DeviceTypeDef {
	
	// device input
	public static final int SWITCH 			= 0x01;
	public static final int BUTTON 			= 0x02;
	public static final int DIMMER 			= 0x03;
	
	public static final int TEMP_SENSOR 	= 0x30;
	public static final int LUMI_SENSOR 	= 0x31;
	public static final int GAS_SENSOR 		= 0x32;
	public static final int PIR_SENSOR 		= 0x38;
	// device output
	public static final int ON_OFF_BULB 	= 0x78;
	public static final int BUZZER 			= 0x79;
	public static final int LEVEL_BULB		= 0x42;
	public static final int RGB_LED 		= 0x43;
	public static final int SERVO_SG90		= 0x44;
}
