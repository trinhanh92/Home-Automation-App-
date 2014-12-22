package anh.trinh.ble_demo.list_view;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Text;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.TextView;
import anh.trinh.ble_demo.HomeActivity;
import anh.trinh.ble_demo.R;
import anh.trinh.ble_demo.custom_view.DateDisplayPicker;
import anh.trinh.ble_demo.custom_view.DeviceArrayAdapter;
import anh.trinh.ble_demo.custom_view.TimeDisplayPicker;
import anh.trinh.ble_demo.data.BTMessageType;
import anh.trinh.ble_demo.data.BluetoothMessage;
import anh.trinh.ble_demo.data.CommandID;
import anh.trinh.ble_demo.data.ConditionDef;
import anh.trinh.ble_demo.data.DeviceInfo;

public class SceneExpListAdapter extends BaseExpandableListAdapter {

	private static final String TAG = "ExpandableListView";
	private HomeActivity mContext;
	private ArrayList<Scene_c> listOfScene;

	public final String[] listOfCond = { "EQUAL", "LESS THAN", "LESS OR EQUAL",
			"GREATER THAN", "GREATER OR EQUAL", "CHANGE VAL", "IN RANGE",
			"IN RANGE EVDAY", "CHANGE VAL OVER" };

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
		final View mParent = convertView;
		// TODO Create child view
		final Rule_c mRuleObj = (Rule_c) getChild(groupPos, childPos);
		final int scenePos = groupPos;
		final int rulePos = childPos;
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
		final Spinner condDevChoose = (Spinner) convertView
				.findViewById(R.id.condDevChoose);
		final EditText condDevVal = (EditText) convertView
				.findViewById(R.id.condDevVal);

		final ImageButton mBtnMenu = (ImageButton) convertView
				.findViewById(R.id.btnMenu);
		mBtnMenu.setFocusable(false);

		TextView mFromDate = (TextView) convertView.findViewById(R.id.fromDate);
		TextView mFromTime = (TextView) convertView.findViewById(R.id.fromTime);
		TextView mToDate = (TextView) convertView.findViewById(R.id.toDate);
		TextView mToTime = (TextView) convertView.findViewById(R.id.toTime);

		Spinner mActChoose = (Spinner) convertView
				.findViewById(R.id.mActChoose);
		final Spinner actDevChoose = (Spinner) convertView
				.findViewById(R.id.actDevChoose);
		final EditText actDevVal = (EditText) convertView
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
		ArrayAdapter<String> mDevAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item,
				getDeviceNameList(mContext.mDevInfoList));
		mDevAdapter
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

		// set adapter for spinner
		mCondChoose.setAdapter(mCondAdapter);
		mActChoose.setAdapter(mActAdapter);
		condDevChoose.setAdapter(mDevAdapter);
		actDevChoose.setAdapter(mDevAdapter);

		mCondChoose.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
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
													mContext.mDevInfoList.get(
															pos).getDevID());
								}

								@Override
								public void onNothingSelected(
										AdapterView<?> arg0) {
									// TODO Auto-generated method stub

								}
							});

				}

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
												mContext.mDevInfoList.get(pos)
														.getDevID());
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {
								// TODO Auto-generated method stub

							}
						});

				listOfScene.get(scenePos).getRuleWithIndex(rulePos)
						.setCond(pos);
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

			// ScenesFragment.listOfScene.get(groupPos).getRuleWithIndex(childPos)
			// .setRuleIndex(childPos);
			mCondChoose.setSelection(mRuleObj.getCond());
			mActChoose.setSelection(mRuleObj.getAction());
			actDevChoose.setSelection(findDevIndexByID(mContext.mDevInfoList,
					mRuleObj.getActDevId()));
			actDevVal.setText(Integer.toString(mRuleObj.getActDevVal()));
			if (llTimeRange.getVisibility() == View.VISIBLE) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				SimpleDateFormat stf = new SimpleDateFormat("HH:mm");
				mFromDate.setText(sdf.format(new Date(dosToJavaTime(mRuleObj
						.getStartDate()))));
				mFromTime.setText(stf.format(new Time(dosToJavaTime(mRuleObj
						.getStartTime()))));
				mToDate.setText(sdf.format(new Date(dosToJavaTime(mRuleObj
						.getEndDate()))));
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
			}
			condDevChoose.setSelection(getDeviceIndex(mContext.mDevInfoList,
					mRuleObj.getCondDevId()));
			condDevVal.setText(Integer.toString(mRuleObj.getCondDevVal()));
		}

		mBtnMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showRuleMenuPopup(mBtnMenu, mContext, rulePos, scenePos,
						condDevVal, actDevVal);
			}
		});
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
		CheckBox isActive = (CheckBox) convertView.findViewById(R.id.isActive);
		TextView mSceneName = (TextView) convertView
				.findViewById(R.id.sceneName);
		final ImageButton btnSceneMenu = (ImageButton) convertView
				.findViewById(R.id.btnSceneMenu);
		mSceneName.setText(sceneObj.getName());
		isActive.setFocusable(false);
		isActive.setChecked(sceneObj.getActived());
		btnSceneMenu.setFocusable(false);

		// set active scene
		isActive.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				// TODO Auto-generated method stub
				((Scene_c) getGroup(scenePos)).setActived(isChecked);
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
				return i;
			}
		}
		return -1;
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
									Log.i(TAG, "time time");
									System.out.println(stf.parse(v.getText()
											.toString()));
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
											.setStartDate(javaToDosTime(sdf
													.parse(getTimeString)
													.getTime()));
									Log.i(TAG, "time time");
									System.out.println(sdf.parse(getTimeString));
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} else {
								try {
									((Rule_c) getChild(scenePos, rulePos))
											.setEndDate(javaToDosTime(sdf
													.parse(getTimeString)
													.getTime()));
									Log.i(TAG, "time time");
									System.out.println(sdf.parse(v.getText()
											.toString()));
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
		Date d = new Date(jTime);
		int year = d.getYear() + 1900;
		// if (year < 1980) {
		// return (1 << 21) | (1 << 16);
		// }
		return (year - 1980) << 25 | (d.getMonth()) << 21 | (d.getDate()) << 16
				| d.getHours() << 11 | d.getMinutes() << 5 | d.getSeconds();
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
	 * Send Rule to CC
	 * 
	 * @param mRule
	 */
	private void sendNewSceneToCC(int scenePos) {
		// Update scene to global data structure
		removeIfSceneExisted(listOfScene.get(scenePos).getName());
		mContext.mActSceneList.add(listOfScene.get(scenePos));
		BluetoothMessage btMsg = new BluetoothMessage();
		btMsg.setType(BTMessageType.BLE_DATA);
		btMsg.setIndex(mContext.mBTMsgIndex);
		btMsg.setLength((byte) 8);
		btMsg.setCmdIdH((byte) CommandID.SET);
		btMsg.setCmdIdL((byte) CommandID.NEW_SCENE);
		btMsg.setPayload(listOfScene.get(scenePos).getName().getBytes());
		try {
			mContext.mProcessMsg.putBLEMessage(mContext.mWriteCharacteristic,
					btMsg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Rename scene
	 * 
	 * @param scenePos
	 */
	private void renameSceneDialog(int scenePos) {
		final int pos = scenePos;
		final String curSceneName = ((Scene_c) getGroup(scenePos)).getName();

		AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
		mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		mBuilder.setTitle("Do you want rename scene " + curSceneName);
		mBuilder.setMessage("Please, enter Scene's name!");

		final EditText input = new EditText(mContext);
		input.setText(curSceneName);
		mBuilder.setView(input);

		mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// TODO Auto-generated method stub
				String sceneName = input.getText().toString();
				if (!sceneName.matches(curSceneName)) {
					listOfScene.get(pos).setName(sceneName);
					notifyDataSetChanged();
					BluetoothMessage btMsg = new BluetoothMessage();
					btMsg.setType(BTMessageType.BLE_DATA);
					btMsg.setIndex(mContext.mBTMsgIndex);
					btMsg.setCmdIdH((byte) CommandID.SET);
					btMsg.setCmdIdL((byte) CommandID.RENAME_SCENE);
					ByteBuffer payload = ByteBuffer.allocate(16);
					payload.put(curSceneName.getBytes());
					payload.put(sceneName.getBytes());
					btMsg.setPayload(payload.array());
					payload.clear();
					try {
						mContext.mProcessMsg.putBLEMessage(
								mContext.mWriteCharacteristic, btMsg);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

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
		if (!((Scene_c) getGroup(scenePos)).getActived()) {
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
			mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
			mBuilder.setTitle("Do you want remove scene " + curSceneName);

			mBuilder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							listOfScene.remove(pos);
							notifyDataSetChanged();

							BluetoothMessage btMsg = new BluetoothMessage();
							btMsg.setType(BTMessageType.BLE_DATA);
							btMsg.setIndex(mContext.mBTMsgIndex);
							btMsg.setCmdIdH((byte) CommandID.SET);
							btMsg.setCmdIdL((byte) CommandID.REMOVE_SCENE);
							btMsg.setPayload(curSceneName.getBytes());
							try {
								mContext.mProcessMsg.putBLEMessage(
										mContext.mWriteCharacteristic, btMsg);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
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
		} else {
			Toast.makeText(mContext, "Can not remove running scene !",
					Toast.LENGTH_SHORT);
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
		for (int i = 0; i < mContext.mActSceneList.size(); i++) {
			if (mContext.mActSceneList.get(i).getName().matches(sceneName)) {
				Log.i(TAG, "name " + mContext.mActSceneList.get(i).getName());
				mContext.mActSceneList.remove(i);
				break;
			}
		}
	}
}
