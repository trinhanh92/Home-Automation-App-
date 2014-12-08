package anh.trinh.ble_demo.list_view;

public class Rule_c {
	
	private int ruleId;
	private int mCondition;
	private int mAction;
	
	private int inputDevID;
	private short inputDevVal;
	
	private int outputDevID;
	private short outputDevVal;
	
	private long startTime;
	private long endTime;

	public Rule_c() {
		// TODO Auto-generated constructor stub
	}
	
	public void setID(int ruleId){
		this.ruleId = ruleId;
	}
	
	public int getID(){
		return this.ruleId;
	}
	
	public void setCond(int mCond){
		this.mCondition = mCond;
	}
	
	public int getCond(){
		return this.mCondition;
	}
	
	public void setAction(int mAct){
		this.mAction = mAct;
	}
	
	public int getAction(){
		return this.mAction;
	}
	
	public void setInputDevId(int mDevID){
		this.inputDevID = mDevID;
	}
	
	public int getInputDevId(){
		return this.inputDevID;
	}
	
	public void setOutputDevId(int mDevID){
		this.outputDevID = mDevID;
	}
	
	public int getOutputDevId(){
		return this.outputDevID;
	}
	
	public void setInputDevVal(short mDevVal){
		this.inputDevVal = mDevVal;
	}
	
	public short getInputDevVal(){
		return this.inputDevVal;
	}
	
	public void setOutputDevVal(short mDevVal){
		this.outputDevVal = mDevVal;
	}
	
	public short getOutputDevVal(){
		return this.outputDevVal;
	}
	
	public void setStartTime(long startTime){
		this.startTime = startTime;
	}
	
	public void setEndTime(long endTime){
		this.endTime = endTime;
	}
	
	public long getStartTime(){
		return this.startTime;
	}
	
	public long getEndTime(){
		return this.endTime;
	}
	
}
