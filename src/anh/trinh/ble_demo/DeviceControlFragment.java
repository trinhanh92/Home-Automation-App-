package anh.trinh.ble_demo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import anh.trinh.ble_demo.data.DataConversion;
import anh.trinh.ble_demo.data.DeviceInfo;
import anh.trinh.ble_demo.list_view.Device_c;
import anh.trinh.ble_demo.list_view.DeviceControlExpListAdapter;
import anh.trinh.ble_demo.list_view.Zone_c;

public class DeviceControlFragment extends Fragment{
	private ExpandableListView 	lvDevControl;
	private DeviceControlExpListAdapter mAdapter;
	public ArrayList<Zone_c> 	listParent 		= new ArrayList<Zone_c>();
	private final String 		TAG				= "DeviceControlFragment";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.fragment_device_control, container, false);
		
		lvDevControl	= (ExpandableListView) rootView
										.findViewById(R.id.elvDeviceControl);
		
		Log.i(TAG, "enter process");
		
		mAdapter = new DeviceControlExpListAdapter((HomeActivity) getActivity(), listParent);
		lvDevControl.setGroupIndicator(null);
		lvDevControl.setAdapter(mAdapter);
		
		return rootView;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "onActivity create");
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		Log.i(TAG, "onDestroyView");
	}
	
	
	
	public void updateUI(ArrayList<DeviceInfo> deviceList){
		prepareDataForDisp(deviceList);
		if(!listParent.isEmpty()){
			mAdapter.notifyDataSetChanged();
			
//			for(int i = 0; i < mAdapter.getGroupCount(); i++){
//				if(!lvDevControl.isGroupExpanded(i)){
//					lvDevControl.expandGroup(i);
//				}
//			}
		}
	}
	
	/**
	 * Initial data for Expandable ListView
	 */
	private void prepareDataForDisp(ArrayList<DeviceInfo> deviceList){
		ArrayList<DeviceInfo> mDevList =  new ArrayList<DeviceInfo>();
		mDevList = 	deviceList;
		ByteBuffer devID = ByteBuffer.allocate(4);
		int mZoneId;
		int mDevId;
		Zone_c zone = null;
		listParent.clear();
		for (int i = 0; i < mDevList.size(); i++){
			mZoneId = DataConversion.byte2Unsigned(devID.putInt(mDevList.get(i).getDevID()).get(0) );
			devID.clear();
			mDevId = mDevList.get(i).getDevID();
			devID.clear();
			
			if(!searchZone(listParent, mZoneId)){
				zone = new Zone_c();
				zone.setName(mZoneId);
				listParent.add(zone);
			}
			
			Device_c device = new Device_c();
			device.setName(mDevId);
			device.setVal(mDevList.get(i).getDevVal());
			listParent.get(getZoneIndex(listParent, mZoneId)).addChildListItem(device);
		}
		
	}
	
	/**
	 * Search zone by ID
	 * 
	 * @param parentList
	 * @param item
	 * @return
	 */
	private boolean searchZone(ArrayList<Zone_c> parentList, int mZoneId){
		if(parentList.isEmpty() ){
			return false;
		}
		for(Zone_c parent : parentList){
			if(parent.getID() == mZoneId){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get zone index in List
	 * 
	 * @param zoneList
	 * @param mZoneId
	 * @return
	 */
	private int getZoneIndex(ArrayList<Zone_c> zoneList, int mZoneId){
		
		for(int i = 0; i< zoneList.size(); i++){
			if(zoneList.get(i).getID() == mZoneId){
				return i;
			}
		}
		return -1;
	}
	
}
