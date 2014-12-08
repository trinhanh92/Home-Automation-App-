package anh.trinh.ble_demo.list_view;

import java.util.ArrayList;
import java.util.List;

public class Scene_c {

	private String sceneName;
	private int sceneId;
	private boolean isActived;
	
	private ArrayList<Rule_c> listOfRules = new  ArrayList<Rule_c>();
	
	
	public Scene_c() {
		// TODO Auto-generated constructor stub
	}
	
	public Scene_c(String sceneName, int sceneId){
		this.sceneName = sceneName;
		this.sceneId = sceneId;
	}
	
	public Scene_c(String sceneName, int sceneId, ArrayList<Rule_c> listOfRule){
		this.sceneName = sceneName;
		this.sceneId = sceneId;
		this.listOfRules = listOfRule;
	}
	
	
	public void setName(String name){
		this.sceneName = name;
	}
	
	public String getName(){
		return this.sceneName;
	}
	
	public void setActived(boolean isActived){
		this.isActived = isActived;
	}
	
	public boolean getActived(){
		return this.isActived;
	}
	
	public void setID(int id){
		this.sceneId = id;
	}
	
	public int getID(){
		return this.sceneId;
	}
	
	public void addRule(Rule_c mRule){
		listOfRules.add(mRule);
	}
	
	public Rule_c getRuleWithIndex(int index){
		return listOfRules.get(index);
	}
	
	public ArrayList<Rule_c> getListOfRules(){
		return this.listOfRules;
	}
	
	public void setListOfRule(ArrayList<Rule_c> listOfRules){
		this.listOfRules = listOfRules;
	}
	public void removeRule(Rule_c mRule){
		listOfRules.remove(mRule);
	}
	public int getNumOfRule(){
		return listOfRules.size();
	}

}
