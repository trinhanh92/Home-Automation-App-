package anh.trinh.ble_demo;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;
import anh.trinh.ble_demo.data.BTMessageType;
import anh.trinh.ble_demo.data.BluetoothMessage;
import anh.trinh.ble_demo.data.CommandID;
import anh.trinh.ble_demo.data.DataConversion;
import anh.trinh.ble_demo.list_view.SceneExpListAdapter;
import anh.trinh.ble_demo.list_view.Scene_c;

public class ScenesFragment extends Fragment {

	private Button btnAddScene;
	private ExpandableListView mSceneExpList;
	private SceneExpListAdapter mAdapter;
	private HomeActivity mContext;
	public ArrayList<Scene_c> listOfScene = new ArrayList<Scene_c>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.fragment_scenes, container,
				false);

		btnAddScene = (Button) rootView.findViewById(R.id.btnAddScene);
		mSceneExpList = (ExpandableListView) rootView
				.findViewById(R.id.elvScene);
		mAdapter = new SceneExpListAdapter((HomeActivity) getActivity(),
				listOfScene);
		mSceneExpList.setAdapter(mAdapter);
		mSceneExpList.setGroupIndicator(null);

		// listen group expand to get rule list of inactive scene
//		mSceneExpList.setOnGroupExpandListener(new OnGroupExpandListener() {
//
//			@Override
//			public void onGroupExpand(int groupPosition) {
//				// TODO Auto-generated method stub
//				if ((listOfScene.get(groupPosition).getNumOfRule() == 0)
//						&& !listOfScene.get(groupPosition).getActived()) {
//					BluetoothMessage btMsg = new BluetoothMessage();
//					btMsg.setType(BTMessageType.BLE_DATA);
//					btMsg.setIndex(mContext.mBTMsgIndex);
//					btMsg.setLength((byte) 2);
//					btMsg.setCmdIdH((byte) CommandID.GET);
//					btMsg.setCmdIdL((byte) CommandID.RULE_WITH_INDEX);
//					btMsg.setPayload(new byte[] {(byte) 0xFF, (byte) 0xFF});
//					mContext.mProcessMsg.putBLEMessage(mContext.mWriteCharacteristic, btMsg);
//					timeout_receive_rule(5000);
//				}
//			}
//		});

		//
		btnAddScene.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Add scene

				AlertDialog.Builder mBuilder = new AlertDialog.Builder(
						getActivity());
				mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
				mBuilder.setTitle("Create a new Scene");
				mBuilder.setMessage("Please, enter Scene's name!");

				final EditText input = new EditText(getActivity());
				input.setHint("name can not over 8 characters");
				input.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						// TODO Auto-generated method stub

					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
						// TODO Auto-generated method stub

					}

					@Override
					public void afterTextChanged(Editable s) {
						// TODO Auto-generated method stub
						if (s.length() > 8) {
							input.setText("");
							input.setHint("name can not over 8 characters");
						}
					}
				});
				mBuilder.setView(input);

				mBuilder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// TODO Auto-generated method stub
								String sceneName = input.getText().toString();
								if (!sceneName.isEmpty()) {
									if (sceneName.length() < 8) {
										for (int i = 0; i < (8 - sceneName
												.length()); i++) {
											sceneName += "\0";
										}
									}
									Scene_c mScene = new Scene_c();
									mScene.setName(sceneName);
									listOfScene.add(mScene);
									mAdapter.notifyDataSetChanged();
									Log.i("Scene Fragment", "update "
											+ listOfScene.size());
								}
							}
						});
				mBuilder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
				mBuilder.show();
			}
		});

		return rootView;
	}

	// time out to receive rule inactive scene
	protected void timeout_receive_rule(int timeout) {
		new CountDownTimer(timeout, timeout) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinish() {
				updateSceneUI(mContext.mSceneList);
			}
		};
		
	}

	/**
	 * update list of scene and scene UI
	 * 
	 * @param listOfScene
	 */
	public void updateSceneUI(ArrayList<Scene_c> listOfScene) {
		this.listOfScene.clear();
		this.listOfScene.addAll(listOfScene);
		// this.listOfScene = listOfScene;
		// Log.i("Scene Fragment", "update " +
		// this.listOfScene.get(0).getName());
		if (!this.listOfScene.isEmpty()) {
			mAdapter.notifyDataSetChanged();

			for (int i = 0; i < listOfScene.size(); i++) {
				if (listOfScene.get(i).getActived() == true) {
					mSceneExpList.expandGroup(i);
				} else {
					mSceneExpList.collapseGroup(i);
				}
			}
		}
	}
	
	
	/**
	 * Get list of scene from other contexts
	 * 
	 * @return
	 */
	public ArrayList<Scene_c> getListOfScene(){
		return mAdapter.getListOfScene();
	}
	
	
}
