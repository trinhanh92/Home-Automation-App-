package anh.trinh.ble_demo.data;

import anh.trinh.ble_demo.R;

public final class CommandID {
	
	// Message type
	public static final int SET = 0x00;
	public static final int GET = 0x01;
	
	// Devices
	public static final int DEV_VAL 				= 0x00;
	public static final int NUM_OF_DEVS 			= 0x01;
	public static final int DEV_WITH_INDEX			= 0x02;
	
	//Scenes
	public static final int NUM_OF_SCENES 			= 0x03;
	public static final int ACT_SCENE_WITH_INDEX 	= 0x04;
	public static final int INACT_SCENE_WITH_INDEX 	= 0x05;
	public static final int NEW_SCENE				= 0x09;
	public static final int REMOVE_SCENE			= 0x0a;
	public static final int RENAME_SCENE            = 0x0b;
	
	//Rules
	public static final int NUM_OF_RULES 	= 0x06;
	public static final int RULE_WITH_INDEX = 0x07;
	
	//Zone
	public final static int ZONE_NAME = 0x08;
	
}
