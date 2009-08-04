package org.meta_environment.rascal.eclipse.console.internal;

import java.util.ArrayList;

public class CommandHistory{
	private ArrayList<String> history;
	
	private int index;
	
	public CommandHistory(){
		super();
		
		history = new ArrayList<String>();
		
		index = 0;
	}
	
	public void addToHistory(String command){
		history.add(command);
		resetState();
	}
	
	// Sooner
	public String getPreviousCommand(){
		if(index == 0){
			return "";
		}
		
		return history.get(--index);
	}
	
	// Later
	public String getNextCommand(){
		if(index == history.size() - 1){
			return "";
		}
		
		return history.get(++index);
	}
	
	public void resetState(){
		index = history.size();
	}
}
