package anh.trinh.ble_demo.list_view;

import java.nio.ByteBuffer;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import anh.trinh.ble_demo.HomeActivity;
import anh.trinh.ble_demo.R;
import anh.trinh.ble_demo.custom_view.NDSpinner;
import anh.trinh.ble_demo.data.BTMessageType;
import anh.trinh.ble_demo.data.BluetoothMessage;
import anh.trinh.ble_demo.data.CommandID;
import anh.trinh.ble_demo.data.ConditionDef;
import anh.trinh.ble_demo.data.DeviceInfo;

public class SceneExpListAdapter extends BaseExpandableListAdapter {

	private static final String TAG = "ExpandableListView";
	private HomeActivity mContext;
	private ArrayList<Scene_c> listOfScene;
	private ArrayList<DeviceInfo> inDeviceList = new ArrayList<DeviceInfo>();
	private ArrayList<DeviceInfo> outDeviceList = new ArrayList<DeviceInfo>();

	public final String[] listOfCond = { "EQUAL", "LESS THAN", "LESS OR EQUAL",
			"GREATER THAN", "GREATER OR EQUAL", "CHANGE VAL", "IN RANGE",
			"IN RANGE EVDAY", "CHANGE VAL OVER THR" };

	public static final String[] listOfAction = { "SET DEV VAL" };

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
	public View getChildView(int groupPos, int childPos, boolean isLastChild,
			View convertView, ViewGroup parent) {
		// TODO Create child view
		final Rule_c mRuleObj = (Rule_c) getChild(groupPos, childPos);
		final int scenePos = groupPos;
		final int rulePos = childPos;
		// Log.i(TAG, "childView");
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

		NDSpinner mCondChoose = (NDSpinner) convertView
				.findViewById(R.id.mCondChoose);
		final NDSpinner condDevChoose = (NDSpinner) convertView
				.findViewById(R.id.condDevChoose);
		final EditText condDevVal = (EditText) convertView
				.findViewById(R.id.condDevVal);

		final ImageButton mBtnSave = (ImageButton) convertView
				.findViewById(R.id.btnSave);
		mBtnSave.setFocusable(false);

		final ImageButton mBtnDel = (ImageButton) convertView
				.findViewById(R.id.btnDel);
		mBtnDel.setFocusable(false);

		TextView mFromDate = (TextView) convertView.findViewById(R.id.fromDate);
		TextView mFromTime = (TextView) convertView.findViewById(R.id.fromTime);
		TextView mToDate = (TextView) convertView.findViewById(R.id.toDate);
		TextView mToTime = (TextView) convertView.findViewById(R.id.toTime);

		NDSpinner mActChoose = (NDSpinner) convertView
				.findViewById(R.id.mActChoose);
		final NDSpinner actDevChoose = (NDSpinner) convertView
				.findViewById(R.id.actDevChoose);
		final EditText actDevVal = (EditText) convertView
				.findViewById(R.id.actDevVal);

		// set data resource for Condition and Action spinner
		getTypeOfDevice(mContext.mDevInfoList);
		ArrayAdapter<String> mCondAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, listOfCond);
		mCondAdapter
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

		ArrayAdapter<String> mActAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, listOfAction);
		mActAdapter
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

		// Choose Device
		ArrayAdapter<String> mCondDevAdapter = new ArrayAdapter<String>(
				mContext, android.R.layout.simple_spinner_item,
				getDeviceNameList(inDeviceList));
		mCondDevAdapter
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

		ArrayAdapter<String> mActDevAdapter = new ArrayAdapter<String>(
				mContext, android.R.layout.simple_spinner_item,
				getDeviceNameList(outDeviceList));
		mActDevAdapter
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

		// set adapter for spinner
		mCondChoose.setAdapter(mCondAdapter);
		mActChoose.setAdapter(mActAdapter);
		condDevChoose.setAdapter(mCondDevAdapter);
		actDevChoose.setAdapter(mActDevAdapter);

		mCondChoose.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				listOfScene.get(scenePos).getRuleWithIndex(rulePos)
						.setCond(pos);
				if ((pos == ConditionDef.IN_RANGE)
						|| (pos == ConditionDef.IN_RANGE_EVDAY)) {
					llDevVal.setVisibility(View.INVISIBLE);
					llTimeRange.setVisibility(View.VISIBLE);
					mTitlle1.setText("From:");
					mTittle2.setText("To:");
				} else {
					llTimeRange.setVisibility(View.INVISIBLE);
					llDevVal.setVisibility(View.VISIBLE);
					mTitlle1.setText("Device");
					mTittle2.setText("Value");
				}

				condDevChoose
						.setOnItemSelectedListener(new OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> arg0,
									View arg1, int pos, long arg3) {
								// TODO Auto-generated method stub
								listOfScene
										.get(scenePos)
										.getRuleWithIndex(rulePos)
										.setCondDevId(
												inDeviceList.get(pos)
														.getDevID());
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {

							}
						});

				actDevChoose
						.setOnItemSelectedListener(new OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> arg0,
									View arg1, int pos, long arg3) {
								// TODO Auto-generated method stub
								listOfScene
										.get(scenePos)
										.getRuleWithIndex(rulePos)
										.setActDevId(
												outDeviceList.get(pos)
														.getDevID());
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {
								// TODO Auto-generated method stub

							}
						});
				//
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		mActChoose.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				listOfScene.get(scenePos).getRuleWithIndex(rulePos)
						.setAction(pos);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});

		if (mRuleObj != null) {
			mCondChoose.setSelection(mRuleObj.getCond());
			mActChoose.setSelection(mRuleObj.getAction());
			actDevChoose.setSelection(findDevIndexByID(outDeviceList,
					mRuleObj.getActDevId()));
			condDevChoose.setSelection(findDevIndexByID(inDeviceList,
					mRuleObj.getCondDevId()));
			actDevVal.setText(Integer.toString(mRuleObj.getActDevVal()));
			condDevVal.setText(Integer.toString(mRuleObj.getCondDevVal()));

			// if (llTimeRange.getVisibility() == View.VISIBLE) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			SimpleDateFormat stf = new SimpleDateFormat("HH:mm");

			if (mRuleObj.getCond() == ConditionDef.IN_RANGE_EVDAY) {
				mFromDate.setText("--");
				mToDate.setText("--");
			} else {
				mFromDate.setText(sdf.format(new Date(dosToJavaTime(mRuleObj
						.getStartDate()))));
				mToDate.setText(sdf.format(new Date(dosToJavaTime(mRuleObj
						.getEndDate()))));
			}
			mFromTime.setText(stf.format(new Time(dosToJavaTime(mRuleObj
					.getStartTime()))));
			mToTime.setText(stf.format(new Time(dosToJavaTime(mRuleObj
					.getEndTime()))));

			mFromDate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					showDatePickerDialog(scenePos, rulePos, (TextView) v);
				}
			});

			mFromTime.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					showTimePickerDialog(scenePos, rulePos, (TextView) v);
				}
			});

			mToDate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					showDatePickerDialog(scenePos, rulePos, (TextView) v);
				}
			});

			mToTime.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					showTimePickerDialog(scenePos, rulePos, (TextView) v);
				}
			});
			// }
		}

		mBtnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (!condDevVal.getText().toString().matches("")) {
					((Rule_c) getChild(scenePos, rulePos))
							.setCondDevVal(Integer.parseInt(condDevVal
									.getText().toString()));
				} else {
					((Rule_c) getChild(scenePos, rulePos))
							.setCondDevVal((short) 0);
				}
				if (!actDevVal.getText().toString().matches("")) {
					((Rule_c) getChild(scenePos, rulePos)).setActDevVal(Integer
							.parseInt(actDevVal.getText().toString()));
				} else {
					((Rule_c) getChild(scenePos, rulePos))
							.setActDevVal((short) 0);
				}
				Toast.makeText(mContext.getApplicationContext(), "saved rule",
						Toast.LENGTH_SHORT).show();
			}
		});

		mBtnDel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder mBuiler = new AlertDialog.Builder(mContext);
				mBuiler.setTitle("Do you want to delete this rule?");
				mBuiler.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								listOfScene.get(scenePos).getListOfRules()
										.remove(rulePos);
								notifyDataSetChanged();
								Toast.makeText(
										mContext.getApplicationContext(),
										"deleted rule", Toast.LENGTH_SHORT)
										.show();
							}
						});
				mBuiler.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
				mBuiler.show();

			}
		});
		// mBtnMenu.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// showRuleMenuPopup(mBtnMenu, mContext, rulePos, scenePos,
		// condDevVal, actDevVal);
		// }
		// });
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
		final Scene_c sceneObj = (Scene_c) getGroup(groupPos);
		// Log.i(TAG, "groupView");
		final int scenePos = groupPos;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.scene_layout, null);

		}
		final CheckBox isActive = (CheckBox) convertView
				.findViewById(R.id.isActive);
		TextView mSceneName = (TextView) convertView
				.findViewById(R.id.sceneName);
		final ImageButton btnSceneMenu = (ImageButton) convertView
				.findViewById(R.id.btnSceneMenu);
		mSceneName.setText(sceneObj.getName());
		isActive.setFocusable(false);
		isActive.setChecked(sceneObj.getActived());
		btnSceneMenu.setFocusable(false);

		// set active scene
		isActive.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((Scene_c) getGroup(scenePos)).setActived(isActive.isChecked());
				BluetoothMessage btMsg = new BluetoothMessage();
				btMsg.setType(BTMessageType.BLE_DATA);
				btMsg.setIndex(mContext.mBTMsgIndex);
				btMsg.setLength((byte) 9);
				btMsg.setCmdIdH((byte) CommandID.SET);
				btMsg.setCmdIdL((byte) CommandID.ACT_SCENE_WITH_INDEX);
				ByteBuffer payload = ByteBuffer.allocate(9);
				payload.put((byte) sceneObj.getID());
				payload.put(sceneObj.getName().getBytes());
				btMsg.setPayload(payload.array());
				payload.clear();
				mContext.mProcessMsg.putBLEMessage(btMsg);
				// set last active scene back to inactive
				if (listOfScene.get(scenePos).getActived()) {
					for (int i = 0; i < listOfScene.size(); i++) {
						if (listOfScene.get(i).getActived() && (i != scenePos)) {
							listOfScene.get(i).setActived(false);
							notifyDataSetChanged();
						}
					}
				}

			}
		});
		// button Menu click
		btnSceneMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showSceneMenuPopup(btnSceneMenu, mContext, scenePos);
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
	 * Find device index in List by device ID
	 * 
	 * @param listOfDev
	 * @param devID
	 * @return
	 */
	private int findDevIndexByID(ArrayList<DeviceInfo> listOfDev, int devID) {
		for (int i = 0; i < listOfDev.size(); i++) {
			if (listOfDev.get(i).getDevID() == devID) {
				// Log.i(TAG, "found devID:" + Integer.toHexString(devID));
				return i;
			}
		}
		Log.i(TAG, "not found devID:" + Integer.toHexString(devID));
		return 0;
	}

	/**
	 * show rule option menu
	 * 
	 */

	private void showRuleMenuPopup(View btnRuleMenu,
			final HomeActivity mContext, final int rulePos, final int scenePos,
			final EditText condDevVal, final EditText actDevVal) {

		PopupMenu mRuleMenu = new PopupMenu(mContext, btnRuleMenu);
		mRuleMenu.getMenuInflater().inflate(R.menu.rule_menu,
				mRuleMenu.getMenu());
		mRuleMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				switch (item.getItemId()) {
				case R.id.saveRule:
					if (!condDevVal.getText().toString().matches("")) {
						((Rule_c) getChild(scenePos, rulePos))
								.setCondDevVal(Short.valueOf(condDevVal
										.getText().toString()));
					} else {
						((Rule_c) getChild(scenePos, rulePos))
								.setCondDevVal((short) 0);
					}
					if (!actDevVal.getText().toString().matches("")) {
						((Rule_c) getChild(scenePos, rulePos))
								.setActDevVal(Short.valueOf(actDevVal.getText()
										.toString()));
					} else {
						((Rule_c) getChild(scenePos, rulePos))
								.setActDevVal((short) 0);
					}
					break;
				case R.id.delRule:
					AlertDialog.Builder mBuiler = new AlertDialog.Builder(
							mContext);
					mBuiler.setTitle("Do you want to delete this rule?");
					mBuiler.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									listOfScene.get(scenePos).getListOfRules()
											.remove(rulePos);
									notifyDataSetChanged();
								}
							});
					mBuiler.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							});
					mBuiler.show();
					break;

				default:
					break;
				}
				return false;
			}
		});
		mRuleMenu.show();
	}

	/**
	 * show scene options menu
	 * 
	 * @param btnSceneMenu
	 * @param mContext
	 */
	private void showSceneMenuPopup(View btnSceneMenu, HomeActivity mContext,
			final int scenePos) {
		PopupMenu mSceneMenu = new PopupMenu(mContext, btnSceneMenu);
		mSceneMenu.getMenuInflater().inflate(R.menu.scene_menu,
				mSceneMenu.getMenu());
		mSceneMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				// add rule
				case R.id.addRule:
					Log.i(TAG, "Add rules");
					addNewRule(scenePos);
					break;
				// remove rule
				case R.id.delRule:

					break;
				// rename scene
				case R.id.renameScene:
					renameSceneDialog(scenePos);
					break;
				case R.id.removeScene:
					removeScene(scenePos);
					break;

				// save
				case R.id.Save:
					sendNewSceneToCC(scenePos);
					break;

				default:
					break;
				}
				return false;
			}
		});
		mSceneMenu.show();
	}

	/**
	 * show time picker dialog
	 * 
	 */
	private void showTimePickerDialog(final int scenePos, final int rulePos,
			final TextView v) {
		final SimpleDateFormat stf = new SimpleDateFormat("HH:mm");
		Calendar c = Calendar.getInstance();
		int mHour = c.get(Calendar.HOUR_OF_DAY);
		int mMinute = c.get(Calendar.MINUTE);
		TimePickerDialog tpd = new TimePickerDialog(mContext,
				new TimePickerDialog.OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						if (view.isShown()) {
							v.setText(hourOfDay + ":" + minute);
							String getTimeString = v.getText().toString();
							if (v.getId() == R.id.fromTime) {
								try {
									((Rule_c) getChild(scenePos, rulePos))
											.setStartTime(javaToDosTime(stf
													.parse(getTimeString)
													.getTime()));
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} else {
								try {
									((Rule_c) getChild(scenePos, rulePos))
											.setEndTime(javaToDosTime(stf
													.parse(getTimeString)
													.getTime()));
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}
					}
				}, mHour, mMinute, true);

		tpd.show();
	}

	/**
	 * show date picker dialog
	 * 
	 */
	private void showDatePickerDialog(final int scenePos, final int rulePos,
			final TextView v) {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog dpd = new DatePickerDialog(mContext,
				new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						if (view.isShown()) {
							v.setText(year + "/" + (monthOfYear + 1) + "/"
									+ dayOfMonth);
							String getTimeString = v.getText().toString();
							if (v.getId() == R.id.fromDate) {
								try {
									((Rule_c) getChild(scenePos, rulePos))
									.setStartDate(javaToDosTime(0));
									
									((Rule_c) getChild(scenePos, rulePos))
											.setStartDate(javaToDosTime(sdf
													.parse(getTimeString)
													.getTime()));
									// Log.i(TAG, "time time");
//									 System.out.println(sdf.parse(getTimeString));
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} else {
								try {
									((Rule_c) getChild(scenePos, rulePos))
									.setEndDate(javaToDosTime(0));
									((Rule_c) getChild(scenePos, rulePos))
											.setEndDate(javaToDosTime(sdf
													.parse(getTimeString)
													.getTime()));
									// Log.i(TAG, "time time");
									// System.out.println(sdf.parse(v.getText()
									// .toString()));
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}
					}
				}, mYear, mMonth, mDay);
		dpd.getDatePicker().setCalendarViewShown(false);
		dpd.show();
	}

	/**
	 * Convert DOS-time to Java time
	 * 
	 * @param dosTime
	 * @return
	 */
	private long dosToJavaTime(int dosTime) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, ((dosTime >> 25) & 0x7f) + 1980);
		cal.set(Calendar.MONTH, ((dosTime >> 21) & 0x0f));
		cal.set(Calendar.DATE, ((dosTime >> 16) & 0x1f));
		cal.set(Calendar.HOUR_OF_DAY, (dosTime >> 11) & 0x1f);
		cal.set(Calendar.MINUTE, (dosTime >> 5) & 0x3f);
		cal.set(Calendar.SECOND, (dosTime & 0x1f) * 2);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime().getTime();
	}

	/**
	 * Convert Java time to DOS time
	 * 
	 * @param jTime
	 * @return
	 */
	private int javaToDosTime(long jTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(jTime);
		// Date d = new Date(jTime);
		// int year = d.getYear();
		// return (year - 1980) << 25 | (d.getMonth() + 1) << 21
		// | (d.getDate()) << 16 | d.getHours() << 11
		// | d.getMinutes() << 5 | d.getSeconds() >> 1;
		int year = cal.get(Calendar.YEAR) + 1900 - 1980;
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
//		Toast.makeText(mContext.getApplicationContext(), String.valueOf(year),
//				Toast.LENGTH_SHORT).show();
		
		 return (year) << 25 | (month) << 21
		 | day << 16 | hour << 11
		 | min << 5 | sec >> 1;
	}

	/**
	 * Add new rule to current Scene
	 * 
	 * @param scenePos
	 */
	private void addNewRule(int scenePos) {
		Rule_c mRule = new Rule_c();
		listOfScene.get(scenePos).addRule(mRule);
		// Log.i("ExpandableList",
		// Integer.toString(listOfScene.get(scenePos).getNumOfRule()));
		notifyDataSetChanged();
	}

	/**
	 * Send Rule to CC
	 * 
	 * @param mRule
	 */
	private void sendNewSceneToCC(int scenePos) {
		// Update scene to global data structure
		mContext.mProgDialog = ProgressDialog.show(mContext, null,
				"saving scene");
		removeIfSceneExisted(listOfScene.get(scenePos).getName());
		mContext.mSceneList.add(listOfScene.get(scenePos));
		BluetoothMessage btMsg = new BluetoothMessage();
		btMsg.setType(BTMessageType.BLE_DATA);
		btMsg.setIndex(mContext.mBTMsgIndex);
		btMsg.setLength((byte) 8);
		btMsg.setCmdIdH((byte) CommandID.SET);
		btMsg.setCmdIdL((byte) CommandID.NEW_SCENE);
		btMsg.setPayload(listOfScene.get(scenePos).getName().getBytes());
		mContext.mProcessMsg.putBLEMessage(btMsg);

		new CountDownTimer(11000, 11000) {

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				if (mContext.mProgDialog.isShowing()) {
					mContext.mProgDialog.dismiss();
					Toast.makeText(mContext.getApplicationContext(),
							"save scene not success", Toast.LENGTH_SHORT)
							.show();
					// listOfScene.clear();
					// listOfScene.addAll(mContext.mSceneList);
					// notifyDataSetChanged();
				}

			}
		}.start();
	}

	/**
	 * Rename scene
	 * 
	 * @param scenePos
	 */
	private void renameSceneDialog(int scenePos) {
		final int pos = scenePos;
		final String curSceneName = ((Scene_c) getGroup(scenePos)).getName();
		if (mContext.mSceneList.get(scenePos).getActived()) {
			Toast.makeText(mContext, "Can not rename running scene !",
					Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
		mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		mBuilder.setTitle("Do you want rename scene " + curSceneName);
		mBuilder.setMessage("Please, enter Scene's name!");

		final EditText input = new EditText(mContext);
		input.setHint("name can not over 8 characters");
		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (s.toString().getBytes().length > 8) {
					input.setText("");
					input.setHint("name can not over 8 characters");
				}
			}
		});
		mBuilder.setView(input);

		mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// TODO Auto-generated method stub
				String sceneName = input.getText().toString();
				if (!sceneName.matches(curSceneName) && !sceneName.isEmpty()) {
					if (sceneName.length() < 8) {
						int remainLen = 8 - sceneName.getBytes().length;
						for (int i = 0; i < remainLen; i++) {
							sceneName += "\0";
						}
					}
					listOfScene.get(pos).setName(sceneName);
					mContext.mSceneList.get(pos).setName(sceneName);
					notifyDataSetChanged();

					BluetoothMessage btMsg = new BluetoothMessage();
					btMsg.setType(BTMessageType.BLE_DATA);
					btMsg.setIndex(mContext.mBTMsgIndex);
					btMsg.setLength((byte) 16);
					btMsg.setCmdIdH((byte) CommandID.SET);
					btMsg.setCmdIdL((byte) CommandID.RENAME_SCENE);
					ByteBuffer payload = ByteBuffer.allocate(16);
					payload.put(curSceneName.getBytes());
					Log.i(TAG, "cursceneName:" + curSceneName);
					Log.i(TAG, "sceneName" + sceneName);
					payload.put(sceneName.getBytes());
					btMsg.setPayload(payload.array());
					payload.clear();
					mContext.mProcessMsg.putBLEMessage(btMsg);

				} else {
					Toast.makeText(mContext.getApplicationContext(),
							"Please enter a other name", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
		mBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
		mBuilder.show();
	}

	/**
	 * Remove scene
	 * 
	 * @param scenePos
	 */
	private void removeScene(int scenePos) {
		final int pos = scenePos;
		final String curSceneName = ((Scene_c) getGroup(scenePos)).getName();
		if (!mContext.mSceneList.get(scenePos).getActived()) {
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
			mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
			mBuilder.setTitle("Do you want remove scene " + curSceneName);

			mBuilder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							listOfScene.remove(pos);
							// mContext.mSceneList.remove(pos);
							notifyDataSetChanged();

							BluetoothMessage btMsg = new BluetoothMessage();
							btMsg.setType(BTMessageType.BLE_DATA);
							btMsg.setIndex(mContext.mBTMsgIndex);
							btMsg.setLength((byte) 8);
							btMsg.setCmdIdH((byte) CommandID.SET);
							btMsg.setCmdIdL((byte) CommandID.REMOVE_SCENE);
							btMsg.setPayload(curSceneName.getBytes());
							mContext.mProcessMsg.putBLEMessage(btMsg);
						}
					});

			mBuilder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
			mBuilder.show();
		} else {
			Toast.makeText(mContext, "Can not remove running scene !",
					Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * Get list of device name
	 * 
	 * @param mDevList
	 * @return
	 */
	private ArrayList<String> getDeviceNameList(ArrayList<DeviceInfo> mDevList) {
		ArrayList<String> listDevName = new ArrayList<String>();
		for (int i = 0; i < mDevList.size(); i++) {
			listDevName.add(mDevList.get(i).getName());
		}
		return listDevName;

	}

	/**
	 * Get index of device in List with device ID
	 * 
	 * @param mDevList
	 * @param devID
	 * @return
	 */
	private int getDeviceIndex(ArrayList<DeviceInfo> mDevList, int devID) {
		for (int i = 0; i < mDevList.size(); i++) {
			if (mDevList.get(i).getDevID() == devID) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Remove scene if exist
	 * 
	 * @param mSceneList
	 * @param sceneName
	 */
	private void removeIfSceneExisted(String sceneName) {
		for (int i = 0; i < mContext.mSceneList.size(); i++) {
			if (mContext.mSceneList.get(i).getName().matches(sceneName)) {
				Log.i(TAG, "name " + mContext.mSceneList.get(i).getName());
				mContext.mSceneList.remove(i);
				break;
			}
		}
	}

	/**
	 * 
	 * @param mDevList
	 */
	private void getTypeOfDevice(ArrayList<DeviceInfo> mDevList) {
		this.inDeviceList.clear();
		this.outDeviceList.clear();
		for (int i = 0; i < mDevList.size(); i++) {
			if ((((byte) mDevList.get(i).getDevID()) & 0x40) != 0) {
				this.outDeviceList.add(mDevList.get(i));
			} else {
				this.inDeviceList.add(mDevList.get(i));
			}
		}

	}

	public ArrayList<Scene_c> getListOfScene() {
		return this.listOfScene;
	}
}
