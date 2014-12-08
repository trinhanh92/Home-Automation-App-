package anh.trinh.ble_demo.list_view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.ls.LSInput;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import anh.trinh.ble_demo.HomeActivity;
import anh.trinh.ble_demo.R;
import anh.trinh.ble_demo.ScenesFragment;
import anh.trinh.ble_demo.data.DeviceInfo;
import anh.trinh.ble_demo.data.DeviceTypeDef;

public class SceneExpListAdapter extends BaseExpandableListAdapter {

	private static final String TAG = "ExpandableListView";
	private Context mContext;
	private ArrayList<Scene_c> listOfScene;
	private ArrayList<DeviceInfo> listOfDevice;

	private static final String[] listOfCond = { "EQUAL", "LESS THAN",
			"LESS OR EQUAL", "GREATER THAN", "GREATER OR EQUAL", "CHANGE VAL",
			"IN RANGE", "IN RANGE EVDAY", "CHANGE VAL OVER" };

	private static final String[] listOfAction = { "SET DEV VAL" };

	public SceneExpListAdapter(Context mContext, ArrayList<Scene_c> listOfScene) {
		this.mContext = mContext;
		this.listOfScene = listOfScene;
	}

	@Override
	public Object getChild(int groupPos, int childPos) {
		// TODO Auto-generated method stub
		return listOfScene.get(groupPos).getRuleWithIndex(childPos);
	}

	@Override
	public long getChildId(int groupPos, int childPos) {
		// TODO Auto-generated method stub
		return listOfScene.get(groupPos).getRuleWithIndex(childPos).getID();
	}

	@Override
	public View getChildView(final int groupPos, final int childPos,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Create child view
		Rule_c mRuleObj = (Rule_c) getChild(groupPos, childPos);
		Log.i(TAG, "childView");
		if (convertView == null) {
			LayoutInflater inf = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inf.inflate(R.layout.rule_layout, null);
		}

		final LinearLayout llDevVal = (LinearLayout) convertView
				.findViewById(R.id.llDevVal);
		final LinearLayout llTimeRange = (LinearLayout) convertView
				.findViewById(R.id.llTimeRange);

		Spinner mCondChoose = (Spinner) convertView
				.findViewById(R.id.mCondChoose);
		Spinner condDevChoose = (Spinner) convertView
				.findViewById(R.id.condDevChoose);
		EditText condDevVal = (EditText) convertView
				.findViewById(R.id.condDevVal);

		EditText mStartTime = (EditText) convertView
				.findViewById(R.id.startTime);
		EditText mEndTime = (EditText) convertView.findViewById(R.id.stopTime);

		Spinner mActChoose = (Spinner) convertView
				.findViewById(R.id.mActChoose);
		Spinner actDevChoose = (Spinner) convertView
				.findViewById(R.id.actDevChoose);
		EditText actDevVal = (EditText) convertView
				.findViewById(R.id.actDevVal);

		// set data resource for Condition and Action spinner
		ArrayAdapter<String> mCondAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, listOfCond);
		mCondAdapter
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		ArrayAdapter<String> mActAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, listOfAction);
		mActAdapter
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

		// set adapter for spinner
		mCondChoose.setAdapter(mCondAdapter);
		mActChoose.setAdapter(mActAdapter);

		mCondChoose.setSelection(mRuleObj.getCond());
		mActChoose.setSelection(mRuleObj.getAction());

		mCondChoose.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				ScenesFragment.listOfScene.get(groupPos)
						.getRuleWithIndex(childPos).setCond(pos);
				// listOfScene.get(groupPos).getRuleWithIndex(groupPos).setCond(pos);
				if ((pos == 6) || (pos == 7)) {
					llDevVal.setVisibility(View.INVISIBLE);
					llTimeRange.setVisibility(View.VISIBLE);
					Log.i(TAG, "choose time");
				} else {
					llTimeRange.setVisibility(View.INVISIBLE);
					llDevVal.setVisibility(View.VISIBLE);
					Log.i(TAG, "choose dev");
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		mActChoose.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});

		if (llTimeRange.getVisibility() == View.VISIBLE) {
			mStartTime.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					// DateTimeInputDialog mDateTimeInput = new
					// DateTimeInputDialog();
					// mDateTimeInput.show(mContext.get, "dialog");
				}
			});
			mStartTime.setText(new SimpleDateFormat("yyyy/MM/dd hh:MM:ss")
					.format(new Date().getDate()));
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPos) {
		// TODO Auto-generated method stub
		return listOfScene.get(groupPos).getNumOfRule();
	}

	@Override
	public Object getGroup(int groupPos) {
		// TODO Auto-generated method stub
		return listOfScene.get(groupPos);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return listOfScene.size();
	}

	@Override
	public long getGroupId(int groupPos) {
		// TODO Auto-generated method stub
		return listOfScene.get(groupPos).getID();
	}

	@Override
	public View getGroupView(int groupPos, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Scene_c sceneObj = (Scene_c) getGroup(groupPos);
		Log.i(TAG, "groupView");
		final int scenePos = groupPos;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.scene_layout, null);

		}
		TextView mSceneName = (TextView) convertView
				.findViewById(R.id.sceneName);
		ImageButton btnAddRule = (ImageButton) convertView
				.findViewById(R.id.btnAddRule);
		ImageButton btnSceneSave = (ImageButton) convertView
				.findViewById(R.id.btnSceneSave);
		mSceneName.setText(sceneObj.getName());

		btnAddRule.setFocusable(false);
		btnSceneSave.setFocusable(false);
		btnAddRule.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				addNewRule(scenePos);
			}
		});

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Add new rule to current Scene
	 * 
	 * @param scenePos
	 */
	private void addNewRule(int scenePos) {
		if (getChildrenCount(scenePos) < 3) {
			Rule_c mRule = new Rule_c();
			mRule.setID(0);
			listOfScene.get(scenePos).addRule(mRule);
			Log.i("ExpandableList",
					Integer.toString(listOfScene.get(scenePos).getNumOfRule()));
			notifyDataSetChanged();
		} else {
			Log.i(TAG, "Max Num of Rule is 3");
		}
	}

	private ArrayList<String> getListOfDeviceName(
			ArrayList<DeviceInfo> listOfDevice) {
		ArrayList<String> listDevName = new ArrayList<String>();
		String mDevName;
		for (int i = 0; i < listOfDevice.size(); i++) {
			switch ((byte) listOfDevice.get(i).getDevID()) {
			case DeviceTypeDef.BUTTON:
				mDevName = "BUTTON";
				break;
			case DeviceTypeDef.BUZZER:
				mDevName = "BUZZER";
				break;
			case DeviceTypeDef.DIMMER:
				mDevName = "DIMMER";
				break;
			case DeviceTypeDef.GAS_SENSOR:
				mDevName = "GAS SENSOR";
				break;
			case DeviceTypeDef.LEVEL_BULB:
				mDevName = "LEVEL BULB";
				break;
			case DeviceTypeDef.LUMI_SENSOR:
				mDevName = "LUMI SENSOR";
				break;
			case DeviceTypeDef.ON_OFF_BULB:
				mDevName = "ON OFF BULB";
				break;
			case DeviceTypeDef.PIR_SENSOR:
				mDevName = "PIR SENSOR";
				break;
			case DeviceTypeDef.RGB_LED:
				mDevName = "RGB LED";
				break;
			case DeviceTypeDef.SERVO_SG90:
				mDevName = "SERVO SG90";
				break;
			case DeviceTypeDef.SWITCH:
				mDevName = "SWTICH";
				break;
			case DeviceTypeDef.TEMP_SENSOR:
				mDevName = "TEMP SENSOR";
				break;

			default:
				mDevName = "BUTTON";
				break;
			}
			if(!listDevName.equals(mDevName)){
				listDevName.add(mDevName);
			}
		}
		return listDevName;

	}
}
