package anh.trinh.ble_demo.list_view;

public class Rule_c {
	
	private int ruleId;
	private int mCondition;
	private int mAction;
	
	private int mCondDevID;
	private int mCondDevVal;
	
	private int mActDevID;
	private int mActDevVal;
	
	private int startDate;
	private int startTime;
	
	private int endDate;
	private int endTime;
	
	public Rule_c() {
		// TODO Auto-generated constructor stub
	}
	
	public void setRuleIndex(int ruleId){
		this.ruleId = ruleId;
	}
	
	public int getRuleIndex(){
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
	
	public void setCondDevId(int mDevID){
		this.mCondDevID = mDevID;
	}
	
	public int getCondDevId(){
		return this.mCondDevID;
	}
	
	public void setActDevId(int mDevID){
		this.mActDevID = mDevID;
	}
	
	public int getActDevId(){
		return this.mActDevID;
	}
	
	public void setCondDevVal(int i){
		this.mCondDevVal = i;
	}
	
	public int getCondDevVal(){
		return this.mCondDevVal;
	}
	
	public void setActDevVal(int mDevVal){
		this.mActDevVal = mDevVal;
	}
	
	public int getActDevVal(){
		return this.mActDevVal;
	}
	
	public void setStartDateTime(int startTime){
		this.startDate = (startTime & 0xFFFF0000);
		this.startTime = startTime & 0x0000FFFF;
	}
	
	public void setStartDate(int startDate){
		this.startDate = startDate;
	}
	
	public void setStartTime(int time){
		this.startTime = time;
	}
	
	public void setEndDateTime(int endTime){
		this.endDate = (endTime & 0xFFFF0000);
		this.endTime = endTime & 0x0000ffff;
	}
	
	public void setEndDate(int date){
		this.endDate = date;
	}
	
	public void setEndTime(int time){
		this.endTime  = time;
	}
	public int getStartDateTime(){
		return (this.startDate) | this.startTime;
	}
	
	public int getStartDate(){
		return this.startDate;
	}
	
	public int getStartTime(){
		return this.startTime;
	}
	
	public int getEndDateTime(){
		return (this.endDate) | this.endTime;
	}
	
	public int getEndDate(){
		return this.endDate;
	}
	
	public int getEndTime(){
		return this.endTime;
	}
}
