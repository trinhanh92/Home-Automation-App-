package anh.trinh.ble_demo.custom_view;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

public class DateDisplayPicker extends TextView implements OnDateSetListener{
	private Context mContext;
	
	public DateDisplayPicker(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		this.mContext = context;
	}

	public DateDisplayPicker(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		setAttributes();
	}
	public DateDisplayPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		setAttributes();
	}
	
	
	private void setAttributes(){
		setHint("Date");
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
        DatePickerDialog dp = new DatePickerDialog(mContext, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.getDatePicker().setCalendarViewShown(false);
        dp.setCancelable(true);
        dp.setCanceledOnTouchOutside(true);
        dp.show();
    }
 
 
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear,
            int dayOfMonth) {
        setText(String.format("%s/%s/%s", monthOfYear + 1, dayOfMonth, year));      
    }

}
