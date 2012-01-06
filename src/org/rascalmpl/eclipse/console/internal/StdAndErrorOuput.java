package org.rascalmpl.eclipse.console.internal;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.console.MessageConsole;

public class StdAndErrorOuput extends Composite{

	
	OutputWidget stdOutView;
	OutputWidget stdErrView;
	
	StdAndErrorOuput(Composite parent, int stdOutBufferSize, int stdErrBufferSize) {
		super(parent,SWT.NONE);
		setLayout(new FillLayout());
		SashForm sashForm = new SashForm(this, SWT.VERTICAL | SWT.H_SCROLL | SWT.V_SCROLL);
		stdOutView = new OutputWidget(sashForm,Display.getDefault().getSystemColor(SWT.COLOR_BLACK),
				stdOutBufferSize);
		stdErrView = new OutputWidget(sashForm,Display.getDefault().getSystemColor(SWT.COLOR_RED),stdErrBufferSize);
	}

	public void addendToStdOut(final String s){
		stdOutView.append(s);
	}
	
	public void addendToStdErr(final String s){
		stdErrView.append(s);
	}
	
	void flipPause(){
		stdOutView.paused = !stdOutView.paused;
		stdErrView.paused = !stdErrView.paused;
	}
	
	public boolean isPaused(){
		return stdOutView.paused;
	}
	
	void clear(){
		stdOutView.clear();
		stdErrView.clear();
	}
}
