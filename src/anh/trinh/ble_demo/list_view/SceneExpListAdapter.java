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
import anh.trinh.ble_demo.DeviceControlFragment;
import anh.trinh.ble_demo.HomeActivity;
import anh.trinh.ble_demo.R;
import anh.trinh.ble_demo.ScenesFragment;
import anh.trinh.ble_demo.custom_view.DateDisplayPicker;
import anh.trinh.ble_demo.custom_view.DeviceArrayAdapter;
import anh.trinh.ble_demo.custom_view.TimeDisplayPicker;
import anh.trinh.ble_demo.data.DeviceInfo;
import anh.trinh.ble_demo.data.DeviceTypeDef;

public class SceneExpListAdapter extends BaseExpandableListAdapter {

	private static final String TAG = "ExpandableListView";
	private HomeActivity mContext;
	private ArrayList<Scene_c> listOfScene;
	private ArrayList<DeviceInfo> listOfDevice;

	private static final String[] listOfCond = { "EQUAL", "LESS THAN",
			"LESS OR EQUAL", "GREATER THAN", "GREATER OR EQUAL", "CHANGE VAL",
			"IN RANGE", "IN RANGE EVDAY", "CHANGE VAL OVER" };

	private static final String[] listOfAction = { "SET DEV VAL" };

	public SceneExpListAdapter(HomeActivity mContext,
			ArrayList<Scene_c> listOfScene) {
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
		return listOfScene.get(groupPos).getRuleWithIndex(childPos)
				.getRuleIndex();
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

		final TextView mTitlle1 = (TextView) convertView
				.findViewById(R.id.tvTittle1);
		final TextView mTittle2 = (TextView) convertView
				.findViewById(R.id.tvTittle2);

		Spinner mCondChoose = (Spinner) convertView
				.findViewById(R.id.mCondChoose);
		Spinner condDevChoose = (Spinner) convertView
				.findViewById(R.id.condDevChoose);
		EditText condDevVal = (EditText) convertView
				.findViewById(R.id.condDevVal);

		DateDisplayPicker mFromDate = (DateDisplayPicker) convertView
				.findViewById(R.id.fromDate);
		TimeDisplayPicker mFromTime = (TimeDisplayPicker) convertView
				.findViewById(R.id.fromTime);
		DateDisplayPicker mToDate = (DateDisplayPicker) convertView
				.findViewById(R.id.toDate);
		TimeDisplayPicker mToTime = (TimeDisplayPicker) convertView
				.findViewById(R.id.toTime);

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

		// Choose Device
		DeviceArrayAdapter mDevAdapter = new DeviceArrayAdapter(mContext,
				android.R.layout.simple_spinner_item, mContext.mDevInfoList);
		mDevAdapter
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

		// set adapter for spinner
		mCondChoose.setAdapter(mCondAdapter);
		mActChoose.setAdapter(mActAdapter);
		condDevChoose.setAdapter(mDevAdapter);
		actDevChoose.setAdapter(mDevAdapter);

		// Set selection position for spinner
		mCondChoose.setSelection(mRuleObj.getCond());
		mActChoose.setSelection(mRuleObj.getAction());

		ScenesFragment.listOfScene.get(groupPos).getRuleWithIndex(childPos)
				.setRuleIndex(childPos);
		mCondChoose.setSelection(mRuleObj.getCond());
		mActChoose.setSelection(mRuleObj.getAction());
		actDevChoose.setSelection(findDevIndexByID(mContext.mDevInfoList,
				mRuleObj.getActDevId()));
		actDevVal.setText(mRuleObj.getActDevVal());

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
					mTitlle1.setText("From:");
					mTittle2.setText("To:");
					Log.i(TAG, "choose time");
				} else {
					llTimeRange.setVisibility(View.INVISIBLE);
					llDevVal.setVisibility(View.VISIBLE);
					mTitlle1.setText("Device");
					mTittle2.setText("Value");
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
			mFromDate.setText(mRuleObj.getStartTime());
			mFromTime.setText(mRuleObj.getStartTime());
			mToDate.setText(mRuleObj.getEndTime());
			mToTime.setText(mRuleObj.getEndTime());

		}
		if (llDevVal.getVisibility() == View.VISIBLE) {
			condDevChoose.setSelection(findDevIndexByID(mContext.mDevInfoList,
					mRuleObj.getCondDevId()));
			condDevVal.setText(mRuleObj.getCondDevVal());
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
		Rule_c mRule = new Rule_c();
		listOfScene.get(scenePos).addRule(mRule);
		Log.i("ExpandableList",
				Integer.toString(listOfScene.get(scenePos).getNumOfRule()));
		notifyDataSetChanged();
	}

	/**
	 * Find device index in List by device ID
	 * 
	 * @param listOfDev
	 * @param devID
	 * @return
	 */
	private int findDevIndexByID(ArrayList<DeviceInfo> listOfDev, int devID) {
		for (int i = 0; i < listOfDev.size(); i++) {
			if (listOfDev.get(i).getDevID() == devID) {
				return i;
			}
		}
		return -1;
	}
}
