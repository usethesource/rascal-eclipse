package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class StdAndErrorViewPart extends ViewPart {
	public static final String ID = "rascal-eclipse.outputview";
	private StdAndErrorOuput outputWidget;
	public static final int STD_OUT_BUFFER_SIZE = 1 << 12;
	public static final int STD_ERR_BUFFER_SIZE = 1 << 12;	
	public static ConcurrentCircularOutputStream stdOutput;
	public static ConcurrentCircularOutputStream stdError;
	public static ConsoleSyncer syncOutput;
	public static ConsoleSyncer syncError;
	
	
	public StdAndErrorViewPart() { 
		super(); 
	}
	
	class PauseOutputAction extends Action{
		
		public PauseOutputAction() {
			setText("Pause output");
		}
		
		@Override
		public void run() {
			outputWidget.flipPause();
			if(outputWidget.isPaused()){
				setText("Resume output");
			} else {
				setText("Pause output");
			}
		}
		
	}
	
	class ClearAction extends Action{
		
		public ClearAction() {
			setText("Clear output");
			setToolTipText("Clears the output");
		}
		
		@Override
		public void run() {
			outputWidget.stdOutView.text.setText("");
			outputWidget.stdErrView.paused = !outputWidget.stdErrView.paused;
		}
		
	}

	@Override
	public void createPartControl(Composite parent) {
		outputWidget = new StdAndErrorOuput(parent, STD_OUT_BUFFER_SIZE, STD_ERR_BUFFER_SIZE);
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		toolbar.add(new PauseOutputAction());
		toolbar.add(new ClearAction());
		
		syncOutput = new ConsoleSyncer(new PausableOutputStream(outputWidget.stdOutView));
		stdOutput = new ConcurrentCircularOutputStream(STD_OUT_BUFFER_SIZE, 20, syncOutput);
		syncOutput.initializeWithStream(stdOutput);

		
		syncError = new ConsoleSyncer(new PausableOutputStream(outputWidget.stdErrView));
		stdError = new ConcurrentCircularOutputStream(STD_OUT_BUFFER_SIZE, 20, syncError);
		syncError.initializeWithStream(stdError);
		

	}



	@Override
	public void setFocus() {
	}
}
