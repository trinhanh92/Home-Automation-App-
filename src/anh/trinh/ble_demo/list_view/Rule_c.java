package anh.trinh.ble_demo.list_view;

public class Rule_c {
	
	private int ruleId;
	private int mCondition;
	private int mAction;
	
	private int mCondDevID;
	private short mCondDevVal;
	
	private int mActDevID;
	private short mActDevVal;
	
	private int startDateTime;
	private int startDate;
	private int startTime;
	
	private int endDateTime;
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
	
	public void setCondDevVal(short mDevVal){
		this.mCondDevVal = mDevVal;
	}
	
	public short getCondDevVal(){
		return this.mCondDevVal;
	}
	
	public void setActDevVal(short mDevVal){
		this.mActDevVal = mDevVal;
	}
	
	public short getActDevVal(){
		return this.mActDevVal;
	}
	
	public void setStartDateTime(int startTime){
		this.startDateTime = startTime;
		this.startDate = (startTime >> 16 ) & 0xffff;
		this.startTime = startTime & 0xffff;
	}
	
	public void setStartDate(int startDate){
		this.startDate = startDate << 16;
	}
	
	public void setStartTime(int time){
		this.startTime = time;
	}
	
	public void setEndDateTime(int endTime){
		this.endDateTime = endTime;
		this.endDate = (endTime >> 16 ) & 0xffff;
		this.endTime = endTime & 0xffff;
	}
	
	public void setEndDate(int date){
		this.endDate = date << 16;
	}
	
	public void setEndTime(int time){
		this.endTime  = time;
	}
	public int getStartDateTime(){
		return this.startDateTime = this.startDate << 16 | this.startTime;
	}
	
	public int getStartDate(){
		return this.startDate;
	}
	
	public int getStartTime(){
		return this.startTime;
	}
	
	public int getEndDateTime(){
		return this.endDateTime = this.endDate << 16 | this.endTime;
	}
	
	public int getEndDate(){
		return this.endDate;
	}
	
	public int getEndTime(){
		return this.endTime;
	}
}
