package org.rascalmpl.eclipse.console.internal;

import java.util.ArrayList;

public class CommandHistory{
	private final static int COMMAND_LIMIT = 1000;
	
	private ArrayList<String> history;
	
	private int index;
	
	private boolean updated;
	
	public CommandHistory(){
		super();
		
		history = new ArrayList<String>();
		
		index = 0;
		
		updated = true;
	}
	
	public void addToHistory(String command){
		if(updated){
			if(history.size() == COMMAND_LIMIT) history.remove(0);
			
			history.add(command);
		}else{
			history.set(index, command);
		}
		resetState();
	}
	
	public void updateCurrent(String command){
		if(command.length() == 0) return;
		
		if(index == history.size()){
			if(history.size() == COMMAND_LIMIT){
				history.remove(0);
				--index;
			}
			
			history.add(command);
			updated = false;
		}else{
			if(index < history.size()){
				history.set(index, command);
			}
		}
	}
	
	// Sooner
	public String getPreviousCommand(){
		if(index == -1 || (--index) == -1){
			return "";
		}
		
		return history.get(index);
	}
	
	// Later
	public String getNextCommand(){
		if(index == history.size() || (++index) == history.size()){
			return "";
		}
		
		return history.get(index);
	}
	
	public void resetState(){
		index = history.size();
		updated = true;
	}
}
