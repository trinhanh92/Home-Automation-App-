package anh.trinh.ble_demo.list_view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import anh.trinh.ble_demo.R;

public class CustomRuleView extends TableLayout {
	Context mContext;
	Spinner mCondChoose;
	Spinner condDevChoose;
	EditText condDevVal;
	
	EditText mStartTime;
	EditText mEndTime;
	
	Spinner mActChoose;
	Spinner actDevChoose;
	EditText actDevVal;
	
	
	

	public CustomRuleView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		LayoutInflater inf = (LayoutInflater) mContext
									.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inf.inflate(R.layout.rule_layout, this, true);
		
		this.mCondChoose = (Spinner) findViewById(R.id.mCondChoose);
		this.condDevChoose = (Spinner) findViewById(R.id.condDevChoose);
		this.condDevVal = (EditText) findViewById(R.id.condDevVal);
		
		this.mStartTime = (EditText) findViewById(R.id.startTime);
		this.mEndTime = (EditText) findViewById(R.id.stopTime);
		
		this.mActChoose = (Spinner) findViewById(R.id.mActChoose);
		this.actDevChoose = (Spinner) findViewById(R.id.actDevChoose);
		this.actDevVal = (EditText) findViewById(R.id.actDevVal);
		
	}

	public CustomRuleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

}
