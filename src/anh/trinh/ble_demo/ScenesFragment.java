package anh.trinh.ble_demo;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import anh.trinh.ble_demo.list_view.SceneExpListAdapter;
import anh.trinh.ble_demo.list_view.Scene_c;

public class ScenesFragment extends Fragment {
		
	private Button btnAddScene;
	private ExpandableListView mSceneExpList; 
	private SceneExpListAdapter mAdapter;
	public static ArrayList<Scene_c> listOfScene = new ArrayList<Scene_c>();
	
	 @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.fragment_scenes, container, false);
		
		btnAddScene = (Button) rootView.findViewById(R.id.btnAddScene);
		mSceneExpList = (ExpandableListView) rootView.findViewById(R.id.elvScene);
				
		mAdapter = new SceneExpListAdapter((HomeActivity)getActivity(), listOfScene);
		mSceneExpList.setAdapter(mAdapter);
		mSceneExpList.setGroupIndicator(null);
		
		btnAddScene.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Add scene
				
				AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
				mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
				mBuilder.setTitle("Create a new Scene");
				mBuilder.setMessage("Please, enter Scene's name!");
				
				final EditText input = new EditText(getActivity());
				mBuilder.setView(input);
				
				mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// TODO Auto-generated method stub
						String sceneName = input.getText().toString();
						if(!sceneName.isEmpty()){
							Scene_c mScene = new Scene_c();
							mScene.setName(sceneName);
							listOfScene.add(mScene);
							mAdapter.notifyDataSetChanged();
						}
					}
				});
//				mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//					}
//				});
				mBuilder.show();
			}
		});
		
		return rootView;
	}
}
