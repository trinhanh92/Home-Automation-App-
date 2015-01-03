package anh.trinh.ble_demo.custom_view;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import anh.trinh.ble_demo.data.DeviceInfo;

public class DeviceArrayAdapter extends ArrayAdapter<DeviceInfo> {
	private Context mContext;
	private ArrayList<DeviceInfo> mListOfDevs;


	public DeviceArrayAdapter(Context context, int resource, ArrayList<DeviceInfo> devices) {
		super(context, resource, devices);
		this.mContext = context;
		this.mListOfDevs = devices;
	}

	public int getCount(){
		return mListOfDevs.size();
	}
	public DeviceInfo getDevice(int pos){
		return mListOfDevs.get(pos);
	}
	
	public int getDeviceId(int pos){
		return mListOfDevs.get(pos).getDevID();
	}
	
	public String getDeviceName(int pos){
		return getDevice(pos).getName();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(getDeviceName(position));
        return label;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
//		TextView listItem = (TextView)super.getView(position, convertView, parent);
		CheckedTextView listItem = new CheckedTextView(mContext);
		listItem.setText(getDeviceName(position));
		listItem.setChecked(true);
		return listItem;
	}

}
