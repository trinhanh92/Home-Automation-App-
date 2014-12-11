package anh.trinh.ble_demo.custom_view;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.TimePicker;

public class TimeDisplayPicker extends TextView implements OnTimeSetListener {

	private Context mContext;

	public TimeDisplayPicker(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		setAttributes();
	}

	public TimeDisplayPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		setAttributes();
	}

	public TimeDisplayPicker(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		this.mContext = context;
	}

	private void setAttributes() {
		setHint("Time");
		setSingleLine();
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDateDialog();
			}
		});
	}

	private void showDateDialog() {
		final Calendar c = Calendar.getInstance();
		TimePickerDialog tp = new TimePickerDialog(mContext, this,
				c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
				DateFormat.is24HourFormat(mContext));
		tp.show();
	}

	@Override
	public void onTimeSet(TimePicker view, int hour, int minute) {
		// TODO Auto-generated method stub
		 setText(String.format("%s:%s", hour, minute)); 
	}

}
