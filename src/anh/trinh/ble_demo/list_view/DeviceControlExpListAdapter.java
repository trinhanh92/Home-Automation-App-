package anh.trinh.ble_demo.list_view;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
//import android.widget.
import anh.trinh.ble_demo.HomeActivity;
import anh.trinh.ble_demo.R;
import anh.trinh.ble_demo.color_picker.ColorPickerDialog;
import anh.trinh.ble_demo.color_picker.ColorPickerDialog.OnColorSelectedListener;
import anh.trinh.ble_demo.data.BTMessageType;
import anh.trinh.ble_demo.data.BluetoothMessage;
import anh.trinh.ble_demo.data.CommandID;
import anh.trinh.ble_demo.data.DataConversion;
import anh.trinh.ble_demo.data.DeviceTypeDef;

public class DeviceControlExpListAdapter extends BaseExpandableListAdapter {

	private HomeActivity mContext;
	private ArrayList<Zone_c> listOfRoom;

	private class DeviceHolder {
		public int mHolderType;
		public TextView mDevName;
		public ImageView mDevImg;
		public TextView mDevVal;
		public TextView mDevType;
		public TextView mDevBlinkVal;
		public SeekBar mSeekVal;
		public SeekBar mSeekBlink;
		// public Spinner mBlinkVal;
		public ToggleButton mOnOff;
		public ImageButton mColorPicker;
	}

	/**
	 * Constructor
	 */
	public DeviceControlExpListAdapter(HomeActivity mContext,
			ArrayList<Zone_c> listParent) {
		this.mContext = mContext;
		this.listOfRoom = listParent;
	}

	/**
	 * Update list data
	 */
	public void updateData(ArrayList<Zone_c> listParent) {
		this.listOfRoom = listParent;
	}

	@Override
	public View getGroupView(final int groupPos, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final Zone_c zoneObj = (Zone_c) getGroup(groupPos);

		if (convertView == null) {
			LayoutInflater inf = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inf.inflate(R.layout.group_list, null);

		}
		final TextView mZoneName = (TextView) convertView
				.findViewById(R.id.roomName);
		TextView mNumDev = (TextView) convertView.findViewById(R.id.numOfDev);
		ImageButton btnRename = (ImageButton) convertView
				.findViewById(R.id.btnRename);

		btnRename.setFocusable(false);
		mZoneName.setText(zoneObj.getName() + " "
				+ String.valueOf(zoneObj.getID()));
		// set zone view color
		switch (zoneObj.getID() % 5) {
		case 0:
			convertView.setBackgroundResource(R.color.accent_pink);
			break;
		case 1:
			convertView.setBackgroundResource(R.color.blue);
			break;
		case 2:
			convertView.setBackgroundResource(R.color.brown);
			break;
		case 3:
			convertView.setBackgroundResource(R.color.deep_orange);
			break;
		case 4:
			convertView.setBackgroundResource(R.color.deep_purple);
			break;
		default:
			convertView.setBackgroundResource(R.color.cyan);
			break;
		}
		mNumDev.setText("(" + Integer.toString(zoneObj.getChildCount())
				+ " devs)");

		// button rename click listener
		btnRename.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
				mBuilder.setTitle("Rename zone");
				mBuilder.setMessage("Please enter zone name");
				final EditText input = new EditText(mContext);
				input.setHint("name not over 16 char");
				input.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
						// TODO Auto-generated method stub

					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
						// TODO Auto-generated method stub

					}

					@Override
					public void afterTextChanged(Editable s) {
						if (s.toString().getBytes().length > 16) {
							input.setText("");
							input.setHint("name not over 16 chars");
						}

					}
				});

				mBuilder.setView(input);

				mBuilder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String zoneName = input.getText().toString();
								if (zoneName.getBytes().length < 16) {
									for (int i = 0; i < 16 - zoneName
											.getBytes().length; i++) {
										zoneName += "\0";
									}
								}

								zoneObj.setName(zoneName);
								mZoneName.setText(zoneName + " s"
										+ String.valueOf(zoneObj.getID()));
								listOfRoom.get(groupPos).setName(zoneName);
								BluetoothMessage btMsg = new BluetoothMessage();
								btMsg.setType(BTMessageType.BLE_DATA);
								btMsg.setIndex(mContext.mBTMsgIndex);
								btMsg.setLength((byte) 17);
								btMsg.setCmdIdH((byte) CommandID.SET);
								btMsg.setCmdIdL((byte) CommandID.ZONE_NAME);
								ByteBuffer payload = ByteBuffer.allocate(17);
								payload.put((byte) zoneObj.getID());
								payload.put(zoneName.getBytes());
								btMsg.setPayload(payload.array());
								payload.clear();
								mContext.mProcessMsg.putBLEMessage(btMsg);

							}
						});
				mBuilder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});

				mBuilder.show();

			}
		});
		return convertView;
	}

	@Override
	public View getChildView(int groupPos, int childPos, boolean isLastChild,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		final Device_c childObj = (Device_c) getChild(groupPos, childPos);
		final int devType = childObj.getID();
		short devVal = childObj.getVal();

		DeviceHolder mDevViewHolder;

		if (convertView == null) {
			mDevViewHolder = new DeviceHolder();
			mDevViewHolder.mHolderType = devType;

			switch ((byte) devType) {

			case DeviceTypeDef.BUTTON:
			case DeviceTypeDef.SWITCH:
			case DeviceTypeDef.PIR_SENSOR:
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.switch_item, null);
				mDevViewHolder.mDevName = (TextView) convertView
						.findViewById(R.id.devName);
				mDevViewHolder.mDevImg = (ImageView) convertView
						.findViewById(R.id.devIcon);
				mDevViewHolder.mOnOff = (ToggleButton) convertView
						.findViewById(R.id.toggleVal);
				mDevViewHolder.mDevType = (TextView) convertView
						.findViewById(R.id.tvInOut);
				break;

			case DeviceTypeDef.SERVO_SG90:
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.bulb, null);
				mDevViewHolder.mDevName = (TextView) convertView
						.findViewById(R.id.devName);
				mDevViewHolder.mDevImg = (ImageView) convertView
						.findViewById(R.id.devIcon);
				mDevViewHolder.mSeekVal = (SeekBar) convertView
						.findViewById(R.id.dimBar);
				mDevViewHolder.mDevVal = (TextView) convertView
						.findViewById(R.id.devVal);
				mDevViewHolder.mDevType = (TextView) convertView
						.findViewById(R.id.tvInOut);
				break;

			case DeviceTypeDef.DIMMER:
			case DeviceTypeDef.GAS_SENSOR:
			case DeviceTypeDef.TEMP_SENSOR:
			case DeviceTypeDef.LUMI_SENSOR:
			case DeviceTypeDef.SOIL_HUMI:
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.sensor_dev, null);
				mDevViewHolder.mDevImg = (ImageView) convertView
						.findViewById(R.id.devIcon);
				mDevViewHolder.mDevName = (TextView) convertView
						.findViewById(R.id.devName);
				mDevViewHolder.mDevVal = (TextView) convertView
						.findViewById(R.id.devVal);
				mDevViewHolder.mDevType = (TextView) convertView
						.findViewById(R.id.tvInOut);
				break;

			case DeviceTypeDef.RGB_LED:
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.rgb_led, null);
				mDevViewHolder.mDevImg = (ImageView) convertView
						.findViewById(R.id.devIcon);
				mDevViewHolder.mDevName = (TextView) convertView
						.findViewById(R.id.devName);
				mDevViewHolder.mDevVal = (TextView) convertView
						.findViewById(R.id.devVal);
				mDevViewHolder.mColorPicker = (ImageButton) convertView
						.findViewById(R.id.btnPicker);
				mDevViewHolder.mDevType = (TextView) convertView
						.findViewById(R.id.tvInOut);
				break;

			case DeviceTypeDef.LEVEL_BULB:
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.level_bulb, null);
				mDevViewHolder.mDevName = (TextView) convertView
						.findViewById(R.id.devName);
				mDevViewHolder.mDevImg = (ImageView) convertView
						.findViewById(R.id.devIcon);
				// mDevViewHolder.mBlinkVal = (Spinner)
				// convertView.findViewById(R.id.blinkVal);
				mDevViewHolder.mSeekBlink = (SeekBar) convertView
						.findViewById(R.id.seekBlink);
				mDevViewHolder.mDevBlinkVal = (TextView) convertView
						.findViewById(R.id.devBlinkVal);
				mDevViewHolder.mSeekVal = (SeekBar) convertView
						.findViewById(R.id.dimBar);
				mDevViewHolder.mDevVal = (TextView) convertView
						.findViewById(R.id.devVal);
				mDevViewHolder.mDevType = (TextView) convertView
						.findViewById(R.id.tvInOut);
				break;

			case DeviceTypeDef.ON_OFF_BULB:
			case DeviceTypeDef.BUZZER:
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.on_off_bulb, null);
				mDevViewHolder.mDevName = (TextView) convertView
						.findViewById(R.id.devName);
				mDevViewHolder.mDevImg = (ImageView) convertView
						.findViewById(R.id.devIcon);
				mDevViewHolder.mSeekBlink = (SeekBar) convertView
						.findViewById(R.id.seekBlink);
				mDevViewHolder.mDevBlinkVal = (TextView) convertView
						.findViewById(R.id.devBlinkVal);
				mDevViewHolder.mOnOff = (ToggleButton) convertView
						.findViewById(R.id.toggleVal);
				mDevViewHolder.mDevType = (TextView) convertView
						.findViewById(R.id.tvInOut);
				break;

			default:
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.sensor_dev, null);
				mDevViewHolder.mDevName = (TextView) convertView
						.findViewById(R.id.devName);
				mDevViewHolder.mDevImg = (ImageView) convertView
						.findViewById(R.id.devIcon);
				mDevViewHolder.mDevVal = (TextView) convertView
						.findViewById(R.id.devVal);
				mDevViewHolder.mDevType = (TextView) convertView
						.findViewById(R.id.tvInOut);
				break;
			}
			convertView.setTag(mDevViewHolder);
		} else {
			mDevViewHolder = (DeviceHolder) convertView.getTag();
			if (mDevViewHolder.mHolderType != devType) {
				mDevViewHolder = new DeviceHolder();
				mDevViewHolder.mHolderType = devType;

				// assign layout match with type of device
				switch ((byte) devType) {

				case DeviceTypeDef.BUTTON:
				case DeviceTypeDef.SWITCH:
				case DeviceTypeDef.PIR_SENSOR:
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.switch_item, null);
					mDevViewHolder.mDevName = (TextView) convertView
							.findViewById(R.id.devName);
					mDevViewHolder.mDevImg = (ImageView) convertView
							.findViewById(R.id.devIcon);
					mDevViewHolder.mOnOff = (ToggleButton) convertView
							.findViewById(R.id.toggleVal);
					mDevViewHolder.mDevType = (TextView) convertView
							.findViewById(R.id.tvInOut);
					break;

				case DeviceTypeDef.SERVO_SG90:
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.bulb, null);
					mDevViewHolder.mDevName = (TextView) convertView
							.findViewById(R.id.devName);
					mDevViewHolder.mDevImg = (ImageView) convertView
							.findViewById(R.id.devIcon);
					mDevViewHolder.mSeekVal = (SeekBar) convertView
							.findViewById(R.id.dimBar);
					mDevViewHolder.mDevVal = (TextView) convertView
							.findViewById(R.id.devVal);
					mDevViewHolder.mDevType = (TextView) convertView
							.findViewById(R.id.tvInOut);
					break;

				case DeviceTypeDef.DIMMER:
				case DeviceTypeDef.GAS_SENSOR:
				case DeviceTypeDef.TEMP_SENSOR:
				case DeviceTypeDef.LUMI_SENSOR:
				case DeviceTypeDef.SOIL_HUMI:
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.sensor_dev, null);
					mDevViewHolder.mDevImg = (ImageView) convertView
							.findViewById(R.id.devIcon);
					mDevViewHolder.mDevName = (TextView) convertView
							.findViewById(R.id.devName);
					mDevViewHolder.mDevVal = (TextView) convertView
							.findViewById(R.id.devVal);
					mDevViewHolder.mDevType = (TextView) convertView
							.findViewById(R.id.tvInOut);
					break;

				case DeviceTypeDef.RGB_LED:
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.rgb_led, null);
					mDevViewHolder.mDevImg = (ImageView) convertView
							.findViewById(R.id.devIcon);
					mDevViewHolder.mDevName = (TextView) convertView
							.findViewById(R.id.devName);
					mDevViewHolder.mDevVal = (TextView) convertView
							.findViewById(R.id.devVal);
					mDevViewHolder.mColorPicker = (ImageButton) convertView
							.findViewById(R.id.btnPicker);
					mDevViewHolder.mDevType = (TextView) convertView
							.findViewById(R.id.tvInOut);
					break;

				case DeviceTypeDef.LEVEL_BULB:
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.level_bulb, null);
					mDevViewHolder.mDevName = (TextView) convertView
							.findViewById(R.id.devName);
					mDevViewHolder.mDevImg = (ImageView) convertView
							.findViewById(R.id.devIcon);
					// mDevViewHolder.mBlinkVal = (Spinner)
					// convertView.findViewById(R.id.blinkVal);
					mDevViewHolder.mSeekBlink = (SeekBar) convertView
							.findViewById(R.id.seekBlink);
					mDevViewHolder.mDevBlinkVal = (TextView) convertView
							.findViewById(R.id.devBlinkVal);
					mDevViewHolder.mSeekVal = (SeekBar) convertView
							.findViewById(R.id.dimBar);
					mDevViewHolder.mDevVal = (TextView) convertView
							.findViewById(R.id.devVal);
					mDevViewHolder.mDevType = (TextView) convertView
							.findViewById(R.id.tvInOut);
					break;

				case DeviceTypeDef.ON_OFF_BULB:
				case DeviceTypeDef.BUZZER:
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.on_off_bulb, null);
					mDevViewHolder.mDevName = (TextView) convertView
							.findViewById(R.id.devName);
					mDevViewHolder.mDevImg = (ImageView) convertView
							.findViewById(R.id.devIcon);
					mDevViewHolder.mSeekBlink = (SeekBar) convertView
							.findViewById(R.id.seekBlink);
					mDevViewHolder.mDevBlinkVal = (TextView) convertView
							.findViewById(R.id.devBlinkVal);
					mDevViewHolder.mOnOff = (ToggleButton) convertView
							.findViewById(R.id.toggleVal);
					mDevViewHolder.mDevType = (TextView) convertView
							.findViewById(R.id.tvInOut);
					break;

				default:
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.sensor_dev, null);
					mDevViewHolder.mDevName = (TextView) convertView
							.findViewById(R.id.devName);
					mDevViewHolder.mDevImg = (ImageView) convertView
							.findViewById(R.id.devIcon);
					mDevViewHolder.mDevVal = (TextView) convertView
							.findViewById(R.id.devVal);
					mDevViewHolder.mDevType = (TextView) convertView
							.findViewById(R.id.tvInOut);
					break;
				}
				convertView.setTag(mDevViewHolder);
			}
		}

		// if call a view in a other class, use follow final variable
		final DeviceHolder mDevHolder = mDevViewHolder;

		switch ((byte) mDevViewHolder.mHolderType) {
		case DeviceTypeDef.BUTTON:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.button_push);
			mDevViewHolder.mOnOff.setClickable(false);
			if (devVal == 0) {
				mDevViewHolder.mOnOff.setChecked(false);
			} else {
				mDevViewHolder.mOnOff.setChecked(true);
			}
			break;
		case DeviceTypeDef.DIMMER:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.dimmer);
			mDevViewHolder.mDevVal.setText(Short.toString(devVal) + "%");
			break;
		case DeviceTypeDef.PIR_SENSOR:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.pir_sensor);
			mDevViewHolder.mOnOff.setClickable(false);
			if (devVal == 0) {
				mDevViewHolder.mOnOff.setChecked(false);
			} else {
				mDevViewHolder.mOnOff.setChecked(true);
			}
			break;
		case DeviceTypeDef.LEVEL_BULB:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.level_bulb_icon);

			// String[] arrBlinkVal =
			// {"1","2","3","4","5","6","7","8","9","10"};
			// ArrayAdapter<String> blinkAdapter = new
			// ArrayAdapter<String>(mContext,
			// android.R.layout.simple_spinner_item, arrBlinkVal);
			// blinkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// mDevViewHolder.mBlinkVal.setAdapter(blinkAdapter);

			if (devVal <= 100) {
				if (mDevViewHolder.mDevVal.getVisibility() == View.INVISIBLE) {
					mDevViewHolder.mDevVal.setVisibility(View.VISIBLE);
				}
				mDevViewHolder.mSeekVal.setProgress(devVal);
				mDevViewHolder.mDevVal.setText(Short.toString(devVal) + "%");
				mDevViewHolder.mDevBlinkVal.setVisibility(View.INVISIBLE);
			} else {
				// mDevViewHolder.mBlinkVal.setSelection(devVal - 101);
				if (mDevViewHolder.mDevBlinkVal.getVisibility() == View.INVISIBLE) {
					mDevViewHolder.mDevBlinkVal.setVisibility(View.VISIBLE);
				}
				mDevViewHolder.mSeekBlink.setProgress(devVal - 101);
				mDevViewHolder.mDevBlinkVal.setText(Short
						.toString((short) (devVal - 100)) + " Hz");
				mDevViewHolder.mDevVal.setVisibility(View.INVISIBLE);
			}

			// seek device value

			mDevViewHolder.mSeekVal
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						int stop, start;

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							// TODO Send Level Bulb Value to CC
							stop = seekBar.getProgress();
							if (stop != start) {
								sendValUpdated(devType, (short) stop);
							}
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub
							start = seekBar.getProgress();
						}

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							// TODO Only Change value on Mobile GUI
							mDevHolder.mDevVal.setText(Integer
									.toString(progress) + "%");
						}
					});

			// set device blink value
			mDevViewHolder.mSeekBlink
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						int stop, start;

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub
							stop = seekBar.getProgress();
							if (stop != start) {
								sendValUpdated(devType, (short) (stop + 101));
							}
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub
							start = seekBar.getProgress();
						}

						@Override
						public void onProgressChanged(SeekBar arg0, int arg1,
								boolean arg2) {
							// TODO Auto-generated method stub

						}
					});
			break;
		case DeviceTypeDef.LUMI_SENSOR:
			System.out.print(devVal);
			mDevViewHolder.mDevImg.setImageResource(R.drawable.light_sensor);
			mDevViewHolder.mDevVal.setText(Short.toString(devVal) + " lux");
			break;
		case DeviceTypeDef.GAS_SENSOR:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.gas_sensor);
			mDevViewHolder.mDevVal.setText(String.valueOf(devVal & 0x0000FFFF) + " ppm");
			break;
		case DeviceTypeDef.TEMP_SENSOR:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.temp_sensor);
			mDevViewHolder.mDevVal.setText(Short.toString(devVal) + "\u2103");
			break;
		case DeviceTypeDef.SOIL_HUMI:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.soil_icon);
			switch (devVal) {
			case 0:
				mDevViewHolder.mDevVal.setText("in water");
				break;
			case 1:
				mDevViewHolder.mDevVal.setText("wet");
				break;
			case 2:
				mDevViewHolder.mDevVal.setText("dry");
				break;
			default:
				mDevViewHolder.mDevVal.setText("???");
				break;
			}
			break;
		case DeviceTypeDef.SERVO_SG90:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.motor);
			mDevViewHolder.mDevVal.setText(Short.toString(devVal) + "\u00B0");
			mDevViewHolder.mSeekVal.setMax(180);
			mDevViewHolder.mSeekVal.setProgress(devVal);
			mDevViewHolder.mSeekVal
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						int sendVal;

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub
							sendVal = seekBar.getProgress();
							sendValUpdated(devType, (short) sendVal);
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							// TODO Auto-generated method stub
							mDevHolder.mDevVal.setText(Integer
									.toString(progress) + "\u00B0");

						}
					});
			break;
		case DeviceTypeDef.ON_OFF_BULB:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.bulb);
			if (devVal == 0) {
				mDevViewHolder.mOnOff.setChecked(false);
			} else {
				mDevViewHolder.mOnOff.setChecked(true);
			}

			if (devVal > 100) {
				mDevViewHolder.mSeekBlink.setProgress(devVal - 101);
				mDevViewHolder.mDevBlinkVal.setText(Short
						.toString((short) (devVal - 100)) + " Hz");
			}

			// set on/off value
			mDevViewHolder.mOnOff.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Send value to CC
					boolean isChecked = ((ToggleButton) v).isChecked();
					if (isChecked) {
						sendValUpdated(devType, (short) 1);
					} else {
						sendValUpdated(devType, (short) 0);
					}
				}
			});

			// set device blink value
			mDevViewHolder.mSeekBlink
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						int stop, start;

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub
							stop = seekBar.getProgress();
							if (stop != start) {
								sendValUpdated(devType, (short) (stop + 101));
							}
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub
							start = seekBar.getProgress();
						}

						@Override
						public void onProgressChanged(SeekBar arg0, int arg1,
								boolean arg2) {
							// TODO Auto-generated method stub

						}
					});
			break;
		case DeviceTypeDef.BUZZER:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.buzzer);
			if (devVal == 0) {
				mDevViewHolder.mOnOff.setChecked(false);
			} else {
				mDevViewHolder.mOnOff.setChecked(true);
			}

			if (devVal > 100) {
				mDevViewHolder.mSeekBlink.setProgress(devVal - 101);
				mDevViewHolder.mDevBlinkVal.setText(Short
						.toString((short) (devVal - 100)) + " Hz");
			}

			mDevViewHolder.mOnOff.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					boolean isChecked = ((ToggleButton) v).isChecked();
					if (isChecked) {
						sendValUpdated(devType, (short) 1);
					} else {
						sendValUpdated(devType, (short) 0);
					}
				}
			});

			// set device blink value
			mDevViewHolder.mSeekBlink
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						int stop, start;

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub
							stop = seekBar.getProgress();
							if (stop != start) {
								sendValUpdated(devType, (short) (stop + 101));
							}
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub
							start = seekBar.getProgress();
						}

						@Override
						public void onProgressChanged(SeekBar arg0, int arg1,
								boolean arg2) {
							// TODO Auto-generated method stub

						}
					});
			break;
		case DeviceTypeDef.RGB_LED:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.rgb_led);
			mDevViewHolder.mDevVal.setText("0x"
					+ Integer.toHexString(devVal & 0xFFFF));
			mDevViewHolder.mColorPicker.setBackgroundColor(DataConversion
					.color16BitTo32Bit(devVal));
			// if click: set 15bit - color as picker chooser
			mDevViewHolder.mColorPicker
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							ColorPickerDialog mColorPickerDialog = new ColorPickerDialog(
									mContext, Color.WHITE,
									new OnColorSelectedListener() {

										@Override
										public void onColorSelected(int color) {
											// TODO Auto-generated method stub
											Log.i("Picker", "changed");
											short mColor16bit = DataConversion
													.color32BitTo16Bit(color);
											mDevHolder.mDevVal.setText("0x"
													+ Integer
															.toHexString(mColor16bit & 0xFFFF));
											mDevHolder.mColorPicker
													.setBackgroundColor(color);
											sendValUpdated(devType, mColor16bit);
										}
									});
							mColorPickerDialog.show();
						}
					});

			// if long click: set toggle color in table color
			mDevViewHolder.mColorPicker
					.setOnLongClickListener(new OnLongClickListener() {

						@Override
						public boolean onLongClick(View v) {
							sendValUpdated(devType, (short) 0x8000);
							return false;
						}
					});
			break;
		case DeviceTypeDef.SWITCH:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.switch_icon);
			mDevViewHolder.mOnOff.setClickable(false);
			if (devVal == 0) {
				mDevViewHolder.mOnOff.setChecked(false);
			} else {
				mDevViewHolder.mOnOff.setChecked(true);
			}
			break;
		default:
			mDevViewHolder.mDevImg.setImageResource(R.drawable.inknon);
			mDevViewHolder.mDevName.setText("Unknown Device");
			mDevViewHolder.mDevVal.setText(String.valueOf(devVal));
			break;
		}
		mDevViewHolder.mDevName.setText(childObj.getName());
		if ((mDevViewHolder.mHolderType & 0x000000c0) != 0) {
			mDevViewHolder.mDevType.setText("O");
			mDevViewHolder.mDevType
					.setBackgroundResource(R.drawable.output_dev_indicate);
		} else {
			mDevViewHolder.mDevType.setText("I");
			mDevViewHolder.mDevType
					.setBackgroundResource(R.drawable.input_dev_indicate);
		}
		return convertView;
	}

	@Override
	public Object getChild(int groupPos, int childPos) {
		// TODO Auto-generated method stub
		return listOfRoom.get(groupPos).getChildList().get(childPos);
	}

	@Override
	public long getChildId(int groupPos, int childPos) {
		// TODO Auto-generated method stub
		return listOfRoom.get(groupPos).getChildIndex(childPos).getID();
	}

	@Override
	public int getChildrenCount(int groupPos) {
		// TODO Auto-generated method stub
		return listOfRoom.get(groupPos).getChildCount();
	}

	@Override
	public Object getGroup(int groupPos) {
		// TODO Auto-generated method stub
		return listOfRoom.get(groupPos);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return listOfRoom.size();
	}

	@Override
	public long getGroupId(int groupPos) {
		// TODO Auto-generated method stub
		return listOfRoom.get(groupPos).getID();
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
		// Log.i("List Adapter", "update data");
	}

	/***********************************************************************************************
	 * Private Functions
	 ***********************************************************************************************/

	/**
	 * Send device updated value to CC
	 * 
	 * @param devInfo
	 */
	private void sendValUpdated(int devType, short devVal) {
		Log.i("BLE SEND", "send dev val");
		BluetoothMessage bleMsg = new BluetoothMessage();
		bleMsg.setType(BTMessageType.BLE_DATA);
		bleMsg.setIndex(mContext.mBTMsgIndex);
		bleMsg.setLength((byte) 6);
		bleMsg.setCmdIdH((byte) CommandID.SET);
		bleMsg.setCmdIdL((byte) CommandID.DEV_VAL);
		bleMsg.setPayload(DataConversion.devInfo2ByteArr(devType, devVal));
		// final Message msg = mContext.mMsgHandler.obtainMessage(
		// CommandID.DEV_VAL, bleMsg);
		// mContext.mMsgHandler.sendMessage(msg);
		mContext.mProcessMsg.putBLEMessage(bleMsg);

	}

}
